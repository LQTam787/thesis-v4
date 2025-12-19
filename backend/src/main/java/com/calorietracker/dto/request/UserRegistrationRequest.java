package com.calorietracker.dto.request;

import com.calorietracker.model.ActivityLevel;
import com.calorietracker.model.GoalType;
import com.calorietracker.model.Sex;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dob;

    @NotNull(message = "Sex is required")
    private Sex sex;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "20.0", message = "Weight must be at least 20 kg")
    @DecimalMax(value = "500.0", message = "Weight must be less than 500 kg")
    private BigDecimal weight;

    @NotNull(message = "Height is required")
    @DecimalMin(value = "50.0", message = "Height must be at least 50 cm")
    @DecimalMax(value = "300.0", message = "Height must be less than 300 cm")
    private BigDecimal height;

    @NotNull(message = "Activity level is required")
    private ActivityLevel activityLevel;

    @DecimalMin(value = "20.0", message = "Goal weight must be at least 20 kg")
    @DecimalMax(value = "500.0", message = "Goal weight must be less than 500 kg")
    private BigDecimal goal;

    @NotNull(message = "Goal type is required")
    private GoalType goalType;

    @NotNull(message = "Weekly goal is required")
    @DecimalMin(value = "0.1", message = "Weekly goal must be at least 0.1 kg")
    @DecimalMax(value = "1.0", message = "Weekly goal must be at most 1.0 kg")
    private BigDecimal weeklyGoal;
}
