package hr.algebra.recipemealplanning.dto;

import hr.algebra.recipemealplanning.domain.enums.MeasurementUnit;

import java.math.BigDecimal;

public record ScaledIngredientLineDTO(
        Long itemId,
        String displayName,
        BigDecimal scaledAmount,
        MeasurementUnit displayUnit,
        BigDecimal scaledCanonicalGrams
) {
}
