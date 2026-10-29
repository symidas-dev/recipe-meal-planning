package hr.algebra.recipemealplanning.domain.model;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public record NutritionalInformation(
        @Column(name = "calories", precision = 8, scale = 2)
        BigDecimal calories,

        @Column(name = "protein", precision = 8, scale = 2)
        BigDecimal protein,

        @Column(name = "carbohydrates", precision = 8, scale = 2)
        BigDecimal carbohydrates,

        @Column(name = "fat", precision = 8, scale = 2)
        BigDecimal fat
) {
    public static final NutritionalInformation ZERO = new NutritionalInformation(
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO
    );

    public NutritionalInformation {
        calories = Objects.requireNonNullElse(calories, BigDecimal.ZERO);
        protein = Objects.requireNonNullElse(protein, BigDecimal.ZERO);
        carbohydrates = Objects.requireNonNullElse(carbohydrates, BigDecimal.ZERO);
        fat = Objects.requireNonNullElse(fat, BigDecimal.ZERO);
    }

    public NutritionalInformation add(NutritionalInformation other) {
        if (other == null) {
            return this;
        }
        return new NutritionalInformation(
                this.calories.add(other.calories),
                this.protein.add(other.protein),
                this.carbohydrates.add(other.carbohydrates),
                this.fat.add(other.fat)
        );
    }

    public NutritionalInformation scale(ScalingFactor factor) {
        if (factor == null) {
            return ZERO;
        }
        return new NutritionalInformation(
                factor.applyTo(this.calories),
                factor.applyTo(this.protein),
                factor.applyTo(this.carbohydrates),
                factor.applyTo(this.fat)
        );
    }
}
