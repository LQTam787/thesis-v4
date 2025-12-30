package com.calorietracker.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * JPA Entity representing a food item in the Calorie Tracker system.
 * 
 * <p>Foods can be either system-wide (available to all users) or custom foods
 * created by individual users. System foods have a null user reference, while
 * custom foods are linked to their creator.</p>
 * 
 * <h2>Attributes:</h2>
 * <ul>
 *   <li><b>name:</b> Display name of the food item</li>
 *   <li><b>image:</b> Optional URL or path to food image</li>
 *   <li><b>mealType:</b> Category (BREAKFAST, LUNCH, DINNER, SNACKS, OTHER)</li>
 *   <li><b>calories:</b> Caloric value per serving</li>
 *   <li><b>user:</b> Owner reference (null for system foods)</li>
 * </ul>
 * 
 * <h2>Design Pattern:</h2>
 * <p>Uses the Null Object pattern for distinguishing system vs custom foods -
 * system foods have user=null, custom foods have user reference set.</p>
 * 
 * @author Calorie Tracker Team
 * @version 1.0.0
 * @see MealType
 * @see User
 */
@Entity
@Table(name = "foods")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Food {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String image;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false)
    private MealType mealType;

    @Column(nullable = false)
    private Integer calories;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * JPA lifecycle callback executed before entity persistence.
     * Automatically sets the createdAt timestamp.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Determines if this food is a custom food created by a user.
     * 
     * <p>System foods (available to all users) have no user association,
     * while custom foods are linked to their creator.</p>
     * 
     * @return true if this is a user-created custom food, false if system food
     */
    public boolean isCustomFood() {
        return user != null;
    }
}
