package hr.algebra.recipemealplanning.domain.entity;

import hr.algebra.recipemealplanning.domain.model.NutritionalComponent;
import hr.algebra.recipemealplanning.domain.model.NutritionalInformation;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "recipes")
@Getter
public class Recipe implements NutritionalComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "instructions", nullable = false, columnDefinition = "TEXT")
    private String instructions;

    @Column(name = "reference_servings", nullable = false)
    private int referenceServings;

    @Column(name = "prep_time_minutes", nullable = false)
    private int prepTimeMinutes;

    @Column(name = "cook_time_minutes", nullable = false)
    private int cookTimeMinutes;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "calories", column = @Column(name = "nominal_calories", nullable = false, precision = 8, scale = 2)),
            @AttributeOverride(name = "protein", column = @Column(name = "nominal_protein", nullable = false, precision = 8, scale = 2)),
            @AttributeOverride(name = "carbohydrates", column = @Column(name = "nominal_carbs", nullable = false, precision = 8, scale = 2)),
            @AttributeOverride(name = "fat", column = @Column(name = "nominal_fat", nullable = false, precision = 8, scale = 2))
    })
    private NutritionalInformation nominalNutrition;

    @Column(name = "is_archived", nullable = false)
    private boolean isArchived = false;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    @Version
    private Long version;

    protected Recipe() {
    }

    public Recipe(
            String title,
            String instructions,
            int referenceServings,
            int prepTimeMinutes,
            int cookTimeMinutes) {
        this.title = Objects.requireNonNull(title, "title must not be null").trim();
        this.instructions = Objects.requireNonNull(instructions, "instructions must not be null").trim();
        if (referenceServings <= 0) {
            throw new IllegalArgumentException("referenceServings must be strictly positive");
        }
        if (prepTimeMinutes < 0 || cookTimeMinutes < 0) {
            throw new IllegalArgumentException("prep and cook times cannot be negative");
        }
        this.referenceServings = referenceServings;
        this.prepTimeMinutes = prepTimeMinutes;
        this.cookTimeMinutes = cookTimeMinutes;
        this.nominalNutrition = NutritionalInformation.ZERO;
    }

    public void updateCulinaryDetails(String title, String instructions, int prepTimeMinutes, int cookTimeMinutes) {
        this.title = Objects.requireNonNull(title, "title must not be null").trim();
        this.instructions = Objects.requireNonNull(instructions, "instructions must not be null").trim();
        if (prepTimeMinutes < 0 || cookTimeMinutes < 0) {
            throw new IllegalArgumentException("prep and cook times cannot be negative");
        }
        this.prepTimeMinutes = prepTimeMinutes;
        this.cookTimeMinutes = cookTimeMinutes;
    }

    public void rebaseServings(int newReferenceServings) {
        if (newReferenceServings <= 0) {
            throw new IllegalArgumentException("newReferenceServings must be strictly positive");
        }
        this.referenceServings = newReferenceServings;
    }

    public void addIngredient(RecipeIngredient ingredient) {
        Objects.requireNonNull(ingredient, "ingredient must not be null");
        ingredients.add(ingredient);
        ingredient.bindToRecipe(this);
    }

    public void removeIngredient(RecipeIngredient ingredient) {
        Objects.requireNonNull(ingredient, "ingredient must not be null");
        ingredients.remove(ingredient);
        ingredient.unbindRecipe();
    }

    public void updateNominalNutrition(NutritionalInformation nutrition) {
        this.nominalNutrition = Objects.requireNonNull(nutrition, "nutrition must not be null");
    }

    public void archive() {
        this.isArchived = true;
    }

    public List<RecipeIngredient> getIngredients() {
        return Collections.unmodifiableList(ingredients);
    }
    public List<Recipe> getNestedRecipes() {
        List<Recipe> nested = new ArrayList<>();
        for (RecipeIngredient line : this.ingredients) {
            if (line.getSubRecipe() != null) {
                nested.add(line.getSubRecipe());
            }
        }
        return Collections.unmodifiableList(nested);
    }

    @Override
    public String getDisplayName() {
        return this.title;
    }

    @Override
    public NutritionalInformation computeNutritionForGrams(java.math.BigDecimal grams) {
        Objects.requireNonNull(grams, "grams must not be null");
        java.math.BigDecimal factor = grams.divide(new java.math.BigDecimal("100.00"), 4, java.math.RoundingMode.HALF_UP);
        return new NutritionalInformation(
                this.nominalNutrition.calories().multiply(factor).setScale(2, java.math.RoundingMode.HALF_UP),
                this.nominalNutrition.protein().multiply(factor).setScale(2, java.math.RoundingMode.HALF_UP),
                this.nominalNutrition.carbohydrates().multiply(factor).setScale(2, java.math.RoundingMode.HALF_UP),
                this.nominalNutrition.fat().multiply(factor).setScale(2, java.math.RoundingMode.HALF_UP)
        );
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Recipe that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
