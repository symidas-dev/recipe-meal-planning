package hr.algebra.recipemealplanning.domain.model;

import java.math.BigDecimal;

public record Servings(int value) {

    public Servings {
        if (value <= 0) {
            throw new IllegalArgumentException("Servings must be strictly positive.");
        }
    }

    public BigDecimal asBigDecimal() {
        return BigDecimal.valueOf(this.value);
    }
}
