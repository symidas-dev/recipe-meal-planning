package hr.algebra.recipemealplanning.repository;

import hr.algebra.recipemealplanning.domain.entity.WeeklyMenu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface WeeklyMenuRepository extends JpaRepository<WeeklyMenu, Long> {

    Optional<WeeklyMenu> findByWeekStartDate(LocalDate targetWeekStartDate);

    Page<WeeklyMenu> findByIsClosedFalse(Pageable pageable);

    @Query("""
            SELECT calendarMenu FROM WeeklyMenu calendarMenu
            LEFT JOIN FETCH calendarMenu.slots calendarSlot
            LEFT JOIN FETCH calendarSlot.recipe assignedRecipe
            WHERE calendarMenu.id = :weeklyMenuId
            """)
    Optional<WeeklyMenu> findMenuWithCalendarSlots(@Param("weeklyMenuId") Long weeklyMenuId);
}
