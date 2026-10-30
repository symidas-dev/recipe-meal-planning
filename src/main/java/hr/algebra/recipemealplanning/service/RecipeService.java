package hr.algebra.recipemealplanning.service;

import hr.algebra.recipemealplanning.domain.entity.Recipe;
import hr.algebra.recipemealplanning.domain.entity.RecipeIngredient;
import hr.algebra.recipemealplanning.domain.entity.TheoreticalIngredient;
import hr.algebra.recipemealplanning.domain.enums.MeasurementUnit;
import hr.algebra.recipemealplanning.domain.model.Density;
import hr.algebra.recipemealplanning.domain.model.MeasuredQuantity;
import hr.algebra.recipemealplanning.domain.model.NutritionalInformation;
import hr.algebra.recipemealplanning.domain.service.NominalNutritionCalculator;
import hr.algebra.recipemealplanning.domain.service.RecipeScalingEngine;
import hr.algebra.recipemealplanning.domain.service.SubRecipeCycleDetector;
import hr.algebra.recipemealplanning.dto.RecipeCreateCommand;
import hr.algebra.recipemealplanning.dto.RecipeIngredientResponseDTO;
import hr.algebra.recipemealplanning.dto.RecipeItemCommand;
import hr.algebra.recipemealplanning.dto.RecipeResponseDTO;
import hr.algebra.recipemealplanning.dto.ScaledRecipeDTO;
import hr.algebra.recipemealplanning.repository.RecipeRepository;
import hr.algebra.recipemealplanning.repository.TheoreticalIngredientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final TheoreticalIngredientRepository theoreticalIngredientRepository;
    private final SubRecipeCycleDetector subRecipeCycleDetector;
    private final NominalNutritionCalculator nominalNutritionCalculator;
    private final RecipeScalingEngine recipeScalingEngine;

    public RecipeService(
            RecipeRepository recipeRepository,
            TheoreticalIngredientRepository theoreticalIngredientRepository,
            SubRecipeCycleDetector subRecipeCycleDetector,
            NominalNutritionCalculator nominalNutritionCalculator,
            RecipeScalingEngine recipeScalingEngine) {
        this.recipeRepository = Objects.requireNonNull(recipeRepository, "recipeRepository must not be null");
        this.theoreticalIngredientRepository = Objects.requireNonNull(theoreticalIngredientRepository, "theoreticalIngredientRepository must not be null");
        this.subRecipeCycleDetector = Objects.requireNonNull(subRecipeCycleDetector, "subRecipeCycleDetector must not be null");
        this.nominalNutritionCalculator = Objects.requireNonNull(nominalNutritionCalculator, "nominalNutritionCalculator must not be null");
        this.recipeScalingEngine = Objects.requireNonNull(recipeScalingEngine, "recipeScalingEngine must not be null");
    }

    public RecipeResponseDTO registerNewRecipe(RecipeCreateCommand recipeCreationCommand) {
        Objects.requireNonNull(recipeCreationCommand, "recipeCreationCommand must not be null");

        Recipe newRecipe = new Recipe(
                recipeCreationCommand.title(),
                recipeCreationCommand.instructions(),
                recipeCreationCommand.referenceServings(),
                recipeCreationCommand.prepTimeMinutes(),
                recipeCreationCommand.cookTimeMinutes()
        );

        List<RecipeIngredient> ingredientLines = new ArrayList<>();
        for (RecipeItemCommand lineItemCommand : recipeCreationCommand.items()) {
            if (lineItemCommand.subRecipeId() != null) {
                Recipe subRecipe = recipeRepository.findRecipeWithIngredients(lineItemCommand.subRecipeId())
                        .orElseThrow(() -> new IllegalArgumentException("Sub-recipe not found with id: " + lineItemCommand.subRecipeId()));

                subRecipeCycleDetector.validateAcyclicity(newRecipe, subRecipe);

                BigDecimal canonicalGrams = computeCanonicalGrams(lineItemCommand.displayAmount(), lineItemCommand.displayUnit(), null);
                RecipeIngredient subRecipeLine = RecipeIngredient.ofSubRecipe(subRecipe, lineItemCommand.displayAmount(), lineItemCommand.displayUnit(), canonicalGrams);
                newRecipe.addIngredient(subRecipeLine);
                ingredientLines.add(subRecipeLine);
            } else {
                TheoreticalIngredient canonicalIngredient = theoreticalIngredientRepository.findById(lineItemCommand.theoreticalIngredientId())
                        .orElseThrow(() -> new IllegalArgumentException("Canonical food not found with id: " + lineItemCommand.theoreticalIngredientId()));

                Density physicalDensity = Density.of(canonicalIngredient.getStandardDensity());
                BigDecimal canonicalGrams = computeCanonicalGrams(lineItemCommand.displayAmount(), lineItemCommand.displayUnit(), physicalDensity);
                RecipeIngredient canonicalLine = RecipeIngredient.ofCanonical(canonicalIngredient, lineItemCommand.displayAmount(), lineItemCommand.displayUnit(), canonicalGrams);
                newRecipe.addIngredient(canonicalLine);
                ingredientLines.add(canonicalLine);
            }
        }

        NutritionalInformation precomputedNominalNutrition = nominalNutritionCalculator.computeNominalNutrition(ingredientLines);
        newRecipe.updateNominalNutrition(precomputedNominalNutrition);

        Recipe savedRecipe = recipeRepository.save(newRecipe);
        return mapToResponseDTO(savedRecipe);
    }

    @Transactional(readOnly = true)
    public RecipeResponseDTO getRecipeTechnicalSheet(Long recipeId) {
        Objects.requireNonNull(recipeId, "recipeId must not be null");
        Recipe recipe = recipeRepository.findRecipeWithIngredients(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with id: " + recipeId));
        return mapToResponseDTO(recipe);
    }

    @Transactional(readOnly = true)
    public Page<RecipeResponseDTO> listActiveCookbook(Pageable pageable) {
        Objects.requireNonNull(pageable, "pageable must not be null");
        return recipeRepository.findByIsArchivedFalse(pageable)
                .map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public ScaledRecipeDTO scaleRecipeForHeadcount(Long recipeId, int targetServings) {
        Objects.requireNonNull(recipeId, "recipeId must not be null");
        Recipe recipe = recipeRepository.findRecipeWithIngredients(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with id: " + recipeId));
        return recipeScalingEngine.scaleRecipe(recipe, targetServings);
    }

    public void archiveRecipeFromCookbook(Long recipeId) {
        Objects.requireNonNull(recipeId, "recipeId must not be null");
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with id: " + recipeId));
        recipe.archive();
        recipeRepository.save(recipe);
    }

    private BigDecimal computeCanonicalGrams(BigDecimal amount, MeasurementUnit unit, Density density) {
        MeasuredQuantity quantity = MeasuredQuantity.of(amount, unit);
        if (unit.getMagnitudeType() == MeasurementUnit.MagnitudeType.MASS) {
            return quantity.toGrams().amount();
        } else if (unit.getMagnitudeType() == MeasurementUnit.MagnitudeType.VOLUME) {
            return quantity.toGrams(density).amount();
        } else {
            return amount;
        }
    }

    private RecipeResponseDTO mapToResponseDTO(Recipe recipe) {
        List<RecipeIngredientResponseDTO> ingredientDTOs = recipe.getIngredients().stream()
                .map(ingredientLine -> new RecipeIngredientResponseDTO(
                        ingredientLine.getId(),
                        ingredientLine.getNutritionalComponent().getDisplayName(),
                        ingredientLine.getDisplayAmount(),
                        ingredientLine.getDisplayUnit(),
                        ingredientLine.getCanonicalGrams(),
                        ingredientLine.getTheoreticalIngredient() != null ? ingredientLine.getTheoreticalIngredient().getId() : null,
                        ingredientLine.getSubRecipe() != null ? ingredientLine.getSubRecipe().getId() : null
                ))
                .toList();

        return new RecipeResponseDTO(
                recipe.getId(),
                recipe.getTitle(),
                recipe.getInstructions(),
                recipe.getReferenceServings(),
                recipe.getPrepTimeMinutes(),
                recipe.getCookTimeMinutes(),
                recipe.getNominalNutrition(),
                recipe.isArchived(),
                ingredientDTOs
        );
    }
}
