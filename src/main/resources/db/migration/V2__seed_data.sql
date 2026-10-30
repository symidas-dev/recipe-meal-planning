-- V2__seed_data.sql: Catálogo Canónico Inicial de Ingredientes, Lotes de Despensa y Recetas Base

-- 1. Inserción de Ingredientes Teóricos Canónicos (14 alimentos base de despensa)
INSERT INTO theoretical_ingredients (
    id, canonical_name, taxonomy_tag, standard_density,
    nominal_calories_per_100g, nominal_protein_per_100g, nominal_carbs_per_100g, nominal_fat_per_100g,
    is_active, version
) VALUES
(1, 'Aceite de oliva virgen extra', 'Grasas y aceites', 0.92, 884.00, 0.00, 0.00, 100.00, TRUE, 0),
(2, 'Huevo de gallina', 'Huevos y derivados', 1.00, 143.00, 12.60, 0.80, 9.50, TRUE, 0),
(3, 'Patata', 'Tubérculos', 1.00, 77.00, 2.00, 17.00, 0.10, TRUE, 0),
(4, 'Cebolla', 'Hortalizas y verduras', 1.00, 40.00, 1.10, 9.30, 0.10, TRUE, 0),
(5, 'Pechuga de pollo', 'Carnes y aves', 1.00, 165.00, 31.00, 0.00, 3.60, TRUE, 0),
(6, 'Arroz basmati', 'Cereales y granos', 0.85, 365.00, 7.10, 80.00, 0.90, TRUE, 0),
(7, 'Zanahoria', 'Hortalizas y verduras', 1.00, 41.00, 0.90, 9.60, 0.20, TRUE, 0),
(8, 'Leche entera de vaca', 'Lácteos', 1.03, 62.00, 3.20, 4.80, 3.30, TRUE, 0),
(9, 'Harina de trigo de fuerza', 'Harinas y derivados', 0.55, 364.00, 10.30, 76.00, 1.00, TRUE, 0),
(10, 'Ajo', 'Hortalizas y verduras', 1.00, 149.00, 6.40, 33.10, 0.50, TRUE, 0),
(11, 'Tomate pera', 'Hortalizas y verduras', 1.00, 18.00, 0.90, 3.90, 0.20, TRUE, 0),
(12, 'Lenteja pardina seca', 'Legumbres', 0.85, 353.00, 24.60, 63.40, 1.10, TRUE, 0),
(13, 'Sal marina fina', 'Condimentos y especias', 1.20, 0.00, 0.00, 0.00, 0.00, TRUE, 0),
(14, 'Avena en copos integrales', 'Cereales y granos', 0.40, 389.00, 16.90, 66.30, 6.90, TRUE, 0);

SELECT setval('theoretical_ingredients_id_seq', (SELECT MAX(id) FROM theoretical_ingredients));

-- 2. Inserción de Lotes Físicos de Despensa (Raw Ingredients para FIFO y escáner de códigos de barras)
INSERT INTO raw_ingredients (
    id, theoretical_ingredient_id, brand_name, barcode_ean13, purchase_date,
    initial_grams, remaining_grams, calories_per_100g, protein_per_100g, carbs_per_100g, fat_per_100g,
    density, average_unit_weight_grams, status, version
) VALUES
(1, 1, 'Hacendado Gran Selección', '8480000123456', '2026-10-01', 920.00, 750.00, 884.00, 0.00, 0.00, 100.00, 0.92, NULL, 'ACTIVE', 0),
(2, 2, 'Huevos Camperos Matines', '8412345678905', '2026-10-25', 600.00, 600.00, 143.00, 12.60, 0.80, 9.50, 1.00, 60.00, 'ACTIVE', 0),
(3, 3, 'Patata Monalisa Selección', '8423456789012', '2026-10-20', 3000.00, 2400.00, 77.00, 2.00, 17.00, 0.10, 1.00, NULL, 'ACTIVE', 0),
(4, 5, 'Pechuga Pollo Limpia Coren', '8434567890123', '2026-10-28', 1000.00, 1000.00, 165.00, 31.00, 0.00, 3.60, 1.00, NULL, 'ACTIVE', 0),
(5, 6, 'Arroz Basmati Sundari', '8445678901235', '2026-09-15', 1000.00, 850.00, 365.00, 7.10, 80.00, 0.90, 0.85, NULL, 'ACTIVE', 0),
(6, 6, 'Arroz Basmati SOS', '8456789012345', '2026-10-20', 1000.00, 1000.00, 365.00, 7.10, 80.00, 0.90, 0.85, NULL, 'QUEUED', 0);

SELECT setval('raw_ingredients_id_seq', (SELECT MAX(id) FROM raw_ingredients));

-- 3. Inserción de Recetas Maestras (Culinary Technical Sheets)
INSERT INTO recipes (
    id, title, instructions, reference_servings, prep_time_minutes, cook_time_minutes,
    nominal_calories, nominal_protein, nominal_carbs, nominal_fat, is_archived, version
) VALUES
(1, 'Tortilla de patatas tradicional', 'Pelar y cortar las patatas y la cebolla en láminas finas. Pochar a fuego medio en aceite de oliva hasta que estén tiernas pero no doradas. Batir los huevos en un bol grande con un toque de sal. Escurrir las patatas y la cebolla y mezclar con el huevo batido durante 5 minutos para que se integren. Cuajar en sartén antiadherente a fuego medio-alto durante 2 minutos por lado dejando el centro jugoso.', 4, 20, 25, 1216.20, 51.45, 118.35, 59.25, FALSE, 0),
(2, 'Pechuga de pollo con arroz basmati y verduras', 'Enjuagar el arroz basmati bajo agua fría y cocerlo en agua con sal durante 12 minutos. Cortar la pechuga de pollo en dados uniformes y saltear a fuego vivo en sartén con una cucharada de aceite de oliva virgen extra hasta que adquiera un tono dorado. Añadir la zanahoria rallada o en bastones finos en los últimos 3 minutos de salteado. Servir sobre la cama de arroz basmati caliente.', 2, 15, 20, 1216.10, 104.55, 129.60, 27.35, FALSE, 0);

SELECT setval('recipes_id_seq', (SELECT MAX(id) FROM recipes));

-- 4. Inserción de Líneas de Ingredientes de Receta (Recipe Ingredients)
INSERT INTO recipe_ingredients (
    id, recipe_id, theoretical_ingredient_id, sub_recipe_id,
    display_amount, display_unit, canonical_grams
) VALUES
-- Líneas para Tortilla de patatas tradicional (Receta 1)
(1, 1, 3, NULL, 600.00, 'GRAM', 600.00),         -- Patata (600g)
(2, 1, 2, NULL, 5.00, 'UNIT', 300.00),           -- 5 Huevos (~300g)
(3, 1, 4, NULL, 1.00, 'UNIT', 150.00),           -- 1 Cebolla (~150g)
(4, 1, 1, NULL, 2.00, 'TABLESPOON', 30.00),      -- 2 Cucharadas AOVE (~30g)

-- Líneas para Pechuga de pollo con arroz basmati y verduras (Receta 2)
(5, 2, 5, NULL, 300.00, 'GRAM', 300.00),         -- Pechuga de pollo (300g)
(6, 2, 6, NULL, 150.00, 'GRAM', 150.00),         -- Arroz basmati (150g)
(7, 2, 7, NULL, 1.00, 'UNIT', 100.00),           -- 1 Zanahoria (~100g)
(8, 2, 1, NULL, 1.00, 'TABLESPOON', 15.00);      -- 1 Cucharada AOVE (~15g)

SELECT setval('recipe_ingredients_id_seq', (SELECT MAX(id) FROM recipe_ingredients));
