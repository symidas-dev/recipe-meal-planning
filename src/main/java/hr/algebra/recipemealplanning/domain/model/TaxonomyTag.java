package hr.algebra.recipemealplanning.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

public record TaxonomyTag(String value) {

    private static final Pattern TAG_PATTERN = Pattern.compile("^[a-z]{2}:[a-z0-9-]+$");

    public TaxonomyTag {
        Objects.requireNonNull(value, "Taxonomy tag value must not be null");
        String trimmed = value.trim().toLowerCase();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("Taxonomy tag must not be blank");
        }
        if (!TAG_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(
                    "Taxonomy tag '%s' does not match expected format '<lang>:<category-slug>' (e.g. 'en:tomato-sauces')"
                            .formatted(value));
        }
        value = trimmed;
    }

    public static TaxonomyTag of(String tag) {
        return new TaxonomyTag(tag);
    }
}
