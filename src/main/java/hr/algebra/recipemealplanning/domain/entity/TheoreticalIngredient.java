package hr.algebra.recipemealplanning.domain.entity;

import hr.algebra.recipemealplanning.domain.model.NutritionalInformation;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "theoretical_ingredients")
@Getter
public class TheoreticalIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "canonical_name", nullable = false, unique = true, length = 150)
    private String canonicalName;

    @Column(name = "taxonomy_tag", nullable = false, length = 150)
    private String taxonomyTag;

    @Column(name = "standard_density", nullable = false, precision = 5, scale = 2)
    private BigDecimal standardDensity;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "calories", column = @Column(name = "nominal_calories_per_100g", nullable = false, precision = 6, scale = 2)),
            @AttributeOverride(name = "protein", column = @Column(name = "nominal_protein_per_100g", nullable = false, precision = 5, scale = 2)),
            @AttributeOverride(name = "carbohydrates", column = @Column(name = "nominal_carbs_per_100g", nullable = false, precision = 5, scale = 2)),
            @AttributeOverride(name = "fat", column = @Column(name = "nominal_fat_per_100g", nullable = false, precision = 5, scale = 2))
    })
    private NutritionalInformation nominalNutritionPer100g;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Version
    private Long version;

    protected TheoreticalIngredient() {
    }

    public TheoreticalIngredient(
            String canonicalName,
            String taxonomyTag,
            BigDecimal standardDensity,
            NutritionalInformation nominalNutritionPer100g) {
        this.canonicalName = Objects.requireNonNull(canonicalName, "canonicalName must not be null").trim();
        this.taxonomyTag = Objects.requireNonNull(taxonomyTag, "taxonomyTag must not be null").trim();
        this.standardDensity = Objects.requireNonNull(standardDensity, "standardDensity must not be null");
        this.nominalNutritionPer100g = Objects.requireNonNull(nominalNutritionPer100g, "nominalNutritionPer100g must not be null");
    }

    public void updatePhysicalProperties(BigDecimal newStandardDensity, NutritionalInformation newNominalNutrition) {
        this.standardDensity = Objects.requireNonNull(newStandardDensity, "standardDensity must not be null");
        this.nominalNutritionPer100g = Objects.requireNonNull(newNominalNutrition, "nominalNutritionPer100g must not be null");
    }

    public void deactivate() {
        this.isActive = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TheoreticalIngredient that)) return false;
        return Objects.equals(canonicalName, that.canonicalName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(canonicalName);
    }
}
