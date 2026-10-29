package hr.algebra.recipemealplanning.domain.entity;

import hr.algebra.recipemealplanning.domain.enums.InventoryStatus;
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
import jakarta.persistence.Version;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "raw_ingredients")
@Getter
public class RawIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "theoretical_ingredient_id", nullable = false)
    private TheoreticalIngredient theoreticalIngredient;

    @Column(name = "brand_name", nullable = false, length = 120)
    private String brandName;

    @Column(name = "barcode_ean13", length = 13)
    private String barcodeEan13;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(name = "initial_grams", nullable = false, precision = 8, scale = 2)
    private BigDecimal initialGrams;

    @Column(name = "remaining_grams", nullable = false, precision = 8, scale = 2)
    private BigDecimal remainingGrams;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "calories", column = @Column(name = "calories_per_100g", nullable = false, precision = 6, scale = 2)),
            @AttributeOverride(name = "protein", column = @Column(name = "protein_per_100g", nullable = false, precision = 5, scale = 2)),
            @AttributeOverride(name = "carbohydrates", column = @Column(name = "carbs_per_100g", nullable = false, precision = 5, scale = 2)),
            @AttributeOverride(name = "fat", column = @Column(name = "fat_per_100g", nullable = false, precision = 5, scale = 2))
    })
    private NutritionalInformation nutritionPer100g;

    @Column(name = "density", precision = 5, scale = 2)
    private BigDecimal density;

    @Column(name = "average_unit_weight_grams", precision = 8, scale = 2)
    private BigDecimal averageUnitWeightGrams;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InventoryStatus status;

    @Version
    private Long version;

    protected RawIngredient() {
    }

    public RawIngredient(
            TheoreticalIngredient theoreticalIngredient,
            String brandName,
            String barcodeEan13,
            LocalDate purchaseDate,
            BigDecimal initialGrams,
            NutritionalInformation nutritionPer100g,
            InventoryStatus status) {
        this.theoreticalIngredient = Objects.requireNonNull(theoreticalIngredient, "theoreticalIngredient must not be null");
        this.brandName = Objects.requireNonNull(brandName, "brandName must not be null").trim();
        this.barcodeEan13 = barcodeEan13 != null ? barcodeEan13.trim() : null;
        this.purchaseDate = Objects.requireNonNull(purchaseDate, "purchaseDate must not be null");
        this.initialGrams = Objects.requireNonNull(initialGrams, "initialGrams must not be null");
        this.remainingGrams = initialGrams;
        this.nutritionPer100g = Objects.requireNonNull(nutritionPer100g, "nutritionPer100g must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    public void consumeGrams(BigDecimal consumed) {
        Objects.requireNonNull(consumed, "consumed amount must not be null");
        if (consumed.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("consumed amount cannot be negative");
        }
        BigDecimal updated = this.remainingGrams.subtract(consumed);
        if (updated.compareTo(BigDecimal.ZERO) <= 0) {
            this.remainingGrams = BigDecimal.ZERO;
            this.status = InventoryStatus.SPENT;
        } else {
            this.remainingGrams = updated;
        }
    }

    public void promoteToActive() {
        if (this.status == InventoryStatus.SPENT) {
            throw new IllegalStateException("cannot promote an already exhausted batch");
        }
        this.status = InventoryStatus.ACTIVE;
    }

    public void demoteToQueued() {
        if (this.status == InventoryStatus.SPENT) {
            throw new IllegalStateException("cannot queue an already exhausted batch");
        }
        this.status = InventoryStatus.QUEUED;
    }

    public void markAsExhausted() {
        this.remainingGrams = BigDecimal.ZERO;
        this.status = InventoryStatus.SPENT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RawIngredient that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
