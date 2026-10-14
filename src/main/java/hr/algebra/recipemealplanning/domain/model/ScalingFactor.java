package hr.algebra.recipemealplanning.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record ScalingFactor(BigDecimal value) {

    private static final int SCALE = 4;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    public ScalingFactor {
        Objects.requireNonNull(value, "Scaling factor cannot be null.");
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Scaling factor must be strictly positive.");
        }
    }

    public static ScalingFactor from(Servings target, Servings reference) {
        Objects.requireNonNull(target, "Target servings cannot be null.");
        Objects.requireNonNull(reference, "Reference servings cannot be null.");

        BigDecimal numerator = target.asBigDecimal();
        BigDecimal denominator = reference.asBigDecimal();
        BigDecimal ratio = numerator.divide(denominator, SCALE, ROUNDING);

        return new ScalingFactor(ratio);
    }

    public BigDecimal applyTo(BigDecimal amount) {
        Objects.requireNonNull(amount, "Amount cannot be null.");

        BigDecimal scaledAmount = amount.multiply(this.value);
        return scaledAmount.setScale(2, ROUNDING);
    }
}
