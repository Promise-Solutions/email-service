package studiozero.service.email.consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import studiozero.service.email.application.usecases.SendDailyEmailUseCase;
import studiozero.service.email.domain.dtos.SubJob;
import studiozero.service.email.domain.dtos.Task;
import studiozero.service.email.infrastructure.consumer.ConsumeEmailEventDto;
import studiozero.service.email.infrastructure.consumer.EmailConsumer;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailConsumer Tests")
class EmailConsumerTest {

    @Mock
    private SendDailyEmailUseCase sendDailyEmailUseCase;

    @InjectMocks
    private EmailConsumer emailConsumer;

    private ConsumeEmailEventDto validEventDto;

    @BeforeEach
    void setUp() {
        List<String> recipients = List.of("employee@example.com");
        List<SubJob> subJobs = List.of(new SubJob("Cliente A", "Atendimento"));
        List<Task> tasks = List.of(new Task("Tarefa 1", LocalDate.now()));

        validEventDto = new ConsumeEmailEventDto(recipients, subJobs, tasks);
    }

    @Test
    @DisplayName("Should consume event and call use case successfully")
    void shouldConsumeEventAndCallUseCase() {
        emailConsumer.consumeEvent(validEventDto);

        verify(sendDailyEmailUseCase, times(1)).execute(validEventDto);
    }

    @Test
    @DisplayName("Should consume event with empty lists")
    void shouldConsumeEventWithEmptyLists() {
        ConsumeEmailEventDto emptyEventDto = new ConsumeEmailEventDto(
                List.of("employee@example.com"),
                Collections.emptyList(),
                Collections.emptyList()
        );

        emailConsumer.consumeEvent(emptyEventDto);

        verify(sendDailyEmailUseCase, times(1)).execute(emptyEventDto);
    }

    @Test
    @DisplayName("Should consume event with null subJobs and tasks")
    void shouldConsumeEventWithNullSubJobsAndTasks() {
        ConsumeEmailEventDto eventWithNulls = new ConsumeEmailEventDto(
                List.of("employee@example.com"),
                null,
                null
        );

        emailConsumer.consumeEvent(eventWithNulls);

        verify(sendDailyEmailUseCase, times(1)).execute(eventWithNulls);
    }

    @Test
    @DisplayName("Should consume event with multiple recipients")
    void shouldConsumeEventWithMultipleRecipients() {
        List<String> multipleRecipients = List.of(
                "employee1@example.com",
                "employee2@example.com",
                "employee3@example.com"
        );

        ConsumeEmailEventDto multiRecipientsEvent = new ConsumeEmailEventDto(
                multipleRecipients,
                List.of(new SubJob("Cliente", "Atendimento")),
                Collections.emptyList()
        );

        emailConsumer.consumeEvent(multiRecipientsEvent);

        verify(sendDailyEmailUseCase, times(1)).execute(multiRecipientsEvent);
    }

    @Test
    @DisplayName("Should consume event with large list of subJobs and tasks")
    void shouldConsumeEventWithLargeList() {
        List<SubJob> largeSubJobsList = List.of(
                new SubJob("Cliente 1", "Atendimento 1"),
                new SubJob("Cliente 2", "Atendimento 2"),
                new SubJob("Cliente 3", "Atendimento 3"),
                new SubJob("Cliente 4", "Atendimento 4"),
                new SubJob("Cliente 5", "Atendimento 5")
        );

        List<Task> largeTasksList = List.of(
                new Task("Tarefa 1", LocalDate.now()),
                new Task("Tarefa 2", LocalDate.now().plusDays(1)),
                new Task("Tarefa 3", LocalDate.now().plusDays(2)),
                new Task("Tarefa 4", LocalDate.now().plusDays(3)),
                new Task("Tarefa 5", LocalDate.now().plusDays(4))
        );

        ConsumeEmailEventDto largeEvent = new ConsumeEmailEventDto(
                List.of("employee@example.com"),
                largeSubJobsList,
                largeTasksList
        );

        emailConsumer.consumeEvent(largeEvent);

        verify(sendDailyEmailUseCase, times(1)).execute(largeEvent);
    }

    @Test
    @DisplayName("Should propagate exception when use case throws IllegalArgumentException")
    void shouldPropagateExceptionWhenUseCaseThrowsIllegalArgumentException() {
        ConsumeEmailEventDto nullEventDto = null;
        doThrow(new IllegalArgumentException("Dados recebidos nulos"))
                .when(sendDailyEmailUseCase).execute(nullEventDto);

        assertThatThrownBy(() -> emailConsumer.consumeEvent(nullEventDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Dados recebidos nulos");

        verify(sendDailyEmailUseCase, times(1)).execute(nullEventDto);
    }

    @Test
    @DisplayName("Should propagate exception when use case throws RuntimeException")
    void shouldPropagateExceptionWhenUseCaseThrowsRuntimeException() {
        doThrow(new RuntimeException("Erro inesperado"))
                .when(sendDailyEmailUseCase).execute(any(ConsumeEmailEventDto.class));

        assertThatThrownBy(() -> emailConsumer.consumeEvent(validEventDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Erro inesperado");

        verify(sendDailyEmailUseCase, times(1)).execute(validEventDto);
    }

    @Test
    @DisplayName("Should handle null eventDto passed to consumer")
    void shouldHandleNullEventDto() {
        ConsumeEmailEventDto nullEventDto = null;

        emailConsumer.consumeEvent(nullEventDto);

        verify(sendDailyEmailUseCase, times(1)).execute(nullEventDto);
    }

    @Test
    @DisplayName("Should call use case exactly once per event")
    void shouldCallUseCaseExactlyOncePerEvent() {
        emailConsumer.consumeEvent(validEventDto);
        emailConsumer.consumeEvent(validEventDto);
        emailConsumer.consumeEvent(validEventDto);

        verify(sendDailyEmailUseCase, times(3)).execute(validEventDto);
    }

    @Test
    @DisplayName("Should consume event with SubJob containing null clientName")
    void shouldConsumeEventWithNullClientName() {
        List<SubJob> subJobsWithNullClient = List.of(
                new SubJob(null, "Atendimento sem cliente")
        );

        ConsumeEmailEventDto eventWithNullClient = new ConsumeEmailEventDto(
                List.of("employee@example.com"),
                subJobsWithNullClient,
                Collections.emptyList()
        );

        emailConsumer.consumeEvent(eventWithNullClient);

        verify(sendDailyEmailUseCase, times(1)).execute(eventWithNullClient);
    }

    @Test
    @DisplayName("Should not interact with use case when never called")
    void shouldNotInteractWithUseCaseWhenNeverCalled() {
        verifyNoInteractions(sendDailyEmailUseCase);
    }

    @Test
    @DisplayName("Should consume event and pass exact DTO to use case")
    void shouldPassExactDtoToUseCase() {
        List<String> specificRecipients = List.of("specific@example.com");
        List<SubJob> specificSubJobs = List.of(new SubJob("Specific Client", "Specific Service"));
        List<Task> specificTasks = List.of(new Task("Specific Task", LocalDate.of(2024, 12, 25)));

        ConsumeEmailEventDto specificEventDto = new ConsumeEmailEventDto(
                specificRecipients,
                specificSubJobs,
                specificTasks
        );

        emailConsumer.consumeEvent(specificEventDto);

        verify(sendDailyEmailUseCase, times(1)).execute(specificEventDto);
        verifyNoMoreInteractions(sendDailyEmailUseCase);
    }
}