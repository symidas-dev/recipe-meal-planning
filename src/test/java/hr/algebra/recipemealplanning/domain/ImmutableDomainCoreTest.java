package hr.algebra.recipemealplanning.domain;

import hr.algebra.recipemealplanning.domain.enums.DayOfWeek;
import hr.algebra.recipemealplanning.domain.enums.MealTime;
import hr.algebra.recipemealplanning.domain.enums.MeasurementUnit;
import hr.algebra.recipemealplanning.domain.model.Density;
import hr.algebra.recipemealplanning.domain.model.MeasuredQuantity;
import hr.algebra.recipemealplanning.domain.model.NutritionalInformation;
import hr.algebra.recipemealplanning.domain.model.ScalingFactor;
import hr.algebra.recipemealplanning.domain.model.Servings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Immutable Domain Core Verification")
class ImmutableDomainCoreTest {

    @Test
    @DisplayName("MealTime contains 5 physiological slots")
    void mealTimeContainsFiveSlots() {
        assertEquals(5, MealTime.values().length);
        assertNotNull(MealTime.valueOf("MID_MORNING_SNACK"));
    }

    @Test
    @DisplayName("DayOfWeek contains 7 natural days")
    void dayOfWeekContainsSevenDays() {
        assertEquals(7, DayOfWeek.values().length);
        assertEquals(DayOfWeek.MONDAY, DayOfWeek.values()[0]);
    }

    @Test
    @DisplayName("Conversion of mass and volume units to base units")
    void conversionToBaseUnits() {
        assertEquals(
                new BigDecimal("2500.00"),
                MeasurementUnit.KILOGRAM.toBaseUnit(new BigDecimal("2.5")).setScale(2)
        );

        assertEquals(
                new BigDecimal("45.0"),
                MeasurementUnit.TABLESPOON.toBaseUnit(new BigDecimal("3"))
        );
    }

    @Test
    @DisplayName("MeasuredQuantity converts mass to grams directly")
    void measuredQuantityConvertsMassToGrams() {
        MeasuredQuantity kilos = MeasuredQuantity.of(new BigDecimal("2.5"), MeasurementUnit.KILOGRAM);
        MeasuredQuantity grams = kilos.toGrams();

        assertEquals(new BigDecimal("2500.00"), grams.amount());
        assertEquals(MeasurementUnit.GRAM, grams.unit());
    }

    @Test
    @DisplayName("MeasuredQuantity converts volume to grams with density")
    void measuredQuantityConvertsVolumeToGramsWithDensity() {
        MeasuredQuantity tablespoons = MeasuredQuantity.of(new BigDecimal("2"), MeasurementUnit.TABLESPOON);
        Density oilDensity = new Density(new BigDecimal("0.92"));
        MeasuredQuantity grams = tablespoons.toGrams(oilDensity);

        assertEquals(new BigDecimal("27.60"), grams.amount());
        assertEquals(MeasurementUnit.GRAM, grams.unit());
    }

    @Test
    @DisplayName("MeasuredQuantity scales by ScalingFactor preserving unit")
    void measuredQuantityScalesByFactor() {
        MeasuredQuantity flour = MeasuredQuantity.grams(new BigDecimal("200.00"));
        ScalingFactor factor = ScalingFactor.from(new Servings(6), new Servings(4));
        MeasuredQuantity scaled = flour.scaleBy(factor);

        assertEquals(new BigDecimal("300.00"), scaled.amount());
        assertEquals(MeasurementUnit.GRAM, scaled.unit());
    }

    @Test
    @DisplayName("NutritionalInformation adds and scales correctly using ScalingFactor")
    void nutritionalInformationOperations() {
        NutritionalInformation base = new NutritionalInformation(
                new BigDecimal("400.00"),
                new BigDecimal("25.00"),
                new BigDecimal("40.00"),
                new BigDecimal("12.00")
        );

        ScalingFactor factor = ScalingFactor.from(new Servings(6), new Servings(4)); // 1.5000
        NutritionalInformation scaled = base.scale(factor);

        assertEquals(new BigDecimal("600.00"), scaled.calories());
        assertEquals(new BigDecimal("37.50"), scaled.protein());
        assertEquals(new BigDecimal("60.00"), scaled.carbohydrates());
        assertEquals(new BigDecimal("18.00"), scaled.fat());
    }

    @Test
    @DisplayName("Domain primitives reject invalid values at birth")
    void domainPrimitivesValidation() {
        assertThrows(IllegalArgumentException.class, () -> new Servings(0));
        assertThrows(IllegalArgumentException.class, () -> MeasuredQuantity.grams(new BigDecimal("-5")));
        assertThrows(IllegalArgumentException.class, () -> new Density(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new ScalingFactor(BigDecimal.ZERO));
    }
}
