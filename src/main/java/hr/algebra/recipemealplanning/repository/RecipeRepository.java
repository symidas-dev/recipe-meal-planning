package hr.algebra.recipemealplanning.repository;

import hr.algebra.recipemealplanning.domain.entity.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    @Query("""
            SELECT culinaryRecipe FROM Recipe culinaryRecipe
            LEFT JOIN FETCH culinaryRecipe.ingredients ingredientAssociation
            LEFT JOIN FETCH ingredientAssociation.theoreticalIngredient canonicalIngredient
            LEFT JOIN FETCH ingredientAssociation.subRecipe childSubRecipe
            WHERE culinaryRecipe.id = :recipeId
            """)
    Optional<Recipe> findRecipeWithIngredients(@Param("recipeId") Long recipeId);

    Page<Recipe> findByIsArchivedFalse(Pageable pageable);

    @Query("""
            SELECT culinaryRecipe FROM Recipe culinaryRecipe
            WHERE culinaryRecipe.isArchived = false
              AND culinaryRecipe.nominalNutrition.calories <= :maximumNominalCalories
            """)
    Page<Recipe> findAvailableUnderCalorieLimit(
            @Param("maximumNominalCalories") BigDecimal maximumNominalCalories,
            Pageable pageable
    );
}
