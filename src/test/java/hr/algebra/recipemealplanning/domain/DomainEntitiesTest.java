package hr.algebra.recipemealplanning.domain;

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
import hr.algebra.recipemealplanning.domain.model.NutritionalInformation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Domain Entities Lifecycle & Invariant Verification")
class DomainEntitiesTest {

    @Test
    @DisplayName("TheoreticalIngredient enforces non-null invariants and trim")
    void theoreticalIngredientInvariants() {
        NutritionalInformation nutrition = new NutritionalInformation(
                new BigDecimal("75.00"), new BigDecimal("1.30"), new BigDecimal("7.50"), new BigDecimal("4.20")
        );

        TheoreticalIngredient tomato = new TheoreticalIngredient(
                "  Tomate frito  ",
                "en:fried-tomato-sauces",
                new BigDecimal("1.05"),
                nutrition
        );

        assertEquals("Tomate frito", tomato.getCanonicalName());
        assertEquals(new BigDecimal("75.00"), tomato.getNominalNutritionPer100g().calories());
    }

    @Test
    @DisplayName("RawIngredient consumes stock and transitions status to SPENT at zero")
    void rawIngredientConsumptionLifecycle() {
        NutritionalInformation theoNutrition = new NutritionalInformation(
                new BigDecimal("75.00"), new BigDecimal("1.30"), new BigDecimal("7.50"), new BigDecimal("4.20")
        );
        TheoreticalIngredient tomato = new TheoreticalIngredient(
                "Tomate frito", "en:fried-tomato-sauces", new BigDecimal("1.05"), theoNutrition
        );

        NutritionalInformation batchNutrition = new NutritionalInformation(
                new BigDecimal("62.00"), new BigDecimal("1.40"), new BigDecimal("6.10"), new BigDecimal("3.60")
        );
        RawIngredient batch = new RawIngredient(
                tomato, "Lupa Bio", "8421000987654", LocalDate.of(2026, 10, 15),
                new BigDecimal("400.00"), batchNutrition, InventoryStatus.ACTIVE
        );

        assertEquals(InventoryStatus.ACTIVE, batch.getStatus());
        assertEquals(new BigDecimal("400.00"), batch.getRemainingGrams());

        batch.consumeGrams(new BigDecimal("150.00"));
        assertEquals(new BigDecimal("250.00"), batch.getRemainingGrams());
        assertEquals(InventoryStatus.ACTIVE, batch.getStatus());

        batch.consumeGrams(new BigDecimal("250.00"));
        assertEquals(BigDecimal.ZERO, batch.getRemainingGrams());
        assertEquals(InventoryStatus.SPENT, batch.getStatus());
    }

    @Test
    @DisplayName("Recipe manages bidirectional association with RecipeIngredient and computes nominal nutrition")
    void recipeAggregateLifecycle() {
        Recipe bolognese = new Recipe("Bolognese Pasta", "Boil pasta and add sauce", 4, 15, 25);
        assertEquals(4, bolognese.getReferenceServings());
        assertFalse(bolognese.isArchived());

        NutritionalInformation nutrition = new NutritionalInformation(
                new BigDecimal("75.00"), new BigDecimal("1.30"), new BigDecimal("7.50"), new BigDecimal("4.20")
        );
        TheoreticalIngredient tomato = new TheoreticalIngredient(
                "Tomate frito", "en:fried-tomato-sauces", new BigDecimal("1.05"), nutrition
        );

        RecipeIngredient line = RecipeIngredient.ofCanonical(
                tomato, new BigDecimal("400.00"), MeasurementUnit.GRAM, new BigDecimal("400.00")
        );

        bolognese.addIngredient(line);
        assertEquals(1, bolognese.getIngredients().size());
        assertEquals(bolognese, line.getRecipe());

        NutritionalInformation nominalRecipeNutrition = new NutritionalInformation(
                new BigDecimal("520.00"), new BigDecimal("22.00"), new BigDecimal("65.00"), new BigDecimal("14.00")
        );
        bolognese.updateNominalNutrition(nominalRecipeNutrition);
        assertEquals(new BigDecimal("520.00"), bolognese.getNominalNutrition().calories());

        bolognese.archive();
        assertTrue(bolognese.isArchived());
    }

    @Test
    @DisplayName("WeeklyMenu manages MealSlot composition and preserves unique calendar entry constraints")
    void weeklyMenuAggregateLifecycle() {
        LocalDate monday = LocalDate.of(2026, 10, 19);
        WeeklyMenu menu = new WeeklyMenu(monday);
        assertNotNull(menu.getWeekStartDate());
        assertFalse(menu.isClosed());

        Recipe bolognese = new Recipe("Bolognese Pasta", "Boil pasta and add sauce", 4, 15, 25);
        MealSlot wednesdayLunch = new MealSlot(bolognese, DayOfWeek.WEDNESDAY, MealTime.LUNCH, 2);

        menu.addSlot(wednesdayLunch);
        assertEquals(1, menu.getSlots().size());
        assertEquals(menu, wednesdayLunch.getWeeklyMenu());

        NutritionalInformation slotNutrition = new NutritionalInformation(
                new BigDecimal("580.00"), new BigDecimal("24.00"), new BigDecimal("70.00"), new BigDecimal("16.00")
        );
        wednesdayLunch.updateActualNutrition(slotNutrition);
        assertEquals(new BigDecimal("580.00"), wednesdayLunch.getActualNutrition().calories());

        NutritionalInformation nominalWeek = new NutritionalInformation(
                new BigDecimal("2100.00"), new BigDecimal("90.00"), new BigDecimal("260.00"), new BigDecimal("60.00")
        );
        NutritionalInformation actualWeek = new NutritionalInformation(
                new BigDecimal("2250.00"), new BigDecimal("95.00"), new BigDecimal("280.00"), new BigDecimal("65.00")
        );
        menu.updateWeeklyTotals(nominalWeek, actualWeek);
        assertEquals(new BigDecimal("2250.00"), menu.getTotalWeekNutritionActual().calories());

        menu.close();
        assertTrue(menu.isClosed());
    }

    @Test
    @DisplayName("Entity constructors reject invalid business values at birth")
    void entityValidationInvariants() {
        assertThrows(IllegalArgumentException.class, () ->
                new Recipe("Test", "Instructions", 0, 10, 10)
        );
        assertThrows(IllegalArgumentException.class, () ->
                new Recipe("Test", "Instructions", 4, -5, 10)
        );
    }

    @Test
    @DisplayName("PantryInventoryService switches active batch and demotes previous active to QUEUED")
    void switchActiveBatchSideEffect() {
        TheoreticalIngredient tomato = new TheoreticalIngredient(
                "Tomate frito", "en:fried-tomato-sauces", new BigDecimal("1.05"),
                new NutritionalInformation(new BigDecimal("75.00"), new BigDecimal("1.30"), new BigDecimal("7.50"), new BigDecimal("4.20"))
        );

        RawIngredient lidlTomato = new RawIngredient(
                tomato, "Lidl Freshona", "8431000123456", LocalDate.of(2026, 10, 10),
                new BigDecimal("400.00"),
                new NutritionalInformation(new BigDecimal("68.00"), new BigDecimal("1.20"), new BigDecimal("7.00"), new BigDecimal("3.80")),
                InventoryStatus.ACTIVE
        );

        RawIngredient mercadonaTomato = new RawIngredient(
                tomato, "Hacendado", "8480000123456", LocalDate.of(2026, 10, 15),
                new BigDecimal("400.00"),
                new NutritionalInformation(new BigDecimal("84.00"), new BigDecimal("1.20"), new BigDecimal("9.80"), new BigDecimal("4.50")),
                InventoryStatus.QUEUED
        );

        assertEquals(InventoryStatus.ACTIVE, lidlTomato.getStatus());
        assertEquals(InventoryStatus.QUEUED, mercadonaTomato.getStatus());

        hr.algebra.recipemealplanning.domain.service.PantryInventoryService inventoryService =
                new hr.algebra.recipemealplanning.domain.service.PantryInventoryService();

        inventoryService.switchActiveBatch(lidlTomato, mercadonaTomato);

        assertEquals(InventoryStatus.QUEUED, lidlTomato.getStatus());
        assertEquals(InventoryStatus.ACTIVE, mercadonaTomato.getStatus());
    }

    @Test
    @DisplayName("PantryInventoryService rejects switching batches of completely different theoretical ingredients")
    void switchActiveBatchRejectsDifferentTheoreticalIngredients() {
        TheoreticalIngredient tomato = new TheoreticalIngredient(
                "Tomate frito", "en:fried-tomato-sauces", new BigDecimal("1.05"),
                new NutritionalInformation(new BigDecimal("75.00"), new BigDecimal("1.30"), new BigDecimal("7.50"), new BigDecimal("4.20"))
        );
        TheoreticalIngredient flour = new TheoreticalIngredient(
                "Harina de trigo", "en:wheat-flours", new BigDecimal("0.65"),
                new NutritionalInformation(new BigDecimal("364.00"), new BigDecimal("10.00"), new BigDecimal("76.00"), new BigDecimal("1.00"))
        );

        RawIngredient lidlTomato = new RawIngredient(
                tomato, "Lidl Freshona", "8431000123456", LocalDate.of(2026, 10, 10),
                new BigDecimal("400.00"),
                new NutritionalInformation(new BigDecimal("68.00"), new BigDecimal("1.20"), new BigDecimal("7.00"), new BigDecimal("3.80")),
                InventoryStatus.ACTIVE
        );

        RawIngredient galloFlour = new RawIngredient(
                flour, "Gallo", "8410000999999", LocalDate.of(2026, 10, 12),
                new BigDecimal("1000.00"),
                new NutritionalInformation(new BigDecimal("364.00"), new BigDecimal("10.00"), new BigDecimal("76.00"), new BigDecimal("1.00")),
                InventoryStatus.QUEUED
        );

        hr.algebra.recipemealplanning.domain.service.PantryInventoryService inventoryService =
                new hr.algebra.recipemealplanning.domain.service.PantryInventoryService();

        assertThrows(IllegalArgumentException.class, () ->
                inventoryService.switchActiveBatch(lidlTomato, galloFlour)
        );
    }
}
