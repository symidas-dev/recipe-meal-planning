package hr.algebra.recipemealplanning.dto;

import hr.algebra.recipemealplanning.domain.enums.MeasurementUnit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RecipeItemCommand(
        Long theoreticalIngredientId,
        Long subRecipeId,
        @NotNull(message = "displayAmount is required")
        @DecimalMin(value = "0.01", message = "displayAmount must be strictly positive")
        BigDecimal displayAmount,
        @NotNull(message = "displayUnit is required")
        MeasurementUnit displayUnit
) {
    public RecipeItemCommand {
        if ((theoreticalIngredientId == null && subRecipeId == null) ||
                (theoreticalIngredientId != null && subRecipeId != null)) {
            throw new IllegalArgumentException(
                    "RecipeItemCommand must reference either a theoreticalIngredientId or a subRecipeId, but not both or neither"
            );
        }
    }

    public static RecipeItemCommand ofCanonical(Long theoreticalIngredientId, BigDecimal displayAmount, MeasurementUnit displayUnit) {
        return new RecipeItemCommand(theoreticalIngredientId, null, displayAmount, displayUnit);
    }

    public static RecipeItemCommand ofSubRecipe(Long subRecipeId, BigDecimal displayAmount, MeasurementUnit displayUnit) {
        return new RecipeItemCommand(null, subRecipeId, displayAmount, displayUnit);
    }
}
