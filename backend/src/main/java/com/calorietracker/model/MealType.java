package com.calorietracker.model;

/**
 * Enumeration representing meal categories for food classification.
 * 
 * <p>Foods are categorized by meal type to help users organize their
 * daily intake and view meals grouped by time of day on the dashboard.</p>
 * 
 * <h2>Categories:</h2>
 * <ul>
 *   <li><b>BREAKFAST:</b> Morning meals</li>
 *   <li><b>LUNCH:</b> Midday meals</li>
 *   <li><b>SNACKS:</b> Between-meal snacks</li>
 *   <li><b>DINNER:</b> Evening meals</li>
 *   <li><b>OTHER:</b> Uncategorized food items</li>
 * </ul>
 * 
 * @author Calorie Tracker Team
 * @version 1.0.0
 * @see Food
 */
public enum MealType {
    /** Morning meal category */
    BREAKFAST,
    
    /** Midday meal category */
    LUNCH,
    
    /** Between-meal snacks category */
    SNACKS,
    
    /** Evening meal category */
    DINNER,
    
    /** Uncategorized food items */
    OTHER
}
