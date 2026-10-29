package hr.algebra.recipemealplanning.service;

import hr.algebra.recipemealplanning.domain.entity.RawIngredient;
import hr.algebra.recipemealplanning.domain.service.PantryInventoryService;
import hr.algebra.recipemealplanning.repository.RawIngredientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class PantryService {

    private final RawIngredientRepository rawIngredientRepository;
    private final PantryInventoryService pantryInventoryDomainService;

    public PantryService(
            RawIngredientRepository rawIngredientRepository,
            PantryInventoryService pantryInventoryDomainService) {
        this.rawIngredientRepository = Objects.requireNonNull(rawIngredientRepository, "rawIngredientRepository must not be null");
        this.pantryInventoryDomainService = Objects.requireNonNull(pantryInventoryDomainService, "pantryInventoryDomainService must not be null");
    }

    public void switchActiveBatch(Long currentlyActiveBatchId, Long targetQueuedBatchId) {
        RawIngredient currentlyActiveBatch = rawIngredientRepository.findById(currentlyActiveBatchId)
                .orElseThrow(() -> new IllegalArgumentException("Active pantry batch not found: " + currentlyActiveBatchId));
        RawIngredient targetQueuedBatch = rawIngredientRepository.findById(targetQueuedBatchId)
                .orElseThrow(() -> new IllegalArgumentException("Target queued batch not found: " + targetQueuedBatchId));

        pantryInventoryDomainService.switchActiveBatch(currentlyActiveBatch, targetQueuedBatch);

        rawIngredientRepository.save(currentlyActiveBatch);
        rawIngredientRepository.save(targetQueuedBatch);
    }

    public List<RawIngredient> saveBatchesAtomically(List<RawIngredient> incomingPantryBatches) {
        Objects.requireNonNull(incomingPantryBatches, "incomingPantryBatches list must not be null");
        return rawIngredientRepository.saveAll(incomingPantryBatches);
    }
}
