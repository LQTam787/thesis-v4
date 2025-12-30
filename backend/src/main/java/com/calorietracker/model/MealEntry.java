package com.calorietracker.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * JPA Entity representing a meal entry (food consumption log) in the system.
 * 
 * <p>Each meal entry records when a user consumed a specific food item,
 * including the date and time of consumption. This entity is central to
 * the calorie tracking functionality.</p>
 * 
 * <h2>Attributes:</h2>
 * <ul>
 *   <li><b>user:</b> The user who logged this meal</li>
 *   <li><b>food:</b> The food item that was consumed</li>
 *   <li><b>entryDate:</b> Date of consumption</li>
 *   <li><b>entryTime:</b> Time of consumption</li>
 * </ul>
 * 
 * <h2>Database Optimization:</h2>
 * <p>Includes a composite index on (user_id, entry_date) for efficient
 * querying of daily meal entries - a common operation in the dashboard.</p>
 * 
 * @author Calorie Tracker Team
 * @version 1.0.0
 * @see User
 * @see Food
 */
@Entity
@Table(name = "meal_entries", indexes = {
    @Index(name = "idx_user_date", columnList = "user_id, entry_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "entry_time", nullable = false)
    private LocalTime entryTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
