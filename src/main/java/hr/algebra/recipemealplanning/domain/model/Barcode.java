package hr.algebra.recipemealplanning.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

public record Barcode(String value) {

    private static final int EAN13_LENGTH = 13;
    private static final int DATA_DIGITS_COUNT = 12;
    private static final int CHECK_DIGIT_INDEX = 12;
    private static final Pattern EAN13_PATTERN = Pattern.compile("^\\d{" + EAN13_LENGTH + "}$");
    private static final int RADIX_DECIMAL = 10;
    private static final int GS1_MODULO = 10;
    private static final int WEIGHT_ONE = 1;
    private static final int WEIGHT_THREE = 3;

    public Barcode {
        Objects.requireNonNull(value, "Barcode value must not be null");
        String trimmed = value.trim();
        if (!EAN13_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(
                    "Barcode '%s' must consist of exactly 13 numeric digits".formatted(value));
        }
        if (!hasValidCheckDigit(trimmed)) {
            throw new IllegalArgumentException(
                    "Barcode '%s' has an invalid GS1 check digit".formatted(value));
        }
        value = trimmed;
    }

    public static Barcode of(String rawCode) {
        return new Barcode(rawCode);
    }

    private static boolean hasValidCheckDigit(String code) {
        int calculated = calculateCheckDigit(code);
        int actual = extractCheckDigit(code);
        return calculated == actual;
    }

    private static int calculateWeightedSum(String code) {
        int totalSum = 0;
        for (int index = 0; index < DATA_DIGITS_COUNT; index++) {
            int digit = digitAt(code, index);
            int weight = isOddPosition(index) ? WEIGHT_ONE : WEIGHT_THREE;
            totalSum += digit * weight;
        }
        return totalSum;
    }

    private static boolean isOddPosition(int zeroBasedIndex) {
        int humanPosition = zeroBasedIndex + 1;
        return humanPosition % 2 != 0;
    }

    private static int calculateCheckDigit(String code) {
        int weightedSum = calculateWeightedSum(code);
        return (GS1_MODULO - (weightedSum % GS1_MODULO)) % GS1_MODULO;
    }

    private static int extractCheckDigit(String code) {
        return digitAt(code, CHECK_DIGIT_INDEX);
    }

    private static int digitAt(String code, int index) {
        return Character.digit(code.charAt(index), RADIX_DECIMAL);
    }
}
