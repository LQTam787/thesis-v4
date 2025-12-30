package com.calorietracker.util;

import com.calorietracker.model.ActivityLevel;
import com.calorietracker.model.GoalType;
import com.calorietracker.model.Sex;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility class for nutritional calculations including BMI, BMR, TDEE, and daily calorie allowance.
 * 
 * <p>This class implements scientifically-backed formulas for calculating metabolic rates
 * and calorie requirements based on user physical characteristics and goals.</p>
 * 
 * <h2>Algorithms Used:</h2>
 * <ul>
 *   <li><b>BMI:</b> Standard Body Mass Index formula (weight / height²)</li>
 *   <li><b>BMR:</b> Mifflin-St Jeor Equation (most accurate for modern populations)</li>
 *   <li><b>TDEE:</b> BMR × Activity Level Multiplier</li>
 *   <li><b>Calorie Allowance:</b> TDEE ± (weekly goal × 1100 cal/kg)</li>
 * </ul>
 * 
 * <h2>Scientific Background:</h2>
 * <p>The Mifflin-St Jeor equation (1990) is considered more accurate than the older
 * Harris-Benedict equation for estimating BMR in modern populations. The 1100 cal/kg
 * factor for weight change is based on the principle that 1 kg of body fat contains
 * approximately 7700 calories, distributed over 7 days.</p>
 * 
 * @author Calorie Tracker Team
 * @version 1.0.0
 * @see ActivityLevel
 * @see GoalType
 * @see Sex
 */
public class CalorieCalculator {

    /**
     * Calculates Body Mass Index (BMI) from weight and height.
     * 
     * <p>BMI is a measure of body fat based on height and weight. While not perfect,
     * it provides a useful indicator of healthy weight ranges.</p>
     * 
     * <h3>BMI Categories:</h3>
     * <ul>
     *   <li>Underweight: &lt; 18.5</li>
     *   <li>Normal: 18.5 - 24.9</li>
     *   <li>Overweight: 25 - 29.9</li>
     *   <li>Obese: ≥ 30</li>
     * </ul>
     * 
     * @param weightKg user's weight in kilograms
     * @param heightCm user's height in centimeters
     * @return BMI value rounded to 2 decimal places
     */
    public static BigDecimal calculateBMI(BigDecimal weightKg, BigDecimal heightCm) {
        // Convert height from cm to meters
        BigDecimal heightM = heightCm.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        // Calculate height squared
        BigDecimal heightSquared = heightM.multiply(heightM);
        // BMI = weight / height²
        return weightKg.divide(heightSquared, 2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates Basal Metabolic Rate (BMR) using the Mifflin-St Jeor Equation.
     * 
     * <p>BMR represents the number of calories the body needs at complete rest
     * to maintain vital functions like breathing, circulation, and cell production.</p>
     * 
     * <h3>Mifflin-St Jeor Equation:</h3>
     * <ul>
     *   <li>Men: BMR = (10 × weight in kg) + (6.25 × height in cm) - (5 × age) + 5</li>
     *   <li>Women: BMR = (10 × weight in kg) + (6.25 × height in cm) - (5 × age) - 161</li>
     * </ul>
     * 
     * @param weightKg user's weight in kilograms
     * @param heightCm user's height in centimeters
     * @param age user's age in years
     * @param sex user's biological sex (affects metabolic rate)
     * @return BMR in calories per day
     */
    public static double calculateBMR(BigDecimal weightKg, BigDecimal heightCm, int age, Sex sex) {
        double weight = weightKg.doubleValue();
        double height = heightCm.doubleValue();
        // Base calculation common to both sexes
        double bmr = (10 * weight) + (6.25 * height) - (5 * age);
        // Apply sex-specific adjustment
        return sex == Sex.MALE ? bmr + 5 : bmr - 161;
    }

    /**
     * Calculates Total Daily Energy Expenditure (TDEE) from BMR and activity level.
     * 
     * <p>TDEE represents the total calories burned per day including physical activity.
     * It is calculated by multiplying BMR by an activity factor.</p>
     * 
     * @param bmr Basal Metabolic Rate in calories
     * @param activityLevel user's physical activity level
     * @return TDEE in calories per day (rounded to nearest integer)
     * @see ActivityLevel
     */
    public static int calculateTDEE(double bmr, ActivityLevel activityLevel) {
        return (int) Math.round(bmr * activityLevel.getMultiplier());
    }

    /**
     * Calculates daily calorie allowance based on TDEE and weight goals.
     * 
     * <p>Adjusts TDEE based on the user's goal type and weekly target pace.
     * The adjustment uses 1100 calories per kg of weekly goal, derived from
     * the fact that 1 kg of fat ≈ 7700 calories (7700 / 7 days ≈ 1100).</p>
     * 
     * <h3>Calculation:</h3>
     * <ul>
     *   <li>LOSE: TDEE - (weeklyGoal × 1100)</li>
     *   <li>GAIN: TDEE + (weeklyGoal × 1100)</li>
     *   <li>MAINTAIN: TDEE (no adjustment)</li>
     * </ul>
     * 
     * @param tdee Total Daily Energy Expenditure
     * @param weeklyGoalKg target weight change per week in kg
     * @param goalType weight management goal (LOSE, MAINTAIN, GAIN)
     * @return daily calorie allowance in calories
     */
    public static int calculateDailyCalorieAllowance(
            int tdee,
            BigDecimal weeklyGoalKg,
            GoalType goalType) {

        double weeklyGoal = weeklyGoalKg.doubleValue();
        // 1 kg fat ≈ 7700 cal, spread over 7 days ≈ 1100 cal/day per kg/week
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

    /**
     * Convenience method to calculate allowed daily calorie intake from raw user data.
     * 
     * <p>This method combines BMR, TDEE, and calorie allowance calculations
     * into a single call for ease of use during user registration and profile updates.</p>
     * 
     * @param weightKg user's weight in kilograms
     * @param heightCm user's height in centimeters
     * @param age user's age in years
     * @param sex user's biological sex
     * @param activityLevel user's physical activity level
     * @param weeklyGoalKg target weight change per week in kg
     * @param goalType weight management goal (LOSE, MAINTAIN, GAIN)
     * @return daily calorie allowance in calories
     */
    public static int calculateAllowedDailyIntake(
            BigDecimal weightKg,
            BigDecimal heightCm,
            int age,
            Sex sex,
            ActivityLevel activityLevel,
            BigDecimal weeklyGoalKg,
            GoalType goalType) {

        // Step 1: Calculate BMR using Mifflin-St Jeor equation
        double bmr = calculateBMR(weightKg, heightCm, age, sex);
        // Step 2: Calculate TDEE by applying activity multiplier
        int tdee = calculateTDEE(bmr, activityLevel);
        // Step 3: Adjust for weight goal
        return calculateDailyCalorieAllowance(tdee, weeklyGoalKg, goalType);
    }
}
