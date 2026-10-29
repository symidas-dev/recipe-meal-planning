package hr.algebra.recipemealplanning.domain.entity;

import hr.algebra.recipemealplanning.domain.enums.DayOfWeek;
import hr.algebra.recipemealplanning.domain.enums.MealTime;
import hr.algebra.recipemealplanning.domain.model.NutritionalInformation;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

import java.util.Objects;

@Entity
@Table(
        name = "meal_slots",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_meal_slot_calendar",
                        columnNames = {"weekly_menu_id", "day_of_week", "meal_time"}
                )
        }
)
@Getter
public class MealSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "weekly_menu_id", nullable = false)
    private WeeklyMenu weeklyMenu;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 15)
    private DayOfWeek dayOfWeek;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_time", nullable = false, length = 20)
    private MealTime mealTime;

    @Column(name = "servings", nullable = false)
    private int servings;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "calories", column = @Column(name = "actual_calories", precision = 8, scale = 2)),
            @AttributeOverride(name = "protein", column = @Column(name = "actual_protein", precision = 8, scale = 2)),
            @AttributeOverride(name = "carbohydrates", column = @Column(name = "actual_carbs", precision = 8, scale = 2)),
            @AttributeOverride(name = "fat", column = @Column(name = "actual_fat", precision = 8, scale = 2))
    })
    private NutritionalInformation actualNutrition;

    protected MealSlot() {
    }

    public MealSlot(
            Recipe recipe,
            DayOfWeek dayOfWeek,
            MealTime mealTime,
            int servings) {
        this.recipe = Objects.requireNonNull(recipe, "recipe must not be null");
        this.dayOfWeek = Objects.requireNonNull(dayOfWeek, "dayOfWeek must not be null");
        this.mealTime = Objects.requireNonNull(mealTime, "mealTime must not be null");
        if (servings <= 0) {
            throw new IllegalArgumentException("servings must be strictly positive");
        }
        this.servings = servings;
        this.actualNutrition = NutritionalInformation.ZERO;
    }

    public void adjustServings(int newServings) {
        if (newServings <= 0) {
            throw new IllegalArgumentException("servings must be strictly positive");
        }
        this.servings = newServings;
    }

    public void reassignRecipe(Recipe newRecipe) {
        this.recipe = Objects.requireNonNull(newRecipe, "recipe must not be null");
    }

    public void updateActualNutrition(NutritionalInformation nutrition) {
        this.actualNutrition = Objects.requireNonNull(nutrition, "nutrition must not be null");
    }

    void setWeeklyMenu(WeeklyMenu weeklyMenu) {
        this.weeklyMenu = weeklyMenu;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MealSlot that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
