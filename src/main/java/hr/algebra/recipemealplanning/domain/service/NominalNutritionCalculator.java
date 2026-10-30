package hr.algebra.recipemealplanning.domain.service;

import hr.algebra.recipemealplanning.domain.entity.RecipeIngredient;
import hr.algebra.recipemealplanning.domain.model.NutritionalComponent;
import hr.algebra.recipemealplanning.domain.model.NutritionalInformation;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class NominalNutritionCalculator {

    public NutritionalInformation computeNominalNutrition(List<RecipeIngredient> ingredientLines) {
        if (ingredientLines == null || ingredientLines.isEmpty()) {
            return NutritionalInformation.ZERO;
        }

        BigDecimal accumulatedCalories = BigDecimal.ZERO;
        BigDecimal accumulatedProtein = BigDecimal.ZERO;
        BigDecimal accumulatedCarbohydrates = BigDecimal.ZERO;
        BigDecimal accumulatedFat = BigDecimal.ZERO;

        for (RecipeIngredient lineItem : ingredientLines) {
            NutritionalComponent component = lineItem.getNutritionalComponent();
            NutritionalInformation lineNutrition = component.computeNutritionForGrams(lineItem.getCanonicalGrams());

            accumulatedCalories = accumulatedCalories.add(lineNutrition.calories());
            accumulatedProtein = accumulatedProtein.add(lineNutrition.protein());
            accumulatedCarbohydrates = accumulatedCarbohydrates.add(lineNutrition.carbohydrates());
            accumulatedFat = accumulatedFat.add(lineNutrition.fat());
        }

        return new NutritionalInformation(
                accumulatedCalories.setScale(2, RoundingMode.HALF_UP),
                accumulatedProtein.setScale(2, RoundingMode.HALF_UP),
                accumulatedCarbohydrates.setScale(2, RoundingMode.HALF_UP),
                accumulatedFat.setScale(2, RoundingMode.HALF_UP)
        );
    }
}
