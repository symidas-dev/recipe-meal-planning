package hr.algebra.recipemealplanning.repository;

import hr.algebra.recipemealplanning.domain.entity.TheoreticalIngredient;
import hr.algebra.recipemealplanning.domain.model.TaxonomyTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TheoreticalIngredientRepository extends JpaRepository<TheoreticalIngredient, Long> {

    @Query("""
            SELECT ingredient FROM TheoreticalIngredient ingredient
            WHERE LOWER(ingredient.canonicalName) = LOWER(:canonicalName)
            """)
    Optional<TheoreticalIngredient> findByCanonicalName(@Param("canonicalName") String canonicalName);

    @Query("""
            SELECT ingredient FROM TheoreticalIngredient ingredient
            WHERE ingredient.taxonomyTag = :#{#categoryTag.value()}
            """)
    List<TheoreticalIngredient> findByCategory(@Param("categoryTag") TaxonomyTag categoryTag);

    Page<TheoreticalIngredient> findByIsActiveTrue(Pageable pageable);
}
