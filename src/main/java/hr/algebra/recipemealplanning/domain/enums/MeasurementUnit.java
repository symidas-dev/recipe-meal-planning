package hr.algebra.recipemealplanning.domain.enums;

import java.math.BigDecimal;

public enum MeasurementUnit {

    GRAM(MagnitudeType.MASS, new BigDecimal("1.0")),
    KILOGRAM(MagnitudeType.MASS, new BigDecimal("1000.0")),

    MILLILITER(MagnitudeType.VOLUME, new BigDecimal("1.0")),
    LITER(MagnitudeType.VOLUME, new BigDecimal("1000.0")),
    TEASPOON(MagnitudeType.VOLUME, new BigDecimal("5.0")),
    TABLESPOON(MagnitudeType.VOLUME, new BigDecimal("15.0")),

    UNIT(MagnitudeType.COUNT, BigDecimal.ONE);

    public enum MagnitudeType {
        MASS,
        VOLUME,
        COUNT
    }

    private final MagnitudeType magnitudeType;
    private final BigDecimal baseConversionFactor;

    MeasurementUnit(MagnitudeType magnitudeType, BigDecimal baseConversionFactor) {
        this.magnitudeType = magnitudeType;
        this.baseConversionFactor = baseConversionFactor;
    }

    public MagnitudeType getMagnitudeType() {
        return magnitudeType;
    }

    public BigDecimal getBaseConversionFactor() {
        return baseConversionFactor;
    }

    public BigDecimal toBaseUnit(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        return amount.multiply(this.baseConversionFactor);
    }
}
