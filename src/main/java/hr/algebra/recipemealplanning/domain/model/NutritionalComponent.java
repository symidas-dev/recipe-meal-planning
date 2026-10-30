package hr.algebra.recipemealplanning.domain.model;

import java.math.BigDecimal;

public interface NutritionalComponent {

    String getDisplayName();

    NutritionalInformation computeNutritionForGrams(BigDecimal grams);
}
