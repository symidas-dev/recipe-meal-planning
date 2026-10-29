package hr.algebra.recipemealplanning.domain;

import hr.algebra.recipemealplanning.domain.enums.InventoryStatus;
import hr.algebra.recipemealplanning.domain.enums.MeasurementUnit;
import hr.algebra.recipemealplanning.domain.model.Barcode;
import hr.algebra.recipemealplanning.domain.model.Density;
import hr.algebra.recipemealplanning.domain.model.MeasuredQuantity;
import hr.algebra.recipemealplanning.domain.model.NutritionalInformation;
import hr.algebra.recipemealplanning.domain.model.ScalingFactor;
import hr.algebra.recipemealplanning.domain.model.Servings;
import hr.algebra.recipemealplanning.domain.model.TaxonomyTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Immutable Domain Core Verification")
class ImmutableDomainCoreTest {

    @Test
    @DisplayName("Servings validates strictly positive integer values")
    void servingsValidation() {
        Servings servings = new Servings(4);
        assertEquals(4, servings.value());
        assertEquals(new BigDecimal("4"), servings.asBigDecimal());
    }

    @Test
    @DisplayName("ScalingFactor calculates exact multiplier and scales amount")
    void scalingFactorCalculation() {
        Servings reference = new Servings(4);
        Servings target = new Servings(6);

        ScalingFactor factor = ScalingFactor.from(target, reference);
        assertEquals(new BigDecimal("1.5000"), factor.value());

        BigDecimal scaledAmount = factor.applyTo(new BigDecimal("250.00"));
        assertEquals(new BigDecimal("375.00"), scaledAmount);
    }

    @Test
    @DisplayName("MeasuredQuantity converts mass directly to grams")
    void measuredQuantityConvertsMassToGrams() {
        MeasuredQuantity kilograms = MeasuredQuantity.of(new BigDecimal("2.5"), MeasurementUnit.KILOGRAM);
        MeasuredQuantity grams = kilograms.toGrams();

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
    @DisplayName("MeasuredQuantity falls back to default culinary density when null")
    void measuredQuantityFallsBackToDefaultDensityWhenNull() {
        MeasuredQuantity milliliters = MeasuredQuantity.milliliters(new BigDecimal("250"));
        MeasuredQuantity grams = milliliters.toGrams(null);

        assertEquals(new BigDecimal("250.00"), grams.amount());
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

        ScalingFactor factor = ScalingFactor.from(new Servings(6), new Servings(4));
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
        assertThrows(IllegalArgumentException.class, () -> new Barcode("12345"));
        assertThrows(IllegalArgumentException.class, () -> new TaxonomyTag("INVALID TAG FORMAT"));
    }

    @Test
    @DisplayName("Barcode rejects invalid GS1 Modulo 10 check digit")
    void barcodeRejectsInvalidCheckDigit() {
        assertThrows(IllegalArgumentException.class, () -> new Barcode("8410916914859"));
    }

    @Test
    @DisplayName("Barcode validates verified GS1 EAN-13 barcodes from Open Food Facts")
    void barcodeEan13Validation() {
        Barcode maretti = Barcode.of("3800205872962");
        assertEquals("3800205872962", maretti.value());

        Barcode orlando = Barcode.of("8410916914858");
        assertEquals("8410916914858", orlando.value());
    }

    @Test
    @DisplayName("TaxonomyTag validates Open Food Facts format and normalizes to lower case")
    void taxonomyTagValidation() {
        TaxonomyTag tag = TaxonomyTag.of("en:fried-tomato-sauces");
        assertEquals("en:fried-tomato-sauces", tag.value());

        TaxonomyTag upperCaseInput = TaxonomyTag.of("ES:TOMATES-FRITOS");
        assertEquals("es:tomates-fritos", upperCaseInput.value());
    }

    @Test
    @DisplayName("InventoryStatus enum supports full pantry lifecycle")
    void inventoryStatusEnumValues() {
        assertEquals(InventoryStatus.ACTIVE, InventoryStatus.valueOf("ACTIVE"));
        assertEquals(InventoryStatus.QUEUED, InventoryStatus.valueOf("QUEUED"));
        assertEquals(InventoryStatus.SPENT, InventoryStatus.valueOf("SPENT"));
    }
}
