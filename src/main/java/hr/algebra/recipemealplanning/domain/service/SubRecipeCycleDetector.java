package hr.algebra.recipemealplanning.domain.service;

import hr.algebra.recipemealplanning.domain.entity.Recipe;
import hr.algebra.recipemealplanning.domain.exception.CircularDependencyException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Service
public class SubRecipeCycleDetector {

    public void validateAcyclicity(Recipe candidateRecipe, Recipe subRecipeToAdd) {
        Objects.requireNonNull(candidateRecipe, "candidateRecipe must not be null");
        Objects.requireNonNull(subRecipeToAdd, "subRecipeToAdd must not be null");

        if (Objects.equals(candidateRecipe.getId(), subRecipeToAdd.getId()) && candidateRecipe.getId() != null) {
            throw new CircularDependencyException(
                    "Self-referencing cycle detected: recipe '%s' (id=%d) cannot contain itself as a sub-recipe"
                            .formatted(candidateRecipe.getTitle(), candidateRecipe.getId())
            );
        }

        Set<Long> visitedRecipeIds = new HashSet<>();
        if (candidateRecipe.getId() != null) {
            visitedRecipeIds.add(candidateRecipe.getId());
        }

        depthFirstSearch(subRecipeToAdd, candidateRecipe.getId(), visitedRecipeIds);
    }

    private void depthFirstSearch(Recipe currentRecipe, Long targetRecipeId, Set<Long> visitedRecipeIds) {
        if (currentRecipe == null || currentRecipe.getId() == null) {
            return;
        }

        if (Objects.equals(currentRecipe.getId(), targetRecipeId)) {
            throw new CircularDependencyException(
                    "Circular dependency detected in recipe hierarchy: recipe id %d is referenced recursively in sub-recipe branch '%s'"
                            .formatted(targetRecipeId, currentRecipe.getTitle())
            );
        }

        if (!visitedRecipeIds.add(currentRecipe.getId())) {
            return;
        }

        for (Recipe nestedRecipe : currentRecipe.getNestedRecipes()) {
            depthFirstSearch(nestedRecipe, targetRecipeId, visitedRecipeIds);
        }

        visitedRecipeIds.remove(currentRecipe.getId());
    }
}
