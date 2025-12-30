package com.calorietracker.model;

/**
 * Enumeration representing user activity levels for TDEE calculation.
 * 
 * <p>Activity level is a key factor in calculating Total Daily Energy Expenditure (TDEE).
 * Each level has an associated multiplier applied to the Basal Metabolic Rate (BMR)
 * to estimate total daily calorie needs.</p>
 * 
 * <h2>Activity Level Multipliers (Harris-Benedict Principle):</h2>
 * <ul>
 *   <li><b>SEDENTARY (1.2):</b> Little or no exercise, desk job</li>
 *   <li><b>LIGHTLY_ACTIVE (1.375):</b> Light exercise 1-3 days/week</li>
 *   <li><b>MODERATELY_ACTIVE (1.55):</b> Moderate exercise 3-5 days/week</li>
 *   <li><b>VERY_ACTIVE (1.725):</b> Hard exercise 6-7 days/week</li>
 * </ul>
 * 
 * <h2>Formula:</h2>
 * <pre>TDEE = BMR × Activity Multiplier</pre>
 * 
 * @author Calorie Tracker Team
 * @version 1.0.0
 * @see com.calorietracker.util.CalorieCalculator
 */
public enum ActivityLevel {
    /** Little or no exercise, desk job - multiplier: 1.2 */
    SEDENTARY(1.2),
    
    /** Light exercise 1-3 days per week - multiplier: 1.375 */
    LIGHTLY_ACTIVE(1.375),
    
    /** Moderate exercise 3-5 days per week - multiplier: 1.55 */
    MODERATELY_ACTIVE(1.55),
    
    /** Hard exercise 6-7 days per week - multiplier: 1.725 */
    VERY_ACTIVE(1.725);

    /** The multiplier applied to BMR to calculate TDEE */
    private final double multiplier;

    /**
     * Constructs an ActivityLevel with the specified multiplier.
     * 
     * @param multiplier the TDEE multiplier for this activity level
     */
    ActivityLevel(double multiplier) {
        this.multiplier = multiplier;
    }

    /**
     * Returns the TDEE multiplier for this activity level.
     * 
     * @return the multiplier to apply to BMR for TDEE calculation
     */
    public double getMultiplier() {
        return multiplier;
    }
}
