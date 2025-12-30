package com.calorietracker.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA Entity representing a weight tracking entry in the system.
 * 
 * <p>Users can log their weight daily to track progress toward their goals.
 * The system enforces one weight entry per user per day through a unique
 * constraint, with updates replacing existing entries for the same date.</p>
 * 
 * <h2>Attributes:</h2>
 * <ul>
 *   <li><b>user:</b> The user who logged this weight entry</li>
 *   <li><b>entryDate:</b> Date of the weight measurement</li>
 *   <li><b>weight:</b> Weight value in kilograms (precision: 5, scale: 2)</li>
 * </ul>
 * 
 * <h2>Business Logic:</h2>
 * <p>When a new weight entry is the latest chronologically, the user's
 * current weight, BMI, and daily calorie allowance are automatically
 * recalculated to reflect the new measurement.</p>
 * 
 * @author Calorie Tracker Team
 * @version 1.0.0
 * @see User
 * @see com.calorietracker.service.WeightEntryService
 */
@Entity
@Table(name = "weight_entries",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "entry_date"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeightEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
