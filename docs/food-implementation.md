# Food Feature Implementation Documentation

**Created**: December 18, 2025  
**Status**: Completed and Verified

---

## Table of Contents

1. [Overview](#overview)
2. [File Structure](#file-structure)
3. [API Endpoints](#api-endpoints)
4. [DTOs (Data Transfer Objects)](#dtos-data-transfer-objects)
5. [FoodService](#foodservice)
6. [FoodController](#foodcontroller)
7. [Testing the API](#testing-the-api)

---

## Overview

This document describes the Food management feature implementation for the Calorie Tracker application. The system provides:

- **Food Listing** - View all available foods (system + custom)
- **Meal Type Filtering** - Filter foods by meal type (BREAKFAST, LUNCH, SNACKS, DINNER, OTHER)
- **Custom Food Management** - Create, update, and delete personal food items
- **Access Control** - Users can only modify their own custom foods, not system foods

---

## File Structure

```
backend/src/main/java/com/calorietracker/
├── controller/
│   └── FoodController.java          # REST endpoints for food operations
├── dto/
│   ├── request/
│   │   └── FoodRequest.java         # Request DTO for create/update
│   └── response/
│       └── FoodResponse.java        # Response DTO for food data
├── model/
│   ├── Food.java                    # JPA entity (existing)
│   └── MealType.java                # Enum (existing)
├── repository/
│   └── FoodRepository.java          # Data access (existing)
└── service/
    └── FoodService.java             # Business logic for food operations
```

---

## API Endpoints

All endpoints require JWT authentication via `Authorization: Bearer <token>` header.

### GET `/api/foods`

Get all available foods (system foods + user's custom foods).

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Oatmeal",
    "image": null,
    "mealType": "BREAKFAST",
    "calories": 150,
    "customFood": false,
    "createdAt": "2025-12-18T08:00:00"
  },
  {
    "id": 10,
    "name": "My Protein Shake",
    "image": "https://example.com/shake.jpg",
    "mealType": "SNACKS",
    "calories": 200,
    "customFood": true,
    "createdAt": "2025-12-18T09:30:00"
  }
]
```

---

### GET `/api/foods/meal-type/{mealType}`

Get foods filtered by meal type.

**Path Parameters:**
- `mealType` - One of: `BREAKFAST`, `LUNCH`, `SNACKS`, `DINNER`, `OTHER`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Oatmeal",
    "image": null,
    "mealType": "BREAKFAST",
    "calories": 150,
    "customFood": false,
    "createdAt": "2025-12-18T08:00:00"
  }
]
```

---

### GET `/api/foods/custom`

Get only the authenticated user's custom foods.

**Response (200 OK):**
```json
[
  {
    "id": 10,
    "name": "My Protein Shake",
    "image": "https://example.com/shake.jpg",
    "mealType": "SNACKS",
    "calories": 200,
    "customFood": true,
    "createdAt": "2025-12-18T09:30:00"
  }
]
```

---

### GET `/api/foods/{id}`

Get a single food by ID.

**Path Parameters:**
- `id` - Food ID

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Oatmeal",
  "image": null,
  "mealType": "BREAKFAST",
  "calories": 150,
  "customFood": false,
  "createdAt": "2025-12-18T08:00:00"
}
```

**Error Response (404 Not Found):**
```json
{
  "message": "Food not found with id: 999"
}
```

---

### POST `/api/foods`

Create a new custom food.

**Request Body:**
```json
{
  "name": "My Protein Shake",
  "image": "https://example.com/shake.jpg",
  "mealType": "SNACKS",
  "calories": 200
}
```

**Success Response (201 Created):**
```json
{
  "id": 10,
  "name": "My Protein Shake",
  "image": "https://example.com/shake.jpg",
  "mealType": "SNACKS",
  "calories": 200,
  "customFood": true,
  "createdAt": "2025-12-18T09:30:00"
}
```

**Error Response (400 Bad Request):**
```json
{
  "name": "Food name is required",
  "mealType": "Meal type is required",
  "calories": "Calories is required"
}
```

---

### PUT `/api/foods/{id}`

Update a custom food (owner only).

**Path Parameters:**
- `id` - Food ID

**Request Body:**
```json
{
  "name": "Updated Protein Shake",
  "image": "https://example.com/new-shake.jpg",
  "mealType": "BREAKFAST",
  "calories": 250
}
```

**Success Response (200 OK):**
```json
{
  "id": 10,
  "name": "Updated Protein Shake",
  "image": "https://example.com/new-shake.jpg",
  "mealType": "BREAKFAST",
  "calories": 250,
  "customFood": true,
  "createdAt": "2025-12-18T09:30:00"
}
```

**Error Responses:**
- `400 Bad Request` - "Cannot update system foods"
- `400 Bad Request` - "You can only update your own custom foods"
- `404 Not Found` - "Food not found with id: {id}"

---

### DELETE `/api/foods/{id}`

Delete a custom food (owner only).

**Path Parameters:**
- `id` - Food ID

**Success Response (204 No Content):**
No body returned.

**Error Responses:**
- `400 Bad Request` - "Cannot delete system foods"
- `400 Bad Request` - "You can only delete your own custom foods"
- `404 Not Found` - "Food not found with id: {id}"

---

## DTOs (Data Transfer Objects)

### FoodRequest.java

```java
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
```

**Validation Rules:**

| Field | Type | Validation |
|-------|------|------------|
| name | String | Required, 2-100 characters |
| image | String | Optional, max 255 characters |
| mealType | MealType | Required (BREAKFAST, LUNCH, SNACKS, DINNER, OTHER) |
| calories | Integer | Required, 0-10000 |

---

### FoodResponse.java

```java
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
```

**Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Food's database ID |
| name | String | Food name |
| image | String | Image URL (nullable) |
| mealType | MealType | Meal category |
| calories | Integer | Calorie count |
| customFood | boolean | true if user-created, false if system food |
| createdAt | LocalDateTime | Creation timestamp |

---

## FoodService

```java
package com.calorietracker.service;

import com.calorietracker.dto.request.FoodRequest;
import com.calorietracker.dto.response.FoodResponse;
import com.calorietracker.exception.BadRequestException;
import com.calorietracker.exception.ResourceNotFoundException;
import com.calorietracker.model.Food;
import com.calorietracker.model.MealType;
import com.calorietracker.model.User;
import com.calorietracker.repository.FoodRepository;
import com.calorietracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FoodService {

    private final FoodRepository foodRepository;
    private final UserRepository userRepository;

    /**
     * Get all foods available to a user (system foods + user's custom foods)
     */
    public List<FoodResponse> getAvailableFoods(Long userId) {
        return foodRepository.findAvailableFoodsForUser(userId)
                .stream()
                .map(FoodResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get foods by meal type for a user
     */
    public List<FoodResponse> getFoodsByMealType(Long userId, MealType mealType) {
        return foodRepository.findByMealTypeForUser(userId, mealType)
                .stream()
                .map(FoodResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get only the user's custom foods
     */
    public List<FoodResponse> getUserCustomFoods(Long userId) {
        return foodRepository.findByUserId(userId)
                .stream()
                .map(FoodResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get a single food by ID
     */
    public FoodResponse getFoodById(Long foodId, Long userId) {
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new ResourceNotFoundException("Food not found with id: " + foodId));

        // Check if user has access to this food (system food or their own custom food)
        if (food.getUser() != null && !food.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Food not found with id: " + foodId);
        }

        return FoodResponse.fromEntity(food);
    }

    /**
     * Create a custom food for a user
     */
    @Transactional
    public FoodResponse createFood(FoodRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Food food = Food.builder()
                .name(request.getName())
                .image(request.getImage())
                .mealType(request.getMealType())
                .calories(request.getCalories())
                .user(user)
                .build();

        Food savedFood = foodRepository.save(food);
        return FoodResponse.fromEntity(savedFood);
    }

    /**
     * Update a custom food (only owner can update)
     */
    @Transactional
    public FoodResponse updateFood(Long foodId, FoodRequest request, Long userId) {
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new ResourceNotFoundException("Food not found with id: " + foodId));

        // Only allow updating custom foods owned by the user
        if (food.getUser() == null) {
            throw new BadRequestException("Cannot update system foods");
        }
        if (!food.getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only update your own custom foods");
        }

        food.setName(request.getName());
        food.setImage(request.getImage());
        food.setMealType(request.getMealType());
        food.setCalories(request.getCalories());

        Food updatedFood = foodRepository.save(food);
        return FoodResponse.fromEntity(updatedFood);
    }

    /**
     * Delete a custom food (only owner can delete)
     */
    @Transactional
    public void deleteFood(Long foodId, Long userId) {
        Food food = foodRepository.findById(foodId)
                .orElseThrow(() -> new ResourceNotFoundException("Food not found with id: " + foodId));

        // Only allow deleting custom foods owned by the user
        if (food.getUser() == null) {
            throw new BadRequestException("Cannot delete system foods");
        }
        if (!food.getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only delete your own custom foods");
        }

        foodRepository.delete(food);
    }
}
```

**Key Features:**

1. **Access Control**: Users can only see system foods and their own custom foods
2. **Ownership Validation**: Update/delete operations verify food ownership
3. **System Food Protection**: System foods (user_id = NULL) cannot be modified
4. **Transactional Operations**: Create, update, delete are wrapped in transactions

---

## FoodController

```java
package com.calorietracker.controller;

import com.calorietracker.dto.request.FoodRequest;
import com.calorietracker.dto.response.FoodResponse;
import com.calorietracker.model.MealType;
import com.calorietracker.model.User;
import com.calorietracker.repository.UserRepository;
import com.calorietracker.service.FoodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;
    private final UserRepository userRepository;

    /**
     * Get all available foods (system + user's custom foods)
     */
    @GetMapping
    public ResponseEntity<List<FoodResponse>> getAllFoods(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        List<FoodResponse> foods = foodService.getAvailableFoods(userId);
        return ResponseEntity.ok(foods);
    }

    /**
     * Get foods filtered by meal type
     */
    @GetMapping("/meal-type/{mealType}")
    public ResponseEntity<List<FoodResponse>> getFoodsByMealType(
            @PathVariable MealType mealType,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        List<FoodResponse> foods = foodService.getFoodsByMealType(userId, mealType);
        return ResponseEntity.ok(foods);
    }

    /**
     * Get only the user's custom foods
     */
    @GetMapping("/custom")
    public ResponseEntity<List<FoodResponse>> getCustomFoods(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        List<FoodResponse> foods = foodService.getUserCustomFoods(userId);
        return ResponseEntity.ok(foods);
    }

    /**
     * Get a single food by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<FoodResponse> getFoodById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        FoodResponse food = foodService.getFoodById(id, userId);
        return ResponseEntity.ok(food);
    }

    /**
     * Create a new custom food
     */
    @PostMapping
    public ResponseEntity<FoodResponse> createFood(
            @Valid @RequestBody FoodRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        FoodResponse food = foodService.createFood(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(food);
    }

    /**
     * Update a custom food
     */
    @PutMapping("/{id}")
    public ResponseEntity<FoodResponse> updateFood(
            @PathVariable Long id,
            @Valid @RequestBody FoodRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        FoodResponse food = foodService.updateFood(id, request, userId);
        return ResponseEntity.ok(food);
    }

    /**
     * Delete a custom food
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFood(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        foodService.deleteFood(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Helper method to extract user ID from authenticated user
     */
    private Long getUserIdFromUserDetails(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
```

**Key Features:**

1. **JWT Authentication**: All endpoints require valid JWT token
2. **Request Validation**: Uses `@Valid` annotation for automatic validation
3. **Proper HTTP Status Codes**: 200 OK, 201 Created, 204 No Content
4. **User Context**: Extracts authenticated user from JWT for ownership checks

---

## Testing the API

### Using cURL

**Get all foods (requires authentication):**
```bash
curl -X GET http://localhost:8080/api/foods \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Get foods by meal type:**
```bash
curl -X GET http://localhost:8080/api/foods/meal-type/BREAKFAST \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Get custom foods only:**
```bash
curl -X GET http://localhost:8080/api/foods/custom \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Create a custom food:**
```bash
curl -X POST http://localhost:8080/api/foods \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Protein Shake",
    "image": "https://example.com/shake.jpg",
    "mealType": "SNACKS",
    "calories": 200
  }'
```

**Update a custom food:**
```bash
curl -X PUT http://localhost:8080/api/foods/10 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Protein Shake",
    "mealType": "BREAKFAST",
    "calories": 250
  }'
```

**Delete a custom food:**
```bash
curl -X DELETE http://localhost:8080/api/foods/10 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## MealType Enum Values

| Value | Description |
|-------|-------------|
| BREAKFAST | Morning meals |
| LUNCH | Midday meals |
| SNACKS | Between-meal snacks |
| DINNER | Evening meals |
| OTHER | Miscellaneous |

---

## Business Rules

1. **System Foods**: Foods with `user_id = NULL` are system/default foods visible to all users
2. **Custom Foods**: Foods with a specific `user_id` are only visible to that user
3. **Ownership**: Users can only update/delete their own custom foods
4. **Protection**: System foods cannot be modified or deleted by any user
5. **Visibility**: When listing foods, users see all system foods + their own custom foods

---

## Dependencies Used

| Dependency | Purpose |
|------------|---------|
| `spring-boot-starter-web` | REST API endpoints |
| `spring-boot-starter-security` | Authentication & authorization |
| `spring-boot-starter-validation` | Request validation |
| `spring-boot-starter-data-jpa` | Database operations |
| `lombok` | Boilerplate reduction |

---

## Next Steps

The following features are planned for implementation:

1. **MealEntryController/Service** - Meal logging
2. **WeightEntryController/Service** - Weight tracking
3. **DashboardController/Service** - Daily summary
4. **Frontend integration** - Connect React components to these endpoints
