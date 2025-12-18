package com.calorietracker.dto.response;

import com.calorietracker.model.Food;
import com.calorietracker.model.MealType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodResponse {

    private Long id;
    private String name;
    private String image;
    private MealType mealType;
    private Integer calories;
    private boolean customFood;
    private LocalDateTime createdAt;

    public static FoodResponse fromEntity(Food food) {
        return FoodResponse.builder()
                .id(food.getId())
                .name(food.getName())
                .image(food.getImage())
                .mealType(food.getMealType())
                .calories(food.getCalories())
                .customFood(food.isCustomFood())
                .createdAt(food.getCreatedAt())
                .build();
    }
}
