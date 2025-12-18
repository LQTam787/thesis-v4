package com.calorietracker.dto.request;

import com.calorietracker.model.MealType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodRequest {

    @NotBlank(message = "Food name is required")
    @Size(min = 2, max = 100, message = "Food name must be between 2 and 100 characters")
    private String name;

    @Size(max = 255, message = "Image URL must be less than 255 characters")
    private String image;

    @NotNull(message = "Meal type is required")
    private MealType mealType;

    @NotNull(message = "Calories is required")
    @Min(value = 0, message = "Calories must be at least 0")
    @Max(value = 10000, message = "Calories must be less than 10000")
    private Integer calories;
}
