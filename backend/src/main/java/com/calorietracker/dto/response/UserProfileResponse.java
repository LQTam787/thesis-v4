package com.calorietracker.dto.response;

import com.calorietracker.model.ActivityLevel;
import com.calorietracker.model.GoalType;
import com.calorietracker.model.Sex;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long userId;
    private String name;
    private String email;
    private LocalDate dob;
    private Sex sex;
    private BigDecimal height;
    private ActivityLevel activityLevel;
    private BigDecimal weight;
    private BigDecimal goal;
    private GoalType goalType;
    private BigDecimal weeklyGoal;
    private BigDecimal bmi;
    private Integer allowedDailyIntake;
}
