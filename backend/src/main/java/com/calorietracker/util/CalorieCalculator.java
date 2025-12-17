package com.calorietracker.util;

import com.calorietracker.model.ActivityLevel;
import com.calorietracker.model.GoalType;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalorieCalculator {

    public static BigDecimal calculateBMI(BigDecimal weightKg, BigDecimal heightCm) {
        BigDecimal heightM = heightCm.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal heightSquared = heightM.multiply(heightM);
        return weightKg.divide(heightSquared, 2, RoundingMode.HALF_UP);
    }

    public static double calculateBMR(BigDecimal weightKg, BigDecimal heightCm, int age) {
        double weight = weightKg.doubleValue();
        double height = heightCm.doubleValue();
        return (10 * weight) + (6.25 * height) - (5 * age) + 5;
    }

    public static int calculateTDEE(double bmr, ActivityLevel activityLevel) {
        return (int) Math.round(bmr * activityLevel.getMultiplier());
    }

    public static int calculateDailyCalorieAllowance(
            int tdee,
            BigDecimal weeklyGoalKg,
            GoalType goalType) {

        double weeklyGoal = weeklyGoalKg.doubleValue();
        int calorieAdjustment = (int) Math.round(weeklyGoal * 1100);

        switch (goalType) {
            case LOSE:
                return tdee - calorieAdjustment;
            case GAIN:
                return tdee + calorieAdjustment;
            case MAINTAIN:
            default:
                return tdee;
        }
    }

    public static int calculateAllowedDailyIntake(
            BigDecimal weightKg,
            BigDecimal heightCm,
            int age,
            ActivityLevel activityLevel,
            BigDecimal weeklyGoalKg,
            GoalType goalType) {

        double bmr = calculateBMR(weightKg, heightCm, age);
        int tdee = calculateTDEE(bmr, activityLevel);
        return calculateDailyCalorieAllowance(tdee, weeklyGoalKg, goalType);
    }
}
