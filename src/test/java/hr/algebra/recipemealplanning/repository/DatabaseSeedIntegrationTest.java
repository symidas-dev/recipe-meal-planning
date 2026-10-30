package hr.algebra.recipemealplanning.repository;

import hr.algebra.recipemealplanning.TestcontainersConfiguration;
import hr.algebra.recipemealplanning.domain.entity.RawIngredient;
import hr.algebra.recipemealplanning.domain.entity.Recipe;
import hr.algebra.recipemealplanning.domain.entity.TheoreticalIngredient;
import hr.algebra.recipemealplanning.domain.enums.MeasurementUnit;
import hr.algebra.recipemealplanning.domain.model.Barcode;
import hr.algebra.recipemealplanning.domain.service.RecipeScalingEngine;
import hr.algebra.recipemealplanning.dto.ScaledRecipeDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@DisplayName("Verificación de Semilla Inicial de Datos (V2__seed_data.sql)")
class DatabaseSeedIntegrationTest {

    @Autowired
    private TheoreticalIngredientRepository theoreticalIngredientRepository;

    @Autowired
    private RawIngredientRepository rawIngredientRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private RecipeScalingEngine recipeScalingEngine;

    @Nested
    @DisplayName("Catálogo Canónico de Ingredientes Teóricos (Frontera LO1)")
    class TheoreticalCatalogBaselineTests {

        @Test
        @DisplayName("Debe existir el catálogo semilla mínimo con al menos 14 ingredientes canónicos y sus macros oficiales")
        void shouldContainBaselineCanonicalIngredients() {
            List<TheoreticalIngredient> ingredients = theoreticalIngredientRepository.findAll();

            assertThat(ingredients)
                    .as("El catálogo semilla debe contar con una despensa inicial para demostración")
                    .hasSizeGreaterThanOrEqualTo(14);

            Optional<TheoreticalIngredient> oliveOil = theoreticalIngredientRepository.findByCanonicalName("Aceite de oliva virgen extra");
            assertThat(oliveOil).isPresent();
            assertThat(oliveOil.get().getNominalNutritionPer100g().calories())
                    .isEqualByComparingTo(new BigDecimal("884.00"));
            assertThat(oliveOil.get().getNominalNutritionPer100g().fat())
                    .isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(oliveOil.get().getStandardDensity())
                    .isEqualByComparingTo(new BigDecimal("0.92"));

            Optional<TheoreticalIngredient> egg = theoreticalIngredientRepository.findByCanonicalName("Huevo de gallina");
            assertThat(egg).isPresent();
            assertThat(egg.get().getNominalNutritionPer100g().protein())
                    .isEqualByComparingTo(new BigDecimal("12.60"));

            Optional<TheoreticalIngredient> potato = theoreticalIngredientRepository.findByCanonicalName("Patata");
            assertThat(potato).isPresent();
            assertThat(potato.get().getTaxonomyTag()).isEqualTo("Tubérculos");
        }
    }

    @Nested
    @DisplayName("Lotes de Despensa Físicos y FIFO (Caso de Uso 2.6)")
    class PantryFifoSeedBaselineTests {

        @Test
        @DisplayName("Debe precargar lotes de despensa con códigos EAN-13 válidos y orden FIFO por fecha")
        void shouldContainPantryBatchesWithValidEan13AndFifoOrder() {
            TheoreticalIngredient basmati = theoreticalIngredientRepository.findByCanonicalName("Arroz basmati").orElseThrow();

            List<RawIngredient> queuedBatches = rawIngredientRepository.findFifoQueue(basmati);
            assertThat(queuedBatches)
                    .as("Debe haber al menos 1 lote de arroz basmati en cola QUEUED")
                    .hasSize(1);
            assertThat(queuedBatches.get(0).getBrandName()).isEqualTo("Arroz Basmati SOS");

            Optional<RawIngredient> activeBatch = rawIngredientRepository.findActiveBatch(basmati);
            assertThat(activeBatch).isPresent();
            assertThat(activeBatch.get().getBrandName()).isEqualTo("Arroz Basmati Sundari");

            // Búsqueda por código de barras GS1 EAN-13
            Barcode sundariBarcode = Barcode.of("8445678901235");
            List<RawIngredient> foundByBarcode = rawIngredientRepository.findByBarcode(sundariBarcode);
            assertThat(foundByBarcode).hasSize(1);
            assertThat(foundByBarcode.get(0).getBrandName()).isEqualTo("Arroz Basmati Sundari");
        }
    }

    @Nested
    @DisplayName("Recetario Maestro y Escalado Dinámico (Casos de Uso 2.2 y 2.3)")
    class CulinaryCookbookBaselineTests {

        @Test
        @DisplayName("Debe existir al menos 2 recetas de muestra con sus líneas de ingredientes asociadas")
        void shouldContainBaselineSampleRecipes() {
            var activeCookbook = recipeRepository.findByIsArchivedFalse(PageRequest.of(0, 10));

            assertThat(activeCookbook.getContent())
                    .as("Debe haber recetas precargadas en el recetario")
                    .hasSizeGreaterThanOrEqualTo(2);

            Optional<Recipe> spanishOmelette = activeCookbook.getContent().stream()
                    .filter(recipe -> "Tortilla de patatas tradicional".equals(recipe.getTitle()))
                    .findFirst();

            assertThat(spanishOmelette).isPresent();
            assertThat(spanishOmelette.get().getReferenceServings()).isEqualTo(4);
            assertThat(spanishOmelette.get().getNominalNutrition().calories())
                    .isEqualByComparingTo(new BigDecimal("1216.20"));

            Recipe recipeWithLines = recipeRepository.findRecipeWithIngredients(spanishOmelette.get().getId()).orElseThrow();
            assertThat(recipeWithLines.getIngredients())
                    .as("La tortilla de patatas debe contener exactamente 4 ingredientes (patata, huevo, cebolla, aceite)")
                    .hasSize(4);

            // Verificamos que se puede escalar dinámicamente con el motor de escalado
            ScaledRecipeDTO scaledFor8 = recipeScalingEngine.scaleRecipe(recipeWithLines, 8);
            assertThat(scaledFor8.targetServings()).isEqualTo(8);
            assertThat(scaledFor8.scaledNutrition().calories())
                    .isEqualByComparingTo(new BigDecimal("2432.40")); // Doble de 1216.20

            // Comprobamos la segunda receta (Pollo con arroz y verduras)
            Optional<Recipe> chickenRice = activeCookbook.getContent().stream()
                    .filter(recipe -> "Pechuga de pollo con arroz basmati y verduras".equals(recipe.getTitle()))
                    .findFirst();

            assertThat(chickenRice).isPresent();
            assertThat(chickenRice.get().getReferenceServings()).isEqualTo(2);
            assertThat(chickenRice.get().getNominalNutrition().protein())
                    .isEqualByComparingTo(new BigDecimal("104.55"));
        }
    }
}
