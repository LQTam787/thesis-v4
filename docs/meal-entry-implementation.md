# Meal Entry Feature Implementation Documentation

**Created**: December 18, 2025  
**Status**: Completed and Verified

---

## Table of Contents

1. [Overview](#overview)
2. [File Structure](#file-structure)
3. [API Endpoints](#api-endpoints)
4. [DTOs (Data Transfer Objects)](#dtos-data-transfer-objects)
5. [MealEntryService](#mealentryservice)
6. [MealEntryController](#mealentrycontroller)
7. [Testing the API](#testing-the-api)

---

## Overview

This document describes the Meal Entry management feature implementation for the Calorie Tracker application. The system provides:

- **Meal Logging** - Log food consumption with date and time
- **Daily Tracking** - View today's meals and total calories
- **Date Filtering** - Get meals for specific dates or date ranges
- **Calorie Summation** - Calculate total calories consumed per day
- **Access Control** - Users can only view/delete their own meal entries

---

## File Structure

```
backend/src/main/java/com/calorietracker/
├── controller/
│   └── MealEntryController.java      # REST endpoints for meal entry operations
├── dto/
│   ├── request/
│   │   └── MealEntryRequest.java     # Request DTO for creating meal entries
│   └── response/
│       └── MealEntryResponse.java    # Response DTO for meal entry data
├── model/
│   └── MealEntry.java                # JPA entity (existing)
├── repository/
│   └── MealEntryRepository.java      # Data access (existing)
└── service/
    └── MealEntryService.java         # Business logic for meal entry operations
```

---

## API Endpoints

All endpoints require JWT authentication via `Authorization: Bearer <token>` header.

### POST `/api/meal-entries`

Create a new meal entry (log a meal).

**Request Body:**
```json
{
  "foodId": 1,
  "entryDate": "2025-12-18",
  "entryTime": "08:30:00"
}
```

**Success Response (201 Created):**
```json
{
  "id": 1,
  "entryDate": "2025-12-18",
  "entryTime": "08:30:00",
  "createdAt": "2025-12-18T08:30:00",
  "foodId": 1,
  "foodName": "Oatmeal",
  "foodImage": null,
  "mealType": "BREAKFAST",
  "calories": 150
}
```

**Error Responses:**
- `400 Bad Request` - Validation errors or "You don't have access to this food"
- `404 Not Found` - "Food not found with id: {id}"

---

### GET `/api/meal-entries/today`

Get all meal entries for today.

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "entryDate": "2025-12-18",
    "entryTime": "08:30:00",
    "createdAt": "2025-12-18T08:30:00",
    "foodId": 1,
    "foodName": "Oatmeal",
    "foodImage": null,
    "mealType": "BREAKFAST",
    "calories": 150
  },
  {
    "id": 2,
    "entryDate": "2025-12-18",
    "entryTime": "12:00:00",
    "createdAt": "2025-12-18T12:00:00",
    "foodId": 5,
    "foodName": "Grilled Chicken Salad",
    "foodImage": "https://example.com/salad.jpg",
    "mealType": "LUNCH",
    "calories": 350
  }
]
```

---

### GET `/api/meal-entries/date/{date}`

Get meal entries for a specific date.

**Path Parameters:**
- `date` - Date in ISO format (YYYY-MM-DD)

**Example:** `GET /api/meal-entries/date/2025-12-17`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "entryDate": "2025-12-17",
    "entryTime": "09:00:00",
    "createdAt": "2025-12-17T09:00:00",
    "foodId": 2,
    "foodName": "Scrambled Eggs",
    "foodImage": null,
    "mealType": "BREAKFAST",
    "calories": 200
  }
]
```

---

### GET `/api/meal-entries/range`

Get meal entries for a date range.

**Query Parameters:**
- `startDate` - Start date in ISO format (YYYY-MM-DD)
- `endDate` - End date in ISO format (YYYY-MM-DD)

**Example:** `GET /api/meal-entries/range?startDate=2025-12-01&endDate=2025-12-18`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "entryDate": "2025-12-01",
    "entryTime": "08:00:00",
    "createdAt": "2025-12-01T08:00:00",
    "foodId": 1,
    "foodName": "Oatmeal",
    "foodImage": null,
    "mealType": "BREAKFAST",
    "calories": 150
  }
]
```

---

### GET `/api/meal-entries/{id}`

Get a single meal entry by ID.

**Path Parameters:**
- `id` - Meal entry ID

**Response (200 OK):**
```json
{
  "id": 1,
  "entryDate": "2025-12-18",
  "entryTime": "08:30:00",
  "createdAt": "2025-12-18T08:30:00",
  "foodId": 1,
  "foodName": "Oatmeal",
  "foodImage": null,
  "mealType": "BREAKFAST",
  "calories": 150
}
```

**Error Response (404 Not Found):**
```json
{
  "message": "Meal entry not found with id: 999"
}
```

---

### DELETE `/api/meal-entries/{id}`

Delete a meal entry (owner only).

**Path Parameters:**
- `id` - Meal entry ID

**Success Response (204 No Content):**
No body returned.

**Error Responses:**
- `400 Bad Request` - "You can only delete your own meal entries"
- `404 Not Found` - "Meal entry not found with id: {id}"

---

### GET `/api/meal-entries/today/calories`

Get total calories consumed today.

**Response (200 OK):**
```json
500
```

---

### GET `/api/meal-entries/date/{date}/calories`

Get total calories consumed for a specific date.

**Path Parameters:**
- `date` - Date in ISO format (YYYY-MM-DD)

**Example:** `GET /api/meal-entries/date/2025-12-17/calories`

**Response (200 OK):**
```json
1850
```

---

## DTOs (Data Transfer Objects)

### MealEntryRequest.java

```java
package com.calorietracker.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealEntryRequest {

    @NotNull(message = "Food ID is required")
    private Long foodId;

    @NotNull(message = "Entry date is required")
    private LocalDate entryDate;

    @NotNull(message = "Entry time is required")
    private LocalTime entryTime;
}
```

**Validation Rules:**

| Field | Type | Validation |
|-------|------|------------|
| foodId | Long | Required |
| entryDate | LocalDate | Required |
| entryTime | LocalTime | Required |

---

### MealEntryResponse.java

```java
package com.calorietracker.dto.response;

import com.calorietracker.model.MealEntry;
import com.calorietracker.model.MealType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealEntryResponse {

    private Long id;
    private LocalDate entryDate;
    private LocalTime entryTime;
    private LocalDateTime createdAt;
    
    // Food details (embedded for convenience)
    private Long foodId;
    private String foodName;
    private String foodImage;
    private MealType mealType;
    private Integer calories;

    public static MealEntryResponse fromEntity(MealEntry mealEntry) {
        return MealEntryResponse.builder()
                .id(mealEntry.getId())
                .entryDate(mealEntry.getEntryDate())
                .entryTime(mealEntry.getEntryTime())
                .createdAt(mealEntry.getCreatedAt())
                .foodId(mealEntry.getFood().getId())
                .foodName(mealEntry.getFood().getName())
                .foodImage(mealEntry.getFood().getImage())
                .mealType(mealEntry.getFood().getMealType())
                .calories(mealEntry.getFood().getCalories())
                .build();
    }
}
```

**Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Meal entry's database ID |
| entryDate | LocalDate | Date when food was consumed |
| entryTime | LocalTime | Time when food was consumed |
| createdAt | LocalDateTime | Record creation timestamp |
| foodId | Long | Associated food's ID |
| foodName | String | Food name |
| foodImage | String | Food image URL (nullable) |
| mealType | MealType | Meal category (BREAKFAST, LUNCH, etc.) |
| calories | Integer | Calorie count of the food |

---

## MealEntryService

```java
package com.calorietracker.service;

import com.calorietracker.dto.request.MealEntryRequest;
import com.calorietracker.dto.response.MealEntryResponse;
import com.calorietracker.exception.BadRequestException;
import com.calorietracker.exception.ResourceNotFoundException;
import com.calorietracker.model.Food;
import com.calorietracker.model.MealEntry;
import com.calorietracker.model.User;
import com.calorietracker.repository.FoodRepository;
import com.calorietracker.repository.MealEntryRepository;
import com.calorietracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MealEntryService {

    private final MealEntryRepository mealEntryRepository;
    private final FoodRepository foodRepository;
    private final UserRepository userRepository;

    @Transactional
    public MealEntryResponse createMealEntry(MealEntryRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Food food = foodRepository.findById(request.getFoodId())
                .orElseThrow(() -> new ResourceNotFoundException("Food not found with id: " + request.getFoodId()));

        // Verify user has access to this food
        if (food.getUser() != null && !food.getUser().getId().equals(userId)) {
            throw new BadRequestException("You don't have access to this food");
        }

        MealEntry mealEntry = MealEntry.builder()
                .user(user)
                .food(food)
                .entryDate(request.getEntryDate())
                .entryTime(request.getEntryTime())
                .build();

        MealEntry savedEntry = mealEntryRepository.save(mealEntry);
        return MealEntryResponse.fromEntity(savedEntry);
    }

    public List<MealEntryResponse> getMealEntriesByDate(Long userId, LocalDate date) {
        return mealEntryRepository.findByUserIdAndEntryDateOrderByEntryTimeAsc(userId, date)
                .stream()
                .map(MealEntryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<MealEntryResponse> getTodayMealEntries(Long userId) {
        return getMealEntriesByDate(userId, LocalDate.now());
    }

    public List<MealEntryResponse> getMealEntriesByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return mealEntryRepository.findByUserIdAndEntryDateBetween(userId, startDate, endDate)
                .stream()
                .map(MealEntryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public MealEntryResponse getMealEntryById(Long entryId, Long userId) {
        MealEntry mealEntry = mealEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Meal entry not found with id: " + entryId));

        if (!mealEntry.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Meal entry not found with id: " + entryId);
        }

        return MealEntryResponse.fromEntity(mealEntry);
    }

    @Transactional
    public void deleteMealEntry(Long entryId, Long userId) {
        MealEntry mealEntry = mealEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Meal entry not found with id: " + entryId));

        if (!mealEntry.getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only delete your own meal entries");
        }

        mealEntryRepository.delete(mealEntry);
    }

    public Integer getTotalCaloriesForDate(Long userId, LocalDate date) {
        return mealEntryRepository.sumCaloriesForUserAndDate(userId, date);
    }

    public Integer getTodayTotalCalories(Long userId) {
        return getTotalCaloriesForDate(userId, LocalDate.now());
    }
}
```

**Key Features:**

1. **Food Access Validation**: Verifies user can access the food (system or own custom food)
2. **Ownership Validation**: Users can only view/delete their own entries
3. **Calorie Aggregation**: Calculates total calories using repository query
4. **Date Filtering**: Supports single date, today, and date range queries

---

## MealEntryController

```java
package com.calorietracker.controller;

import com.calorietracker.dto.request.MealEntryRequest;
import com.calorietracker.dto.response.MealEntryResponse;
import com.calorietracker.model.User;
import com.calorietracker.repository.UserRepository;
import com.calorietracker.service.MealEntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/meal-entries")
@RequiredArgsConstructor
public class MealEntryController {

    private final MealEntryService mealEntryService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<MealEntryResponse> createMealEntry(
            @Valid @RequestBody MealEntryRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        MealEntryResponse mealEntry = mealEntryService.createMealEntry(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(mealEntry);
    }

    @GetMapping("/today")
    public ResponseEntity<List<MealEntryResponse>> getTodayMealEntries(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        List<MealEntryResponse> entries = mealEntryService.getTodayMealEntries(userId);
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<MealEntryResponse>> getMealEntriesByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        List<MealEntryResponse> entries = mealEntryService.getMealEntriesByDate(userId, date);
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/range")
    public ResponseEntity<List<MealEntryResponse>> getMealEntriesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        List<MealEntryResponse> entries = mealEntryService.getMealEntriesByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MealEntryResponse> getMealEntryById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        MealEntryResponse entry = mealEntryService.getMealEntryById(id, userId);
        return ResponseEntity.ok(entry);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMealEntry(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        mealEntryService.deleteMealEntry(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/today/calories")
    public ResponseEntity<Integer> getTodayTotalCalories(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        Integer totalCalories = mealEntryService.getTodayTotalCalories(userId);
        return ResponseEntity.ok(totalCalories);
    }

    @GetMapping("/date/{date}/calories")
    public ResponseEntity<Integer> getTotalCaloriesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        Integer totalCalories = mealEntryService.getTotalCaloriesForDate(userId, date);
        return ResponseEntity.ok(totalCalories);
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
2. **Request Validation**: Uses `@Valid` annotation for automatic validation
3. **Date Parsing**: Uses `@DateTimeFormat` for ISO date format parsing
4. **Proper HTTP Status Codes**: 200 OK, 201 Created, 204 No Content

---

## Testing the API

### Using cURL

**Log a meal (create meal entry):**
```bash
curl -X POST http://localhost:8080/api/meal-entries \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "foodId": 1,
    "entryDate": "2025-12-18",
    "entryTime": "08:30:00"
  }'
```

**Get today's meals:**
```bash
curl -X GET http://localhost:8080/api/meal-entries/today \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Get meals for a specific date:**
```bash
curl -X GET http://localhost:8080/api/meal-entries/date/2025-12-17 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Get meals for a date range:**
```bash
curl -X GET "http://localhost:8080/api/meal-entries/range?startDate=2025-12-01&endDate=2025-12-18" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Get today's total calories:**
```bash
curl -X GET http://localhost:8080/api/meal-entries/today/calories \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Delete a meal entry:**
```bash
curl -X DELETE http://localhost:8080/api/meal-entries/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Business Rules

1. **Food Access**: Users can only log meals with system foods or their own custom foods
2. **Ownership**: Users can only view and delete their own meal entries
3. **Date/Time**: Entry date and time are user-specified (allows backdating)
4. **Calorie Calculation**: Total calories are calculated by summing food calories from entries
5. **Ordering**: Entries are returned ordered by entry time (ascending)

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

1. **WeightEntryController/Service** - Weight tracking
2. **DashboardController/Service** - Daily summary (combines meal entries + user data)
3. **Frontend integration** - Connect React components to these endpoints
