package com.calorietracker.dto.response;

import com.calorietracker.model.MealEntry;
import com.calorietracker.model.MealType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealEntryResponse {

    private Long id;
    private LocalDate entryDate;
    private LocalTime entryTime;
    private LocalDateTime createdAt;
    
    // Food details (embedded for convenience)
    private Long foodId;
    private String foodName;
    private String foodImage;
    private MealType mealType;
    private Integer calories;

    public static MealEntryResponse fromEntity(MealEntry mealEntry) {
        return MealEntryResponse.builder()
                .id(mealEntry.getId())
                .entryDate(mealEntry.getEntryDate())
                .entryTime(mealEntry.getEntryTime())
                .createdAt(mealEntry.getCreatedAt())
                .foodId(mealEntry.getFood().getId())
                .foodName(mealEntry.getFood().getName())
                .foodImage(mealEntry.getFood().getImage())
                .mealType(mealEntry.getFood().getMealType())
                .calories(mealEntry.getFood().getCalories())
                .build();
    }
}
