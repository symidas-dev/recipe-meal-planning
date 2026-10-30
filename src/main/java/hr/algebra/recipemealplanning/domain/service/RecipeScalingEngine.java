package hr.algebra.recipemealplanning.domain.service;

import hr.algebra.recipemealplanning.domain.entity.Recipe;
import hr.algebra.recipemealplanning.domain.entity.RecipeIngredient;
import hr.algebra.recipemealplanning.domain.model.NutritionalComponent;
import hr.algebra.recipemealplanning.domain.model.NutritionalInformation;
import hr.algebra.recipemealplanning.domain.model.ScalingFactor;
import hr.algebra.recipemealplanning.domain.model.Servings;
import hr.algebra.recipemealplanning.dto.ScaledIngredientLineDTO;
import hr.algebra.recipemealplanning.dto.ScaledRecipeDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class RecipeScalingEngine {

    public ScaledRecipeDTO scaleRecipe(Recipe originalRecipe, int targetServings) {
        Objects.requireNonNull(originalRecipe, "originalRecipe must not be null");
        if (targetServings <= 0) {
            throw new IllegalArgumentException("targetServings must be strictly positive");
        }

        Servings referenceServings = new Servings(originalRecipe.getReferenceServings());
        Servings desiredTargetServings = new Servings(targetServings);
        ScalingFactor scalingMultiplier = ScalingFactor.from(desiredTargetServings, referenceServings);
        BigDecimal multiplier = scalingMultiplier.value();

        List<ScaledIngredientLineDTO> scaledLines = new ArrayList<>();
        for (RecipeIngredient lineItem : originalRecipe.getIngredients()) {
            BigDecimal scaledAmount = lineItem.getDisplayAmount()
                    .multiply(multiplier)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal scaledCanonicalGrams = lineItem.getCanonicalGrams()
                    .multiply(multiplier)
                    .setScale(2, RoundingMode.HALF_UP);

            NutritionalComponent component = lineItem.getNutritionalComponent();
            scaledLines.add(new ScaledIngredientLineDTO(
                    lineItem.getId(),
                    component.getDisplayName(),
                    scaledAmount,
                    lineItem.getDisplayUnit(),
                    scaledCanonicalGrams
            ));
        }

        NutritionalInformation nominal = originalRecipe.getNominalNutrition();
        NutritionalInformation scaledNutrition = new NutritionalInformation(
                nominal.calories().multiply(multiplier).setScale(2, RoundingMode.HALF_UP),
                nominal.protein().multiply(multiplier).setScale(2, RoundingMode.HALF_UP),
                nominal.carbohydrates().multiply(multiplier).setScale(2, RoundingMode.HALF_UP),
                nominal.fat().multiply(multiplier).setScale(2, RoundingMode.HALF_UP)
        );

        return new ScaledRecipeDTO(
                originalRecipe.getId(),
                originalRecipe.getTitle(),
                originalRecipe.getReferenceServings(),
                targetServings,
                multiplier,
                originalRecipe.getPrepTimeMinutes(),
                originalRecipe.getCookTimeMinutes(),
                scaledNutrition,
                scaledLines
        );
    }
}
