package hr.algebra.recipemealplanning.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public record Density(BigDecimal value) {

    private static final BigDecimal DEFAULT_VALUE = new BigDecimal("1.00");
    public static final Density DEFAULT_CULINARY_WATER_EQUIVALENT = new Density(DEFAULT_VALUE);

    public Density {
        value = Objects.requireNonNullElse(value, DEFAULT_VALUE);
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Density must be strictly positive.");
        }
    }

    public static Density of(BigDecimal value) {
        if (value == null || value.compareTo(DEFAULT_VALUE) == 0) {
            return DEFAULT_CULINARY_WATER_EQUIVALENT;
        }
        return new Density(value);
    }
}
