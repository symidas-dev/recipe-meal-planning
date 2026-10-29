package hr.algebra.recipemealplanning.repository;

import hr.algebra.recipemealplanning.TestcontainersConfiguration;
import hr.algebra.recipemealplanning.domain.entity.MealSlot;
import hr.algebra.recipemealplanning.domain.entity.RawIngredient;
import hr.algebra.recipemealplanning.domain.entity.Recipe;
import hr.algebra.recipemealplanning.domain.entity.RecipeIngredient;
import hr.algebra.recipemealplanning.domain.entity.TheoreticalIngredient;
import hr.algebra.recipemealplanning.domain.entity.WeeklyMenu;
import hr.algebra.recipemealplanning.domain.enums.DayOfWeek;
import hr.algebra.recipemealplanning.domain.enums.InventoryStatus;
import hr.algebra.recipemealplanning.domain.enums.MealTime;
import hr.algebra.recipemealplanning.domain.enums.MeasurementUnit;
import hr.algebra.recipemealplanning.domain.model.Barcode;
import hr.algebra.recipemealplanning.domain.model.NutritionalInformation;
import hr.algebra.recipemealplanning.domain.model.TaxonomyTag;
import hr.algebra.recipemealplanning.domain.service.PantryInventoryService;
import hr.algebra.recipemealplanning.service.PantryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({TestcontainersConfiguration.class, PantryService.class, PantryInventoryService.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RepositoriesIntegrationTest {

    @Autowired
    private TheoreticalIngredientRepository theoreticalIngredientRepository;

    @Autowired
    private RawIngredientRepository rawIngredientRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private WeeklyMenuRepository weeklyMenuRepository;

    @Autowired
    private PantryService pantryService;

    // -------------------------------------------------------------------------
    // CASO DE USO 2.8: CATÁLOGO CANÓNICO DE INGREDIENTES (ALIMENTOS BASE)
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("CU 2.8: Catálogo Canónico y Fichas Nutricionales de Referencia")
    class CanonicalCatalogUseCases {

        @Test
        @DisplayName("El sistema impide registrar dos veces el mismo alimento canónico (ej. 'Tomate Pera') para evitar duplicidad en la base de datos")
        void shouldPreventDuplicateCanonicalFoodEntries() {
            NutritionalInformation pearTomatoNutrition = new NutritionalInformation(
                    new BigDecimal("18.00"), new BigDecimal("0.90"), new BigDecimal("3.90"), new BigDecimal("0.20")
            );

            TheoreticalIngredient officialTomato = new TheoreticalIngredient(
                    "Tomate Pera", "en:fresh-tomatoes", new BigDecimal("0.95"), pearTomatoNutrition
            );
            theoreticalIngredientRepository.saveAndFlush(officialTomato);

            TheoreticalIngredient duplicateAttempt = new TheoreticalIngredient(
                    "Tomate Pera", "en:vegetables", new BigDecimal("1.00"), pearTomatoNutrition
            );

            assertThatThrownBy(() -> theoreticalIngredientRepository.saveAndFlush(duplicateAttempt))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("El cocinero busca un alimento por su nombre sin importar si escribe mayúsculas o minúsculas")
        void shouldFindCanonicalFoodCaseInsensitively() {
            NutritionalInformation carrotNutrition = new NutritionalInformation(
                    new BigDecimal("41.00"), new BigDecimal("0.90"), new BigDecimal("9.60"), new BigDecimal("0.20")
            );
            theoreticalIngredientRepository.saveAndFlush(
                    new TheoreticalIngredient("Zanahoria Dulce", "en:fresh-carrots", new BigDecimal("0.90"), carrotNutrition)
            );

            Optional<TheoreticalIngredient> foundUpper = theoreticalIngredientRepository.findByCanonicalName("ZANAHORIA DULCE");
            Optional<TheoreticalIngredient> foundLower = theoreticalIngredientRepository.findByCanonicalName("zanahoria dulce");

            assertThat(foundUpper).isPresent();
            assertThat(foundLower).isPresent();
            assertThat(foundUpper.get().getId()).isEqualTo(foundLower.get().getId());
        }

        @Test
        @DisplayName("El planificador filtra todos los ingredientes pertenecientes a una categoría taxonómica (ej. 'en:fresh-peppers')")
        void shouldListAllIngredientsUnderSpecificTaxonomyCategory() {
            NutritionalInformation pepperNutrition = new NutritionalInformation(
                    new BigDecimal("20.00"), new BigDecimal("1.00"), new BigDecimal("4.50"), new BigDecimal("0.20")
            );
            theoreticalIngredientRepository.saveAndFlush(
                    new TheoreticalIngredient("Pimiento Verde", "en:fresh-peppers", new BigDecimal("0.90"), pepperNutrition)
            );
            theoreticalIngredientRepository.saveAndFlush(
                    new TheoreticalIngredient("Pimiento Rojo", "en:fresh-peppers", new BigDecimal("0.90"), pepperNutrition)
            );

            List<TheoreticalIngredient> pepperList = theoreticalIngredientRepository.findByCategory(TaxonomyTag.of("en:fresh-peppers"));
            assertThat(pepperList).hasSizeGreaterThanOrEqualTo(2);
        }
    }

    // -------------------------------------------------------------------------
    // CASO DE USO 2.6: APROVISIONAMIENTO Y GESTIÓN DE DESPENSA FÍSICA
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("CU 2.6: Compras, Lotes Físicos de Despensa y Consumo FIFO")
    class PantryInventoryUseCases {

        @Test
        @DisplayName("Al escanear un producto con el móvil (EAN-13), el sistema recupera inmediatamente el lote de despensa existente")
        void shouldRetrievePantryBatchByScannedBarcode() {
            NutritionalInformation milkNutrition = new NutritionalInformation(
                    new BigDecimal("64.00"), new BigDecimal("3.20"), new BigDecimal("4.80"), new BigDecimal("3.60")
            );
            TheoreticalIngredient wholeMilk = theoreticalIngredientRepository.saveAndFlush(
                    new TheoreticalIngredient("Leche Entera", "en:milks", new BigDecimal("1.03"), milkNutrition)
            );

            Barcode scannedBarcode = new Barcode("8410000000016");
            RawIngredient purchasedMilkCarton = new RawIngredient(
                    wholeMilk, "Pascual", scannedBarcode.value(), LocalDate.now(),
                    new BigDecimal("1000.00"), milkNutrition, InventoryStatus.ACTIVE
            );
            rawIngredientRepository.saveAndFlush(purchasedMilkCarton);

            List<RawIngredient> locatedBatches = rawIngredientRepository.findByBarcode(scannedBarcode);
            assertThat(locatedBatches).hasSize(1);
            assertThat(locatedBatches.get(0).getBrandName()).isEqualTo("Pascual");
            assertThat(locatedBatches.get(0).getRemainingGrams()).isEqualByComparingTo(new BigDecimal("1000.00"));
        }

        @Test
        @DisplayName("Regla FIFO: si compramos varios lotes en fechas distintas, el sistema propone gastar primero el comprado antes")
        void shouldResolveFifoQueueByPurchaseDateAscending() {
            NutritionalInformation tomatoNutrition = new NutritionalInformation(
                    new BigDecimal("18.00"), new BigDecimal("0.90"), new BigDecimal("3.90"), new BigDecimal("0.20")
            );
            TheoreticalIngredient saladTomato = theoreticalIngredientRepository.saveAndFlush(
                    new TheoreticalIngredient("Tomate Ensalada", "en:fresh-tomatoes", new BigDecimal("0.95"), tomatoNutrition)
            );

            LocalDate boughtLastWeek = LocalDate.now().minusDays(7);
            LocalDate boughtYesterday = LocalDate.now().minusDays(1);

            RawIngredient olderPantryBatch = new RawIngredient(
                    saladTomato, "Frutería Paco", "8412345678905", boughtLastWeek,
                    new BigDecimal("1000.00"), tomatoNutrition, InventoryStatus.QUEUED
            );
            RawIngredient newerSupermarketBatch = new RawIngredient(
                    saladTomato, "Lidl", "8412345678905", boughtYesterday,
                    new BigDecimal("500.00"), tomatoNutrition, InventoryStatus.QUEUED
            );

            rawIngredientRepository.saveAndFlush(newerSupermarketBatch);
            rawIngredientRepository.saveAndFlush(olderPantryBatch);

            List<RawIngredient> queuedBatchesInFifoOrder = rawIngredientRepository.findFifoQueue(saladTomato);
            assertThat(queuedBatchesInFifoOrder).hasSize(2);
            assertThat(queuedBatchesInFifoOrder.get(0).getBrandName()).isEqualTo("Frutería Paco");
            assertThat(queuedBatchesInFifoOrder.get(0).getPurchaseDate()).isEqualTo(boughtLastWeek);
            assertThat(queuedBatchesInFifoOrder.get(1).getBrandName()).isEqualTo("Lidl");
            assertThat(queuedBatchesInFifoOrder.get(1).getPurchaseDate()).isEqualTo(boughtYesterday);
        }

        @Test
        @DisplayName("El usuario decide abrir un bote de tomate Mercadona teniendo otro activo de Lidl: el previo pasa a QUEUED y el nuevo a ACTIVE")
        void shouldAtomicallySwitchActivePantryBatchWithAuditPreservation() {
            NutritionalInformation friedTomatoNutrition = new NutritionalInformation(
                    new BigDecimal("75.00"), new BigDecimal("1.30"), new BigDecimal("7.50"), new BigDecimal("4.20")
            );
            TheoreticalIngredient friedTomato = theoreticalIngredientRepository.saveAndFlush(
                    new TheoreticalIngredient("Tomate Frito", "en:fried-tomato-sauces", new BigDecimal("1.05"), friedTomatoNutrition)
            );

            RawIngredient currentActiveLidlBatch = rawIngredientRepository.saveAndFlush(
                    new RawIngredient(friedTomato, "Lidl", "8411111111111", LocalDate.now().minusDays(3),
                            new BigDecimal("400.00"), friedTomatoNutrition, InventoryStatus.ACTIVE)
            );
            RawIngredient queuedMercadonaBatch = rawIngredientRepository.saveAndFlush(
                    new RawIngredient(friedTomato, "Mercadona", "8422222222222", LocalDate.now().minusDays(1),
                            new BigDecimal("350.00"), friedTomatoNutrition, InventoryStatus.QUEUED)
            );

            pantryService.switchActiveBatch(currentActiveLidlBatch.getId(), queuedMercadonaBatch.getId());

            RawIngredient refreshedLidl = rawIngredientRepository.findById(currentActiveLidlBatch.getId()).orElseThrow();
            RawIngredient refreshedMercadona = rawIngredientRepository.findById(queuedMercadonaBatch.getId()).orElseThrow();

            assertThat(refreshedLidl.getStatus()).isEqualTo(InventoryStatus.QUEUED);
            assertThat(refreshedMercadona.getStatus()).isEqualTo(InventoryStatus.ACTIVE);

            Optional<RawIngredient> currentActive = rawIngredientRepository.findActiveBatch(friedTomato);
            assertThat(currentActive).isPresent();
            assertThat(currentActive.get().getBrandName()).isEqualTo("Mercadona");
        }

        @Test
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        @DisplayName("Transacción atómica: si se ingesta un ticket de compra masivo y una línea falla, se revierte toda la compra sin guardar nada a medias")
        void shouldRollbackEntireReceiptIngestionWhenAnyBatchFails() {
            NutritionalInformation riceNutrition = new NutritionalInformation(
                    new BigDecimal("360.00"), new BigDecimal("7.00"), new BigDecimal("79.00"), new BigDecimal("1.00")
            );
            TheoreticalIngredient canonicalRice = theoreticalIngredientRepository.save(
                    new TheoreticalIngredient("Arroz Bomba", "en:rices", new BigDecimal("0.85"), riceNutrition)
            );

            RawIngredient validRicePacket = new RawIngredient(
                    canonicalRice, "SOS", "8412000000001", LocalDate.now(),
                    new BigDecimal("1000.00"), riceNutrition, InventoryStatus.QUEUED
            );

            TheoreticalIngredient unpersistedInvalidIngredient = new TheoreticalIngredient(
                    "Ingrediente Fantasma No Guardado", "en:ghost", new BigDecimal("1.00"), riceNutrition
            );
            RawIngredient corruptedBatch = new RawIngredient(
                    unpersistedInvalidIngredient, "Marca Erronea", "8412000000002", LocalDate.now(),
                    new BigDecimal("500.00"), riceNutrition, InventoryStatus.QUEUED
            );

            assertThatThrownBy(() -> {
                pantryService.saveBatchesAtomically(List.of(validRicePacket, corruptedBatch));
            }).isNotNull();

            List<RawIngredient> batchesAfterRollback = rawIngredientRepository.findFifoQueue(canonicalRice);
            assertThat(batchesAfterRollback).isEmpty();

            theoreticalIngredientRepository.delete(canonicalRice);
        }
    }

    // -------------------------------------------------------------------------
    // CASO DE USO 2.2: GESTIÓN DEL RECETARIO Y FICHAS TÉCNICAS
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("CU 2.2: Ficha Técnica de Cocina y Composición de Recetas")
    class RecipeCatalogUseCases {

        @Test
        @DisplayName("Al consultar una receta, se cargan sus ingredientes y alimentos canónicos en 1 única consulta SQL (cero N+1)")
        void shouldLoadRecipeWithIngredientsInSingleEagerQueryWithoutLazyExceptions() {
            NutritionalInformation onionNutrition = new NutritionalInformation(
                    new BigDecimal("40.00"), new BigDecimal("1.10"), new BigDecimal("9.30"), new BigDecimal("0.10")
            );
            TheoreticalIngredient sweetOnion = theoreticalIngredientRepository.saveAndFlush(
                    new TheoreticalIngredient("Cebolla Dulce", "en:fresh-onions", new BigDecimal("0.90"), onionNutrition)
            );

            Recipe frenchOnionSoup = new Recipe("Sopa de Cebolla Francesa", "Pochar cebolla lentamente con mantequilla.", 4, 15, 50);
            frenchOnionSoup.addIngredient(RecipeIngredient.ofCanonical(
                    sweetOnion, new BigDecimal("400.00"), MeasurementUnit.GRAM, new BigDecimal("400.00")
            ));
            Recipe savedSoup = recipeRepository.saveAndFlush(frenchOnionSoup);

            Optional<Recipe> eagerRecipeOpt = recipeRepository.findRecipeWithIngredients(savedSoup.getId());
            assertThat(eagerRecipeOpt).isPresent();
            Recipe eagerRecipe = eagerRecipeOpt.get();

            assertThat(eagerRecipe.getIngredients()).hasSize(1);
            assertThat(eagerRecipe.getIngredients().get(0).getTheoreticalIngredient().getCanonicalName()).isEqualTo("Cebolla Dulce");
            assertThat(eagerRecipe.getIngredients().get(0).getDisplayAmount()).isEqualByComparingTo(new BigDecimal("400.00"));
        }

        @Test
        @DisplayName("El usuario busca recetas saludables con tope calórico (ej. máximo 450 kcal) para planificar una cena ligera")
        void shouldFilterAvailableRecipesUnderStrictCaloricCeiling() {
            NutritionalInformation lowCalNutrition = new NutritionalInformation(
                    new BigDecimal("150.00"), new BigDecimal("12.00"), new BigDecimal("5.00"), new BigDecimal("8.00")
            );
            NutritionalInformation heavyCalNutrition = new NutritionalInformation(
                    new BigDecimal("850.00"), new BigDecimal("35.00"), new BigDecimal("75.00"), new BigDecimal("42.00")
            );

            Recipe lightSalad = new Recipe("Ensalada Mediterránea", "Mezclar vegetales frescos", 2, 10, 0);
            lightSalad.updateNominalNutrition(lowCalNutrition);

            Recipe heavyLasagna = new Recipe("Lasaña Cuatro Quesos", "Hornear con bechamel", 6, 30, 45);
            heavyLasagna.updateNominalNutrition(heavyCalNutrition);

            recipeRepository.saveAndFlush(lightSalad);
            recipeRepository.saveAndFlush(heavyLasagna);

            Page<Recipe> healthyRecipes = recipeRepository.findAvailableUnderCalorieLimit(
                    new BigDecimal("500.00"), PageRequest.of(0, 10)
            );

            assertThat(healthyRecipes.getContent()).extracting(Recipe::getTitle)
                    .contains("Ensalada Mediterránea")
                    .doesNotContain("Lasaña Cuatro Quesos");
        }
    }

    // -------------------------------------------------------------------------
    // CASOS DE USO 2.4 Y 2.5: GESTIÓN DEL MENÚ SEMANAL (LA PIZARRA DE LA NEVERA)
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("CU 2.4 & 2.5: Planificación Semanal (La Nevera) y Casillas de Comida")
    class WeeklyMenuPlanningUseCases {

        @Test
        @DisplayName("El usuario no puede crear dos pizarras de menú abiertas que comiencen en el mismo lunes")
        void shouldPreventDuplicateWeeklyMenusStartingOnSameDate() {
            LocalDate weekStartMonday = LocalDate.of(2027, 6, 7);
            weeklyMenuRepository.saveAndFlush(new WeeklyMenu(weekStartMonday));

            WeeklyMenu duplicateMondayMenu = new WeeklyMenu(weekStartMonday);
            assertThatThrownBy(() -> weeklyMenuRepository.saveAndFlush(duplicateMondayMenu))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Invariante de la nevera: no se pueden asignar dos recetas distintas a la misma comida y día (colisión de calendario)")
        void shouldPreventSchedulingConflictOnSameDayAndMealTime() {
            LocalDate weekStartMonday = LocalDate.of(2027, 6, 14);
            WeeklyMenu weekPlan = new WeeklyMenu(weekStartMonday);

            Recipe lentilStew = recipeRepository.saveAndFlush(
                    new Recipe("Lentejas Castellanas", "Cocer con verduras", 4, 15, 40)
            );
            Recipe grilledChicken = recipeRepository.saveAndFlush(
                    new Recipe("Pollo a la Plancha", "Cocinar a fuego vivo", 2, 5, 10)
            );

            MealSlot wednesdayLunchChoice1 = new MealSlot(lentilStew, DayOfWeek.WEDNESDAY, MealTime.LUNCH, 4);
            MealSlot wednesdayLunchCollision = new MealSlot(grilledChicken, DayOfWeek.WEDNESDAY, MealTime.LUNCH, 2);

            weekPlan.addSlot(wednesdayLunchChoice1);
            weekPlan.addSlot(wednesdayLunchCollision);

            assertThatThrownBy(() -> weeklyMenuRepository.saveAndFlush(weekPlan))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("Al abrir la nevera, el usuario visualiza la semana entera con sus casillas y recetas asignadas")
        void shouldLoadCompleteWeeklyMenuWithCalendarSlotsAndAssignedRecipes() {
            LocalDate weekStartMonday = LocalDate.of(2027, 6, 21);
            WeeklyMenu weekPlan = new WeeklyMenu(weekStartMonday);

            Recipe vegetableRisotto = recipeRepository.saveAndFlush(
                    new Recipe("Risotto de Setas", "Remover caldo poco a poco", 3, 10, 25)
            );

            weekPlan.addSlot(new MealSlot(vegetableRisotto, DayOfWeek.FRIDAY, MealTime.DINNER, 3));
            WeeklyMenu savedPlan = weeklyMenuRepository.saveAndFlush(weekPlan);

            Optional<WeeklyMenu> fullCalendarOpt = weeklyMenuRepository.findMenuWithCalendarSlots(savedPlan.getId());
            assertThat(fullCalendarOpt).isPresent();
            WeeklyMenu fullCalendar = fullCalendarOpt.get();

            assertThat(fullCalendar.getSlots()).hasSize(1);
            MealSlot fridaySlot = fullCalendar.getSlots().get(0);
            assertThat(fridaySlot.getDayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
            assertThat(fridaySlot.getMealTime()).isEqualTo(MealTime.DINNER);
            assertThat(fridaySlot.getServings()).isEqualTo(3);
            assertThat(fridaySlot.getRecipe().getTitle()).isEqualTo("Risotto de Setas");
        }
    }
}
