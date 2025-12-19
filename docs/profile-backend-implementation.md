# Profile Backend Implementation Documentation

**Created**: December 19, 2025  
**Status**: Completed

---

## Overview

This document describes the backend implementation for the User Profile feature in the Calorie Tracker application. The profile endpoint returns comprehensive user information including personal details, weight goals, and BMI data.

---

## File Structure

```
backend/src/main/java/com/calorietracker/
├── controller/
│   └── UserController.java          # REST controller for user endpoints
├── dto/
│   └── response/
│       └── UserProfileResponse.java # DTO for profile response
├── model/
│   └── User.java                    # User entity (existing)
└── service/
    └── UserService.java             # User service (existing)
```

---

## API Endpoint

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

import com.calorietracker.dto.response.UserProfileResponse;
import com.calorietracker.model.User;
import com.calorietracker.service.UserService;
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
}
```

**Key Implementation Details:**

1. **Authentication**: Uses `@AuthenticationPrincipal` to get the authenticated user's details from the JWT token
2. **User Lookup**: Retrieves user by email using `UserService.getUserByEmail()`
3. **Response Building**: Uses Lombok's `@Builder` pattern to construct the response DTO
4. **All Fields Mapped**: Maps all relevant user fields from the User entity to the response DTO

---

## Dependencies

The controller depends on:

| Dependency | Purpose |
|------------|---------|
| `UserService` | Retrieves user data from database |
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
| 401 Unauthorized | Missing or invalid JWT token |
| 404 Not Found | User not found (should not occur for authenticated users) |

---

## Related Files

- `User.java` - User entity model
- `UserService.java` - Service with `getUserByEmail()` method
- `UserRepository.java` - JPA repository for User entity
- `JwtService.java` - JWT token validation
- `SecurityConfig.java` - Security configuration (endpoint requires authentication)
