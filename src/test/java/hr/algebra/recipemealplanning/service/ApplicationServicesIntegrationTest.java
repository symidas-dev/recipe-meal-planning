package hr.algebra.recipemealplanning.service;

import hr.algebra.recipemealplanning.TestcontainersConfiguration;
import hr.algebra.recipemealplanning.domain.entity.TheoreticalIngredient;
import hr.algebra.recipemealplanning.domain.enums.DayOfWeek;
import hr.algebra.recipemealplanning.domain.enums.MealTime;
import hr.algebra.recipemealplanning.domain.enums.MeasurementUnit;
import hr.algebra.recipemealplanning.domain.exception.CircularDependencyException;
import hr.algebra.recipemealplanning.domain.model.NutritionalInformation;
import hr.algebra.recipemealplanning.domain.service.SubRecipeCycleDetector;
import hr.algebra.recipemealplanning.dto.RecipeCreateCommand;
import hr.algebra.recipemealplanning.dto.RecipeItemCommand;
import hr.algebra.recipemealplanning.dto.RecipeResponseDTO;
import hr.algebra.recipemealplanning.dto.ScaledRecipeDTO;
import hr.algebra.recipemealplanning.dto.WeeklyMenuResponseDTO;
import hr.algebra.recipemealplanning.repository.TheoreticalIngredientRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Transactional
class ApplicationServicesIntegrationTest {

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private WeeklyMenuService weeklyMenuService;

    @Autowired
    private TheoreticalIngredientRepository theoreticalIngredientRepository;

    @Nested
    @DisplayName("CU 2.2 & 2.3: Gestión del Recetario, Fichas Técnicas y Escalado Dinámico")
    class RecipeServiceUseCases {

        @Test
        @DisplayName("El cocinero registra una receta con ingredientes canónicos y el sistema precomputa sus calorías y macronutrientes nominales")
        void shouldRegisterRecipeAndPrecomputeNominalNutrition() {
            NutritionalInformation potatoNutrition = new NutritionalInformation(
                    new BigDecimal("77.00"), new BigDecimal("2.00"), new BigDecimal("17.00"), new BigDecimal("0.10")
            );
            NutritionalInformation eggNutrition = new NutritionalInformation(
                    new BigDecimal("155.00"), new BigDecimal("13.00"), new BigDecimal("1.10"), new BigDecimal("11.00")
            );

            TheoreticalIngredient potato = theoreticalIngredientRepository.save(
                    new TheoreticalIngredient("Patata Agria", "en:potatoes", new BigDecimal("1.00"), potatoNutrition)
            );
            TheoreticalIngredient egg = theoreticalIngredientRepository.save(
                    new TheoreticalIngredient("Huevo Campero", "en:eggs", new BigDecimal("1.00"), eggNutrition)
            );

            RecipeCreateCommand omeletteCommand = new RecipeCreateCommand(
                    "Tortilla de Patatas",
                    "Freír patatas a fuego lento, batir huevos, mezclar y cuajar.",
                    4,
                    15,
                    20,
                    List.of(
                            RecipeItemCommand.ofCanonical(potato.getId(), new BigDecimal("600.00"), MeasurementUnit.GRAM),
                            RecipeItemCommand.ofCanonical(egg.getId(), new BigDecimal("300.00"), MeasurementUnit.GRAM)
                    )
            );

            RecipeResponseDTO createdRecipe = recipeService.registerNewRecipe(omeletteCommand);

            assertThat(createdRecipe.id()).isNotNull();
            assertThat(createdRecipe.title()).isEqualTo("Tortilla de Patatas");
            assertThat(createdRecipe.referenceServings()).isEqualTo(4);
            assertThat(createdRecipe.ingredients()).hasSize(2);

            // 600g patatas = 6 * 77 = 462 kcal; 300g huevos = 3 * 155 = 465 kcal -> Total = 927.00 kcal
            assertThat(createdRecipe.nominalNutrition().calories()).isEqualByComparingTo(new BigDecimal("927.00"));
            assertThat(createdRecipe.nominalNutrition().protein()).isEqualByComparingTo(new BigDecimal("51.00"));
        }

        @Test
        @DisplayName("Prevención de ciclos en subrecetas: el sistema detecta dependencias circulares mediante DFS y lanza CircularDependencyException")
        void shouldRejectCircularDependencyInSubRecipesViaDfs() {
            NutritionalInformation flourNutrition = new NutritionalInformation(
                    new BigDecimal("364.00"), new BigDecimal("10.00"), new BigDecimal("76.00"), new BigDecimal("1.00")
            );
            TheoreticalIngredient flour = theoreticalIngredientRepository.save(
                    new TheoreticalIngredient("Harina de Trigo", "en:flours", new BigDecimal("0.60"), flourNutrition)
            );

            RecipeResponseDTO doughBase = recipeService.registerNewRecipe(new RecipeCreateCommand(
                    "Masa Base",
                    "Amasar harina con agua",
                    2,
                    10,
                    0,
                    List.of(RecipeItemCommand.ofCanonical(flour.getId(), new BigDecimal("200.00"), MeasurementUnit.GRAM))
            ));

            RecipeResponseDTO rusticBread = recipeService.registerNewRecipe(new RecipeCreateCommand(
                    "Pan Rústico",
                    "Hornear masa fermentada",
                    4,
                    20,
                    45,
                    List.of(RecipeItemCommand.ofSubRecipe(doughBase.id(), new BigDecimal("200.00"), MeasurementUnit.GRAM))
            ));

            SubRecipeCycleDetector detector = new SubRecipeCycleDetector();
            assertThatThrownBy(() -> {
                detector.validateAcyclicity(
                        new hr.algebra.recipemealplanning.domain.entity.Recipe("Dough", "inst", 2, 5, 0) {
                            @Override
                            public Long getId() {
                                return doughBase.id();
                            }
                        },
                        new hr.algebra.recipemealplanning.domain.entity.Recipe("Rustic", "inst", 4, 10, 20) {
                            @Override
                            public Long getId() {
                                return rusticBread.id();
                            }
                            @Override
                            public List<hr.algebra.recipemealplanning.domain.entity.Recipe> getNestedRecipes() {
                                return List.of(
                                        new hr.algebra.recipemealplanning.domain.entity.Recipe("Dough", "inst", 2, 5, 0) {
                                            @Override
                                            public Long getId() {
                                                return doughBase.id();
                                            }
                                        }
                                );
                            }
                        }
                );
            }).isInstanceOf(CircularDependencyException.class);
        }

        @Test
        @DisplayName("Escalado dinámico en memoria: el usuario escala una receta de 4 raciones base a 6 comensales sin alterar la base original")
        void shouldDynamicallyScaleRecipeQuantitiesWithoutMutatingOriginal() {
            NutritionalInformation riceNutrition = new NutritionalInformation(
                    new BigDecimal("360.00"), new BigDecimal("7.00"), new BigDecimal("79.00"), new BigDecimal("1.00")
            );
            TheoreticalIngredient rice = theoreticalIngredientRepository.save(
                    new TheoreticalIngredient("Arroz Redondo", "en:rices", new BigDecimal("0.85"), riceNutrition)
            );

            RecipeResponseDTO originalRecipe = recipeService.registerNewRecipe(new RecipeCreateCommand(
                    "Arroz Blanco",
                    "Hervir arroz en agua hirviendo.",
                    4,
                    5,
                    15,
                    List.of(RecipeItemCommand.ofCanonical(rice.getId(), new BigDecimal("400.00"), MeasurementUnit.GRAM))
            ));

            ScaledRecipeDTO scaledTo6 = recipeService.scaleRecipeForHeadcount(originalRecipe.id(), 6);

            assertThat(scaledTo6.referenceServings()).isEqualTo(4);
            assertThat(scaledTo6.targetServings()).isEqualTo(6);
            assertThat(scaledTo6.scalingFactor()).isEqualByComparingTo(new BigDecimal("1.50"));

            // 400g * 1.5 = 600.00g
            assertThat(scaledTo6.scaledIngredients().get(0).scaledAmount()).isEqualByComparingTo(new BigDecimal("600.00"));

            RecipeResponseDTO freshRead = recipeService.getRecipeTechnicalSheet(originalRecipe.id());
            assertThat(freshRead.referenceServings()).isEqualTo(4);
            assertThat(freshRead.ingredients().get(0).displayAmount()).isEqualByComparingTo(new BigDecimal("400.00"));
        }
    }

    @Nested
    @DisplayName("CU 2.4 & 2.5: Planificación Semanal (La Nevera), Asignación de Casillas y Cierre")
    class WeeklyMenuServiceUseCases {

        @Test
        @DisplayName("El usuario crea la pizarra semanal, consulta por fecha y asigna o reasigna casillas sin colisión")
        void shouldManageWeeklyBoardAndReassignMealSlots() {
            NutritionalInformation soupNutrition = new NutritionalInformation(
                    new BigDecimal("50.00"), new BigDecimal("2.00"), new BigDecimal("8.00"), new BigDecimal("1.00")
            );
            TheoreticalIngredient leek = theoreticalIngredientRepository.save(
                    new TheoreticalIngredient("Puerro", "en:vegetables", new BigDecimal("0.90"), soupNutrition)
            );

            RecipeResponseDTO soupRecipe = recipeService.registerNewRecipe(new RecipeCreateCommand(
                    "Crema de Puerros",
                    "Triturar puerros cocidos",
                    2,
                    10,
                    20,
                    List.of(RecipeItemCommand.ofCanonical(leek.getId(), new BigDecimal("300.00"), MeasurementUnit.GRAM))
            ));

            LocalDate wednesdayDate = LocalDate.of(2027, 10, 13);
            WeeklyMenuResponseDTO weeklyBoard = weeklyMenuService.createWeeklyBoardForWeek(wednesdayDate);

            assertThat(weeklyBoard.weekStartDate()).isEqualTo(LocalDate.of(2027, 10, 11));
            assertThat(weeklyBoard.slots()).isEmpty();

            Optional<WeeklyMenuResponseDTO> retrievedBoard = weeklyMenuService.findWeeklyBoardByDate(wednesdayDate);
            assertThat(retrievedBoard).isPresent();
            assertThat(retrievedBoard.get().id()).isEqualTo(weeklyBoard.id());

            WeeklyMenuResponseDTO updatedBoard = weeklyMenuService.assignMealToSlot(
                    weeklyBoard.id(), DayOfWeek.TUESDAY, MealTime.LUNCH, soupRecipe.id(), 3
            );

            assertThat(updatedBoard.slots()).hasSize(1);
            assertThat(updatedBoard.slots().get(0).recipeTitle()).isEqualTo("Crema de Puerros");
            assertThat(updatedBoard.slots().get(0).servings()).isEqualTo(3);

            WeeklyMenuResponseDTO reallocatedBoard = weeklyMenuService.assignMealToSlot(
                    weeklyBoard.id(), DayOfWeek.TUESDAY, MealTime.LUNCH, soupRecipe.id(), 4
            );

            assertThat(reallocatedBoard.slots()).hasSize(1);
            assertThat(reallocatedBoard.slots().get(0).servings()).isEqualTo(4);

            weeklyMenuService.closeWeeklyBoard(weeklyBoard.id());
            WeeklyMenuResponseDTO closedBoard = weeklyMenuService.getWeeklyBoardById(weeklyBoard.id());
            assertThat(closedBoard.isClosed()).isTrue();

            assertThatThrownBy(() -> {
                weeklyMenuService.assignMealToSlot(
                        weeklyBoard.id(), DayOfWeek.FRIDAY, MealTime.DINNER, soupRecipe.id(), 2
                );
            }).isInstanceOf(IllegalStateException.class);
        }
    }
}
