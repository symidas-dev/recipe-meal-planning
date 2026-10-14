package hr.algebra.recipemealplanning;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import org.junit.jupiter.api.Disabled;

@Disabled("Requires running Docker daemon for Testcontainers")
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class RecipeMealPlanningApplicationTests {
	@Test
	void contextLoads() {
	}

}
