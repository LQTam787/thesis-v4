package com.calorietracker.model;

/**
 * Enumeration representing user weight management goals.
 * 
 * <p>The goal type determines how the daily calorie allowance is calculated
 * relative to the user's Total Daily Energy Expenditure (TDEE).</p>
 * 
 * <h2>Calorie Adjustment:</h2>
 * <ul>
 *   <li><b>LOSE:</b> Calorie deficit (TDEE - adjustment) for weight loss</li>
 *   <li><b>MAINTAIN:</b> No adjustment (TDEE) to maintain current weight</li>
 *   <li><b>GAIN:</b> Calorie surplus (TDEE + adjustment) for weight gain</li>
 * </ul>
 * 
 * <p>The adjustment amount is calculated based on the user's weekly goal pace,
 * using the formula: adjustment = weeklyGoal × 1100 calories.</p>
 * 
 * @author Calorie Tracker Team
 * @version 1.0.0
 * @see com.calorietracker.util.CalorieCalculator
 */
public enum GoalType {
    /** Weight loss goal - creates calorie deficit */
    LOSE,
    
    /** Weight maintenance goal - maintains TDEE */
    MAINTAIN,
    
    /** Weight gain goal - creates calorie surplus */
    GAIN
}
