package com.calorietracker.model;

/**
 * Enumeration representing biological sex for BMR calculation.
 * 
 * <p>Sex is a required factor in the Mifflin-St Jeor equation for calculating
 * Basal Metabolic Rate (BMR), as metabolic rates differ between males and females.</p>
 * 
 * <h2>BMR Formula Difference:</h2>
 * <ul>
 *   <li><b>MALE:</b> BMR = (10 × weight) + (6.25 × height) - (5 × age) + 5</li>
 *   <li><b>FEMALE:</b> BMR = (10 × weight) + (6.25 × height) - (5 × age) - 161</li>
 * </ul>
 * 
 * @author Calorie Tracker Team
 * @version 1.0.0
 * @see com.calorietracker.util.CalorieCalculator
 */
public enum Sex {
    /** Male biological sex - adds 5 to BMR formula */
    MALE,
    
    /** Female biological sex - subtracts 161 from BMR formula */
    FEMALE
}
