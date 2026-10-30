package hr.algebra.recipemealplanning.repository;

import hr.algebra.recipemealplanning.domain.entity.RawIngredient;
import hr.algebra.recipemealplanning.domain.entity.TheoreticalIngredient;
import hr.algebra.recipemealplanning.domain.model.Barcode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RawIngredientRepository extends JpaRepository<RawIngredient, Long> {

    @Query("""
            SELECT pantryBatch FROM RawIngredient pantryBatch
            WHERE pantryBatch.theoreticalIngredient = :targetIngredient
              AND pantryBatch.status = 'QUEUED'
            ORDER BY pantryBatch.purchaseDate ASC
            """)
    List<RawIngredient> findFifoQueue(@Param("targetIngredient") TheoreticalIngredient targetIngredient);

    @Query("""
            SELECT pantryBatch FROM RawIngredient pantryBatch
            WHERE pantryBatch.theoreticalIngredient = :targetIngredient
              AND pantryBatch.status = 'ACTIVE'
            """)
    Optional<RawIngredient> findActiveBatch(@Param("targetIngredient") TheoreticalIngredient targetIngredient);

    @Query("""
            SELECT pantryBatch FROM RawIngredient pantryBatch
            WHERE pantryBatch.barcodeEan13 = :#{#scannedBarcode.value()}
            """)
    List<RawIngredient> findByBarcode(@Param("scannedBarcode") Barcode scannedBarcode);
}
