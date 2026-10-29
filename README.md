# Recipe and Meal Planning (P20)

[![CI Build](https://github.com/JavierLianoRioz/recipe-meal-planning/actions/workflows/ci.yml/badge.svg)](https://github.com/JavierLianoRioz/recipe-meal-planning/actions)

> **Course:** Rapid Development of Java Applications Using Programming Frameworks  
> **Institution:** Algebra Bernays University — Academic Year 2026/27  
> **Student:** Javier Liaño Rioz  
> **Project:** P20 — Recipe and Meal Planning  

---

## Table of Contents

1. [General Description](#1-general-description)
2. [Public Deployment and External Access](#2-public-deployment-and-external-access)
3. [Demo Credentials by Role](#3-demo-credentials-by-role)
4. [Technology Stack and Architectural Decisions](#4-technology-stack-and-architectural-decisions)
5. [Local Setup and Execution Instructions](#5-local-setup-and-execution-instructions)
   - [Prerequisites](#51-prerequisites)
   - [Running with Docker Compose](#52-running-with-docker-compose)
   - [Running with Maven Wrapper](#53-running-with-maven-wrapper)
6. [Figma Prototype to Thymeleaf View Mapping](#6-figma-prototype-to-thymeleaf-view-mapping)
7. [Artificial Intelligence Integration (LLM)](#7-artificial-intelligence-integration-llm)
8. [Current Limitations and Out-of-Scope Problems](#8-current-limitations-and-out-of-scope-problems)
   - [8.1 Out-of-Scope Problems and Comprehensive Enhancements (GitHub Issues)](#81-out-of-scope-problems-and-comprehensive-enhancements-github-issues)
   - [8.2 Assumed Domain Approximations (v1.0 Core)](#82-assumed-domain-approximations-v10-core)

---

## 1. General Description

The **Recipe and Meal Planning** platform is a full-stack, enterprise-grade Java web application designed to solve culinary formulation, recursive recipe composition, household meal planning, and automated grocery procurement.

### Core Aggregate Structure
```text
Recipe -> RecipeIngredient (amount, unit, canonicalGrams) -> RawIngredient | Recipe (sub-recipe)
WeeklyPlan -> CalendarEntry (dayOfWeek, mealTime) -> Menu -> MenuCourse (servings) -> Recipe
```

### Advanced Specialized Capabilities
1. **Dimension-Aware Recipe Scaling:** Dynamic scaling of ingredient quantities across arbitrary headcount targets with physical volume-to-mass conversions ($\rho$).
2. **Nutritional Aggregation:** Real-time consolidated nutritional balance (calories, macronutrients) computed across complex multi-course meals and weekly schedules.
3. **Consolidated Grocery List Generation:** Automated deduplication and unit normalization of ingredients across distinct recipes, applying discrete packaging ceilings.
4. **AI-Powered Ingredient Normalizer:** Natural language extraction and mapping of unformatted grocery lines into canonical database ingredients.

---

## 2. Public Deployment and External Access

- **Public Production URL:** `https://<app-service>.onrender.com`
- **OpenAPI Documentation (Swagger UI):** `https://<app-service>.onrender.com/swagger-ui.html`
- **Spring Actuator Health Endpoint:** `https://<app-service>.onrender.com/actuator/health`

> **Note on Cold Starts:** On free-tier cloud containers (e.g., Render), instances spin down after 15 minutes of inactivity. Initial requests may experience a 50–60 second cold-start delay while the runtime and connection pool initialize.

---

## 3. Demo Credentials by Role

The database is seeded on startup via Flyway migrations with demonstration accounts covering all security tiers:

| Role | Username | Password | Operational Capabilities |
| :--- | :--- | :--- | :--- |
| `ROLE_ADMIN` | `admin` | `Admin123!` | Global ingredient catalog administration, database auditing, and Quartz scheduled jobs management. |
| `ROLE_PLANNER` | `planner` | `Planner123!` | Recipe authoring, sub-recipe nesting, weekly meal planning, and AI normalization tools. |
| `ROLE_VIEWER` | `viewer` | `Viewer123!` | Read-only browsing of public recipes, nutrition facts, and generated grocery lists. |

---

## 4. Technology Stack and Architectural Decisions

- **Runtime & Core Framework:** Java 21 LTS / Spring Boot 4.1
- **Security:** Spring Security 7 (BCrypt password hashing, CSRF protection, method-level `@PreAuthorize`)
- **Persistence & ORM:** Spring Data JPA over Hibernate 7, PostgreSQL 16+, Flyway database migrations
- **Presentation Layer:** Thymeleaf with Layout Dialect and Spring Security Dialect
- **REST API:** Open standards compliant, Springdoc OpenAPI 3.x, RFC 7807 `ProblemDetail` error payload
- **Quality Assurance:** JUnit 5/6, Mockito, Testcontainers for PostgreSQL, JaCoCo code coverage ($\ge 80\%$)
- **Scheduled Tasks:** Quartz JDBC JobStore with distributed locks

### Architectural Decision Records (ADRs)
- [`docs/adr/001-persistence-association-and-subrecipes.md`](docs/adr/001-persistence-association-and-subrecipes.md): Modeling association entities with attributes and recursive sub-recipe trees.
- [`docs/adr/002-write-time-normalization-and-discrete-units.md`](docs/adr/002-write-time-normalization-and-discrete-units.md): Write-time normalization to canonical grams and discrete unit scaling.

---

## 5. Local Setup and Execution Instructions

### 5.1 Prerequisites
- Java Development Kit (JDK) 21 or higher.
- Docker & Docker Compose (for containerized PostgreSQL and testcontainers integration).

### 5.2 Running with Docker Compose
To launch the database service locally:
```bash
docker compose up -d
```

### 5.3 Running with Maven Wrapper
To build, run the verification test suite, and launch the development server:
```bash
# Execute unit and integration tests with coverage reporting
./mvnw clean verify

# Launch application with local development profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## 6. Figma Prototype to Thymeleaf View Mapping

- **Figma Project Workspace:** [Interactive Figma Prototype](https://www.figma.com/)
- **Offline Export:** `docs/prototype.pdf`

| Figma Frame (Desktop 1440 / Mobile 390) | Thymeleaf Template (`src/main/resources/templates/`) | Design Alignment Status |
| :--- | :--- | :--- |
| `01-recipe-list` | `recipes/list.html` | Exact alignment with pagination and search. |
| `02-recipe-empty` | `recipes/list.html` (`th:if="${#lists.isEmpty(recipes)}"`) | Exact alignment with empty-state call to action. |
| `03-recipe-form` | `recipes/form.html` | Dynamic ingredient row management. |
| `04-recipe-form-errors` | `recipes/form.html` (`th:errors`) | Inline validation banners and field highlighting. |
| `05-recipe-detail` | `recipes/detail.html` | Interactive serving scaler and precomputed nutrition panel. |
| `06-ai-ingredient-parser` | `recipes/ai-normalize.html` | Interactive prompt terminal and database mapping preview. |

---

## 7. Artificial Intelligence Integration (LLM)

The application incorporates an AI assistant module for normalizing unstructured culinary text lines (e.g., *"2 tablespoons of cold-pressed olive oil"*) into validated records mapped to pre-existing catalog ingredients.

- **Architecture:** Hexagonal architecture isolating the domain behind `IngredientNormalizer`.
- **Testing & Safety:** Automated tests run against `StubIngredientNormalizer` with zero third-party latency or external billing costs.
- **Documentation:**
  - `docs/ai-usage.md`: Complete engineering log of AI tool assistance during development.
  - `docs/ai-evaluation.md`: 10 empirical test benchmarks evaluating parsing accuracy across Croatian and English inputs.

---

## 8. Current Limitations and Out-of-Scope Problems

To prevent scope creep and maintain strict alignment with the course evaluation rubric (Learning Outcomes LO1–LO7), several comprehensive real-world problems were deliberately analyzed, decoupled from the v1.0 core, and cataloged into **GitHub Issues** as future enhancements.

### 8.1 Out-of-Scope Problems and Comprehensive Enhancements (GitHub Issues)

1. **Domestic Pantry Tracking & Net Grocery Demand (Issue #1):**
   - *Problem:* A standard shopping list assumes zero items exist at home. If 400g of tomato sauce or 6 eggs are already in the pantry, the user still buys the full weekly amount.
   - *Enhancement:* A `PantryItem` aggregate that subtracts on-hand household inventory:
     $$\text{netShoppingDemand} = \max\Big(0, \; \text{weeklyGrossDemand} - \text{pantryStock}\Big)$$
   - *Status:* Logged as secondary backlog enhancement (`Issue 01 - Domestic Pantry Inventory`).

2. **Retail Commercial Packaging Optimization & Surplus (Issue #2):**
   - *Problem:* Supermarkets sell goods in discrete commercial bundles (e.g., cartons of 6 or 12 eggs, 1 kg flour bags). Needing 7 eggs requires buying 2 cartons of 6 (12 eggs), leaving a surplus.
   - *Enhancement:* A commercial packaging optimizer using `standardPackageSize`:
     $$\text{packagesToBuy} = \left\lceil \frac{\text{weeklyNetDemand}}{\text{standardPackageSize}} \right\rceil$$
     crediting the residual surplus into future weeks.
   - *Status:* Logged as secondary backlog enhancement (`Issue 02 - Retail Package Optimization`).

3. **Time vs. Cost Trade-offs: Homemade vs. Store-Bought (Issue #3):**
   - *Problem:* Users often deliberate whether making tomato sauce from scratch (fresh tomatoes, onion, oil — 45 min) is worth the time savings compared to opening a commercial canned jar (2 min).
   - *Enhancement:* Estimated cost calculator based on raw ingredient retail prices, displaying a comparison card (*Homemade vs Store-bought*).
   - *Status:* Logged as secondary backlog enhancement (`Issue 03 - Cost Estimation and Homemade Comparison`).

4. **Recursive Allergen and Dietary Restriction Traversal (Issue #4):**
   - *Problem:* Tracking inherited trace allergens (gluten, lactose, nuts) across deeply nested composite sub-recipes.
   - *Enhancement:* Recursive propagation engine marking parent recipes based on leaf ingredients.
   - *Status:* Logged as secondary backlog enhancement (`Issue 04 - Allergen Inheritance Engine`).

### 8.2 Assumed Domain Approximations (v1.0 Core)

1. **Aqueous Approximation for Standard Culinary Liquids:** Ordinary culinary liquids without an explicit density record in the database default to $\rho = 1.00\text{ g/ml}$. Calibrated densities are maintained specifically for lipids and culinary oils ($\rho = 0.92\text{ g/ml}$).
2. **Sub-Recipe Cycle Protection:** Directed Acyclic Graph (DAG) cycle validation via Depth-First Search (DFS) runs during recipe persistence, rejecting recursive self-references with HTTP 422.
