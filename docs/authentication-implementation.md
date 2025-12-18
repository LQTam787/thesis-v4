# Authentication Implementation Documentation

**Created**: December 18, 2025  
**Status**: Completed and Verified

---

## Table of Contents

1. [Overview](#overview)
2. [File Structure](#file-structure)
3. [API Endpoints](#api-endpoints)
4. [DTOs (Data Transfer Objects)](#dtos-data-transfer-objects)
5. [UserService](#userservice)
6. [AuthController](#authcontroller)
7. [Testing the API](#testing-the-api)

---

## Overview

This document describes the authentication system implementation for the Calorie Tracker application. The system provides:

- **User Registration** with automatic BMI and daily calorie allowance calculation
- **User Login** with JWT token generation
- **Password Security** using BCrypt hashing
- **Input Validation** using Jakarta Bean Validation

---

## File Structure

```
backend/src/main/java/com/calorietracker/
├── controller/
│   └── AuthController.java          # REST endpoints for auth
├── dto/
│   ├── request/
│   │   ├── UserRegistrationRequest.java
│   │   └── LoginRequest.java
│   └── response/
│       └── AuthResponse.java
└── service/
    └── UserService.java              # Business logic for user operations
```

---

## API Endpoints

### POST `/api/auth/register`

Registers a new user with personal metrics and calculates BMI and daily calorie allowance.

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "dob": "1990-05-15",
  "weight": 75.5,
  "height": 175.0,
  "activityLevel": "MODERATELY_ACTIVE",
  "goal": 70.0,
  "goalType": "LOSE",
  "weeklyGoal": 0.5
}
```

**Success Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "bmi": 24.65,
  "allowedDailyIntake": 1925,
  "message": "Registration successful"
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "Email already registered"
}
```

---

### POST `/api/auth/login`

Authenticates a user and returns a JWT token.

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

**Success Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "bmi": 24.65,
  "allowedDailyIntake": 1925,
  "message": "Login successful"
}
```

**Error Response (401 Unauthorized):**
```json
{
  "message": "Invalid email or password"
}
```

---

## DTOs (Data Transfer Objects)

### UserRegistrationRequest.java

```java
package com.calorietracker.dto.request;

import com.calorietracker.model.ActivityLevel;
import com.calorietracker.model.GoalType;
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
```

**Validation Rules:**

| Field | Type | Validation |
|-------|------|------------|
| name | String | Required, 2-100 characters |
| email | String | Required, valid email format |
| password | String | Required, min 6 characters |
| dob | LocalDate | Required, must be in the past |
| weight | BigDecimal | Required, 20-500 kg |
| height | BigDecimal | Required, 50-300 cm |
| activityLevel | ActivityLevel | Required (SEDENTARY, LIGHTLY_ACTIVE, MODERATELY_ACTIVE, VERY_ACTIVE) |
| goal | BigDecimal | Optional, 20-500 kg |
| goalType | GoalType | Required (LOSE, MAINTAIN, GAIN) |
| weeklyGoal | BigDecimal | Required, 0.1-1.0 kg |

---

### LoginRequest.java

```java
package com.calorietracker.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
```

---

### AuthResponse.java

```java
package com.calorietracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private Long userId;
    private String name;
    private String email;
    private BigDecimal bmi;
    private Integer allowedDailyIntake;
    private String message;
}
```

**Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| token | String | JWT authentication token |
| type | String | Token type (always "Bearer") |
| userId | Long | User's database ID |
| name | String | User's display name |
| email | String | User's email address |
| bmi | BigDecimal | Calculated Body Mass Index |
| allowedDailyIntake | Integer | Calculated daily calorie allowance |
| message | String | Status message |

---

## UserService

```java
package com.calorietracker.service;

import com.calorietracker.dto.request.UserRegistrationRequest;
import com.calorietracker.exception.BadRequestException;
import com.calorietracker.exception.ResourceNotFoundException;
import com.calorietracker.model.User;
import com.calorietracker.repository.UserRepository;
import com.calorietracker.util.CalorieCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(UserRegistrationRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        // Calculate age from DOB
        int age = LocalDate.now().getYear() - request.getDob().getYear();

        // Calculate BMI
        BigDecimal bmi = CalorieCalculator.calculateBMI(
                request.getWeight(),
                request.getHeight()
        );

        // Calculate allowed daily intake
        int allowedDailyIntake = CalorieCalculator.calculateAllowedDailyIntake(
                request.getWeight(),
                request.getHeight(),
                age,
                request.getActivityLevel(),
                request.getWeeklyGoal(),
                request.getGoalType()
        );

        // Build and save user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .dob(request.getDob())
                .weight(request.getWeight())
                .height(request.getHeight())
                .activityLevel(request.getActivityLevel())
                .goal(request.getGoal())
                .goalType(request.getGoalType())
                .weeklyGoal(request.getWeeklyGoal())
                .bmi(bmi)
                .allowedDailyIntake(allowedDailyIntake)
                .build();

        return userRepository.save(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    @Transactional
    public User updateUserWeight(Long userId, BigDecimal newWeight) {
        User user = getUserById(userId);

        // Update weight
        user.setWeight(newWeight);

        // Recalculate BMI
        BigDecimal bmi = CalorieCalculator.calculateBMI(newWeight, user.getHeight());
        user.setBmi(bmi);

        // Recalculate allowed daily intake
        int age = user.getAge();
        int allowedDailyIntake = CalorieCalculator.calculateAllowedDailyIntake(
                newWeight,
                user.getHeight(),
                age,
                user.getActivityLevel(),
                user.getWeeklyGoal(),
                user.getGoalType()
        );
        user.setAllowedDailyIntake(allowedDailyIntake);

        return userRepository.save(user);
    }
}
```

**Key Features:**

1. **Email Uniqueness Check**: Prevents duplicate registrations
2. **Password Encryption**: Uses BCrypt via `PasswordEncoder`
3. **Automatic Calculations**: BMI and daily calorie allowance computed on registration
4. **Weight Update Support**: Recalculates metrics when weight changes

---

## AuthController

```java
package com.calorietracker.controller;

import com.calorietracker.dto.request.LoginRequest;
import com.calorietracker.dto.request.UserRegistrationRequest;
import com.calorietracker.dto.response.AuthResponse;
import com.calorietracker.model.User;
import com.calorietracker.service.JwtService;
import com.calorietracker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody UserRegistrationRequest request) {
        User user = userService.registerUser(request);

        // Generate JWT token for the newly registered user
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .bmi(user.getBmi())
                .allowedDailyIntake(user.getAllowedDailyIntake())
                .message("Registration successful")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder()
                            .message("Invalid email or password")
                            .build());
        }

        User user = userService.getUserByEmail(request.getEmail());
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtService.generateToken(userDetails);

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .bmi(user.getBmi())
                .allowedDailyIntake(user.getAllowedDailyIntake())
                .message("Login successful")
                .build();

        return ResponseEntity.ok(response);
    }
}
```

**Key Features:**

1. **Validation**: Uses `@Valid` annotation for automatic request validation
2. **JWT Generation**: Creates token immediately after successful registration/login
3. **Error Handling**: Returns appropriate HTTP status codes (201, 200, 401)
4. **Stateless Authentication**: No server-side session storage

---

## Testing the API

### Using cURL

**Register a new user:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "password123",
    "dob": "1990-05-15",
    "weight": 75.5,
    "height": 175.0,
    "activityLevel": "MODERATELY_ACTIVE",
    "goal": 70.0,
    "goalType": "LOSE",
    "weeklyGoal": 0.5
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

### Using the JWT Token

After login, include the token in the `Authorization` header for protected endpoints:

```bash
curl -X GET http://localhost:8080/api/dashboard/today \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## Calorie Calculation Formulas

The system uses the following formulas (implemented in `CalorieCalculator.java`):

### BMI (Body Mass Index)
```
BMI = weight(kg) / (height(m))²
```

### BMR (Basal Metabolic Rate) - Mifflin-St Jeor Equation
```
BMR = (10 × weight) + (6.25 × height) - (5 × age) + 5
```

### TDEE (Total Daily Energy Expenditure)
```
TDEE = BMR × Activity Multiplier

Activity Multipliers:
- SEDENTARY: 1.2
- LIGHTLY_ACTIVE: 1.375
- MODERATELY_ACTIVE: 1.55
- VERY_ACTIVE: 1.725
```

### Daily Calorie Allowance
```
Adjustment = weeklyGoal(kg) × 1100 calories

LOSE: TDEE - Adjustment
GAIN: TDEE + Adjustment
MAINTAIN: TDEE
```

---

## Dependencies Used

| Dependency | Purpose |
|------------|---------|
| `spring-boot-starter-web` | REST API endpoints |
| `spring-boot-starter-security` | Authentication & authorization |
| `spring-boot-starter-validation` | Request validation |
| `spring-boot-starter-data-jpa` | Database operations |
| `jjwt-api`, `jjwt-impl`, `jjwt-jackson` | JWT token handling |
| `lombok` | Boilerplate reduction |
| `mysql-connector-j` | MySQL database driver |

---

## Next Steps

The following features are planned for implementation:

1. **FoodController/Service** - Food CRUD operations
2. **MealEntryController/Service** - Meal logging
3. **WeightEntryController/Service** - Weight tracking
4. **DashboardController/Service** - Daily summary
5. **Frontend integration** - Connect React components to these endpoints
