package hr.algebra.recipemealplanning.domain.entity;

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
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
        name = "weekly_menus",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_weekly_menu_start_date",
                        columnNames = {"week_start_date"}
                )
        }
)
@Getter
public class WeeklyMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "calories", column = @Column(name = "total_week_calories_nominal", nullable = false, precision = 8, scale = 2)),
            @AttributeOverride(name = "protein", column = @Column(name = "total_week_protein_nominal", nullable = false, precision = 8, scale = 2)),
            @AttributeOverride(name = "carbohydrates", column = @Column(name = "total_week_carbs_nominal", nullable = false, precision = 8, scale = 2)),
            @AttributeOverride(name = "fat", column = @Column(name = "total_week_fat_nominal", nullable = false, precision = 8, scale = 2))
    })
    private NutritionalInformation totalWeekNutritionNominal;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "calories", column = @Column(name = "total_week_calories_actual", nullable = false, precision = 8, scale = 2)),
            @AttributeOverride(name = "protein", column = @Column(name = "total_week_protein_actual", nullable = false, precision = 8, scale = 2)),
            @AttributeOverride(name = "carbohydrates", column = @Column(name = "total_week_carbs_actual", nullable = false, precision = 8, scale = 2)),
            @AttributeOverride(name = "fat", column = @Column(name = "total_week_fat_actual", nullable = false, precision = 8, scale = 2))
    })
    private NutritionalInformation totalWeekNutritionActual;

    @Column(name = "is_closed", nullable = false)
    private boolean isClosed = false;

    @OneToMany(mappedBy = "weeklyMenu", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MealSlot> slots = new ArrayList<>();

    @Version
    private Long version;

    protected WeeklyMenu() {
    }

    public WeeklyMenu(LocalDate weekStartDate) {
        this.weekStartDate = Objects.requireNonNull(weekStartDate, "weekStartDate must not be null");
        this.totalWeekNutritionNominal = NutritionalInformation.ZERO;
        this.totalWeekNutritionActual = NutritionalInformation.ZERO;
    }

    public void addSlot(MealSlot slot) {
        Objects.requireNonNull(slot, "slot must not be null");
        slots.add(slot);
        slot.setWeeklyMenu(this);
    }

    public void removeSlot(MealSlot slot) {
        Objects.requireNonNull(slot, "slot must not be null");
        slots.remove(slot);
        slot.setWeeklyMenu(null);
    }

    public void updateWeeklyTotals(NutritionalInformation nominal, NutritionalInformation actual) {
        this.totalWeekNutritionNominal = Objects.requireNonNull(nominal, "nominal nutrition must not be null");
        this.totalWeekNutritionActual = Objects.requireNonNull(actual, "actual nutrition must not be null");
    }

    public void close() {
        this.isClosed = true;
    }

    public List<MealSlot> getSlots() {
        return Collections.unmodifiableList(slots);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WeeklyMenu that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
