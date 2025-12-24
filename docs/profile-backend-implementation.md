# Profile Backend Implementation Documentation

**Created**: December 19, 2025  
**Updated**: December 24, 2025  
**Status**: Completed

---

## Overview

This document describes the backend implementation for the User Profile feature in the Calorie Tracker application. The profile endpoints allow users to view and update their profile information including personal details, weight goals, and BMI data.

---

## File Structure

```
backend/src/main/java/com/calorietracker/
├── controller/
│   └── UserController.java          # REST controller for user endpoints
├── dto/
│   ├── request/
│   │   └── UpdateProfileRequest.java # DTO for profile update request
│   └── response/
│       └── UserProfileResponse.java # DTO for profile response
├── model/
│   └── User.java                    # User entity (existing)
└── service/
    └── UserService.java             # User service with updateProfile method
```

---

## API Endpoints

### GET `/api/users/profile`

Returns the authenticated user's profile information.

**Authentication**: Required (JWT Bearer token)

**Request Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "userId": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "dob": "1990-05-15",
  "sex": "MALE",
  "height": 175.00,
  "activityLevel": "MODERATELY_ACTIVE",
  "weight": 75.50,
  "goal": 70.00,
  "goalType": "LOSE",
  "weeklyGoal": 0.50,
  "bmi": 24.65,
  "allowedDailyIntake": 1925
}
```

---

### PUT `/api/users/profile`

Updates the authenticated user's profile information.

**Authentication**: Required (JWT Bearer token)

**Request Headers:**
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "John Doe",
  "sex": "MALE",
  "dob": "1990-05-15",
  "height": 175.00,
  "weight": 75.50,
  "activityLevel": "MODERATELY_ACTIVE",
  "goal": 70.00,
  "weeklyGoal": 0.50
}
```

**Request Field Descriptions:**

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| `name` | String | Yes | 2-100 characters | User's display name |
| `sex` | String | Yes | MALE or FEMALE | User's sex |
| `dob` | String | Yes | Past date (YYYY-MM-DD) | Date of birth |
| `height` | Number | Yes | 50-300 | Height in centimeters |
| `weight` | Number | Yes | 20-500 | Current weight in kilograms |
| `activityLevel` | String | Yes | Valid enum value | Activity level |
| `goal` | Number | Yes | 20-500 | Target weight in kilograms |
| `weeklyGoal` | Number | Yes | 0.1-1.0 | Weekly weight change goal in kg |

**Automatic Calculations:**
- `goalType` is derived by comparing `goal` with `weight`:
  - `goal < weight` → `LOSE`
  - `goal > weight` → `GAIN`
  - `goal == weight` → `MAINTAIN`
- `bmi` is recalculated from weight and height
- `allowedDailyIntake` is recalculated based on all updated parameters

**Response (200 OK):**
```json
{
  "userId": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "dob": "1990-05-15",
  "sex": "MALE",
  "height": 175.00,
  "activityLevel": "MODERATELY_ACTIVE",
  "weight": 75.50,
  "goal": 70.00,
  "goalType": "LOSE",
  "weeklyGoal": 0.50,
  "bmi": 24.65,
  "allowedDailyIntake": 1925
}
```

---

## UpdateProfileRequest DTO

**File**: `src/main/java/com/calorietracker/dto/request/UpdateProfileRequest.java`

```java
package com.calorietracker.dto.request;

import com.calorietracker.model.ActivityLevel;
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
public class UpdateProfileRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotNull(message = "Sex is required")
    private Sex sex;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dob;

    @NotNull(message = "Height is required")
    @DecimalMin(value = "50.0", message = "Height must be at least 50 cm")
    @DecimalMax(value = "300.0", message = "Height must be less than 300 cm")
    private BigDecimal height;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "20.0", message = "Weight must be at least 20 kg")
    @DecimalMax(value = "500.0", message = "Weight must be less than 500 kg")
    private BigDecimal weight;

    @NotNull(message = "Activity level is required")
    private ActivityLevel activityLevel;

    @NotNull(message = "Goal is required")
    @DecimalMin(value = "20.0", message = "Goal weight must be at least 20 kg")
    @DecimalMax(value = "500.0", message = "Goal weight must be less than 500 kg")
    private BigDecimal goal;

    @NotNull(message = "Weekly goal is required")
    @DecimalMin(value = "0.1", message = "Weekly goal must be at least 0.1 kg")
    @DecimalMax(value = "1.0", message = "Weekly goal must be at most 1.0 kg")
    private BigDecimal weeklyGoal;
}
```

---

## UserProfileResponse DTO

**File**: `src/main/java/com/calorietracker/dto/response/UserProfileResponse.java`

```java
package com.calorietracker.dto.response;

import com.calorietracker.model.ActivityLevel;
import com.calorietracker.model.GoalType;
import com.calorietracker.model.Sex;
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
public class UserProfileResponse {

    private Long userId;
    private String name;
    private String email;
    private LocalDate dob;
    private Sex sex;
    private BigDecimal height;
    private ActivityLevel activityLevel;
    private BigDecimal weight;
    private BigDecimal goal;
    private GoalType goalType;
    private BigDecimal weeklyGoal;
    private BigDecimal bmi;
    private Integer allowedDailyIntake;
}
```

**Field Descriptions:**

| Field | Type | Description |
|-------|------|-------------|
| `userId` | Long | User's database ID |
| `name` | String | User's display name |
| `email` | String | User's email address |
| `dob` | LocalDate | Date of birth (YYYY-MM-DD) |
| `sex` | Sex (enum) | MALE or FEMALE |
| `height` | BigDecimal | Height in centimeters |
| `activityLevel` | ActivityLevel (enum) | SEDENTARY, LIGHTLY_ACTIVE, MODERATELY_ACTIVE, VERY_ACTIVE |
| `weight` | BigDecimal | Current weight in kilograms |
| `goal` | BigDecimal | Target weight in kilograms |
| `goalType` | GoalType (enum) | LOSE, MAINTAIN, or GAIN |
| `weeklyGoal` | BigDecimal | Weekly weight change goal in kg/week (0.1-1.0) |
| `bmi` | BigDecimal | Calculated Body Mass Index |
| `allowedDailyIntake` | Integer | Calculated daily calorie allowance |

---

## UserController

**File**: `src/main/java/com/calorietracker/controller/UserController.java`

```java
package com.calorietracker.controller;

import com.calorietracker.dto.request.UpdateProfileRequest;
import com.calorietracker.dto.response.UserProfileResponse;
import com.calorietracker.model.User;
import com.calorietracker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());

        UserProfileResponse response = UserProfileResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .dob(user.getDob())
                .sex(user.getSex())
                .height(user.getHeight())
                .activityLevel(user.getActivityLevel())
                .weight(user.getWeight())
                .goal(user.getGoal())
                .goalType(user.getGoalType())
                .weeklyGoal(user.getWeeklyGoal())
                .bmi(user.getBmi())
                .allowedDailyIntake(user.getAllowedDailyIntake())
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        User updatedUser = userService.updateProfile(currentUser.getId(), request);

        UserProfileResponse response = UserProfileResponse.builder()
                .userId(updatedUser.getId())
                .name(updatedUser.getName())
                .email(updatedUser.getEmail())
                .dob(updatedUser.getDob())
                .sex(updatedUser.getSex())
                .height(updatedUser.getHeight())
                .activityLevel(updatedUser.getActivityLevel())
                .weight(updatedUser.getWeight())
                .goal(updatedUser.getGoal())
                .goalType(updatedUser.getGoalType())
                .weeklyGoal(updatedUser.getWeeklyGoal())
                .bmi(updatedUser.getBmi())
                .allowedDailyIntake(updatedUser.getAllowedDailyIntake())
                .build();

        return ResponseEntity.ok(response);
    }
}
```

**Key Implementation Details:**

1. **Authentication**: Uses `@AuthenticationPrincipal` to get the authenticated user's details from the JWT token
2. **User Lookup**: Retrieves user by email using `UserService.getUserByEmail()`
3. **Response Building**: Uses Lombok's `@Builder` pattern to construct the response DTO
4. **All Fields Mapped**: Maps all relevant user fields from the User entity to the response DTO
5. **Validation**: Uses `@Valid` annotation to trigger request body validation
6. **Update Flow**: PUT endpoint calls `UserService.updateProfile()` which handles goalType derivation and recalculations

---

## UserService - updateProfile Method

**File**: `src/main/java/com/calorietracker/service/UserService.java`

```java
@Transactional
public User updateProfile(Long userId, UpdateProfileRequest request) {
    User user = getUserById(userId);

    // Update basic fields
    user.setName(request.getName());
    user.setSex(request.getSex());
    user.setDob(request.getDob());
    user.setHeight(request.getHeight());
    user.setWeight(request.getWeight());
    user.setActivityLevel(request.getActivityLevel());
    user.setGoal(request.getGoal());
    user.setWeeklyGoal(request.getWeeklyGoal());

    // Derive goalType from comparing goal with new weight
    GoalType goalType = deriveGoalType(request.getWeight(), request.getGoal());
    user.setGoalType(goalType);

    // Recalculate BMI
    BigDecimal bmi = CalorieCalculator.calculateBMI(request.getWeight(), request.getHeight());
    user.setBmi(bmi);

    // Recalculate allowed daily intake
    int age = user.getAge();
    int allowedDailyIntake = CalorieCalculator.calculateAllowedDailyIntake(
            request.getWeight(),
            request.getHeight(),
            age,
            request.getSex(),
            request.getActivityLevel(),
            request.getWeeklyGoal(),
            goalType
    );
    user.setAllowedDailyIntake(allowedDailyIntake);

    return userRepository.save(user);
}

private GoalType deriveGoalType(BigDecimal currentWeight, BigDecimal goalWeight) {
    int comparison = goalWeight.compareTo(currentWeight);
    if (comparison < 0) {
        return GoalType.LOSE;
    } else if (comparison > 0) {
        return GoalType.GAIN;
    } else {
        return GoalType.MAINTAIN;
    }
}
```

**Key Logic:**
1. Updates all editable fields from the request (including weight)
2. Derives `goalType` by comparing goal weight with new weight
3. Recalculates BMI using new weight and height
4. Recalculates daily calorie allowance using all updated parameters

---

## Dependencies

The controller depends on:

| Dependency | Purpose |
|------------|---------|
| `UserService` | Retrieves and updates user data |
| `UpdateProfileRequest` | DTO for profile update request |
| `UserProfileResponse` | DTO for API response |
| `User` | Entity model with all user fields |

---

## Enum Values

### Sex
- `MALE`
- `FEMALE`

### ActivityLevel
- `SEDENTARY` - Little to no exercise
- `LIGHTLY_ACTIVE` - 1-3 days/week
- `MODERATELY_ACTIVE` - 3-5 days/week
- `VERY_ACTIVE` - 6-7 days/week

### GoalType
- `LOSE` - Lose weight
- `MAINTAIN` - Maintain current weight
- `GAIN` - Gain weight

---

## Error Handling

| Error | HTTP Status | Cause |
|-------|-------------|-------|
| 400 Bad Request | Validation failed (invalid field values) |
| 401 Unauthorized | Missing or invalid JWT token |
| 404 Not Found | User not found (should not occur for authenticated users) |

---

## Related Files

- `User.java` - User entity model
- `UserService.java` - Service with `getUserByEmail()` and `updateProfile()` methods
- `UserRepository.java` - JPA repository for User entity
- `CalorieCalculator.java` - Utility for BMI and calorie calculations
- `JwtService.java` - JWT token validation
- `SecurityConfig.java` - Security configuration (endpoint requires authentication)
