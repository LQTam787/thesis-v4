# Weight Entry Feature Implementation Documentation

**Created**: December 18, 2025  
**Status**: Completed and Verified

---

## Table of Contents

1. [Overview](#overview)
2. [File Structure](#file-structure)
3. [API Endpoints](#api-endpoints)
4. [DTOs (Data Transfer Objects)](#dtos-data-transfer-objects)
5. [WeightEntryService](#weightentryservice)
6. [WeightEntryController](#weightentrycontroller)
7. [Testing the API](#testing-the-api)

---

## Overview

This document describes the Weight Entry management feature implementation for the Calorie Tracker application. The system provides:

- **Weight Logging** - Record daily weight measurements
- **Weight History** - View all weight entries over time
- **Date Filtering** - Get weight entries for specific dates or date ranges
- **Automatic User Update** - Updates user's current weight and recalculates BMI/calories when latest weight changes
- **Access Control** - Users can only view/delete their own weight entries

---

## File Structure

```
backend/src/main/java/com/calorietracker/
├── controller/
│   └── WeightEntryController.java      # REST endpoints for weight entry operations
├── dto/
│   ├── request/
│   │   └── WeightEntryRequest.java     # Request DTO for creating weight entries
│   └── response/
│       └── WeightEntryResponse.java    # Response DTO for weight entry data
├── model/
│   └── WeightEntry.java                # JPA entity (existing)
├── repository/
│   └── WeightEntryRepository.java      # Data access (existing)
└── service/
    └── WeightEntryService.java         # Business logic for weight entry operations
```

---

## API Endpoints

All endpoints require JWT authentication via `Authorization: Bearer <token>` header.

### POST `/api/weight-entries`

Create or update a weight entry for a specific date. If an entry already exists for the date, it will be updated.

**Request Body:**
```json
{
  "entryDate": "2025-12-18",
  "weight": 75.5
}
```

**Success Response (201 Created):**
```json
{
  "id": 1,
  "entryDate": "2025-12-18",
  "weight": 75.5,
  "createdAt": "2025-12-18T08:30:00"
}
```

**Error Responses:**
- `400 Bad Request` - Validation errors

---

### GET `/api/weight-entries`

Get all weight entries for the authenticated user (ordered by date ascending).

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "entryDate": "2025-12-15",
    "weight": 76.0,
    "createdAt": "2025-12-15T08:00:00"
  },
  {
    "id": 2,
    "entryDate": "2025-12-18",
    "weight": 75.5,
    "createdAt": "2025-12-18T08:30:00"
  }
]
```

---

### GET `/api/weight-entries/latest`

Get the most recent weight entry.

**Response (200 OK):**
```json
{
  "id": 2,
  "entryDate": "2025-12-18",
  "weight": 75.5,
  "createdAt": "2025-12-18T08:30:00"
}
```

**Error Response (404 Not Found):**
```json
{
  "message": "No weight entries found"
}
```

---

### GET `/api/weight-entries/date/{date}`

Get weight entry for a specific date.

**Path Parameters:**
- `date` - Date in ISO format (YYYY-MM-DD)

**Example:** `GET /api/weight-entries/date/2025-12-18`

**Response (200 OK):**
```json
{
  "id": 1,
  "entryDate": "2025-12-18",
  "weight": 75.5,
  "createdAt": "2025-12-18T08:30:00"
}
```

**Error Response (404 Not Found):**
```json
{
  "message": "Weight entry not found for date: 2025-12-18"
}
```

---

### GET `/api/weight-entries/range`

Get weight entries for a date range.

**Query Parameters:**
- `startDate` - Start date in ISO format (YYYY-MM-DD)
- `endDate` - End date in ISO format (YYYY-MM-DD)

**Example:** `GET /api/weight-entries/range?startDate=2025-12-01&endDate=2025-12-18`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "entryDate": "2025-12-01",
    "weight": 77.0,
    "createdAt": "2025-12-01T08:00:00"
  },
  {
    "id": 2,
    "entryDate": "2025-12-15",
    "weight": 76.0,
    "createdAt": "2025-12-15T08:00:00"
  }
]
```

---

### GET `/api/weight-entries/{id}`

Get a single weight entry by ID.

**Path Parameters:**
- `id` - Weight entry ID

**Response (200 OK):**
```json
{
  "id": 1,
  "entryDate": "2025-12-18",
  "weight": 75.5,
  "createdAt": "2025-12-18T08:30:00"
}
```

**Error Response (404 Not Found):**
```json
{
  "message": "Weight entry not found with id: 999"
}
```

---

### DELETE `/api/weight-entries/{id}`

Delete a weight entry (owner only).

**Path Parameters:**
- `id` - Weight entry ID

**Success Response (204 No Content):**
No body returned.

**Error Responses:**
- `400 Bad Request` - "You can only delete your own weight entries"
- `404 Not Found` - "Weight entry not found with id: {id}"

---

## DTOs (Data Transfer Objects)

### WeightEntryRequest.java

```java
package com.calorietracker.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
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
public class WeightEntryRequest {

    @NotNull(message = "Entry date is required")
    private LocalDate entryDate;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "20.0", message = "Weight must be at least 20 kg")
    @DecimalMax(value = "500.0", message = "Weight must be less than 500 kg")
    private BigDecimal weight;
}
```

**Validation Rules:**

| Field | Type | Validation |
|-------|------|------------|
| entryDate | LocalDate | Required |
| weight | BigDecimal | Required, 20-500 kg |

---

### WeightEntryResponse.java

```java
package com.calorietracker.dto.response;

import com.calorietracker.model.WeightEntry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeightEntryResponse {

    private Long id;
    private LocalDate entryDate;
    private BigDecimal weight;
    private LocalDateTime createdAt;

    public static WeightEntryResponse fromEntity(WeightEntry weightEntry) {
        return WeightEntryResponse.builder()
                .id(weightEntry.getId())
                .entryDate(weightEntry.getEntryDate())
                .weight(weightEntry.getWeight())
                .createdAt(weightEntry.getCreatedAt())
                .build();
    }
}
```

**Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| id | Long | Weight entry's database ID |
| entryDate | LocalDate | Date of weight measurement |
| weight | BigDecimal | Weight in kg |
| createdAt | LocalDateTime | Record creation timestamp |

---

## WeightEntryService

```java
package com.calorietracker.service;

import com.calorietracker.dto.request.WeightEntryRequest;
import com.calorietracker.dto.response.WeightEntryResponse;
import com.calorietracker.exception.BadRequestException;
import com.calorietracker.exception.ResourceNotFoundException;
import com.calorietracker.model.User;
import com.calorietracker.model.WeightEntry;
import com.calorietracker.repository.UserRepository;
import com.calorietracker.repository.WeightEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeightEntryService {

    private final WeightEntryRepository weightEntryRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Transactional
    public WeightEntryResponse createOrUpdateWeightEntry(WeightEntryRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check if entry already exists for this date
        Optional<WeightEntry> existingEntry = weightEntryRepository
                .findByUserIdAndEntryDate(userId, request.getEntryDate());

        WeightEntry weightEntry;
        if (existingEntry.isPresent()) {
            // Update existing entry
            weightEntry = existingEntry.get();
            weightEntry.setWeight(request.getWeight());
        } else {
            // Create new entry
            weightEntry = WeightEntry.builder()
                    .user(user)
                    .entryDate(request.getEntryDate())
                    .weight(request.getWeight())
                    .build();
        }

        WeightEntry savedEntry = weightEntryRepository.save(weightEntry);

        // If this is the latest weight entry, update user's current weight
        Optional<WeightEntry> latestEntry = weightEntryRepository.findFirstByUserIdOrderByEntryDateDesc(userId);
        if (latestEntry.isPresent() && latestEntry.get().getId().equals(savedEntry.getId())) {
            userService.updateUserWeight(userId, request.getWeight());
        }

        return WeightEntryResponse.fromEntity(savedEntry);
    }

    public List<WeightEntryResponse> getAllWeightEntries(Long userId) {
        return weightEntryRepository.findByUserIdOrderByEntryDateAsc(userId)
                .stream()
                .map(WeightEntryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public WeightEntryResponse getWeightEntryByDate(Long userId, LocalDate date) {
        WeightEntry weightEntry = weightEntryRepository.findByUserIdAndEntryDate(userId, date)
                .orElseThrow(() -> new ResourceNotFoundException("Weight entry not found for date: " + date));

        return WeightEntryResponse.fromEntity(weightEntry);
    }

    public WeightEntryResponse getLatestWeightEntry(Long userId) {
        WeightEntry weightEntry = weightEntryRepository.findFirstByUserIdOrderByEntryDateDesc(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No weight entries found"));

        return WeightEntryResponse.fromEntity(weightEntry);
    }

    public List<WeightEntryResponse> getWeightEntriesByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return weightEntryRepository.findByUserIdAndEntryDateBetweenOrderByEntryDateAsc(userId, startDate, endDate)
                .stream()
                .map(WeightEntryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public WeightEntryResponse getWeightEntryById(Long entryId, Long userId) {
        WeightEntry weightEntry = weightEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Weight entry not found with id: " + entryId));

        if (!weightEntry.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Weight entry not found with id: " + entryId);
        }

        return WeightEntryResponse.fromEntity(weightEntry);
    }

    @Transactional
    public void deleteWeightEntry(Long entryId, Long userId) {
        WeightEntry weightEntry = weightEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Weight entry not found with id: " + entryId));

        if (!weightEntry.getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only delete your own weight entries");
        }

        weightEntryRepository.delete(weightEntry);

        // If we deleted the latest entry, update user weight to the new latest
        Optional<WeightEntry> newLatest = weightEntryRepository.findFirstByUserIdOrderByEntryDateDesc(userId);
        if (newLatest.isPresent()) {
            userService.updateUserWeight(userId, newLatest.get().getWeight());
        }
    }
}
```

**Key Features:**

1. **Upsert Logic**: Creates new entry or updates existing entry for the same date
2. **Auto User Update**: Updates user's current weight when latest entry changes
3. **Ownership Validation**: Users can only view/delete their own entries
4. **BMI/Calorie Recalculation**: Triggers recalculation via UserService when weight changes

---

## WeightEntryController

```java
package com.calorietracker.controller;

import com.calorietracker.dto.request.WeightEntryRequest;
import com.calorietracker.dto.response.WeightEntryResponse;
import com.calorietracker.model.User;
import com.calorietracker.repository.UserRepository;
import com.calorietracker.service.WeightEntryService;
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
@RequestMapping("/api/weight-entries")
@RequiredArgsConstructor
public class WeightEntryController {

    private final WeightEntryService weightEntryService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<WeightEntryResponse> createOrUpdateWeightEntry(
            @Valid @RequestBody WeightEntryRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        WeightEntryResponse weightEntry = weightEntryService.createOrUpdateWeightEntry(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(weightEntry);
    }

    @GetMapping
    public ResponseEntity<List<WeightEntryResponse>> getAllWeightEntries(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        List<WeightEntryResponse> entries = weightEntryService.getAllWeightEntries(userId);
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/latest")
    public ResponseEntity<WeightEntryResponse> getLatestWeightEntry(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        WeightEntryResponse entry = weightEntryService.getLatestWeightEntry(userId);
        return ResponseEntity.ok(entry);
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<WeightEntryResponse> getWeightEntryByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        WeightEntryResponse entry = weightEntryService.getWeightEntryByDate(userId, date);
        return ResponseEntity.ok(entry);
    }

    @GetMapping("/range")
    public ResponseEntity<List<WeightEntryResponse>> getWeightEntriesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        List<WeightEntryResponse> entries = weightEntryService.getWeightEntriesByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WeightEntryResponse> getWeightEntryById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        WeightEntryResponse entry = weightEntryService.getWeightEntryById(id, userId);
        return ResponseEntity.ok(entry);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWeightEntry(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        weightEntryService.deleteWeightEntry(id, userId);
        return ResponseEntity.noContent().build();
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

**Log a weight entry:**
```bash
curl -X POST http://localhost:8080/api/weight-entries \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "entryDate": "2025-12-18",
    "weight": 75.5
  }'
```

**Get all weight entries:**
```bash
curl -X GET http://localhost:8080/api/weight-entries \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Get latest weight entry:**
```bash
curl -X GET http://localhost:8080/api/weight-entries/latest \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Get weight entry for a specific date:**
```bash
curl -X GET http://localhost:8080/api/weight-entries/date/2025-12-18 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Get weight entries for a date range:**
```bash
curl -X GET "http://localhost:8080/api/weight-entries/range?startDate=2025-12-01&endDate=2025-12-18" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Delete a weight entry:**
```bash
curl -X DELETE http://localhost:8080/api/weight-entries/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Business Rules

1. **One Entry Per Day**: Only one weight entry allowed per date (upsert behavior)
2. **Ownership**: Users can only view and delete their own weight entries
3. **Auto-Update User**: When the latest weight entry changes, user's current weight is updated
4. **BMI Recalculation**: User's BMI and daily calorie allowance are recalculated when weight changes
5. **Ordering**: Entries are returned ordered by entry date (ascending)

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

1. **DashboardController/Service** - Daily summary (combines meal entries + weight + user data)
2. **Frontend integration** - Connect React components to these endpoints
