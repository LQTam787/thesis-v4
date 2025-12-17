package com.calorietracker.repository;

import com.calorietracker.model.Food;
import com.calorietracker.model.MealType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {
    @Query("SELECT f FROM Food f WHERE f.user IS NULL OR f.user.id = :userId")
    List<Food> findAvailableFoodsForUser(@Param("userId") Long userId);

    @Query("SELECT f FROM Food f WHERE (f.user IS NULL OR f.user.id = :userId) AND f.mealType = :mealType")
    List<Food> findByMealTypeForUser(@Param("userId") Long userId, @Param("mealType") MealType mealType);

    List<Food> findByUserIsNull();

    List<Food> findByUserId(Long userId);
}
