package hr.algebra.recipemealplanning.dto;

import hr.algebra.recipemealplanning.domain.enums.MeasurementUnit;

import java.math.BigDecimal;

public record RecipeIngredientResponseDTO(
        Long id,
        String displayName,
        BigDecimal displayAmount,
        MeasurementUnit displayUnit,
        BigDecimal canonicalGrams,
        Long theoreticalIngredientId,
        Long subRecipeId
) {
}
