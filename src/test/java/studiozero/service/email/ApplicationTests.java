package studiozero.service.email;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Disabled("Desativado temporariamente — causa erro por conta das variáveis de ambiente que não são reconhecidas MySQL/RabbitMQ")
@SpringBootTest(properties = "springdoc.api-docs.enabled=false")
class ApplicationTests {

	@Test
	void contextLoads() {
	}

}
