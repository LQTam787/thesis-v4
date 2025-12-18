package com.calorietracker.dto.response;

import com.calorietracker.model.GoalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private LocalDate date;
    
    // Calorie summary
    private Integer allowedDailyIntake;
    private Integer consumedCalories;
    private Integer remainingCalories;
    
    // User info
    private String userName;
    private GoalType goalType;
    private BigDecimal currentWeight;
    private BigDecimal goalWeight;
    
    // Today's weight entry (if exists)
    private BigDecimal todayWeight;
    
    // Meal entries grouped by meal type
    private Map<String, List<MealEntryResponse>> mealsByType;
    
    // Total meals count for the day
    private Integer totalMealsCount;
}
