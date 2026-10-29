package hr.algebra.recipemealplanning.domain.service;

import hr.algebra.recipemealplanning.domain.entity.RawIngredient;
import hr.algebra.recipemealplanning.domain.enums.InventoryStatus;

import java.util.List;
import java.util.Objects;

public class PantryInventoryService {

    public void switchActiveBatch(RawIngredient currentlyActive, RawIngredient batchToActivate) {
        Objects.requireNonNull(currentlyActive, "currentlyActive batch must not be null");
        Objects.requireNonNull(batchToActivate, "batchToActivate must not be null");

        if (!Objects.equals(currentlyActive.getTheoreticalIngredient(), batchToActivate.getTheoreticalIngredient())) {
            throw new IllegalArgumentException(
                    "Cannot switch batches of different theoretical ingredients: '%s' vs '%s'"
                            .formatted(
                                    currentlyActive.getTheoreticalIngredient().getCanonicalName(),
                                    batchToActivate.getTheoreticalIngredient().getCanonicalName()
                            )
            );
        }

        if (currentlyActive.getStatus() == InventoryStatus.SPENT) {
            throw new IllegalStateException("Currently active batch is already exhausted");
        }

        currentlyActive.demoteToQueued();
        batchToActivate.promoteToActive();
    }

    public void enforceSingleActiveBatch(RawIngredient batchToActivate, List<RawIngredient> allBatchesForIngredient) {
        Objects.requireNonNull(batchToActivate, "batchToActivate must not be null");
        Objects.requireNonNull(allBatchesForIngredient, "allBatchesForIngredient must not be null");

        for (RawIngredient batch : allBatchesForIngredient) {
            if (batch != batchToActivate && batch.getStatus() == InventoryStatus.ACTIVE) {
                batch.demoteToQueued();
            }
        }
        batchToActivate.promoteToActive();
    }
}
