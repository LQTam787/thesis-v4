package com.calorietracker.repository;

import com.calorietracker.model.WeightEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeightEntryRepository extends JpaRepository<WeightEntry, Long> {
    List<WeightEntry> findByUserIdOrderByEntryDateAsc(Long userId);

    Optional<WeightEntry> findByUserIdAndEntryDate(Long userId, LocalDate entryDate);

    Optional<WeightEntry> findFirstByUserIdOrderByEntryDateDesc(Long userId);

    List<WeightEntry> findByUserIdAndEntryDateBetweenOrderByEntryDateAsc(
        Long userId, LocalDate startDate, LocalDate endDate);
}
