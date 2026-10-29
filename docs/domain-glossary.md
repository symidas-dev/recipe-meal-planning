# Canonical Domain Glossary (Ubiquitous Language)

> **Project:** P20 — Recipe and Meal Planning  
> **Course:** Rapid Development of Java Applications Using Programming Frameworks  
> **Institution:** Algebra Bernays University — Academic Year 2026/27  
> **Student:** Javier Liaño Rioz  

This document establishes the **Ubiquitous Language** of the project, using real-world culinary metaphors. All Java class names, attributes, database schemas, service methods, REST endpoints, and architectural documentation adhere strictly to these definitions.

---

## 1. Culinary & Recipe Entities (The Kitchen Domain)

### `RawIngredient`
* **Definition:** An indivisible, primary grocery item available in retail markets. Represents the terminal leaf in the recipe ingredient tree.
* **Real-World Analogy:** A bottle of olive oil, a carton of eggs, or a bag of flour.
* **System Responsibility:** Provides standard reference nutritional facts per 100g (calories, protein, carbohydrates, fat), physical density ($\rho$), and average piece weight in grams. Only `RawIngredient` items appear on the final consolidated shopping list.

### `Recipe`
* **Definition:** A complete culinary specification for preparing a single dish or intermediate component.
* **Real-World Analogy:** A recipe sheet or cookbook entry.
* **System Responsibility:** Fundamental unit of culinary knowledge. Encapsulates title, active preparation time, passive cooking time, step-by-step instructions, reference servings ($P_{ref}$), and its list of `RecipeIngredient` items. Can be served as a final dish or reused recursively as a sub-recipe.

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

## 2. Weekly Meal Planning Entities (The Fridge Whiteboard Domain)

### `WeeklyMenu`
* **Definition:** The 7-day calendar whiteboard where a household plans food consumption from Monday to Sunday.
* **Real-World Analogy:** The magnetic weekly planner stuck to the kitchen refrigerator.
* **System Responsibility:** Root aggregate for meal planning. Tracks the start date (`weekStartDate`) and contains `MealSlot` items. Coordinates weekly nutritional accumulation and consolidated grocery lists.

### `MealSlot`
* **Definition:** An individual meal slot within a specific day and time on the weekly board.
* **Real-World Analogy:** The whiteboard box for *"Wednesday Lunch"* where you write what you are eating and for how many people.
* **System Responsibility:** Couples a `WeeklyMenu` with a specific `Recipe`, specifying the `DayOfWeek` (MONDAY..SUNDAY), the `MealTime` (BREAKFAST..DINNER), and the real target headcount (`servings`).

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
