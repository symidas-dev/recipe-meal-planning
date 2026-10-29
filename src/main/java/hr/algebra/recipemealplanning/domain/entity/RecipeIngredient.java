package hr.algebra.recipemealplanning.domain.entity;

import hr.algebra.recipemealplanning.domain.enums.MeasurementUnit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "recipe_ingredients")
@Getter
public class RecipeIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theoretical_ingredient_id")
    private TheoreticalIngredient theoreticalIngredient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_recipe_id")
    private Recipe subRecipe;

    @Column(name = "display_amount", nullable = false, precision = 8, scale = 2)
    private BigDecimal displayAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "display_unit", nullable = false, length = 20)
    private MeasurementUnit displayUnit;

    @Column(name = "canonical_grams", nullable = false, precision = 8, scale = 2)
    private BigDecimal canonicalGrams;

    protected RecipeIngredient() {
    }

    public static RecipeIngredient ofCanonical(
            TheoreticalIngredient theoreticalIngredient,
            BigDecimal displayAmount,
            MeasurementUnit displayUnit,
            BigDecimal canonicalGrams) {
        RecipeIngredient item = new RecipeIngredient();
        item.theoreticalIngredient = Objects.requireNonNull(theoreticalIngredient, "theoreticalIngredient must not be null");
        item.displayAmount = Objects.requireNonNull(displayAmount, "displayAmount must not be null");
        item.displayUnit = Objects.requireNonNull(displayUnit, "displayUnit must not be null");
        item.canonicalGrams = Objects.requireNonNull(canonicalGrams, "canonicalGrams must not be null");
        return item;
    }

    public static RecipeIngredient ofSubRecipe(
            Recipe subRecipe,
            BigDecimal displayAmount,
            MeasurementUnit displayUnit,
            BigDecimal canonicalGrams) {
        RecipeIngredient item = new RecipeIngredient();
        item.subRecipe = Objects.requireNonNull(subRecipe, "subRecipe must not be null");
        item.displayAmount = Objects.requireNonNull(displayAmount, "displayAmount must not be null");
        item.displayUnit = Objects.requireNonNull(displayUnit, "displayUnit must not be null");
        item.canonicalGrams = Objects.requireNonNull(canonicalGrams, "canonicalGrams must not be null");
        return item;
    }

    public boolean isSubRecipe() {
        return subRecipe != null;
    }

    void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecipeIngredient that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
