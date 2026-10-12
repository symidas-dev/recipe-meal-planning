package hr.algebra.recipemealplanning;

import org.springframework.boot.SpringApplication;

public class TestRecipeMealPlanningApplication {

	public static void main(String[] args) {
		SpringApplication.from(RecipeMealPlanningApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
