package hr.algebra.recipemealplanning.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Objects;

public record RecipeCreateCommand(
        @NotBlank(message = "title must not be blank")
        @Size(min = 3, max = 150, message = "title must be between 3 and 150 characters")
        String title,

        @NotBlank(message = "instructions must not be blank")
        String instructions,

        @NotNull(message = "referenceServings is required")
        @Min(value = 1, message = "referenceServings must be at least 1")
        @Max(value = 100, message = "referenceServings must not exceed 100")
        Integer referenceServings,

        @NotNull(message = "prepTimeMinutes is required")
        @Min(value = 0, message = "prepTimeMinutes cannot be negative")
        Integer prepTimeMinutes,

        @NotNull(message = "cookTimeMinutes is required")
        @Min(value = 0, message = "cookTimeMinutes cannot be negative")
        Integer cookTimeMinutes,

        @NotEmpty(message = "recipe must contain at least one ingredient line item")
        @Valid
        List<RecipeItemCommand> items
) {
    public RecipeCreateCommand {
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(instructions, "instructions must not be null");
        Objects.requireNonNull(referenceServings, "referenceServings must not be null");
        Objects.requireNonNull(prepTimeMinutes, "prepTimeMinutes must not be null");
        Objects.requireNonNull(cookTimeMinutes, "cookTimeMinutes must not be null");
        Objects.requireNonNull(items, "items must not be null");
        items = List.copyOf(items);
    }
}
