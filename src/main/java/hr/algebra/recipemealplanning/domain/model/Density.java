package hr.algebra.recipemealplanning.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public record Density(BigDecimal value) {

    public Density {
        Objects.requireNonNull(value, "Density cannot be null.");
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Density must be strictly positive.");
        }
    }
}
