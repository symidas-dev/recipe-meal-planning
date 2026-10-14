# ADR 002: Write-Time Normalization, Culinary Metrology, and Discrete Unit Scaling

- **Status:** Accepted
- **Date:** 2026-10-13
- **Author:** Javier Liaño Rioz
- **Project Context:** P20 - Recipe and Meal Planning (Algebra Bernays University)

---

## 1. Context and Problem Statement

The culinary planning domain requires that users author recipes using familiar, diverse measurement units (grams, kilograms, milliliters, tablespoons, whole units), while satisfying two non-functional requirements:

1. **Instant nutritional calculations ($O(1)$) and database B-Tree indexability.**
2. **Clean, consolidated weekly grocery list aggregation.**

### The Core Design Challenge
Converting units on-the-fly during every HTTP read introduces severe computational overhead ($O(N)$ tree traversals), dimensional exceptions at read-time, and risks of JPA $N+1$ queries.

Furthermore, European consumer packaging (Regulation EU 1169/2011) presents practical realities:
- Liquid retail items do not display their physical density ($\rho$) on commercial labels.
- Discrete grocery items (eggs, apples) permit fractional culinary consumption ($0.5$ eggs in a scaled recipe) but cannot be purchased as fractions at the grocery store.

---

## 2. Considered Alternatives

### Option A: On-the-Fly Dynamic Unit Conversion
- **Approach:** Persist only raw user input (`displayAmount`, `displayUnit`) and convert dimensions dynamically during every read query.
- **Drawbacks:** Drastically degrades read performance, prevents B-Tree indexing on nutritional attributes in SQL, and shifts dimensional validation errors to read operations.

### Option B: Mandatory Physical Density for All Liquid Ingredients
- **Approach:** Require an explicit density value ($\rho$) in the database for every liquid or volume-based ingredient, rejecting input otherwise.
- **Drawbacks:** High friction with real-world culinary facts: neither home cooks nor supermarket labels state the physical density of milk, broths, vinegar, or juices.

### Option C: Deterministic Write-Time Normalization with Canonical Mass and Scaling Duality (Selected)
- **Approach:** Normalize at insertion time (`write-time normalization`), calculating and persisting `canonicalGrams` (`BigDecimal`). Maintain continuous precision during recipe preparation while enforcing discrete ceiling aggregation during grocery list compilation.

---

## 3. Decision Outcome

We adopt **Option C** under the following metrology rules:

### 3.1. Dual Persistence in `RecipeIngredient`
- **Display Fields:** `displayAmount` (`BigDecimal`) and `displayUnit` (`MeasurementUnit`) preserve user intent verbatim.
- **Canonical Mass:** `canonicalGrams` (`BigDecimal`, nullable) precalculated once upon insertion.

### 3.2. Deterministic Write-Time Resolution Rules

1. **Mass Dimensions (`GRAM`, `KILOGRAM`):**
   $$\text{canonicalGrams} = \text{amount} \times \text{unit.baseConversionFactor}$$

2. **Volume Dimensions (`MILLILITER`, `LITER`, `TABLESPOON`, `TEASPOON`):**
   $$\text{canonicalGrams} = (\text{amount} \times \text{unit.baseConversionFactor}) \times \rho_{effective}$$
   - If `rawIngredient.density` is defined, $\rho_{effective} = \text{density}$.
   - If undefined, standard aqueous culinary approximation is applied: $\rho_{effective} = 1.00\text{ g/ml}$.
   - Calibrated densities are seeded for lipids and oils (e.g., olive oil at $0.92\text{ g/ml}$).

3. **Discrete Dimensions (`UNIT`):**
   - If `rawIngredient.averageUnitWeightGrams` is present:
     $$\text{canonicalGrams} = \text{amount} \times \text{averageUnitWeightGrams}$$
   - If uncalibrated, `canonicalGrams` remains `null`.

### 3.3. Continuous vs. Discrete Scaling Duality
- **Recipe Serving Scaler (Kitchen Domain):** Operates on continuous real numbers. If scaling a recipe requires $0.5$ eggs or $1.25$ apples, the scaled recipe displays $0.5$ units.
- **Weekly Grocery List Generator (Procurement Domain):** Enforces a ceiling integer step:
  $$\text{groceryUnits} = \left\lceil \sum \text{scaledContinuousUnits} \right\rceil$$

---

## 4. Consequences and Trade-offs

- **Positive:** Guaranteed $O(1)$ constant-time reads for recipe nutrition facts and weekly macro balances.
- **Positive:** Clean database indexing via B-Tree on precomputed nutritional attributes.
- **Positive:** Zero read-time conversion crashes and clean grocery list consolidation.
- **Trade-off:** Aqueous liquid approximation ($\rho = 1.00$) may produce negligible mass deviations for non-aqueous liquids if density is omitted. Mitigated by seeding essential culinary oils and syrups with explicit density values.
