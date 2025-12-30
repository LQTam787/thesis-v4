package com.calorietracker.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA Entity representing a user in the Calorie Tracker system.
 * 
 * <p>This entity stores all user-related information including personal details,
 * physical measurements, fitness goals, and calculated nutritional values.
 * It serves as the central entity with relationships to meals, foods, weight entries,
 * plans, and reviews.</p>
 * 
 * <h2>Key Attributes:</h2>
 * <ul>
 *   <li><b>Personal Info:</b> name, email, password (BCrypt encrypted), date of birth, sex</li>
 *   <li><b>Physical Measurements:</b> weight (kg), height (cm), BMI (auto-calculated)</li>
 *   <li><b>Fitness Goals:</b> goal weight, goal type (LOSE/MAINTAIN/GAIN), weekly goal pace</li>
 *   <li><b>Calculated Values:</b> BMI, allowed daily calorie intake (using Mifflin-St Jeor equation)</li>
 * </ul>
 * 
 * <h2>Relationships:</h2>
 * <ul>
 *   <li>One-to-Many with {@link Food} (custom foods created by user)</li>
 *   <li>One-to-Many with {@link MealEntry} (logged meals)</li>
 *   <li>One-to-Many with {@link WeightEntry} (weight tracking history)</li>
 *   <li>One-to-One with {@link Plan} (AI-generated meal plan)</li>
 *   <li>One-to-One with {@link Review} (AI-generated progress review)</li>
 * </ul>
 * 
 * @author Calorie Tracker Team
 * @version 1.0.0
 * @see ActivityLevel
 * @see GoalType
 * @see Sex
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private LocalDate dob;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sex sex;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal height;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level", nullable = false)
    private ActivityLevel activityLevel;

    @Column(precision = 5, scale = 2)
    private BigDecimal goal;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", nullable = false)
    private GoalType goalType;

    @Column(name = "weekly_goal", nullable = false, precision = 3, scale = 2)
    private BigDecimal weeklyGoal;

    @Column(name = "allowed_daily_intake", nullable = false)
    private Integer allowedDailyIntake;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal bmi;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Food> customFoods;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MealEntry> mealEntries;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WeightEntry> weightEntries;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Plan plan;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Review review;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * JPA lifecycle callback executed before entity update.
     * Automatically updates the updatedAt timestamp.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Calculates the user's current age based on date of birth.
     * 
     * <p>Note: This is a simplified calculation using only the year difference.
     * For more precise age calculation, consider using Period.between().</p>
     * 
     * @return the user's age in years
     */
    public int getAge() {
        return LocalDate.now().getYear() - dob.getYear();
    }
}
