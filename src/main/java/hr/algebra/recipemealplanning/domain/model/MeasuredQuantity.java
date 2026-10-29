package hr.algebra.recipemealplanning.domain.model;

import hr.algebra.recipemealplanning.domain.enums.MeasurementUnit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record MeasuredQuantity(
        BigDecimal amount,
        MeasurementUnit unit
) {

    private static final int DECIMAL_SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    public MeasuredQuantity {
        Objects.requireNonNull(amount, "Amount cannot be null.");
        Objects.requireNonNull(unit, "MeasurementUnit cannot be null.");

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative.");
        }
    }

    public static MeasuredQuantity of(BigDecimal amount, MeasurementUnit unit) {
        return new MeasuredQuantity(amount, unit);
    }

    public static MeasuredQuantity grams(BigDecimal amount) {
        return of(amount, MeasurementUnit.GRAM);
    }

    public static MeasuredQuantity milliliters(BigDecimal amount) {
        return of(amount, MeasurementUnit.MILLILITER);
    }

    public MeasuredQuantity scaleBy(ScalingFactor factor) {
        Objects.requireNonNull(factor, "ScalingFactor cannot be null.");

        BigDecimal scaledAmount = factor.applyTo(this.amount);
        return new MeasuredQuantity(scaledAmount, this.unit);
    }

    public MeasuredQuantity toGrams() {
        if (this.unit.getMagnitudeType() != MeasurementUnit.MagnitudeType.MASS) {
            throw new IllegalArgumentException("Unit must be of type MASS to convert to grams without density.");
        }

        BigDecimal baseAmount = this.unit.toBaseUnit(this.amount);
        BigDecimal roundedGrams = baseAmount.setScale(DECIMAL_SCALE, ROUNDING);

        return new MeasuredQuantity(roundedGrams, MeasurementUnit.GRAM);
    }

    public MeasuredQuantity toGrams(Density density) {
        if (this.unit.getMagnitudeType() != MeasurementUnit.MagnitudeType.VOLUME) {
            throw new IllegalArgumentException("Unit must be of type VOLUME to convert to grams with density.");
        }

        Density effectiveDensity = Objects.requireNonNullElse(density, Density.DEFAULT_CULINARY_WATER_EQUIVALENT);

        BigDecimal milliliters = this.unit.toBaseUnit(this.amount);
        BigDecimal gramsFromDensity = milliliters.multiply(effectiveDensity.value());
        BigDecimal roundedGrams = gramsFromDensity.setScale(DECIMAL_SCALE, ROUNDING);

        return new MeasuredQuantity(roundedGrams, MeasurementUnit.GRAM);
    }
}
