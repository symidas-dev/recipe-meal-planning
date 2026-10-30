package hr.algebra.recipemealplanning.service;

import hr.algebra.recipemealplanning.domain.entity.Recipe;
import hr.algebra.recipemealplanning.domain.entity.WeeklyMenu;
import hr.algebra.recipemealplanning.domain.enums.DayOfWeek;
import hr.algebra.recipemealplanning.domain.enums.MealTime;
import hr.algebra.recipemealplanning.dto.MealSlotResponseDTO;
import hr.algebra.recipemealplanning.dto.WeeklyMenuResponseDTO;
import hr.algebra.recipemealplanning.repository.RecipeRepository;
import hr.algebra.recipemealplanning.repository.WeeklyMenuRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class WeeklyMenuService {

    private final WeeklyMenuRepository weeklyMenuRepository;
    private final RecipeRepository recipeRepository;

    public WeeklyMenuService(
            WeeklyMenuRepository weeklyMenuRepository,
            RecipeRepository recipeRepository) {
        this.weeklyMenuRepository = Objects.requireNonNull(weeklyMenuRepository, "weeklyMenuRepository must not be null");
        this.recipeRepository = Objects.requireNonNull(recipeRepository, "recipeRepository must not be null");
    }

    public WeeklyMenuResponseDTO createWeeklyBoardForWeek(LocalDate targetDateInWeek) {
        Objects.requireNonNull(targetDateInWeek, "targetDateInWeek must not be null");

        LocalDate mondayOfWeek = targetDateInWeek.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

        if (weeklyMenuRepository.findByWeekStartDate(mondayOfWeek).isPresent()) {
            throw new IllegalStateException("A weekly menu board already exists for week starting on: " + mondayOfWeek);
        }

        WeeklyMenu newWeeklyBoard = weeklyMenuRepository.save(new WeeklyMenu(mondayOfWeek));
        return mapToResponseDTO(newWeeklyBoard);
    }

    @Transactional(readOnly = true)
    public Optional<WeeklyMenuResponseDTO> findWeeklyBoardByDate(LocalDate targetDateInWeek) {
        Objects.requireNonNull(targetDateInWeek, "targetDateInWeek must not be null");
        LocalDate mondayOfWeek = targetDateInWeek.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        return weeklyMenuRepository.findByWeekStartDate(mondayOfWeek)
                .map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public WeeklyMenuResponseDTO getWeeklyBoardById(Long weeklyMenuId) {
        Objects.requireNonNull(weeklyMenuId, "weeklyMenuId must not be null");
        WeeklyMenu weeklyMenu = weeklyMenuRepository.findMenuWithCalendarSlots(weeklyMenuId)
                .orElseThrow(() -> new IllegalArgumentException("Weekly menu board not found with id: " + weeklyMenuId));
        return mapToResponseDTO(weeklyMenu);
    }

    public WeeklyMenuResponseDTO assignMealToSlot(
            Long weeklyMenuId,
            DayOfWeek dayOfWeek,
            MealTime mealTime,
            Long recipeId,
            int servings) {
        Objects.requireNonNull(weeklyMenuId, "weeklyMenuId must not be null");
        Objects.requireNonNull(dayOfWeek, "dayOfWeek must not be null");
        Objects.requireNonNull(mealTime, "mealTime must not be null");
        Objects.requireNonNull(recipeId, "recipeId must not be null");

        WeeklyMenu weeklyMenu = weeklyMenuRepository.findMenuWithCalendarSlots(weeklyMenuId)
                .orElseThrow(() -> new IllegalArgumentException("Weekly menu board not found with id: " + weeklyMenuId));

        if (weeklyMenu.isClosed()) {
            throw new IllegalStateException("Cannot modify a closed weekly menu board");
        }

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found with id: " + recipeId));

        weeklyMenu.assignOrUpdateSlot(recipe, dayOfWeek, mealTime, servings);

        WeeklyMenu savedMenu = weeklyMenuRepository.save(weeklyMenu);
        return mapToResponseDTO(savedMenu);
    }

    public void clearMealSlot(Long weeklyMenuId, DayOfWeek dayOfWeek, MealTime mealTime) {
        Objects.requireNonNull(weeklyMenuId, "weeklyMenuId must not be null");
        Objects.requireNonNull(dayOfWeek, "dayOfWeek must not be null");
        Objects.requireNonNull(mealTime, "mealTime must not be null");

        WeeklyMenu weeklyMenu = weeklyMenuRepository.findMenuWithCalendarSlots(weeklyMenuId)
                .orElseThrow(() -> new IllegalArgumentException("Weekly menu board not found with id: " + weeklyMenuId));

        if (weeklyMenu.isClosed()) {
            throw new IllegalStateException("Cannot modify a closed weekly menu board");
        }

        weeklyMenu.removeSlotMatching(dayOfWeek, mealTime);

        weeklyMenuRepository.save(weeklyMenu);
    }

    public void closeWeeklyBoard(Long weeklyMenuId) {
        Objects.requireNonNull(weeklyMenuId, "weeklyMenuId must not be null");

        WeeklyMenu weeklyMenu = weeklyMenuRepository.findById(weeklyMenuId)
                .orElseThrow(() -> new IllegalArgumentException("Weekly menu board not found with id: " + weeklyMenuId));

        weeklyMenu.close();
        weeklyMenuRepository.save(weeklyMenu);
    }

    @Transactional(readOnly = true)
    public Page<WeeklyMenuResponseDTO> listActiveWeeklyBoards(Pageable pageable) {
        Objects.requireNonNull(pageable, "pageable must not be null");
        return weeklyMenuRepository.findByIsClosedFalse(pageable)
                .map(this::mapToResponseDTO);
    }

    private WeeklyMenuResponseDTO mapToResponseDTO(WeeklyMenu menu) {
        List<MealSlotResponseDTO> slotDTOs = menu.getSlots().stream()
                .map(slot -> new MealSlotResponseDTO(
                        slot.getId(),
                        slot.getRecipe().getId(),
                        slot.getRecipe().getTitle(),
                        slot.getDayOfWeek(),
                        slot.getMealTime(),
                        slot.getServings(),
                        slot.getActualNutrition()
                ))
                .toList();

        return new WeeklyMenuResponseDTO(
                menu.getId(),
                menu.getWeekStartDate(),
                menu.getTotalWeekNutritionNominal(),
                menu.getTotalWeekNutritionActual(),
                menu.isClosed(),
                slotDTOs
        );
    }
}
