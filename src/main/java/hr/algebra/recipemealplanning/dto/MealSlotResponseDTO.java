package hr.algebra.recipemealplanning.dto;

import hr.algebra.recipemealplanning.domain.enums.DayOfWeek;
import hr.algebra.recipemealplanning.domain.enums.MealTime;
import hr.algebra.recipemealplanning.domain.model.NutritionalInformation;

public record MealSlotResponseDTO(
        Long id,
        Long recipeId,
        String recipeTitle,
        DayOfWeek dayOfWeek,
        MealTime mealTime,
        int servings,
        NutritionalInformation actualNutrition
) {
}
