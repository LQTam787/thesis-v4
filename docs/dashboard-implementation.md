# Dashboard Feature Implementation Documentation

**Created**: December 18, 2025  
**Status**: Completed and Verified

---

## Table of Contents

1. [Overview](#overview)
2. [File Structure](#file-structure)
3. [API Endpoints](#api-endpoints)
4. [DTOs (Data Transfer Objects)](#dtos-data-transfer-objects)
5. [DashboardService](#dashboardservice)
6. [DashboardController](#dashboardcontroller)
7. [Testing the API](#testing-the-api)

---

## Overview

This document describes the Dashboard feature implementation for the Calorie Tracker application. The Dashboard provides a daily summary combining:

- **Calorie Tracking** - Daily allowed intake, consumed calories, remaining calories
- **User Information** - Name, goal type, current weight, goal weight
- **Weight Entry** - Today's logged weight (if exists)
- **Meal Entries** - All meals for the day grouped by meal type (Breakfast, Lunch, Dinner, etc.)

---

## File Structure

```
backend/src/main/java/com/calorietracker/
├── controller/
│   └── DashboardController.java      # REST endpoints for dashboard data
├── dto/
│   └── response/
│       └── DashboardResponse.java    # Response DTO for dashboard data
└── service/
    └── DashboardService.java         # Business logic for aggregating dashboard data
```

---

## API Endpoints

All endpoints require JWT authentication via `Authorization: Bearer <token>` header.

### GET `/api/dashboard`

Get today's dashboard summary for the authenticated user.

**Response (200 OK):**
```json
{
  "date": "2025-12-18",
  "allowedDailyIntake": 2000,
  "consumedCalories": 850,
  "remainingCalories": 1150,
  "userName": "John Doe",
  "goalType": "LOSE",
  "currentWeight": 75.50,
  "goalWeight": 70.00,
  "todayWeight": 75.50,
  "mealsByType": {
    "BREAKFAST": [
      {
        "id": 1,
        "entryDate": "2025-12-18",
        "entryTime": "08:30:00",
        "createdAt": "2025-12-18T08:30:00",
        "foodId": 1,
        "foodName": "Oatmeal",
        "foodImage": null,
        "mealType": "BREAKFAST",
        "calories": 300
      }
    ],
    "LUNCH": [
      {
        "id": 2,
        "entryDate": "2025-12-18",
        "entryTime": "12:30:00",
        "createdAt": "2025-12-18T12:30:00",
        "foodId": 2,
        "foodName": "Grilled Chicken Salad",
        "foodImage": null,
        "mealType": "LUNCH",
        "calories": 550
      }
    ]
  },
  "totalMealsCount": 2
}
```

---

### GET `/api/dashboard/date/{date}`

Get dashboard summary for a specific date.

**Path Parameters:**
- `date` - Date in ISO format (YYYY-MM-DD)

**Example:** `GET /api/dashboard/date/2025-12-15`

**Response (200 OK):**
```json
{
  "date": "2025-12-15",
  "allowedDailyIntake": 2000,
  "consumedCalories": 1800,
  "remainingCalories": 200,
  "userName": "John Doe",
  "goalType": "LOSE",
  "currentWeight": 75.50,
  "goalWeight": 70.00,
  "todayWeight": 76.00,
  "mealsByType": {
    "BREAKFAST": [...],
    "LUNCH": [...],
    "DINNER": [...]
  },
  "totalMealsCount": 5
}
```

---

## DTOs (Data Transfer Objects)

### DashboardResponse.java

```java
package com.calorietracker.dto.response;

import com.calorietracker.model.GoalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private LocalDate date;
    
    // Calorie summary
    private Integer allowedDailyIntake;
    private Integer consumedCalories;
    private Integer remainingCalories;
    
    // User info
    private String userName;
    private GoalType goalType;
    private BigDecimal currentWeight;
    private BigDecimal goalWeight;
    
    // Today's weight entry (if exists)
    private BigDecimal todayWeight;
    
    // Meal entries grouped by meal type
    private Map<String, List<MealEntryResponse>> mealsByType;
    
    // Total meals count for the day
    private Integer totalMealsCount;
}
```

**Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| date | LocalDate | The date for this dashboard data |
| allowedDailyIntake | Integer | User's daily calorie allowance |
| consumedCalories | Integer | Total calories consumed for the day |
| remainingCalories | Integer | Calories remaining (allowed - consumed) |
| userName | String | User's display name |
| goalType | GoalType | User's goal (LOSE, MAINTAIN, GAIN) |
| currentWeight | BigDecimal | User's current weight in kg |
| goalWeight | BigDecimal | User's target weight in kg |
| todayWeight | BigDecimal | Weight logged for this date (null if not logged) |
| mealsByType | Map<String, List<MealEntryResponse>> | Meals grouped by type (BREAKFAST, LUNCH, DINNER, SNACKS, OTHER) |
| totalMealsCount | Integer | Total number of meals logged for the day |

---

## DashboardService

```java
package com.calorietracker.service;

import com.calorietracker.dto.response.DashboardResponse;
import com.calorietracker.dto.response.MealEntryResponse;
import com.calorietracker.exception.ResourceNotFoundException;
import com.calorietracker.model.MealEntry;
import com.calorietracker.model.User;
import com.calorietracker.model.WeightEntry;
import com.calorietracker.repository.MealEntryRepository;
import com.calorietracker.repository.UserRepository;
import com.calorietracker.repository.WeightEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final MealEntryRepository mealEntryRepository;
    private final WeightEntryRepository weightEntryRepository;

    public DashboardResponse getDashboardData(Long userId, LocalDate date) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Get consumed calories for the date
        Integer consumedCalories = mealEntryRepository.sumCaloriesForUserAndDate(userId, date);
        Integer remainingCalories = user.getAllowedDailyIntake() - consumedCalories;

        // Get today's weight entry if exists
        Optional<WeightEntry> todayWeightEntry = weightEntryRepository.findByUserIdAndEntryDate(userId, date);
        BigDecimal todayWeight = todayWeightEntry.map(WeightEntry::getWeight).orElse(null);

        // Get meal entries for the date, grouped by meal type
        List<MealEntry> mealEntries = mealEntryRepository.findByUserIdAndEntryDateOrderByEntryTimeAsc(userId, date);
        
        Map<String, List<MealEntryResponse>> mealsByType = mealEntries.stream()
                .map(MealEntryResponse::fromEntity)
                .collect(Collectors.groupingBy(
                        entry -> entry.getMealType().name(),
                        Collectors.toList()
                ));

        return DashboardResponse.builder()
                .date(date)
                .allowedDailyIntake(user.getAllowedDailyIntake())
                .consumedCalories(consumedCalories)
                .remainingCalories(remainingCalories)
                .userName(user.getName())
                .goalType(user.getGoalType())
                .currentWeight(user.getWeight())
                .goalWeight(user.getGoal())
                .todayWeight(todayWeight)
                .mealsByType(mealsByType)
                .totalMealsCount(mealEntries.size())
                .build();
    }

    public DashboardResponse getTodayDashboard(Long userId) {
        return getDashboardData(userId, LocalDate.now());
    }
}
```

**Key Features:**

1. **Calorie Calculation**: Uses existing `sumCaloriesForUserAndDate` query from MealEntryRepository
2. **Weight Integration**: Fetches weight entry for the specified date if it exists
3. **Meal Grouping**: Groups meal entries by meal type for easy frontend display
4. **Reusable Logic**: `getDashboardData` accepts any date, `getTodayDashboard` is a convenience method

---

## DashboardController

```java
package com.calorietracker.controller;

import com.calorietracker.dto.response.DashboardResponse;
import com.calorietracker.model.User;
import com.calorietracker.repository.UserRepository;
import com.calorietracker.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<DashboardResponse> getTodayDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        DashboardResponse dashboard = dashboardService.getTodayDashboard(userId);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<DashboardResponse> getDashboardByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        DashboardResponse dashboard = dashboardService.getDashboardData(userId, date);
        return ResponseEntity.ok(dashboard);
    }

    private Long getUserIdFromUserDetails(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
```

**Key Features:**

1. **JWT Authentication**: All endpoints require valid JWT token
2. **Date Parsing**: Uses `@DateTimeFormat` for ISO date format parsing
3. **Proper HTTP Status Codes**: Returns 200 OK for successful requests

---

## Testing the API

### Using cURL

**Get today's dashboard:**
```bash
curl -X GET http://localhost:8080/api/dashboard \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Get dashboard for a specific date:**
```bash
curl -X GET http://localhost:8080/api/dashboard/date/2025-12-15 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Business Rules

1. **User-Specific Data**: Dashboard only shows data for the authenticated user
2. **Calorie Calculation**: Remaining calories can be negative if user exceeds daily allowance
3. **Weight Entry**: `todayWeight` is null if no weight was logged for that date
4. **Meal Grouping**: Meals are grouped by their food's meal type (BREAKFAST, LUNCH, DINNER, SNACKS, OTHER)
5. **Ordering**: Meals within each group are ordered by entry time (ascending)

---

## Dependencies Used

| Dependency | Purpose |
|------------|---------|
| `spring-boot-starter-web` | REST API endpoints |
| `spring-boot-starter-security` | Authentication & authorization |
| `spring-boot-starter-data-jpa` | Database operations |
| `lombok` | Boilerplate reduction |

---

## Related Features

- **MealEntry** - Provides meal data for the dashboard
- **WeightEntry** - Provides weight data for the dashboard
- **User** - Provides user profile and calorie allowance data

---

## Next Steps

The following features are planned for implementation:

1. **Frontend Dashboard Component** - Connect React dashboard to these endpoints
2. **Calorie Progress Bar** - Visual representation of daily calorie consumption
3. **Quick Meal Logging** - Add meal entry directly from dashboard
