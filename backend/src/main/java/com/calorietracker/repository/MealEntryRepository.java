package com.calorietracker.repository;

import com.calorietracker.model.MealEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MealEntryRepository extends JpaRepository<MealEntry, Long> {
    List<MealEntry> findByUserIdAndEntryDate(Long userId, LocalDate entryDate);

    List<MealEntry> findByUserIdAndEntryDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COALESCE(SUM(f.calories), 0) FROM MealEntry me JOIN me.food f WHERE me.user.id = :userId AND me.entryDate = :date")
    Integer sumCaloriesForUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    List<MealEntry> findByUserIdAndEntryDateOrderByEntryTimeAsc(Long userId, LocalDate entryDate);
}
