# Canonical Domain Glossary (Ubiquitous Language)

> **Project:** P20 — Recipe and Meal Planning  
> **Course:** Rapid Development of Java Applications Using Programming Frameworks  
> **Institution:** Algebra Bernays University — Academic Year 2026/27  
> **Student:** Javier Liaño Rioz  

This document establishes the **Ubiquitous Language** of the project. All Java class names, attributes, database schemas, service methods, REST endpoints, and architectural documentation adhere strictly to these definitions.

---

## 1. Culinary & Recipe Entities

### `RawIngredient`
* **Definition:** An indivisible, primary grocery item available in retail markets. Represents the terminal leaf in the recipe ingredient tree.
* **Real-World Analogy:** A bottle of olive oil, a carton of eggs, or a bag of flour.
* **System Responsibility:** Provides standard reference nutritional facts per 100g (calories, protein, carbohydrates, fat), physical density ($\rho$), and average piece weight in grams. Only `RawIngredient` items appear on the final consolidated shopping list.

### `Recipe`
* **Definition:** A complete culinary specification for preparing a single dish or intermediate component.
* **Real-World Analogy:** A recipe sheet or cookbook entry.
* **System Responsibility:** Fundamental unit of culinary knowledge. Encapsulates title, active preparation time, passive cooking time, step-by-step instructions, reference servings, and its list of `RecipeIngredient` items. Can be served as a final dish or reused recursively as a sub-recipe.

### `SubRecipe`
* **Definition:** An intermediate culinary preparation that, while being a standalone recipe, is incorporated as an ingredient into a higher-level recipe.
* **Real-World Analogy:** Homemade béchamel sauce, chicken broth, or shortcrust pastry.
* **System Responsibility:** Realizes the *Composite Pattern*. The shopping list and nutrition engines traverse sub-recipes recursively until resolving terminal raw ingredients.

### `RecipeIngredient`
* **Definition:** A quantified line item within a recipe specification. Couples a numeric scalar (`amount`) with a physical dimension (`unit`).
* **Real-World Analogy:** An entry such as *"2 tablespoons of olive oil"* or *"250 grams of béchamel"*.
* **System Responsibility:** Solves the mandatory association entity requirement (*The hard part*). Mutually exclusively references either a `RawIngredient` or a `Recipe` (sub-recipe). Stores both user input (`displayAmount`, `displayUnit`) and write-time normalized mass (`canonicalGrams`).

### `ReferenceServings` ($P_{ref}$)
* **Definition:** The baseline number of portions for which raw recipe quantities were originally formulated.
* **System Responsibility:** Constant denominator for the serving scaling factor. Ensures baseline recipe definitions remain immutable regardless of dynamic headcounts.

---

## 2. Menu & Temporal Planning Entities

### `Menu`
* **Definition:** A coordinated gastronomic offering grouping one or more dishes served together in a single dining session.
* **Real-World Analogy:** A multi-course meal: *"Mixed salad (starter), baked salmon (main), and fruit salad (dessert)"*.
* **System Responsibility:** Autonomous aggregate decoupled from the calendar. Can be authored, persisted in a personal library, and scheduled across multiple weeks without data duplication.

### `MenuCourse`
* **Definition:** An individual course or component within a `Menu`.
* **Real-World Analogy:** Each individual plate served at the table.
* **System Responsibility:** Couples a `Menu` with a specific `Recipe` and defines course-specific `servings`, accommodating guests who skip courses or share portions.

### `WeeklyPlan`
* **Definition:** A 7-day calendar window wherein an individual or household schedules meal consumption.
* **Real-World Analogy:** A weekly meal calendar (Monday through Sunday).
* **System Responsibility:** Root aggregate for meal planning. Contains `CalendarEntry` slots and generates aggregated nutritional reports and consolidated shopping lists.

### `CalendarEntry`
* **Definition:** A specific schedule slot binding a `WeeklyPlan` with a `Menu` at a specific day and meal time.
* **Real-World Analogy:** *"Wednesday at lunch, serve Menu #3"*.
* **System Responsibility:** Solves temporal allocation. Couples `DayOfWeek` (MONDAY..SUNDAY) and `MealTime` (BREAKFAST, MID_MORNING_SNACK, LUNCH, AFTERNOON_SNACK, DINNER) with a `Menu`.

---

## 3. Scaling & Dimensional Metrology Concepts

### `ScalingFactor` ($k$)
* **Definition:** Dimensionless multiplier representing the ratio between desired target servings ($P_{target}$) and baseline reference servings ($P_{ref}$):
  $$k = \frac{P_{target}}{P_{ref}}$$
* **System Responsibility:** Safely scales continuous ingredient quantities using `BigDecimal` arithmetic with `HALF_UP` rounding.

### `CanonicalGrams`
* **Definition:** Mass in grams computed and stored upon ingredient insertion (`write-time normalization`) to guarantee $O(1)$ read performance and B-Tree indexing.

### `Density` ($\rho$)
* **Definition:** Physical ratio of mass to volume, expressed in grams per milliliter ($\text{g/ml}$).
* **System Responsibility:** Bridges the dimensional gap between volumetric measurements (cups, tablespoons, milliliters) and mass metrics:
  $$\text{mass} = \text{volume} \times \rho$$
  Water-based liquids default to $\rho = 1.00\text{ g/ml}$, while lipids and oils carry calibrated values (e.g., olive oil at $0.92\text{ g/ml}$).
