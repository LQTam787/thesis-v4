package com.calorietracker.util;

import com.calorietracker.model.ActivityLevel;
import com.calorietracker.model.GoalType;
import com.calorietracker.model.Sex;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalorieCalculator {

    public static BigDecimal calculateBMI(BigDecimal weightKg, BigDecimal heightCm) {
        BigDecimal heightM = heightCm.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal heightSquared = heightM.multiply(heightM);
        return weightKg.divide(heightSquared, 2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate Basal Metabolic Rate (BMR) using Mifflin-St Jeor Equation
     * For men: BMR = (10 × weight in kg) + (6.25 × height in cm) - (5 × age in years) + 5
     * For women: BMR = (10 × weight in kg) + (6.25 × height in cm) - (5 × age in years) - 161
     */
    public static double calculateBMR(BigDecimal weightKg, BigDecimal heightCm, int age, Sex sex) {
        double weight = weightKg.doubleValue();
        double height = heightCm.doubleValue();
        double bmr = (10 * weight) + (6.25 * height) - (5 * age);
        return sex == Sex.MALE ? bmr + 5 : bmr - 161;
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
            Sex sex,
            ActivityLevel activityLevel,
            BigDecimal weeklyGoalKg,
            GoalType goalType) {

        double bmr = calculateBMR(weightKg, heightCm, age, sex);
        int tdee = calculateTDEE(bmr, activityLevel);
        return calculateDailyCalorieAllowance(tdee, weeklyGoalKg, goalType);
    }
}
