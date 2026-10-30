package hr.algebra.recipemealplanning.dto;

import hr.algebra.recipemealplanning.domain.model.NutritionalInformation;

import java.util.List;

public record RecipeResponseDTO(
        Long id,
        String title,
        String instructions,
        int referenceServings,
        int prepTimeMinutes,
        int cookTimeMinutes,
        NutritionalInformation nominalNutrition,
        boolean isArchived,
        List<RecipeIngredientResponseDTO> ingredients
) {
    public RecipeResponseDTO {
        ingredients = ingredients != null ? List.copyOf(ingredients) : List.of();
    }
}
