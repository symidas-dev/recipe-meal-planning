package hr.algebra.recipemealplanning.dto;

import hr.algebra.recipemealplanning.domain.model.NutritionalInformation;

import java.math.BigDecimal;
import java.util.List;

public record ScaledRecipeDTO(
        Long recipeId,
        String title,
        int referenceServings,
        int targetServings,
        BigDecimal scalingFactor,
        int prepTimeMinutes,
        int cookTimeMinutes,
        NutritionalInformation scaledNutrition,
        List<ScaledIngredientLineDTO> scaledIngredients
) {
    public ScaledRecipeDTO {
        scaledIngredients = scaledIngredients != null ? List.copyOf(scaledIngredients) : List.of();
    }
}
