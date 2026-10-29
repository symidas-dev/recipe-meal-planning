CREATE TABLE theoretical_ingredients (
    id BIGSERIAL PRIMARY KEY,
    canonical_name VARCHAR(150) NOT NULL,
    taxonomy_tag VARCHAR(150) NOT NULL,
    standard_density NUMERIC(5,2) NOT NULL CHECK (standard_density > 0),
    nominal_calories_per_100g NUMERIC(6,2) NOT NULL CHECK (nominal_calories_per_100g >= 0),
    nominal_protein_per_100g NUMERIC(5,2) NOT NULL CHECK (nominal_protein_per_100g >= 0),
    nominal_carbs_per_100g NUMERIC(5,2) NOT NULL CHECK (nominal_carbs_per_100g >= 0),
    nominal_fat_per_100g NUMERIC(5,2) NOT NULL CHECK (nominal_fat_per_100g >= 0),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT DEFAULT 0,
    CONSTRAINT uk_theoretical_ingredient_canonical_name UNIQUE (canonical_name)
);

CREATE TABLE raw_ingredients (
    id BIGSERIAL PRIMARY KEY,
    theoretical_ingredient_id BIGINT NOT NULL,
    brand_name VARCHAR(120) NOT NULL,
    barcode_ean13 VARCHAR(13),
    purchase_date DATE NOT NULL,
    initial_grams NUMERIC(8,2) NOT NULL CHECK (initial_grams > 0),
    remaining_grams NUMERIC(8,2) NOT NULL CHECK (remaining_grams >= 0 AND remaining_grams <= initial_grams),
    calories_per_100g NUMERIC(6,2) NOT NULL CHECK (calories_per_100g >= 0),
    protein_per_100g NUMERIC(5,2) NOT NULL CHECK (protein_per_100g >= 0),
    carbs_per_100g NUMERIC(5,2) NOT NULL CHECK (carbs_per_100g >= 0),
    fat_per_100g NUMERIC(5,2) NOT NULL CHECK (fat_per_100g >= 0),
    density NUMERIC(5,2) CHECK (density IS NULL OR density > 0),
    average_unit_weight_grams NUMERIC(8,2) CHECK (average_unit_weight_grams IS NULL OR average_unit_weight_grams > 0),
    status VARCHAR(20) NOT NULL CHECK (status IN ('QUEUED', 'ACTIVE', 'SPENT')),
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_raw_ingredients_theoretical FOREIGN KEY (theoretical_ingredient_id)
        REFERENCES theoretical_ingredients(id)
);

CREATE INDEX idx_raw_ingredients_theoretical_status
    ON raw_ingredients(theoretical_ingredient_id, status);

CREATE TABLE recipes (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    instructions TEXT NOT NULL,
    reference_servings INT NOT NULL CHECK (reference_servings > 0),
    prep_time_minutes INT NOT NULL CHECK (prep_time_minutes >= 0),
    cook_time_minutes INT NOT NULL CHECK (cook_time_minutes >= 0),
    nominal_calories NUMERIC(8,2) NOT NULL CHECK (nominal_calories >= 0),
    nominal_protein NUMERIC(8,2) NOT NULL CHECK (nominal_protein >= 0),
    nominal_carbs NUMERIC(8,2) NOT NULL CHECK (nominal_carbs >= 0),
    nominal_fat NUMERIC(8,2) NOT NULL CHECK (nominal_fat >= 0),
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_recipes_nominal_calories
    ON recipes(nominal_calories);

CREATE TABLE recipe_ingredients (
    id BIGSERIAL PRIMARY KEY,
    recipe_id BIGINT NOT NULL,
    theoretical_ingredient_id BIGINT,
    sub_recipe_id BIGINT,
    display_amount NUMERIC(8,2) NOT NULL CHECK (display_amount > 0),
    display_unit VARCHAR(20) NOT NULL CHECK (display_unit IN ('GRAM', 'KILOGRAM', 'MILLILITER', 'LITER', 'UNIT', 'TABLESPOON', 'TEASPOON', 'PINCH')),
    canonical_grams NUMERIC(8,2) NOT NULL CHECK (canonical_grams > 0),
    CONSTRAINT fk_recipe_ingredients_recipe FOREIGN KEY (recipe_id)
        REFERENCES recipes(id) ON DELETE CASCADE,
    CONSTRAINT fk_recipe_ingredients_theoretical FOREIGN KEY (theoretical_ingredient_id)
        REFERENCES theoretical_ingredients(id),
    CONSTRAINT fk_recipe_ingredients_sub_recipe FOREIGN KEY (sub_recipe_id)
        REFERENCES recipes(id),
    CONSTRAINT chk_recipe_ingredient_ref CHECK (
        (theoretical_ingredient_id IS NOT NULL AND sub_recipe_id IS NULL) OR
        (theoretical_ingredient_id IS NULL AND sub_recipe_id IS NOT NULL)
    )
);

CREATE TABLE weekly_menus (
    id BIGSERIAL PRIMARY KEY,
    week_start_date DATE NOT NULL,
    total_week_calories_nominal NUMERIC(8,2) NOT NULL DEFAULT 0.00 CHECK (total_week_calories_nominal >= 0),
    total_week_protein_nominal NUMERIC(8,2) NOT NULL DEFAULT 0.00 CHECK (total_week_protein_nominal >= 0),
    total_week_carbs_nominal NUMERIC(8,2) NOT NULL DEFAULT 0.00 CHECK (total_week_carbs_nominal >= 0),
    total_week_fat_nominal NUMERIC(8,2) NOT NULL DEFAULT 0.00 CHECK (total_week_fat_nominal >= 0),
    total_week_calories_actual NUMERIC(8,2) NOT NULL DEFAULT 0.00 CHECK (total_week_calories_actual >= 0),
    total_week_protein_actual NUMERIC(8,2) NOT NULL DEFAULT 0.00 CHECK (total_week_protein_actual >= 0),
    total_week_carbs_actual NUMERIC(8,2) NOT NULL DEFAULT 0.00 CHECK (total_week_carbs_actual >= 0),
    total_week_fat_actual NUMERIC(8,2) NOT NULL DEFAULT 0.00 CHECK (total_week_fat_actual >= 0),
    is_closed BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT DEFAULT 0,
    CONSTRAINT uk_weekly_menu_start_date UNIQUE (week_start_date)
);

CREATE TABLE meal_slots (
    id BIGSERIAL PRIMARY KEY,
    weekly_menu_id BIGINT NOT NULL,
    recipe_id BIGINT NOT NULL,
    day_of_week VARCHAR(15) NOT NULL CHECK (day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')),
    meal_time VARCHAR(20) NOT NULL CHECK (meal_time IN ('BREAKFAST', 'MID_MORNING_SNACK', 'LUNCH', 'AFTERNOON_SNACK', 'DINNER')),
    servings INT NOT NULL CHECK (servings > 0),
    actual_calories NUMERIC(8,2) CHECK (actual_calories IS NULL OR actual_calories >= 0),
    actual_protein NUMERIC(8,2) CHECK (actual_protein IS NULL OR actual_protein >= 0),
    actual_carbs NUMERIC(8,2) CHECK (actual_carbs IS NULL OR actual_carbs >= 0),
    actual_fat NUMERIC(8,2) CHECK (actual_fat IS NULL OR actual_fat >= 0),
    CONSTRAINT fk_meal_slots_weekly_menu FOREIGN KEY (weekly_menu_id)
        REFERENCES weekly_menus(id) ON DELETE CASCADE,
    CONSTRAINT fk_meal_slots_recipe FOREIGN KEY (recipe_id)
        REFERENCES recipes(id),
    CONSTRAINT uk_meal_slot_calendar UNIQUE (weekly_menu_id, day_of_week, meal_time)
);
