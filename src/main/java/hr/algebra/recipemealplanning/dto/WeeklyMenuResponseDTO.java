package hr.algebra.recipemealplanning.dto;

import hr.algebra.recipemealplanning.domain.model.NutritionalInformation;

import java.time.LocalDate;
import java.util.List;

public record WeeklyMenuResponseDTO(
        Long id,
        LocalDate weekStartDate,
        NutritionalInformation totalWeekNutritionNominal,
        NutritionalInformation totalWeekNutritionActual,
        boolean isClosed,
        List<MealSlotResponseDTO> slots
) {
    public WeeklyMenuResponseDTO {
        slots = slots != null ? List.copyOf(slots) : List.of();
    }
}
