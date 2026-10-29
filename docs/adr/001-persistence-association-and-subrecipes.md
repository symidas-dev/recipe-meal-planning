# ADR 001: Persistence Modeling for Ingredients, Composite Sub-Recipes, Menus, and Temporal Planning

- **Status:** Accepted
- **Date:** 2026-10-13
- **Author:** Javier Liaño Rioz
- **Project Context:** P20 - Recipe and Meal Planning (Algebra Bernays University)

---

## 1. Context and Problem Statement

The system requires managing culinary recipes, market raw ingredients, multi-course menu compositions, weekly meal scheduling, and aggregated grocery list generation.

From a relational persistence perspective using Spring Data JPA / Hibernate 7, four key architectural challenges arise:

1. **Attributes in the Recipe-Ingredient association:** A recipe does not merely reference an ingredient; it defines a specific numeric quantity and a physical measurement unit.
2. **Recursive composition (Sub-Recipes / Composite Pattern):** In authentic gastronomy, a recipe can incorporate either primary market items or intermediate preparations (sub-recipes) that are complete recipes in their own right.
3. **Decoupling between Menus and Calendar Schedules:** A complete multi-course menu (starter, main, dessert) must exist as an autonomous, reusable entity in a user's library, independent of whether it is scheduled in a specific week.
4. **Precomputed nutritional facts for $O(1)$ reads:** Repeatedly traversing recursive ingredient trees on every HTTP read introduces severe query latency and database load.

---

## 2. Considered Alternatives

### Option A: Direct `@ManyToMany` Relationship Without Intermediate Entities
- **Approach:** Link `Recipe` directly to `RawIngredient`, and `WeeklyPlan` directly to `Recipe`.
- **Drawbacks:** Unviable. Prevents attaching attributes (`amount`, `unit`, `servings` per dish) without violating relational normalization. Violates the explicit course requirement (*"The hard part: Modelling the association entity with attributes properly instead of a bare many-to-many"*).

### Option B: Merging Menus Directly into the Weekly Calendar
- **Approach:** Associate recipes directly with calendar slots (`CalendarEntry` -> `Recipe`), bypassing the `Menu` aggregate.
- **Drawbacks:** Prevents modeling multi-course meals served together in a single sitting, blocks per-course serving differentiation, and prevents users from saving curated menus into their personal library.

### Option C: Decoupled Architecture with Association Entities and Write-Time Precomputation (Selected)
- **Approach:**
  - `RecipeIngredient` as an intermediate association entity with attributes (`amount`, `unit`, `canonicalGrams`) and a mutually exclusive reference to either `RawIngredient` or `Recipe` (sub-recipe).
  - `Menu` and `MenuCourse` as an autonomous aggregate grouping dishes with course-specific servings.
  - `CalendarEntry` as a temporal connector between `WeeklyPlan` and `Menu`.
  - Persistent precomputed nutritional summary columns on `Recipe` and `Menu` for $O(1)$ reads.

---

## 3. Decision Outcome

We adopt **Option C**. The entity relationship model is structured as follows:

```text
Recipe [1] <---> [0..*] RecipeIngredient [0..1] ---> [1] TheoreticalIngredient
                                         [0..1] ---> [1] Recipe (Sub-Recipe)

RawIngredient [0..*] ---> [1] TheoreticalIngredient (Canonical Catalog)

WeeklyMenu [1] <---> [0..*] MealSlot [0..*] ---> [1] Recipe
```

### 3.1. Invariant Rules
1. **Mutual Exclusivity:** Every `RecipeIngredient` instance must reference either a `RawIngredient` or a `Recipe` (sub-recipe), enforced by database check constraints and domain validation.
2. **Acyclic Sub-Recipes:** A sub-recipe tree cannot contain circular dependencies. Cycle detection via Depth-First Search (DFS) is enforced in the service layer prior to persistence.
3. **Serving Differentiation:** `MenuCourse.servings` defaults to the menu baseline but allows per-course overrides.

---

## 4. Consequences and Trade-offs

- **Positive:** Full compliance with course requirements and architectural evaluation criteria. Clean separation of concerns between gastronomic design and calendar scheduling.
- **Positive:** Constant-time read performance ($O(1)$) on recipe browsing and nutrition queries via persistent precomputed columns.
- **Negative:** Slightly higher insertion complexity due to cycle detection and precomputed summary updates.
- **Risk Mitigation:** Mitigate JPA $N+1$ query issues by utilizing explicit Entity Graphs and Fetch Joins on multi-tier aggregates.
