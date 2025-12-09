package studiozero.service.email.consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import studiozero.service.email.application.usecases.SendDailyEmailUseCase;
import studiozero.service.email.domain.dtos.SubJob;
import studiozero.service.email.domain.dtos.Task;
import studiozero.service.email.domain.repositories.SendEmailRepository;
import studiozero.service.email.infrastructure.consumer.ConsumeEmailEventDto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("SendDailyEmailUseCase Tests")
class SendDailyEmailUseCaseTest {

    @Mock
    private SendEmailRepository sendEmailRepository;

    @InjectMocks
    private SendDailyEmailUseCase sendDailyEmailUseCase;

    @Captor
    private ArgumentCaptor<List<String>> recipientsCaptor;

    @Captor
    private ArgumentCaptor<String> subjectCaptor;

    @Captor
    private ArgumentCaptor<String> contentCaptor;

    private List<String> recipients;

    @BeforeEach
    void setUp() {
        recipients = List.of("employee1@example.com", "employee2@example.com");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when eventDto is null")
    void shouldThrowExceptionWhenEventDtoIsNull() {
        assertThatThrownBy(() -> sendDailyEmailUseCase.execute(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Dados recebidos nulos");

        verify(sendEmailRepository, never()).sendEmail(anyList(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when recipients list is null")
    void shouldThrowExceptionWhenRecipientsIsNull() {
        ConsumeEmailEventDto eventDto = new ConsumeEmailEventDto(
                null,
                Collections.emptyList(),
                Collections.emptyList()
        );

        assertThatThrownBy(() -> sendDailyEmailUseCase.execute(eventDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Lista de destinatários vazia ou nula");

        verify(sendEmailRepository, never()).sendEmail(anyList(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when recipients list is empty")
    void shouldThrowExceptionWhenRecipientsIsEmpty() {
        ConsumeEmailEventDto eventDto = new ConsumeEmailEventDto(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );

        assertThatThrownBy(() -> sendDailyEmailUseCase.execute(eventDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Lista de destinatários vazia ou nula");

        verify(sendEmailRepository, never()).sendEmail(anyList(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should send 'no duties' email when both subJobs and tasks are empty")
    void shouldSendNoDutiesEmailWhenNoTasksOrSubJobs() {
        ConsumeEmailEventDto eventDto = new ConsumeEmailEventDto(
                recipients,
                Collections.emptyList(),
                Collections.emptyList()
        );

        sendDailyEmailUseCase.execute(eventDto);

        verify(sendEmailRepository, times(1)).sendEmail(
                recipientsCaptor.capture(),
                subjectCaptor.capture(),
                contentCaptor.capture()
        );

        assertThat(recipientsCaptor.getValue()).isEqualTo(recipients);
        assertThat(subjectCaptor.getValue()).isEqualTo("StudioZero: Sem deveres para hoje");
        assertThat(contentCaptor.getValue()).isEqualTo("Não há atendimentos ou tarefas para hoje");
    }

    @Test
    @DisplayName("Should send email with only subJobs when tasks list is empty")
    void shouldSendEmailWithOnlySubJobs() {
        List<SubJob> subJobs = List.of(
                new SubJob("Cliente A", "Atendimento Yoga"),
                new SubJob("Cliente B", "Consulta Pilates")
        );

        ConsumeEmailEventDto eventDto = new ConsumeEmailEventDto(
                recipients,
                subJobs,
                Collections.emptyList()
        );

        sendDailyEmailUseCase.execute(eventDto);

        verify(sendEmailRepository, times(1)).sendEmail(
                recipientsCaptor.capture(),
                subjectCaptor.capture(),
                contentCaptor.capture()
        );

        assertThat(recipientsCaptor.getValue()).isEqualTo(recipients);
        assertThat(subjectCaptor.getValue()).isEqualTo("StudioZero: Você tem deveres para hoje!");

        String content = contentCaptor.getValue();
        assertThat(content).contains("Olá, aqui está suas tarefas e atendimentos do dia!");
        assertThat(content).contains("Atendimentos para hoje:");
        assertThat(content).contains("- Atendimento Yoga (Cliente: Cliente A)");
        assertThat(content).contains("- Consulta Pilates (Cliente: Cliente B)");
        assertThat(content).contains("Tarefas para hoje:");
    }

    @Test
    @DisplayName("Should send email with only tasks when subJobs list is empty")
    void shouldSendEmailWithOnlyTasks() {
        List<Task> tasks = List.of(
                new Task("Preparar relatório", LocalDate.of(2024, 12, 10)),
                new Task("Revisar agenda", LocalDate.of(2024, 12, 10))
        );

        ConsumeEmailEventDto eventDto = new ConsumeEmailEventDto(
                recipients,
                Collections.emptyList(),
                tasks
        );

        sendDailyEmailUseCase.execute(eventDto);

        verify(sendEmailRepository, times(1)).sendEmail(
                recipientsCaptor.capture(),
                subjectCaptor.capture(),
                contentCaptor.capture()
        );

        assertThat(recipientsCaptor.getValue()).isEqualTo(recipients);
        assertThat(subjectCaptor.getValue()).isEqualTo("StudioZero: Você tem deveres para hoje!");

        String content = contentCaptor.getValue();
        assertThat(content).contains("Olá, aqui está suas tarefas e atendimentos do dia!");
        assertThat(content).contains("Atendimentos para hoje:");
        assertThat(content).contains("Tarefas para hoje:");
        assertThat(content).contains("- Preparar relatório (Prazo: 10/12/2024)");
        assertThat(content).contains("- Revisar agenda (Prazo: 10/12/2024)");
    }

    @Test
    @DisplayName("Should send email with both subJobs and tasks")
    void shouldSendEmailWithSubJobsAndTasks() {
        List<SubJob> subJobs = List.of(
                new SubJob("Cliente A", "Atendimento Yoga")
        );

        List<Task> tasks = List.of(
                new Task("Preparar relatório", LocalDate.of(2024, 12, 10))
        );

        ConsumeEmailEventDto eventDto = new ConsumeEmailEventDto(
                recipients,
                subJobs,
                tasks
        );

        sendDailyEmailUseCase.execute(eventDto);

        verify(sendEmailRepository, times(1)).sendEmail(
                recipientsCaptor.capture(),
                subjectCaptor.capture(),
                contentCaptor.capture()
        );

        String content = contentCaptor.getValue();
        assertThat(content).contains("- Atendimento Yoga (Cliente: Cliente A)");
        assertThat(content).contains("- Preparar relatório (Prazo: 10/12/2024)");
    }

    @Test
    @DisplayName("Should handle subJob with null clientName")
    void shouldHandleSubJobWithNullClientName() {
        List<SubJob> subJobs = List.of(
                new SubJob(null, "Atendimento sem cliente")
        );

        ConsumeEmailEventDto eventDto = new ConsumeEmailEventDto(
                recipients,
                subJobs,
                Collections.emptyList()
        );

        sendDailyEmailUseCase.execute(eventDto);

        verify(sendEmailRepository, times(1)).sendEmail(
                any(),
                any(),
                contentCaptor.capture()
        );

        String content = contentCaptor.getValue();
        assertThat(content).contains("- Atendimento sem cliente (Cliente: Não informado)");
    }

    @Test
    @DisplayName("Should format dates correctly in DD/MM/YYYY format")
    void shouldFormatDatesCorrectly() {
        List<Task> tasks = List.of(
                new Task("Tarefa 1", LocalDate.of(2024, 1, 5)),
                new Task("Tarefa 2", LocalDate.of(2024, 12, 25))
        );

        ConsumeEmailEventDto eventDto = new ConsumeEmailEventDto(
                recipients,
                Collections.emptyList(),
                tasks
        );

        sendDailyEmailUseCase.execute(eventDto);

        verify(sendEmailRepository, times(1)).sendEmail(
                any(),
                any(),
                contentCaptor.capture()
        );

        String content = contentCaptor.getValue();
        assertThat(content).contains("(Prazo: 05/01/2024)");
        assertThat(content).contains("(Prazo: 25/12/2024)");
    }

    @Test
    @DisplayName("Should send email to multiple recipients")
    void shouldSendEmailToMultipleRecipients() {
        List<String> multipleRecipients = List.of(
                "employee1@example.com",
                "employee2@example.com",
                "employee3@example.com"
        );

        List<Task> tasks = List.of(
                new Task("Tarefa teste", LocalDate.now())
        );

        ConsumeEmailEventDto eventDto = new ConsumeEmailEventDto(
                multipleRecipients,
                Collections.emptyList(),
                tasks
        );

        sendDailyEmailUseCase.execute(eventDto);

        verify(sendEmailRepository, times(1)).sendEmail(
                recipientsCaptor.capture(),
                any(),
                any()
        );

        assertThat(recipientsCaptor.getValue()).hasSize(3);
        assertThat(recipientsCaptor.getValue()).containsExactlyElementsOf(multipleRecipients);
    }

    @Test
    @DisplayName("Should handle large lists of subJobs and tasks")
    void shouldHandleLargeListsOfSubJobsAndTasks() {
        List<SubJob> largeSubJobsList = new ArrayList<>();
        List<Task> largeTasksList = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            largeSubJobsList.add(new SubJob("Cliente " + i, "Atendimento " + i));
            largeTasksList.add(new Task("Tarefa " + i, LocalDate.now().plusDays(i)));
        }

        ConsumeEmailEventDto eventDto = new ConsumeEmailEventDto(
                recipients,
                largeSubJobsList,
                largeTasksList
        );

        sendDailyEmailUseCase.execute(eventDto);

        verify(sendEmailRepository, times(1)).sendEmail(
                any(),
                eq("StudioZero: Você tem deveres para hoje!"),
                contentCaptor.capture()
        );

        String content = contentCaptor.getValue();
        assertThat(content).contains("Atendimento 1");
        assertThat(content).contains("Atendimento 10");
        assertThat(content).contains("Tarefa 1");
        assertThat(content).contains("Tarefa 10");
    }
}