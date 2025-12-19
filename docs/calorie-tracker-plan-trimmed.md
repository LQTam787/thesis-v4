# Calorie Tracking App - Comprehensive Implementation Guide
**Tech Stack**: React JS (Frontend) + Spring Boot (Backend) + MySQL (Database)  
**Target User**: New coding student with limited expertise in all three technologies  
**Purpose**: This document serves as the complete reference for implementing a calorie tracking web application across multiple chat sessions.

---

## Table of Contents
1. [Project Overview](#project-overview)
2. [System Architecture](#system-architecture)
3. [Database Design](#database-design)
4. [Backend Implementation (Spring Boot)](#backend-implementation-spring-boot)
5. [Frontend Implementation (React)](#frontend-implementation-react)
6. [Business Logic & Calculations](#business-logic--calculations)
7. [Authentication & Security](#authentication--security)
8. [API Specifications](#api-specifications)
9. [Session-by-Session Implementation Plan](#session-by-session-implementation-plan)
10. [Reference Materials](#reference-materials)

---

## Project Overview

### Core Functionality
A web-based calorie tracking application that enables users to:
- Register and authenticate securely
- Set and manage weight goals (lose/maintain/gain)
- Log daily food intake across meal types
- Track weight progress over time
- View historical data and trends
- Manage a personal food database with custom entries

### User Journey
1. User registers with personal metrics (height, weight, age, activity level, goals)
2. System calculates BMI and daily calorie allowance
3. User logs meals throughout the day
4. Dashboard shows real-time progress (consumed vs. allowed calories)
5. User logs weight periodically
6. History page shows past data organized by date and meal type
7. Weight tracking page visualizes progress with graphs

### Key Design Principles
- Beginner-friendly: Code should be clear, well-commented, and follow best practices
- Separation of concerns: Frontend/Backend/Database clearly separated
- RESTful API: Standard HTTP methods and status codes
- Responsive design: Works on desktop and mobile browsers
- Secure: Passwords hashed, authentication required for protected routes

---

## System Architecture

### High-Level Architecture
```
┌─────────────────┐         ┌──────────────────┐         ┌─────────────┐
│   React App     │ ◄─────► │  Spring Boot API │ ◄─────► │   MySQL DB  │
│   (Port 3000)   │  HTTP   │   (Port 8080)    │  JDBC   │ (Port 3306) │
└─────────────────┘         └──────────────────┘         └─────────────┘
```

### Communication Flow
1. React sends HTTP requests to Spring Boot REST API
2. Spring Boot validates requests and processes business logic
3. Spring Boot uses JPA/Hibernate to interact with MySQL
4. MySQL returns data through JPA entities
5. Spring Boot sends JSON responses to React
6. React updates UI based on received data

### Development Ports
- Frontend (React): `http://localhost:3000`
- Backend (Spring Boot): `http://localhost:8080`
- Database (MySQL): `localhost:3306`

### Technology Versions (Recommended)
- Node.js: 18.x or 20.x LTS
- React: 18.x
- Java: 17 LTS or 21 LTS
- Spring Boot: 3.2.x or 3.3.x
- MySQL: 8.0.x

---

## Database Design

### Database: `calorie_tracker`

### Table: `users`
Stores user account information and calculated metrics.

```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,  -- BCrypt hashed
    dob DATE NOT NULL,
    weight DECIMAL(5,2) NOT NULL,  -- Current weight in kg
    height DECIMAL(5,2) NOT NULL,  -- Height in cm
    activity_level ENUM('SEDENTARY', 'LIGHTLY_ACTIVE', 'MODERATELY_ACTIVE', 'VERY_ACTIVE') NOT NULL,
    goal DECIMAL(5,2),  -- Target weight in kg
    goal_type ENUM('LOSE', 'MAINTAIN', 'GAIN') NOT NULL,
    weekly_goal DECIMAL(3,2) NOT NULL,  -- 0.10 to 1.00 kg/week
    allowed_daily_intake INT NOT NULL,  -- Calculated calories/day
    bmi DECIMAL(4,2) NOT NULL,  -- Calculated BMI
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email)
);
```

**Field Details:**
- `id`: Auto-increment primary key
- `email`: Must be unique, used for login
- `password`: Never store plain text, always BCrypt hash
- `dob`: Date of birth for age calculation in calorie formulas
- `weight`: Current weight, updated when user logs weight
- `height`: Static after registration (typically doesn't change)
- `activity_level`: Multiplier for BMR calculation
- `weekly_goal`: Rate of weight change, affects calorie allowance
- `allowed_daily_intake`: Pre-calculated to avoid recalculating on every request
- `bmi`: Pre-calculated, recalculated when weight changes

### Table: `foods`
Stores both default/system foods and user-created custom foods.

```sql
CREATE TABLE foods (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    image VARCHAR(255),  -- URL or filename
    meal_type ENUM('BREAKFAST', 'LUNCH', 'SNACKS', 'DINNER', 'OTHER') NOT NULL,
    calories INT NOT NULL,
    user_id BIGINT NULL,  -- NULL = system/default food, NOT NULL = custom user food
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_meal_type (meal_type)
);
```

**Field Details:**
- `user_id`: NULL for default foods visible to all users, specific user_id for custom foods
- `meal_type`: Suggested meal category, helps organize food list
- `calories`: Integer value, assumed per standard serving
- `image`: Optional, can be URL or path to uploaded file

**Default Foods Strategy:**
- Seed database with common foods (user_id = NULL)
- When querying, show: (user_id = NULL) OR (user_id = current_user_id)
- Users can only delete their own custom foods

### Table: `meal_entries`
Records each instance of a user consuming a food item.

```sql
CREATE TABLE meal_entries (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    food_id BIGINT NOT NULL,
    entry_date DATE NOT NULL,
    entry_time TIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (food_id) REFERENCES foods(id) ON DELETE CASCADE,
    INDEX idx_user_date (user_id, entry_date),
    INDEX idx_user_id (user_id)
);
```

**Field Details:**
- `entry_date`: Date when food was consumed (separate from created_at for backdating)
- `entry_time`: Time when food was consumed
- Combination of date + time allows chronological ordering within a day
- Foreign key to food_id means if a food is deleted, entries remain but show deleted food

**Query Patterns:**
- Today's entries: `WHERE user_id = ? AND entry_date = CURDATE()`
- Date range: `WHERE user_id = ? AND entry_date BETWEEN ? AND ?`
- Grouped by meal type: JOIN with foods table and GROUP BY meal_type

### Table: `weight_entries`
Tracks user weight measurements over time.

```sql
CREATE TABLE weight_entries (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    entry_date DATE NOT NULL,
    weight DECIMAL(5,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_date (user_id, entry_date),
    INDEX idx_user_id (user_id)
);
```

**Field Details:**
- `entry_date`: Date of weight measurement
- `weight`: Weight in kg
- UNIQUE constraint on (user_id, entry_date): One weight entry per user per day
- If user logs weight multiple times same day, UPDATE existing entry

**Weight Update Strategy:**
- When weight entry created, also update `users.weight` to keep current weight in sync
- Weight tracking graph queries this table ordered by entry_date
- History page joins this with meal_entries by date

### Entity Relationships
```
users (1) ──────< (many) foods [custom foods only]
users (1) ──────< (many) meal_entries
users (1) ──────< (many) weight_entries
foods (1) ──────< (many) meal_entries
```

---

## Backend Implementation (Spring Boot)

### Project Configuration

#### Maven Dependencies (pom.xml)
```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Utilities -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    
    <!-- JWT for authentication -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

#### application.properties
```properties
# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/calorie_tracker?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_mysql_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.properties.hibernate.format_sql=true

# JWT Configuration
jwt.secret=your-secret-key-at-least-256-bits-long-for-HS256
jwt.expiration=86400000

# File Upload Configuration (for food images)
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB

# Logging
logging.level.org.springframework.web=INFO
logging.level.com.yourname.calorietracker=DEBUG
```

### Package Structure
```
src/main/java/com/yourname/calorietracker/
├── CalorieTrackerApplication.java          # Main Spring Boot application class
├── config/
│   ├── SecurityConfig.java                 # Spring Security configuration
│   ├── WebConfig.java                      # CORS and web configuration
│   └── JwtAuthenticationFilter.java        # JWT token filter
├── controller/                             # REST API endpoints
│   ├── AuthController.java                 # Register, login endpoints
│   ├── UserController.java                 # User profile management
│   ├── FoodController.java                 # Food CRUD operations
│   ├── MealEntryController.java            # Meal logging
│   ├── WeightEntryController.java          # Weight logging
│   └── DashboardController.java            # Dashboard summary data
├── service/                                # Business logic layer
│   ├── UserService.java
│   ├── FoodService.java
│   ├── MealEntryService.java
│   ├── WeightEntryService.java
│   └── JwtService.java                     # JWT token generation/validation
├── repository/                             # Data access layer
│   ├── UserRepository.java
│   ├── FoodRepository.java
│   ├── MealEntryRepository.java
│   └── WeightEntryRepository.java
├── model/                                  # JPA entities
│   ├── User.java
│   ├── Food.java
│   ├── MealEntry.java
│   ├── WeightEntry.java
│   ├── ActivityLevel.java                  # Enum
│   ├── GoalType.java                       # Enum
│   └── MealType.java                       # Enum
├── dto/                                    # Data Transfer Objects
│   ├── request/
│   │   ├── UserRegistrationRequest.java
│   │   ├── LoginRequest.java
│   │   ├── FoodRequest.java
│   │   ├── MealEntryRequest.java
│   │   └── WeightEntryRequest.java
│   └── response/
│       ├── AuthResponse.java
│       ├── DashboardResponse.java
│       ├── UserProfileResponse.java
│       └── ApiResponse.java               # Generic response wrapper
├── exception/                              # Custom exceptions
│   ├── ResourceNotFoundException.java
│   ├── BadRequestException.java
│   ├── GlobalExceptionHandler.java         # @ControllerAdvice for error handling
│   └── UnauthorizedException.java
└── util/                                   # Utility classes
    ├── CalorieCalculator.java              # BMI and calorie calculations
    └── DateUtils.java                      # Date manipulation helpers
```

### Model Classes (Detailed)

#### User.java
```java
package com.yourname.calorietracker.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(nullable = false)
    private String password;  // BCrypt hashed
    
    @Column(nullable = false)
    private LocalDate dob;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sex sex;
    
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal weight;
    
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal height;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level", nullable = false)
    private ActivityLevel activityLevel;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal goal;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", nullable = false)
    private GoalType goalType;
    
    @Column(name = "weekly_goal", nullable = false, precision = 3, scale = 2)
    private BigDecimal weeklyGoal;
    
    @Column(name = "allowed_daily_intake", nullable = false)
    private Integer allowedDailyIntake;
    
    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal bmi;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relationships (optional, can be lazy loaded)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Food> customFoods;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MealEntry> mealEntries;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WeightEntry> weightEntries;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper method to calculate age
    public int getAge() {
        return LocalDate.now().getYear() - dob.getYear();
    }
}
```

#### Food.java
```java
package com.yourname.calorietracker.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "foods")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Food {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 255)
    private String image;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false)
    private MealType mealType;
    
    @Column(nullable = false)
    private Integer calories;
    
    // NULL for default/system foods, specific user for custom foods
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Helper method to check if food is custom
    public boolean isCustomFood() {
        return user != null;
    }
}
```

#### MealEntry.java
```java
package com.yourname.calorietracker.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "meal_entries", indexes = {
    @Index(name = "idx_user_date", columnList = "user_id, entry_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;
    
    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;
    
    @Column(name = "entry_time", nullable = false)
    private LocalTime entryTime;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

#### WeightEntry.java
```java
package com.yourname.calorietracker.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "weight_entries", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "entry_date"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeightEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;
    
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal weight;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

#### Enums
```java
package com.yourname.calorietracker.model;

public enum ActivityLevel {
    SEDENTARY(1.2),           // Little to no exercise
    LIGHTLY_ACTIVE(1.375),    // Light exercise 1-3 days/week
    MODERATELY_ACTIVE(1.55),  // Moderate exercise 3-5 days/week
    VERY_ACTIVE(1.725);       // Hard exercise 6-7 days/week
    
    private final double multiplier;
    
    ActivityLevel(double multiplier) {
        this.multiplier = multiplier;
    }
    
    public double getMultiplier() {
        return multiplier;
    }
}

public enum GoalType {
    LOSE,      // Weight loss
    MAINTAIN,  // Maintain current weight
    GAIN       // Weight gain
}

public enum MealType {
    BREAKFAST,
    LUNCH,
    SNACKS,
    DINNER,
    OTHER
}
```

### Repository Interfaces

#### UserRepository.java
```java
package com.yourname.calorietracker.repository;

import com.yourname.calorietracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

#### FoodRepository.java
```java
package com.yourname.calorietracker.repository;

import com.yourname.calorietracker.model.Food;
import com.yourname.calorietracker.model.MealType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {
    // Find all default foods (user_id is NULL) or user's custom foods
    @Query("SELECT f FROM Food f WHERE f.user IS NULL OR f.user.id = :userId")
    List<Food> findAvailableFoodsForUser(@Param("userId") Long userId);
    
    // Find foods by meal type for a specific user
    @Query("SELECT f FROM Food f WHERE (f.user IS NULL OR f.user.id = :userId) AND f.mealType = :mealType")
    List<Food> findByMealTypeForUser(@Param("userId") Long userId, @Param("mealType") MealType mealType);
    
    // Find only default/system foods
    List<Food> findByUserIsNull();
    
    // Find only user's custom foods
    List<Food> findByUserId(Long userId);
}
```

#### MealEntryRepository.java
```java
package com.yourname.calorietracker.repository;

import com.yourname.calorietracker.model.MealEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MealEntryRepository extends JpaRepository<MealEntry, Long> {
    // Find all meal entries for a user on a specific date
    List<MealEntry> findByUserIdAndEntryDate(Long userId, LocalDate entryDate);
    
    // Find meal entries for a user within a date range
    List<MealEntry> findByUserIdAndEntryDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    
    // Calculate total calories consumed on a specific date
    @Query("SELECT COALESCE(SUM(f.calories), 0) FROM MealEntry me JOIN me.food f WHERE me.user.id = :userId AND me.entryDate = :date")
    Integer sumCaloriesForUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    // Find entries ordered by time for display
    List<MealEntry> findByUserIdAndEntryDateOrderByEntryTimeAsc(Long userId, LocalDate entryDate);
}
```

#### WeightEntryRepository.java
```java
package com.yourname.calorietracker.repository;

import com.yourname.calorietracker.model.WeightEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeightEntryRepository extends JpaRepository<WeightEntry, Long> {
    // Find all weight entries for a user, ordered by date
    List<WeightEntry> findByUserIdOrderByEntryDateAsc(Long userId);
    
    // Find weight entry for a specific user and date
    Optional<WeightEntry> findByUserIdAndEntryDate(Long userId, LocalDate entryDate);
    
    // Find most recent weight entry for a user
    Optional<WeightEntry> findFirstByUserIdOrderByEntryDateDesc(Long userId);
    
    // Find weight entries within a date range
    List<WeightEntry> findByUserIdAndEntryDateBetweenOrderByEntryDateAsc(
        Long userId, LocalDate startDate, LocalDate endDate);
}
```

### Service Classes (Business Logic)

#### CalorieCalculator.java (Utility)
```java
package com.yourname.calorietracker.util;

import com.yourname.calorietracker.model.ActivityLevel;
import com.yourname.calorietracker.model.GoalType;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalorieCalculator {
    
    /**
     * Calculate BMI (Body Mass Index)
     * Formula: weight(kg) / (height(m))^2
     */
    public static BigDecimal calculateBMI(BigDecimal weightKg, BigDecimal heightCm) {
        BigDecimal heightM = heightCm.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal heightSquared = heightM.multiply(heightM);
        return weightKg.divide(heightSquared, 2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate Basal Metabolic Rate (BMR) using Mifflin-St Jeor Equation
     * For men: BMR = (10 × weight in kg) + (6.25 × height in cm) - (5 × age in years) + 5
     * For women: BMR = (10 × weight in kg) + (6.25 × height in cm) - (5 × age in years) - 161
     * 
     * Note: We'll use men's formula by default since gender wasn't specified in requirements
     * In a real app, you'd add a gender field to User model
     */
    public static double calculateBMR(BigDecimal weightKg, BigDecimal heightCm, int age) {
        double weight = weightKg.doubleValue();
        double height = heightCm.doubleValue();
        
        // Using men's formula as default
        return (10 * weight) + (6.25 * height) - (5 * age) + 5;
    }
    
    /**
     * Calculate Total Daily Energy Expenditure (TDEE)
     * TDEE = BMR × Activity Level Multiplier
     */
    public static int calculateTDEE(double bmr, ActivityLevel activityLevel) {
        return (int) Math.round(bmr * activityLevel.getMultiplier());
    }
    
    /**
     * Calculate daily calorie allowance based on weight goal
     * 
     * Calorie adjustment:
     * - 1 kg weight loss per week = 7700 calories deficit per week = 1100 calories/day deficit
     * - 0.5 kg weight loss per week = 3850 calories deficit per week = 550 calories/day deficit
     * - Similar logic for weight gain (surplus instead of deficit)
     * 
     * Formula: TDEE + (weeklyGoalKg × 1100 × goalDirection)
     * where goalDirection: -1 for loss, 0 for maintain, +1 for gain
     */
    public static int calculateDailyCalorieAllowance(
            int tdee, 
            BigDecimal weeklyGoalKg, 
            GoalType goalType) {
        
        double weeklyGoal = weeklyGoalKg.doubleValue();
        int calorieAdjustment = (int) Math.round(weeklyGoal * 1100);
        
        switch (goalType) {
            case LOSE:
                return tdee - calorieAdjustment;
            case GAIN:
                return tdee + calorieAdjustment;
            case MAINTAIN:
            default:
                return tdee;
        }
    }
    
    /**
     * All-in-one method to calculate allowed daily intake
     */
    public static int calculateAllowedDailyIntake(
            BigDecimal weightKg,
            BigDecimal heightCm,
            int age,
            ActivityLevel activityLevel,
            BigDecimal weeklyGoalKg,
            GoalType goalType) {
        
        double bmr = calculateBMR(weightKg, heightCm, age);
        int tdee = calculateTDEE(bmr, activityLevel);
        return calculateDailyCalorieAllowance(tdee, weeklyGoalKg, goalType);
    }
}
```

#### UserService.java
```java
package com.yourname.calorietracker.service;

import com.yourname.calorietracker.dto.request.UserRegistrationRequest;
import com.yourname.calorietracker.dto.response.UserProfileResponse;
import com.yourname.calorietracker.exception.BadRequestException;
import com.yourname.calorietracker.exception.ResourceNotFoundException;
import com.yourname.calorietracker.model.User;
import com.yourname.calorietracker.repository.UserRepository;
import com.yourname.calorietracker.util.CalorieCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Register a new user with calculated BMI and calorie allowance
     */
    @Transactional
    public User registerUser(UserRegistrationRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        
        // Validate weekly goal range
        if (request.getWeeklyGoal().compareTo(new BigDecimal("0.1")) < 0 ||
            request.getWeeklyGoal().compareTo(new BigDecimal("1.0")) > 0) {
            throw new BadRequestException("Weekly goal must be between 0.1 and 1.0 kg");
        }
        
        // Calculate age from DOB
        int age = java.time.LocalDate.now().getYear() - request.getDob().getYear();
        
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
            .sex(request.getSex())
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
    
    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
    
    /**
     * Get user by ID
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
    
    /**
     * Update user profile and recalculate metrics if weight changed
     */
    @Transactional
    public User updateUserProfile(Long userId, UserProfileResponse updateRequest) {
        User user = getUserById(userId);
        
        boolean weightChanged = false;
        
        // Update fields
        if (updateRequest.getName() != null) {
            user.setName(updateRequest.getName());
        }
        if (updateRequest.getWeight() != null && 
            !updateRequest.getWeight().equals(user.getWeight())) {
            user.setWeight(updateRequest.getWeight());
            weightChanged = true;
        }
        if (updateRequest.getActivityLevel() != null) {
            user.setActivityLevel(updateRequest.getActivityLevel());
        }
        if (updateRequest.getGoal() != null) {
            user.setGoal(updateRequest.getGoal());
        }
        if (updateRequest.getGoalType() != null) {
            user.setGoalType(updateRequest.getGoalType());
        }
        if (updateRequest.getWeeklyGoal() != null) {
            user.setWeeklyGoal(updateRequest.getWeeklyGoal());
        }
        
        // Recalculate if weight changed or goals changed
        if (weightChanged || updateRequest.getActivityLevel() != null || 
            updateRequest.getWeeklyGoal() != null || updateRequest.getGoalType() != null) {
            
            // Recalculate BMI
            user.setBmi(CalorieCalculator.calculateBMI(user.getWeight(), user.getHeight()));
            
            // Recalculate allowed daily intake
            int newAllowance = CalorieCalculator.calculateAllowedDailyIntake(
                user.getWeight(),
                user.getHeight(),
                user.getAge(),
                user.getActivityLevel(),
                user.getWeeklyGoal(),
                user.getGoalType()
            );
            user.setAllowedDailyIntake(newAllowance);
        }
        
        return userRepository.save(user);
    }
}
```

---

## API Specifications

### Authentication Endpoints

#### POST /api/auth/register
Register a new user account.

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "SecurePass123",
  "dob": "1990-01-15",
  "weight": 75.5,
  "height": 175.0,
  "activityLevel": "MODERATELY_ACTIVE",
  "goal": 70.0,
  "goalType": "LOSE",
  "weeklyGoal": 0.5
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "name": "John Doe",
      "email": "john@example.com",
      "bmi": 24.65,
      "allowedDailyIntake": 1850
    }
  }
}
```

#### POST /api/auth/login
Authenticate existing user.

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "SecurePass123"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "name": "John Doe",
      "email": "john@example.com"
    }
  }
}
```

### Dashboard Endpoints

#### GET /api/dashboard/today
Get today's calorie summary for authenticated user.

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "date": "2024-03-15",
    "allowedDailyIntake": 1850,
    "consumedCalories": 1245,
    "remainingCalories": 605,
    "percentageConsumed": 67.3,
    "mealEntriesToday": [
      {
        "id": 1,
        "food": {
          "id": 10,
          "name": "Scrambled Eggs",
          "calories": 250,
          "mealType": "BREAKFAST"
        },
        "entryTime": "08:30:00"
      }
    ]
  }
}
```

### Food Endpoints

#### GET /api/foods
Get all available foods (default + user's custom).

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Query Parameters:**
- `mealType` (optional): Filter by meal type

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Oatmeal",
      "calories": 150,
      "mealType": "BREAKFAST",
      "image": null,
      "isCustom": false
    },
    {
      "id": 45,
      "name": "Mom's Special Pasta",
      "calories": 450,
      "mealType": "DINNER",
      "image": "uploads/pasta.jpg",
      "isCustom": true
    }
  ]
}
```

#### POST /api/foods
Add a custom food item.

**Headers:**
```
Authorization: Bearer <jwt_token>
Content-Type: multipart/form-data
```

**Request Body (Form Data):**
```
name: "Protein Shake"
calories: 200
mealType: "SNACKS"
image: [file]
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Food added successfully",
  "data": {
    "id": 46,
    "name": "Protein Shake",
    "calories": 200,
    "mealType": "SNACKS",
    "image": "uploads/shake.jpg",
    "isCustom": true
  }
}
```

#### DELETE /api/foods/{id}
Delete a custom food (only if user owns it).

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Food deleted successfully"
}
```

### Meal Entry Endpoints

#### POST /api/meal-entries
Log a meal entry.

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Request Body:**
```json
{
  "foodId": 10,
  "entryDate": "2024-03-15",
  "entryTime": "12:30:00"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Meal logged successfully",
  "data": {
    "id": 123,
    "food": {
      "id": 10,
      "name": "Grilled Chicken",
      "calories": 350
    },
    "entryDate": "2024-03-15",
    "entryTime": "12:30:00"
  }
}
```

#### GET /api/meal-entries/date/{date}
Get all meal entries for a specific date.

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Path Parameters:**
- `date`: Date in format YYYY-MM-DD

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "date": "2024-03-15",
    "totalCalories": 1650,
    "mealsByType": {
      "BREAKFAST": [
        {
          "id": 120,
          "food": {
            "name": "Oatmeal",
            "calories": 150
          },
          "entryTime": "08:00:00"
        }
      ],
      "LUNCH": [
        {
          "id": 121,
          "food": {
            "name": "Grilled Chicken Salad",
            "calories": 400
          },
          "entryTime": "13:00:00"
        }
      ],
      "DINNER": [],
      "SNACKS": [],
      "OTHER": []
    },
    "weightEntry": {
      "weight": 75.2,
      "entryDate": "2024-03-15"
    }
  }
}
```

#### DELETE /api/meal-entries/{id}
Delete a meal entry.

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Meal entry deleted successfully"
}
```

### Weight Entry Endpoints

#### POST /api/weight-entries
Log a weight measurement.

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Request Body:**
```json
{
  "weight": 74.8,
  "entryDate": "2024-03-15"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Weight logged successfully",
  "data": {
    "id": 45,
    "weight": 74.8,
    "entryDate": "2024-03-15"
  }
}
```

**Note:** If entry for this date exists, it will be updated instead.

#### GET /api/weight-entries
Get all weight entries for authenticated user.

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Query Parameters:**
- `startDate` (optional): Filter from date
- `endDate` (optional): Filter to date

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 40,
      "weight": 76.5,
      "entryDate": "2024-03-01"
    },
    {
      "id": 45,
      "weight": 74.8,
      "entryDate": "2024-03-15"
    }
  ]
}
```

---

## Frontend Implementation (React)

### Project Setup Commands
```bash
# Create React app
npx create-react-app calorie-tracker-frontend
cd calorie-tracker-frontend

# Install dependencies
npm install react-router-dom axios chart.js react-chartjs-2

# Optional: Install UI library
npm install @mui/material @emotion/react @emotion/styled
# OR
npm install bootstrap
```

### Project Structure
```
src/
├── App.js                          # Main app component with routing
├── App.css                         # Global styles
├── index.js                        # Entry point
├── components/
│   ├── auth/
│   │   ├── Register.js             # Registration form
│   │   ├── Register.css
│   │   ├── Login.js                # Login form
│   │   └── Login.css
│   ├── dashboard/
│   │   ├── Dashboard.js            # Main dashboard view
│   │   ├── Dashboard.css
│   │   ├── CalorieBar.js           # Visual calorie progress bar
│   │   ├── MealLogger.js           # Form to log meals
│   │   └── TodaysMeals.js          # Display today's meal list
│   ├── food/
│   │   ├── FoodList.js             # Display all foods
│   │   ├── FoodList.css
│   │   ├── AddFoodForm.js          # Form to add custom food
│   │   └── FoodCard.js             # Individual food item display
│   ├── history/
│   │   ├── History.js              # History page with date picker
│   │   ├── History.css
│   │   ├── DayView.js              # Single day's detailed view
│   │   └── MealTypeSection.js      # Expandable meal type section
│   ├── weight/
│   │   ├── WeightTracking.js       # Weight tracking page
│   │   ├── WeightTracking.css
│   │   ├── WeightGraph.js          # Chart.js weight graph
│   │   └── WeightEntryForm.js      # Form to log weight
│   ├── common/
│   │   ├── Navbar.js               # Navigation bar
│   │   ├── ProtectedRoute.js       # Route wrapper for auth
│   │   ├── Loading.js              # Loading spinner
│   │   └── ErrorMessage.js         # Error display component
│   └── profile/
│       ├── Profile.js              # User profile view/edit
│       └── Profile.css
├── services/
│   ├── api.js                      # Axios instance with interceptors
│   ├── authService.js              # Authentication API calls
│   ├── foodService.js              # Food API calls
│   ├── mealService.js              # Meal entry API calls
│   ├── weightService.js            # Weight entry API calls
│   └── dashboardService.js         # Dashboard API calls
├── context/
│   └── AuthContext.js              # Global auth state management
├── utils/
│   ├── calculations.js             # Client-side calculation helpers
│   └── validators.js               # Form validation functions
└── constants/
    └── enums.js                    # Activity levels, meal types, etc.
```

### Key React Components (Detailed)

#### App.js (Main Routing)
```javascript
import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/common/ProtectedRoute';
import Navbar from './components/common/Navbar';
import Login from './components/auth/Login';
import Register from './components/auth/Register';
import Dashboard from './components/dashboard/Dashboard';
import FoodList from './components/food/FoodList';
import History from './components/history/History';
import WeightTracking from './components/weight/WeightTracking';
import Profile from './components/profile/Profile';
import './App.css';

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="App">
          <Navbar />
          <div className="container">
            <Routes>
              {/* Public routes */}
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />
              
              {/* Protected routes */}
              <Route path="/dashboard" element={
                <ProtectedRoute>
                  <Dashboard />
                </ProtectedRoute>
              } />
              
              <Route path="/foods" element={
                <ProtectedRoute>
                  <FoodList />
                </ProtectedRoute>
              } />
              
              <Route path="/history" element={
                <ProtectedRoute>
                  <History />
                </ProtectedRoute>
              } />
              
              <Route path="/weight" element={
                <ProtectedRoute>
                  <WeightTracking />
                </ProtectedRoute>
              } />
              
              <Route path="/profile" element={
                <ProtectedRoute>
                  <Profile />
                </ProtectedRoute>
              } />
              
              {/* Default redirect */}
              <Route path="/" element={<Navigate to="/dashboard" />} />
            </Routes>
          </div>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;
```

#### AuthContext.js (Global State)
```javascript
import React, { createContext, useState, useContext, useEffect } from 'react';
import { login as apiLogin, register as apiRegister } from '../services/authService';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [loading, setLoading] = useState(true);

  // Check for existing token on mount
  useEffect(() => {
    const storedToken = localStorage.getItem('token');
    const storedUser = localStorage.getItem('user');
    
    if (storedToken && storedUser) {
      setToken(storedToken);
      setUser(JSON.parse(storedUser));
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    try {
      const response = await apiLogin(email, password);
      setToken(response.data.token);
      setUser(response.data.user);
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('user', JSON.stringify(response.data.user));
      return { success: true };
    } catch (error) {
      return { 
        success: false, 
        error: error.response?.data?.message || 'Login failed' 
      };
    }
  };

  const register = async (userData) => {
    try {
      const response = await apiRegister(userData);
      setToken(response.data.token);
      setUser(response.data.user);
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('user', JSON.stringify(response.data.user));
      return { success: true };
    } catch (error) {
      return { 
        success: false, 
        error: error.response?.data?.message || 'Registration failed' 
      };
    }
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  };

  const value = {
    user,
    token,
    login,
    register,
    logout,
    isAuthenticated: !!token
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};
```

#### api.js (Axios Configuration)
```javascript
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

// Create axios instance
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expired or invalid
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
```

#### Dashboard.js (Main Dashboard)
```javascript
import React, { useState, useEffect } from 'react';
import { getDashboardToday } from '../../services/dashboardService';
import CalorieBar from './CalorieBar';
import MealLogger from './MealLogger';
import TodaysMeals from './TodaysMeals';
import Loading from '../common/Loading';
import ErrorMessage from '../common/ErrorMessage';
import './Dashboard.css';

const Dashboard = () => {
  const [dashboardData, setDashboardData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchDashboard = async () => {
    try {
      setLoading(true);
      const response = await getDashboardToday();
      setDashboardData(response.data);
      setError(null);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load dashboard');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboard();
  }, []);

  const handleMealLogged = () => {
    // Refresh dashboard after logging a meal
    fetchDashboard();
  };

  if (loading) return <Loading />;
  if (error) return <ErrorMessage message={error} />;
  if (!dashboardData) return null;

  return (
    <div className="dashboard">
      <h1>Dashboard</h1>
      
      <div className="dashboard-summary">
        <CalorieBar 
          allowed={dashboardData.allowedDailyIntake}
          consumed={dashboardData.consumedCalories}
          remaining={dashboardData.remainingCalories}
        />
      </div>

      <div className="dashboard-actions">
        <MealLogger onMealLogged={handleMealLogged} />
      </div>

      <div className="dashboard-meals">
        <TodaysMeals 
          meals={dashboardData.mealEntriesToday}
          onDelete={fetchDashboard}
        />
      </div>
    </div>
  );
};

export default Dashboard;
```

#### CalorieBar.js (Visual Progress)
```javascript
import React from 'react';
import './CalorieBar.css';

const CalorieBar = ({ allowed, consumed, remaining }) => {
  const percentage = Math.min((consumed / allowed) * 100, 100);
  const isOverLimit = consumed > allowed;

  return (
    <div className="calorie-bar-container">
      <div className="calorie-stats">
        <div className="stat">
          <span className="label">Allowed</span>
          <span className="value">{allowed}</span>
        </div>
        <div className="stat">
          <span className="label">Consumed</span>
          <span className="value consumed">{consumed}</span>
        </div>
        <div className="stat">
          <span className="label">Remaining</span>
          <span className={`value ${isOverLimit ? 'over-limit' : ''}`}>
            {remaining}
          </span>
        </div>
      </div>

      <div className="progress-bar">
        <div 
          className={`progress-fill ${isOverLimit ? 'over-limit' : ''}`}
          style={{ width: `${percentage}%` }}
        />
      </div>

      <div className="percentage-text">
        {percentage.toFixed(1)}% of daily goal
      </div>
    </div>
  );
};

export default CalorieBar;
```

---

## Session-by-Session Implementation Plan

### Phase 1: Foundation & Setup (Sessions 1-4)

#### Session 1: Environment Setup & Project Initialization
**Duration:** 1-1.5 hours  
**Prerequisites:** None  
**Goal:** Get development environment ready

**Tasks:**
1. Verify software installations:
   - Node.js (18.x or 20.x): `node --version`
   - Java JDK (17 or 21): `java --version`
   - MySQL (8.0): `mysql --version`
   - Git: `git --version`

2. Install missing software if needed:
   - Node.js: https://nodejs.org/
   - Java JDK: https://adoptium.net/
   - MySQL: https://dev.mysql.com/downloads/installer/
   - IDE: VS Code, IntelliJ IDEA

3. Create React project:
```bash
npx create-react-app calorie-tracker-frontend
cd calorie-tracker-frontend
npm install react-router-dom axios chart.js react-chartjs-2
npm start  # Verify it runs
```

4. Create Spring Boot project:
   - Go to https://start.spring.io/
   - Project: Maven
   - Language: Java
   - Spring Boot: 3.2.x or latest stable
   - Group: com.yourname
   - Artifact: calorie-tracker
   - Dependencies: Spring Web, Spring Data JPA, MySQL Driver, Lombok, Spring Security, Validation
   - Download and extract
   - Open in IDE

5. Test basic setup:
   - React app runs on port 3000
   - Spring Boot app runs (even without DB connection yet)

**Success Criteria:**
- Both projects created and runnable
- All tools installed and verified

---

#### Session 2: Database Setup & Entity Creation
**Duration:** 1-1.5 hours  
**Prerequisites:** Session 1 complete  
**Goal:** Database ready with all tables

**Tasks:**
1. Start MySQL and create database:
```sql
CREATE DATABASE calorie_tracker;
USE calorie_tracker;
```

2. Configure Spring Boot (application.properties):
```properties
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/calorie_tracker
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

3. Create enum classes:
   - ActivityLevel.java
   - GoalType.java
   - MealType.java

4. Create entity classes:
   - User.java (complete with all fields)
   - Food.java
   - MealEntry.java
   - WeightEntry.java

5. Run Spring Boot application:
   - Tables should auto-create via Hibernate
   - Verify in MySQL Workbench

6. Create repository interfaces:
   - UserRepository
   - FoodRepository
   - MealEntryRepository
   - WeightEntryRepository

**Success Criteria:**
- Database connected
- All 4 tables created in MySQL
- Spring Boot starts without errors
- Can query empty tables in MySQL

**Common Issues & Solutions:**
- **MySQL connection refused**: Check MySQL service is running
- **Access denied**: Verify username/password in application.properties
- **Table creation fails**: Check entity annotations, verify ddl-auto=update

---

#### Session 3: Utility Classes & Calculations
**Duration:** 1 hour  
**Prerequisites:** Session 2 complete  
**Goal:** Implement all calculation logic

**Tasks:**
1. Create CalorieCalculator.java in util package:
   - calculateBMI() method
   - calculateBMR() method (Mifflin-St Jeor)
   - calculateTDEE() method
   - calculateDailyCalorieAllowance() method
   - calculateAllowedDailyIntake() convenience method

2. Write unit tests for calculations:
```java
// Test BMI: 75kg, 175cm should give ~24.49
// Test BMR: verify formula works
// Test calorie adjustments for different goals
```

3. Create validators.js in React utils:
```javascript
export const validateEmail = (email) => {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return re.test(email);
};

export const validatePassword = (password) => {
  return password.length >= 8;
};

export const validateWeeklyGoal = (goal) => {
  return goal >= 0.1 && goal <= 1.0;
};
```

4. Test calculations manually:
   - Create a simple test endpoint that calculates BMI
   - Call it from Postman to verify math

**Success Criteria:**
- All calculation methods implemented and working
- BMI calculation verified (75kg / 1.75m² = 24.49)
- Calorie calculations produce sensible results
- Validators work in JavaScript

---

#### Session 4: Backend Authentication - Part 1 (DTOs & Models)
**Duration:** 1-1.5 hours  
**Prerequisites:** Session 3 complete  
**Goal:** Set up authentication infrastructure

**Tasks:**
1. Create DTO classes:
   - UserRegistrationRequest.java
   - LoginRequest.java
   - AuthResponse.java
   - ApiResponse.java (generic wrapper)

Example UserRegistrationRequest:
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationRequest {
    @NotBlank(message = "Name is required")
    private String name;
    
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dob;
    
    @NotNull(message = "Sex is required")
    private Sex sex;
    
    @NotNull(message = "Weight is required")
    @DecimalMin(value = "30.0", message = "Weight must be at least 30kg")
    private BigDecimal weight;
    
    @NotNull(message = "Height is required")
    @DecimalMin(value = "100.0", message = "Height must be at least 100cm")
    private BigDecimal height;
    
    @NotNull(message = "Activity level is required")
    private ActivityLevel activityLevel;
    
    private BigDecimal goal;
    
    @NotNull(message = "Goal type is required")
    private GoalType goalType;
    
    @NotNull(message = "Weekly goal is required")
    @DecimalMin(value = "0.1")
    @DecimalMax(value = "1.0")
    private BigDecimal weeklyGoal;
}
```

2. Add JWT dependencies to pom.xml (see earlier section)

3. Create JwtService.java:
   - generateToken(String email) method
   - validateToken(String token) method
   - extractEmail(String token) method
   - TOKEN_EXPIRATION = 24 hours

4. Create exception classes:
   - ResourceNotFoundException.java
   - BadRequestException.java
   - UnauthorizedException.java
   - GlobalExceptionHandler.java with @ControllerAdvice

**Success Criteria:**
- All DTO classes created with validation annotations
- JWT service implemented
- Exception handling configured
- Project still compiles

---

### Phase 2: Authentication & User Management (Sessions 5-7)

#### Session 5: Backend Authentication - Part 2 (Service & Controller)
**Duration:** 1.5-2 hours  
**Prerequisites:** Session 4 complete  
**Goal:** Complete backend authentication

**Tasks:**
1. Implement UserService.java:
   - registerUser() method
   - getUserByEmail() method
   - getUserById() method
   - Password encoding with BCrypt
   - Automatic BMI and calorie calculation

2. Create AuthController.java:
```java
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationRequest request) {
        User user = userService.registerUser(request);
        String token = jwtService.generateToken(user.getEmail());
        
        AuthResponse response = new AuthResponse(token, user);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse(true, "Registration successful", response));
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.getUserByEmail(request.getEmail());
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        
        String token = jwtService.generateToken(user.getEmail());
        AuthResponse response = new AuthResponse(token, user);
        
        return ResponseEntity.ok(new ApiResponse(true, "Login successful", response));
    }
}
```

3. Configure Spring Security:
   - Create SecurityConfig.java
   - Disable CSRF for API
   - Configure CORS for React (port 3000)
   - Make /api/auth/** endpoints public
   - Require authentication for all other /api/** endpoints

4. Create JwtAuthenticationFilter.java:
   - Extract JWT from Authorization header
   - Validate token
   - Set authentication in SecurityContext

**Success Criteria:**
- POST /api/auth/register works in Postman
- Returns JWT token
- User saved in database with hashed password
- BMI and allowedDailyIntake calculated correctly
- POST /api/auth/login works with valid credentials
- Invalid credentials return 401

**Testing in Postman:**
```json
// POST http://localhost:8080/api/auth/register
{
  "name": "Test User",
  "email": "test@example.com",
  "password": "password123",
  "dob": "1995-06-15",
  "weight": 70.0,
  "height": 170.0,
  "activityLevel": "MODERATELY_ACTIVE",
  "goal": 65.0,
  "goalType": "LOSE",
  "weeklyGoal": 0.5
}
```

---

#### Session 6: Frontend Authentication - Login & Register
**Duration:** 1.5-2 hours  
**Prerequisites:** Session 5 complete  
**Goal:** Users can register and login from React

**Tasks:**
1. Create authService.js:
```javascript
import api from './api';

export const register = async (userData) => {
  const response = await api.post('/auth/register', userData);
  return response.data;
};

export const login = async (email, password) => {
  const response = await api.post('/auth/login', { email, password });
  return response.data;
};

export const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
};
```

2. Complete AuthContext.js (see earlier implementation)

3. Create Register.js component:
   - Multi-step form or single long form
   - All fields from UserRegistrationRequest
   - Client-side validation
   - Display errors from backend
   - Redirect to dashboard on success

4. Create Login.js component:
   - Email and password fields
   - "Remember me" checkbox (optional)
   - Link to register page
   - Display errors
   - Redirect to dashboard on success

5. Create ProtectedRoute.js:
```javascript
import { Navigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

const ProtectedRoute = ({ children }) => {
  const { isAuthenticated } = useAuth();
  
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  
  return children;
};

export default ProtectedRoute;
```

6. Add basic styling (CSS or use a UI library like Bootstrap/Material-UI)

**Success Criteria:**
- User can register from React form
- Registration stores token and redirects to dashboard
- User can login with created credentials
- Protected routes redirect to login when not authenticated
- Token persists after page refresh
- Logout clears token and redirects to login

**Testing Flow:**
1. Go to http://localhost:3000/register
2. Fill form and submit
3. Should redirect to /dashboard
4. Refresh page - should stay authenticated
5. Logout - should go to /login
6. Try to access /dashboard - should redirect to /login
7. Login again - should work

---

#### Session 7: User Profile Page
**Duration:** 1 hour  
**Prerequisites:** Session 6 complete  
**Goal:** View and edit user profile

**Tasks:**
1. Create Profile.js component:
   - Display user information (name, email, height, weight, BMI, etc.)
   - Editable fields: name, weight, activity level, goals
   - Non-editable: email, height, DOB
   - Update button
   - Show calculated BMI and daily calorie allowance

2. Create profileService.js:
```javascript
export const getProfile = async () => {
  const response = await api.get('/users/profile');
  return response.data;
};

export const updateProfile = async (profileData) => {
  const response = await api.put('/users/profile', profileData);
  return response.data;
};
```

3. Backend: UserController.java:
```java
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtService jwtService;
    
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        String email = extractEmailFromRequest(request);
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(new ApiResponse(true, null, user));
    }
    
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            HttpServletRequest request,
            @RequestBody UserUpdateRequest updateRequest) {
        String email = extractEmailFromRequest(request);
        User user = userService.getUserByEmail(email);
        User updated = userService.updateUserProfile(user.getId(), updateRequest);
        return ResponseEntity.ok(new ApiResponse(true, "Profile updated", updated));
    }
    
    private String extractEmailFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtService.extractEmail(token);
    }
}
```

4. Update AuthContext to refresh user data after profile update

**Success Criteria:**
- Profile page displays current user information
- Can update weight and see BMI recalculate
- Can change activity level and see calorie allowance update
- Changes persist after page refresh
- Validation prevents invalid inputs

---

### Phase 3: Core Features - Food & Meals (Sessions 8-11)

#### Session 8: Seed Default Foods & Food Backend
**Duration:** 1.5 hours  
**Prerequisites:** Session 7 complete  
**Goal:** Food database ready with default foods

**Tasks:**
1. Create SQL script to seed default foods (data.sql):
```sql
INSERT INTO foods (name, meal_type, calories, user_id) VALUES
-- Breakfast
('Oatmeal', 'BREAKFAST', 150, NULL),
('Scrambled Eggs (2)', 'BREAKFAST', 200, NULL),
('Greek Yogurt', 'BREAKFAST', 120, NULL),
('Whole Wheat Toast', 'BREAKFAST', 80, NULL),
('Banana', 'BREAKFAST', 105, NULL),

-- Lunch
('Grilled Chicken Salad', 'LUNCH', 350, NULL),
('Turkey Sandwich', 'LUNCH', 400, NULL),
('Chicken Caesar Salad', 'LUNCH', 450, NULL),
('Vegetable Stir Fry', 'LUNCH', 300, NULL),

-- Dinner
('Baked Salmon with Vegetables', 'DINNER', 500, NULL),
('Grilled Chicken Breast', 'DINNER', 350, NULL),
('Pasta with Tomato Sauce', 'DINNER', 450, NULL),
('Beef Stir Fry', 'DINNER', 550, NULL),

-- Snacks
('Apple', 'SNACKS', 95, NULL),
('Almonds (1oz)', 'SNACKS', 160, NULL),
('Protein Bar', 'SNACKS', 200, NULL),
('Carrot Sticks', 'SNACKS', 35, NULL),

-- Other
('Coffee (black)', 'OTHER', 5, NULL),
('Green Tea', 'OTHER', 0, NULL),
('Orange Juice (8oz)', 'OTHER', 110, NULL);
```

2. Implement FoodService.java:
   - getAllFoodsForUser() - returns default + user's custom
   - getFoodById()
   - createCustomFood()
   - deleteCustomFood() - only if user owns it
   - Optional: Image upload handling

3. Create FoodController.java:
```java
@RestController
@RequestMapping("/api/foods")
@CrossOrigin(origins = "http://localhost:3000")
public class FoodController {
    
    @Autowired
    private FoodService foodService;
    
    @GetMapping
    public ResponseEntity<?> getAllFoods(HttpServletRequest request) {
        Long userId = extractUserIdFromRequest(request);
        List<Food> foods = foodService.getAllFoodsForUser(userId);
        return ResponseEntity.ok(new ApiResponse(true, null, foods));
    }
    
    @PostMapping
    public ResponseEntity<?> createFood(
            HttpServletRequest request,
            @Valid @RequestBody FoodRequest foodRequest) {
        Long userId = extractUserIdFromRequest(request);
        Food food = foodService.createCustomFood(userId, foodRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse(true, "Food added", food));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFood(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = extractUserIdFromRequest(request);
        foodService.deleteCustomFood(id, userId);
        return ResponseEntity.ok(new ApiResponse(true, "Food deleted", null));
    }
}
```

**Success Criteria:**
- Database seeded with ~20 default foods
- GET /api/foods returns all foods for user
- POST /api/foods creates custom food
- Custom foods only visible to owner
- Cannot delete default foods (user_id = NULL)

---

#### Session 9: Frontend Food List & Add Food
**Duration:** 1.5 hours  
**Prerequisites:** Session 8 complete  
**Goal:** Users can view and add foods

**Tasks:**
1. Create foodService.js:
```javascript
export const getAllFoods = async () => {
  const response = await api.get('/foods');
  return response.data;
};

export const createFood = async (foodData) => {
  const response = await api.post('/foods', foodData);
  return response.data;
};

export const deleteFood = async (foodId) => {
  const response = await api.delete(`/foods/${foodId}`);
  return response.data;
};
```

2. Create constants/enums.js:
```javascript
export const MEAL_TYPES = [
  'BREAKFAST',
  'LUNCH', 
  'SNACKS',
  'DINNER',
  'OTHER'
];

export const ACTIVITY_LEVELS = [
  { value: 'SEDENTARY', label: 'Sedentary (little to no exercise)' },
  { value: 'LIGHTLY_ACTIVE', label: 'Lightly Active (1-3 days/week)' },
  { value: 'MODERATELY_ACTIVE', label: 'Moderately Active (3-5 days/week)' },
  { value: 'VERY_ACTIVE', label: 'Very Active (6-7 days/week)' }
];

export const GOAL_TYPES = ['LOSE', 'MAINTAIN', 'GAIN'];
```

3. Create FoodCard.js component:
   - Display food name, calories, meal type
   - Show "Custom" badge if user created it
   - Delete button for custom foods only
   - Clean card design

4. Create FoodList.js:
   - Fetch and display all foods
   - Filter by meal type (tabs or dropdown)
   - Search functionality (optional)
   - Grid or list layout
   - Button to open Add Food modal/form

5. Create AddFoodForm.js:
   - Modal or separate form
   - Fields: name, calories, meal type, image (optional)
   - Validation
   - Submit and refresh food list

**Success Criteria:**
- Food list displays all foods organized by meal type
- Can add a new custom food
- Custom food appears immediately after adding
- Can delete own custom foods
- Cannot delete default foods
- UI is clean and intuitive

---

#### Session 10: Meal Entry Backend
**Duration:** 1.5 hours  
**Prerequisites:** Session 9 complete  
**Goal:** API for logging and retrieving meals

**Tasks:**
1. Implement MealEntryService.java:
```java
@Service
@RequiredArgsConstructor
public class MealEntryService {
    
    private final MealEntryRepository mealEntryRepository;
    private final UserRepository userRepository;
    private final FoodRepository foodRepository;
    
    public MealEntry logMeal(Long userId, MealEntryRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Food food = foodRepository.findById(request.getFoodId())
            .orElseThrow(() -> new ResourceNotFoundException("Food not found"));
        
        MealEntry entry = MealEntry.builder()
            .user(user)
            .food(food)
            .entryDate(request.getEntryDate())
            .entryTime(request.getEntryTime())
            .build();
        
        return mealEntryRepository.save(entry);
    }
    
    public List<MealEntry> getMealsForDate(Long userId, LocalDate date) {
        return mealEntryRepository.findByUserIdAndEntryDateOrderByEntryTimeAsc(userId, date);
    }
    
    public Integer getTotalCaloriesForDate(Long userId, LocalDate date) {
        return mealEntryRepository.sumCaloriesForUserAndDate(userId, date);
    }
    
    public void deleteMealEntry(Long entryId, Long userId) {
        MealEntry entry = mealEntryRepository.findById(entryId)
            .orElseThrow(() -> new ResourceNotFoundException("Meal entry not found"));
        
        if (!entry.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Not authorized to delete this entry");
        }
        
        mealEntryRepository.delete(entry);
    }
}
```

2. Create MealEntryController.java:
```java
@RestController
@RequestMapping("/api/meal-entries")
@CrossOrigin(origins = "http://localhost:3000")
public class MealEntryController {
    
    @Autowired
    private MealEntryService mealEntryService;
    
    @PostMapping
    public ResponseEntity<?> logMeal(
            HttpServletRequest request,
            @Valid @RequestBody MealEntryRequest mealRequest) {
        Long userId = extractUserIdFromRequest(request);
        MealEntry entry = mealEntryService.logMeal(userId, mealRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse(true, "Meal logged", entry));
    }
    
    @GetMapping("/date/{date}")
    public ResponseEntity<?> getMealsByDate(
            HttpServletRequest request,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        Long userId = extractUserIdFromRequest(request);
        List<MealEntry> meals = mealEntryService.getMealsForDate(userId, date);
        Integer totalCalories = mealEntryService.getTotalCaloriesForDate(userId, date);
        
        Map<String, Object> response = new HashMap<>();
        response.put("meals", meals);
        response.put("totalCalories", totalCalories);
        response.put("date", date);
        
        return ResponseEntity.ok(new ApiResponse(true, null, response));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMeal(
            HttpServletRequest request,
            @PathVariable Long id) {
        Long userId = extractUserIdFromRequest(request);
        mealEntryService.deleteMealEntry(id, userId);
        return ResponseEntity.ok(new ApiResponse(true, "Meal deleted", null));
    }
}
```

3. Create DashboardService.java & DashboardController.java:
   - GET /api/dashboard/today endpoint
   - Returns: allowed intake, consumed, remaining, today's meals
   - DTO: DashboardResponse.java

**Success Criteria:**
- Can POST meal entry via Postman
- GET meals by date returns all entries
- Total calories calculated correctly
- Can delete meal entry
- Dashboard endpoint returns complete summary

**Test in Postman:**
```json
// POST http://localhost:8080/api/meal-entries
{
  "foodId": 1,
  "entryDate": "2024-03-15",
  "entryTime": "08:30:00"
}
```

---

#### Session 11: Dashboard & Meal Logging Frontend
**Duration:** 2 hours  
**Prerequisites:** Session 10 complete  
**Goal:** Complete functional dashboard

**Tasks:**
1. Create mealService.js & dashboardService.js

2. Implement Dashboard.js (see earlier implementation)

3. Create MealLogger.js component:
   - Dropdown to select food (searchable is nice)
   - Date picker (default: today)
   - Time picker (default: current time)
   - Submit button
   - Success/error messages

4. Create TodaysMeals.js:
   - List of meals logged today
   - Grouped by meal type
   - Show time, food name, calories
   - Delete button for each entry
   - Running total of calories

5. Implement CalorieBar.js (see earlier implementation)

6. Add CSS styling to make dashboard look good:
   - Progress bar with color gradient
   - Cards for different sections
   - Responsive layout

**Success Criteria:**
- Dashboard displays current calorie status
- Progress bar visually shows consumption
- Can log a meal from dashboard
- Dashboard updates immediately after logging
- Today's meals display in organized list
- Can delete a meal entry
- Total calories update in real-time

---

### Phase 4: History & Weight Tracking (Sessions 12-15)

#### Session 12: History Page Backend
**Duration:** 1 hour  
**Prerequisites:** Session 11 complete  
**Goal:** API for historical data retrieval

**Tasks:**
1. Update MealEntryController with history endpoint:
```java
@GetMapping("/history")
public ResponseEntity<?> getMealHistory(
        HttpServletRequest request,
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
    Long userId = extractUserIdFromRequest(request);
    
    List<MealEntry> meals = mealEntryService.getMealsForDate(userId, date);
    Integer totalCalories = mealEntryService.getTotalCaloriesForDate(userId, date);
    
    // Get weight for that date
    Optional<WeightEntry> weightEntry = weightEntryRepository
        .findByUserIdAndEntryDate(userId, date);
    
    // Group meals by meal type
    Map<MealType, List<MealEntry>> mealsByType = meals.stream()
        .collect(Collectors.groupingBy(me -> me.getFood().getMealType()));
    
    Map<String, Object> response = new HashMap<>();
    response.put("date", date);
    response.put("totalCalories", totalCalories);
    response.put("mealsByType", mealsByType);
    response.put("weightEntry", weightEntry.orElse(null));
    
    return ResponseEntity.ok(new ApiResponse(true, null, response));
}
```

2. Create helper method to get date range of available data:
```java
@GetMapping("/date-range")
public ResponseEntity<?> getAvailableDateRange(HttpServletRequest request) {
    Long userId = extractUserIdFromRequest(request);
    LocalDate firstEntry = mealEntryRepository.findFirstDateForUser(userId);
    LocalDate lastEntry = LocalDate.now();
    
    Map<String, LocalDate> range = new HashMap<>();
    range.put("startDate", firstEntry);
    range.put("endDate", lastEntry);
    
    return ResponseEntity.ok(new ApiResponse(true, null, range));
}
```

**Success Criteria:**
- GET /api/meal-entries/history?date=2024-03-15 returns organized data
- Meals grouped by meal type
- Total calories calculated
- Weight entry included if exists

---

#### Session 13: History Page Frontend
**Duration:** 1.5-2 hours  
**Prerequisites:** Session 12 complete  
**Goal:** Users can browse past days

**Tasks:**
1. Create historyService.js:
```javascript
export const getHistoryForDate = async (date) => {
  const response = await api.get(`/meal-entries/history`, {
    params: { date: date }
  });
  return response.data;
};
```

2. Create History.js:
   - Date picker/calendar to select date
   - Previous/Next day navigation buttons
   - Display selected date prominently
   - Show DayView component with data

3. Create DayView.js:
   - Display total calories for the day
   - Show weight if logged that day
   - Render MealTypeSection for each meal type

4. Create MealTypeSection.js:
   - Expandable/collapsible section
   - Show meal type name
   - List all meals under that type with times
   - Show subtotal calories for that meal type

Example structure:
```jsx
<div className="day-view">
  <h2>{date}</h2>
  <div className="day-summary">
    <p>Total Calories: {totalCalories}</p>
    {weight && <p>Weight: {weight} kg</p>}
  </div>
  
  <MealTypeSection title="Breakfast" meals={breakfastMeals} />
  <MealTypeSection title="Lunch" meals={lunchMeals} />
  <MealTypeSection title="Snacks" meals={snackMeals} />
  <MealTypeSection title="Dinner" meals={dinnerMeals} />
  <MealTypeSection title="Other" meals={otherMeals} />
</div>
```

5. Add CSS for clean historical view

**Success Criteria:**
- Can navigate through different dates
- Each date shows organized meal history
- Empty meal types are handled gracefully
- Can expand/collapse meal type sections
- Weight displays if logged for that date

---

#### Session 14: Weight Tracking Backend
**Duration:** 1 hour  
**Prerequisites:** Session 13 complete  
**Goal:** Weight entry and retrieval API

**Tasks:**
1. Implement WeightEntryService.java:
```java
@Service
@RequiredArgsConstructor
public class WeightEntryService {
    
    private final WeightEntryRepository weightEntryRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    
    @Transactional
    public WeightEntry logWeight(Long userId, WeightEntryRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Check if entry exists for this date
        Optional<WeightEntry> existing = weightEntryRepository
            .findByUserIdAndEntryDate(userId, request.getEntryDate());
        
        WeightEntry entry;
        if (existing.isPresent()) {
            // Update existing entry
            entry = existing.get();
            entry.setWeight(request.getWeight());
        } else {
            // Create new entry
            entry = WeightEntry.builder()
                .user(user)
                .entryDate(request.getEntryDate())
                .weight(request.getWeight())
                .build();
        }
        
        entry = weightEntryRepository.save(entry);
        
        // Update user's current weight if this is today or latest entry
        Optional<WeightEntry> latest = weightEntryRepository
            .findFirstByUserIdOrderByEntryDateDesc(userId);
        if (latest.isPresent() && latest.get().getId().equals(entry.getId())) {
            user.setWeight(request.getWeight());
            // Recalculate BMI
            user.setBmi(CalorieCalculator.calculateBMI(request.getWeight(), user.getHeight()));
            userRepository.save(user);
        }
        
        return entry;
    }
    
    public List<WeightEntry> getAllWeightEntries(Long userId) {
        return weightEntryRepository.findByUserIdOrderByEntryDateAsc(userId);
    }
    
    public List<WeightEntry> getWeightEntriesInRange(
            Long userId, LocalDate startDate, LocalDate endDate) {
        return weightEntryRepository
            .findByUserIdAndEntryDateBetweenOrderByEntryDateAsc(userId, startDate, endDate);
    }
}
```

2. Create WeightEntryController.java:
```java
@RestController
@RequestMapping("/api/weight-entries")
@CrossOrigin(origins = "http://localhost:3000")
public class WeightEntryController {
    
    @Autowired
    private WeightEntryService weightEntryService;
    
    @PostMapping
    public ResponseEntity<?> logWeight(
            HttpServletRequest request,
            @Valid @RequestBody WeightEntryRequest weightRequest) {
        Long userId = extractUserIdFromRequest(request);
        WeightEntry entry = weightEntryService.logWeight(userId, weightRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse(true, "Weight logged", entry));
    }
    
    @GetMapping
    public ResponseEntity<?> getWeightEntries(
            HttpServletRequest request,
            @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        Long userId = extractUserIdFromRequest(request);
        
        List<WeightEntry> entries;
        if (startDate != null && endDate != null) {
            entries = weightEntryService.getWeightEntriesInRange(userId, startDate, endDate);
        } else {
            entries = weightEntryService.getAllWeightEntries(userId);
        }
        
        return ResponseEntity.ok(new ApiResponse(true, null, entries));
    }
}
```

**Success Criteria:**
- Can POST weight entry
- Duplicate date updates existing entry
- User's current weight updates when logging today's weight
- GET all weight entries returns chronological list
- Date range filtering works

---

#### Session 15: Weight Tracking Frontend with Graph
**Duration:** 2 hours  
**Prerequisites:** Session 14 complete  
**Goal:** Visual weight tracking with Chart.js

**Tasks:**
1. Create weightService.js:
```javascript
export const logWeight = async (weightData) => {
  const response = await api.post('/weight-entries', weightData);
  return response.data;
};

export const getAllWeightEntries = async () => {
  const response = await api.get('/weight-entries');
  return response.data;
};

export const getWeightEntriesInRange = async (startDate, endDate) => {
  const response = await api.get('/weight-entries', {
    params: { startDate, endDate }
  });
  return response.data;
};
```

2. Create WeightGraph.js component using Chart.js:
```javascript
import React from 'react';
import { Line } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
} from 'chart.js';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
);

const WeightGraph = ({ weightEntries, goalWeight }) => {
  // Prepare data for Chart.js
  const data = {
    labels: weightEntries.map(entry => entry.entryDate),
    datasets: [
      {
        label: 'Weight (kg)',
        data: weightEntries.map(entry => entry.weight),
        borderColor: 'rgb(75, 192, 192)',
        backgroundColor: 'rgba(75, 192, 192, 0.2)',
        tension: 0.1
      },
      {
        label: 'Goal Weight',
        data: weightEntries.map(() => goalWeight),
        borderColor: 'rgb(255, 99, 132)',
        backgroundColor: 'rgba(255, 99, 132, 0.2)',
        borderDash: [5, 5],
        pointRadius: 0
      }
    ]
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top',
      },
      title: {
        display: true,
        text: 'Weight Progress'
      }
    },
    scales: {
      y: {
        beginAtZero: false,
        title: {
          display: true,
          text: 'Weight (kg)'
        }
      },
      x: {
        title: {
          display: true,
          text: 'Date'
        }
      }
    }
  };

  return (
    <div style={{ height: '400px' }}>
      <Line data={data} options={options} />
    </div>
  );
};

export default WeightGraph;
```

3. Create WeightEntryForm.js:
```javascript
import React, { useState } from 'react';

const WeightEntryForm = ({ onWeightLogged }) => {
  const [weight, setWeight] = useState('');
  const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      await logWeight({ weight: parseFloat(weight), entryDate: date });
      setWeight('');
      setDate(new Date().toISOString().split('T')[0]);
      onWeightLogged(); // Refresh parent component
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to log weight');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="weight-entry-form">
      <h3>Log Weight</h3>
      {error && <div className="error">{error}</div>}
      
      <div className="form-group">
        <label>Weight (kg)</label>
        <input
          type="number"
          step="0.1"
          min="30"
          max="300"
          value={weight}
          onChange={(e) => setWeight(e.target.value)}
          required
        />
      </div>

      <div className="form-group">
        <label>Date</label>
        <input
          type="date"
          value={date}
          max={new Date().toISOString().split('T')[0]}
          onChange={(e) => setDate(e.target.value)}
          required
        />
      </div>

      <button type="submit" disabled={loading}>
        {loading ? 'Logging...' : 'Log Weight'}
      </button>
    </form>
  );
};

export default WeightEntryForm;
```

4. Create WeightTracking.js (main component):
```javascript
import React, { useState, useEffect } from 'react';
import { getAllWeightEntries } from '../../services/weightService';
import { useAuth } from '../../context/AuthContext';
import WeightGraph from './WeightGraph';
import WeightEntryForm from './WeightEntryForm';
import Loading from '../common/Loading';
import ErrorMessage from '../common/ErrorMessage';
import './WeightTracking.css';

const WeightTracking = () => {
  const { user } = useAuth();
  const [weightEntries, setWeightEntries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchWeightEntries = async () => {
    try {
      setLoading(true);
      const response = await getAllWeightEntries();
      setWeightEntries(response.data);
      setError(null);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load weight entries');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchWeightEntries();
  }, []);

  const handleWeightLogged = () => {
    fetchWeightEntries(); // Refresh graph
  };

  if (loading) return <Loading />;
  if (error) return <ErrorMessage message={error} />;

  const latestWeight = weightEntries.length > 0 
    ? weightEntries[weightEntries.length - 1].weight 
    : user.weight;
  const startWeight = weightEntries.length > 0 
    ? weightEntries[0].weight 
    : user.weight;
  const weightChange = latestWeight - startWeight;

  return (
    <div className="weight-tracking">
      <h1>Weight Tracking</h1>

      <div className="weight-stats">
        <div className="stat-card">
          <h3>Current Weight</h3>
          <p className="stat-value">{latestWeight} kg</p>
        </div>
        <div className="stat-card">
          <h3>Goal Weight</h3>
          <p className="stat-value">{user.goal} kg</p>
        </div>
        <div className="stat-card">
          <h3>Progress</h3>
          <p className={`stat-value ${weightChange < 0 ? 'positive' : 'negative'}`}>
            {weightChange > 0 ? '+' : ''}{weightChange.toFixed(1)} kg
          </p>
        </div>
      </div>

      <div className="weight-graph-container">
        {weightEntries.length > 0 ? (
          <WeightGraph 
            weightEntries={weightEntries} 
            goalWeight={user.goal}
          />
        ) : (
          <p>No weight entries yet. Start logging your weight to see your progress!</p>
        )}
      </div>

      <div className="weight-entry-section">
        <WeightEntryForm onWeightLogged={handleWeightLogged} />
      </div>

      <div className="weight-history">
        <h3>Weight History</h3>
        <table>
          <thead>
            <tr>
              <th>Date</th>
              <th>Weight (kg)</th>
              <th>Change</th>
            </tr>
          </thead>
          <tbody>
            {weightEntries.map((entry, index) => {
              const prevWeight = index > 0 ? weightEntries[index - 1].weight : entry.weight;
              const change = entry.weight - prevWeight;
              return (
                <tr key={entry.id}>
                  <td>{entry.entryDate}</td>
                  <td>{entry.weight}</td>
                  <td className={change < 0 ? 'positive' : change > 0 ? 'negative' : ''}>
                    {index > 0 ? `${change > 0 ? '+' : ''}${change.toFixed(1)}` : '-'}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default WeightTracking;
```

5. Add CSS for weight tracking page

**Success Criteria:**
- Weight tracking page displays graph with all entries
- Graph shows actual weight vs goal weight
- Can log new weight entry
- Graph updates immediately after logging
- Weight history table shows all entries
- Current stats display correctly (current, goal, progress)

---

### Phase 5: Polish & Enhancement (Sessions 16-20)

#### Session 16: Navbar & Navigation
**Duration:** 1 hour  
**Prerequisites:** Session 15 complete  
**Goal:** Complete navigation system

**Tasks:**
1. Create Navbar.js:
```javascript
import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import './Navbar.css';

const Navbar = () => {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (!isAuthenticated) {
    return (
      <nav className="navbar">
        <div className="navbar-brand">
          <Link to="/">CalorieTracker</Link>
        </div>
        <div className="navbar-menu">
          <Link to="/login">Login</Link>
          <Link to="/register">Register</Link>
        </div>
      </nav>
    );
  }

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <Link to="/dashboard">CalorieTracker</Link>
      </div>
      <div className="navbar-menu">
        <Link to="/dashboard">Dashboard</Link>
        <Link to="/foods">Foods</Link>
        <Link to="/history">History</Link>
        <Link to="/weight">Weight</Link>
        <Link to="/profile">Profile</Link>
        <button onClick={handleLogout} className="logout-btn">
          Logout ({user?.name})
        </button>
      </div>
    </nav>
  );
};

export default Navbar;
```

2. Add mobile-responsive hamburger menu (optional but recommended)

3. Style navbar with CSS:
   - Sticky position at top
   - Consistent color scheme
   - Active link highlighting
   - Smooth transitions

**Success Criteria:**
- Navbar displays on all pages
- All navigation links work correctly
- User name shown when logged in
- Logout button works
- Responsive on mobile devices

---

#### Session 17: UI/UX Improvements & Styling
**Duration:** 2-3 hours  
**Prerequisites:** Session 16 complete  
**Goal:** Polish the entire application

**Tasks:**
1. Choose and implement a consistent design system:
   - Option A: Custom CSS with variables
   - Option B: Bootstrap integration
   - Option C: Material-UI components

2. Define CSS variables for theming:
```css
:root {
  --primary-color: #4CAF50;
  --secondary-color: #2196F3;
  --danger-color: #f44336;
  --warning-color: #ff9800;
  --success-color: #4CAF50;
  --text-color: #333;
  --bg-color: #f5f5f5;
  --card-bg: #ffffff;
  --border-radius: 8px;
  --box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
```

3. Create consistent components:
   - Loading.js - Spinner component
   - ErrorMessage.js - Error display
   - SuccessMessage.js - Success feedback
   - Modal.js - Reusable modal (optional)
   - Button.js - Styled button variants

4. Improve form styling:
   - Consistent input styling
   - Better validation feedback
   - Clear error messages
   - Loading states on submit buttons

5. Improve CalorieBar visualization:
   - Gradient colors (green → yellow → red)
   - Smooth animations
   - Better typography
   - Responsive sizing

6. Add loading states everywhere:
   - Skeleton screens for data loading
   - Disabled states for buttons during API calls
   - Progress indicators

7. Add toast notifications for user actions:
   - "Meal logged successfully"
   - "Food added"
   - "Weight recorded"
   - Error notifications

8. Mobile responsiveness:
   - Test on mobile viewport
   - Adjust layouts for small screens
   - Make forms mobile-friendly
   - Touch-friendly buttons

9. Add transitions and animations:
   - Smooth page transitions
   - Card hover effects
   - Button interactions
   - Graph animations

**Success Criteria:**
- Consistent visual design across all pages
- All forms have proper validation feedback
- Loading states prevent duplicate submissions
- Responsive on mobile, tablet, desktop
- Smooth user experience
- Professional appearance

---

#### Session 18: Error Handling & Validation
**Duration:** 1.5 hours  
**Prerequisites:** Session 17 complete  
**Goal:** Robust error handling

**Tasks:**
1. Backend: Improve GlobalExceptionHandler:
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ApiResponse(false, ex.getMessage(), null));
    }
    
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ApiResponse(false, ex.getMessage(), null));
    }
    
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ApiResponse(false, ex.getMessage(), null));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ApiResponse(false, "Validation failed", errors));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiResponse(false, "An error occurred", null));
    }
}
```

2. Frontend: Create error handling utilities:
```javascript
// utils/errorHandler.js
export const handleApiError = (error) => {
  if (error.response) {
    // Server responded with error
    return error.response.data.message || 'An error occurred';
  } else if (error.request) {
    // Request made but no response
    return 'No response from server. Please check your connection.';
  } else {
    // Something else happened
    return error.message || 'An unexpected error occurred';
  }
};
```

3. Add form validation on frontend:
   - Email format validation
   - Password strength requirements
   - Number range validations
   - Required field checks
   - Real-time validation feedback

4. Add network error handling:
   - Retry logic for failed requests
   - Offline detection
   - Timeout handling

5. Add boundary error components:
```javascript
// ErrorBoundary.js - catches React errors
class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true };
  }

  componentDidCatch(error, errorInfo) {
    console.error('Error caught by boundary:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return <h1>Something went wrong. Please refresh the page.</h1>;
    }
    return this.props.children;
  }
}
```

**Success Criteria:**
- All API errors display user-friendly messages
- Form validation prevents invalid submissions
- Network errors handled gracefully
- No unhandled promise rejections
- Error boundaries catch React errors
- Validation messages are clear and helpful

---

#### Session 19: Additional Features & Enhancements
**Duration:** 2-3 hours  
**Prerequisites:** Session 18 complete  
**Goal:** Add nice-to-have features

**Tasks:**
1. **Edit functionality for meal entries:**
   - Add edit button to meal entries
   - Modal or inline form to change food or time
   - Update API endpoint

2. **Search and filter foods:**
   - Search bar on food list page
   - Filter by meal type
   - Sort options (name, calories)

3. **Export data feature:**
   - Export weight data as CSV
   - Export meal history for date range
   - Download button functionality

4. **Dashboard statistics:**
   - Weekly average calories
   - Calories consumed vs target (7-day view)
   - Most logged foods
   - Streak counter (consecutive days logging)

5. **Macro nutrients (optional extension):**
   - Add protein, carbs, fat fields to Food model
   - Display macro breakdown on dashboard
   - Pie chart for macros

6. **Settings page:**
   - Change password
   - Notification preferences
   - Display units (kg vs lbs, optional)
   - Dark mode toggle (optional)

7. **Quick add recent foods:**
   - Show recently logged foods on dashboard
   - One-click to log again

8. **Goal progress indicators:**
   - Days to reach goal weight
   - Expected date based on weekly goal
   - Progress percentage

**Implementation Priority:**
- High: Edit meal entries, food search/filter
- Medium: Dashboard statistics, quick add recent
- Low: Export data, macros, settings page

**Success Criteria:**
- At least 3-4 additional features implemented
- Features work smoothly with existing functionality
- UI remains clean and not cluttered
- Performance not impacted

---

#### Session 20: Testing, Bug Fixes & Deployment
**Duration:** 2-3 hours  
**Prerequisites:** Session 19 complete  
**Goal:** Production-ready application

**Tasks:**
1. **User Acceptance Testing:**
   - Test complete user journey:
     - Register → Dashboard → Log meals → View history → Track weight
   - Test all CRUD operations
   - Test with multiple users
   - Test edge cases:
     - Empty states
     - Very long food names
     - Extreme weight values
     - Date boundaries

2. **Bug fixing:**
   - Fix any discovered issues
   - Handle edge cases
   - Improve error messages

3. **Performance optimization:**
   - Add indexes to frequently queried columns
   - Optimize React re-renders
   - Lazy load components
   - Compress images

4. **Security review:**
   - Verify password hashing
   - Check JWT expiration
   - Validate all user inputs
   - SQL injection prevention (handled by JPA)
   - XSS prevention

5. **Documentation:**
   - README.md with setup instructions
   - API documentation (optional: Swagger)
   - User guide (optional)
   - Comment complex code

6. **Deployment preparation:**
   - Environment variables for production
   - CORS configuration for production domain
   - Database migration strategy
   - Build production React bundle

7. **Optional: Deploy to cloud:**
   - **Backend options:**
     - Heroku (free tier)
     - Railway.app
     - AWS Elastic Beanstalk
   - **Frontend options:**
     - Vercel
     - Netlify
     - GitHub Pages
   - **Database:**
     - Railway MySQL
     - AWS RDS
     - PlanetScale (free tier)

**Deployment Steps:**
```bash
# Frontend build
cd calorie-tracker-frontend
npm run build

# Backend package
cd calorie-tracker-backend
mvn clean package

# Deploy JAR and build folder to hosting services
```

**Success Criteria:**
- All major bugs fixed
- Application tested end-to-end
- README documentation complete
- Security review passed
- Optional: Successfully deployed to cloud

---

## Business Logic & Calculations

### Detailed Formula Implementations

#### BMI Calculation
```
BMI = weight (kg) / height (m)²

Example:
Weight: 75 kg
Height: 175 cm = 1.75 m
BMI = 75 / (1.75 × 1.75) = 75 / 3.0625 = 24.49

BMI Categories:
- Underweight: < 18.5
- Normal: 18.5 - 24.9
- Overweight: 25.0 - 29.9
- Obese: ≥ 30.0
```

#### Basal Metabolic Rate (Mifflin-St Jeor Equation)
```
For men: BMR = (10 × weight in kg) + (6.25 × height in cm) - (5 × age) + 5
For women: BMR = (10 × weight in kg) + (6.25 × height in cm) - (5 × age) - 161

Example (assuming male):
Weight: 75 kg
Height: 175 cm
Age: 30 years
BMR = (10 × 75) + (6.25 × 175) - (5 × 30) + 5
BMR = 750 + 1093.75 - 150 + 5
BMR = 1698.75 calories/day
```

#### Total Daily Energy Expenditure (TDEE)
```
TDEE = BMR × Activity Multiplier

Activity Multipliers:
- Sedentary (little/no exercise): 1.2
- Lightly Active (1-3 days/week): 1.375
- Moderately Active (3-5 days/week): 1.55
- Very Active (6-7 days/week): 1.725
- Extremely Active (athlete): 1.9 (not used in our app)

Example:
BMR: 1698.75
Activity: Moderately Active (1.55)
TDEE = 1698.75 × 1.55 = 2633 calories/day
```

#### Calorie Adjustment for Weight Goals
```
Calorie deficit/surplus per kg per week:
- 1 kg weight change ≈ 7700 calories
- To lose/gain 1 kg/week: ±1100 calories/day adjustment
- To lose/gain 0.5 kg/week: ±550 calories/day adjustment

Formula:
Daily Calorie Allowance = TDEE ± (weekly_goal_kg × 1100)

For weight loss: subtract
For weight gain: add
For maintenance: no adjustment

Example (weight loss):
TDEE: 2633 calories/day
Goal: Lose 0.5 kg/week
Adjustment: -550 calories/day
Daily Allowance = 2633 - 550 = 2083 calories/day

Example (weight gain):
TDEE: 2633 calories/day
Goal: Gain 0.5 kg/week
Adjustment: +550 calories/day
Daily Allowance = 2633 + 550 = 3183 calories/day
```

### Safe Limits & Validations

#### Minimum Calorie Intake
- **Women**: Minimum 1200 calories/day
- **Men**: Minimum 1500 calories/day
- **Safety check**: Never allow calculated allowance below these minimums

Implementation:
```java
int calculatedAllowance = tdee + adjustment;
int minimumAllowance = 1500; // Assuming male, add gender field to be more accurate
return Math.max(calculatedAllowance, minimumAllowance);
```

#### Weight Loss Rate Limits
- Maximum recommended: 1.0 kg/week
- Minimum: 0.1 kg/week (for precision)
- Validation in DTO:
```java
@DecimalMin(value = "0.1", message = "Minimum weekly goal is 0.1 kg")
@DecimalMax(value = "1.0", message = "Maximum weekly goal is 1.0 kg")
private BigDecimal weeklyGoal;
```

### Manual Testing Checklist

#### Registration & Login
- [ ] Can register with valid data
- [ ] Cannot register with existing email
- [ ] Password is hashed in database
- [ ] BMI calculated correctly
- [ ] Calorie allowance calculated correctly
- [ ] Can login with valid credentials
- [ ] Cannot login with invalid credentials
- [ ] Token stored in localStorage
- [ ] Token persists after refresh

#### Dashboard
- [ ] Displays current calorie status
- [ ] Progress bar shows correct percentage
- [ ] Can log a meal
- [ ] Total calories update immediately
- [ ] Today's meals display correctly
- [ ] Can delete a meal entry

#### Food Management
- [ ] Food list displays all available foods
- [ ] Can add custom food
- [ ] Custom food appears in list
- [ ] Can delete own custom food
- [ ] Cannot delete default food
- [ ] Image upload works (if implemented)

#### History
- [ ] Can navigate to different dates
- [ ] Displays meals grouped by type
- [ ] Shows total calories for day
- [ ] Shows weight if logged
- [ ] Empty dates handled gracefully

#### Weight Tracking
- [ ] Can log weight
- [ ] Graph displays all entries
- [ ] Goal weight line shows correctly
- [ ] Latest weight updates user profile
- [ ] Cannot log future dates
- [ ] Duplicate date updates existing entry

#### Profile
- [ ] Displays user information
- [ ] Can update weight
- [ ] BMI recalculates
- [ ] Can update activity level
- [ ] Calorie allowance recalculates
- [ ] Changes persist

---

## Reference Materials

### Useful Documentation Links
- **Spring Boot**: https://spring.io/guides
- **Spring Data JPA**: https://spring.io/projects/spring-data-jpa
- **Spring Security**: https://spring.io/projects/spring-security
- **React**: https://react.dev/learn
- **React Router**: https://reactrouter.com/
- **Axios**: https://axios-http.com/docs/intro
- **Chart.js**: https://www.chartjs.org/docs/latest/
- **MySQL**: https://dev.mysql.com/doc/

### Nutrition & Calculation Resources
- **Mifflin-St Jeor Equation**: Used for BMR calculation
- **Harris-Benedict Equation**: Alternative BMR formula
- **Activity Level Multipliers**: Standard TDEE calculations
- **Calorie-to-Weight Conversion**: ~7700 cal = 1 kg

---

## Session Progress Tracker

Use this to track completion across chat sessions:

### Phase 1: Foundation (Complete ☐)
- [ ] Session 1: Environment Setup
- [ ] Session 2: Database & Entities
- [ ] Session 3: Calculations & Utils
- [ ] Session 4: Auth DTOs & Infrastructure

### Phase 2: Authentication (Complete ☐)
- [ ] Session 5: Backend Auth (Service & Controller)
- [ ] Session 6: Frontend Auth (Login & Register)
- [ ] Session 7: User Profile Page

### Phase 3: Core Features (Complete ☐)
- [ ] Session 8: Seed Foods & Food Backend
- [ ] Session 9: Frontend Food Management
- [ ] Session 10: Meal Entry Backend
- [ ] Session 11: Dashboard & Meal Logging

### Phase 4: Tracking (Complete ☐)
- [ ] Session 12: History Backend
- [ ] Session 13: History Frontend
- [ ] Session 14: Weight Entry Backend
- [ ] Session 15: Weight Tracking Frontend with Graph

### Phase 5: Polish (Complete ☐)
- [ ] Session 16: Navbar & Navigation
- [ ] Session 17: UI/UX Improvements
- [ ] Session 18: Error Handling
- [ ] Session 19: Additional Features
- [ ] Session 20: Testing & Deployment

---

## Quick Reference Commands

### Start Development Servers
```bash
# Start MySQL (varies by OS)
# Windows: Start from Services
# Mac: brew services start mysql
# Linux: sudo service mysql start

# Start Spring Boot backend
cd calorie-tracker-backend
./mvnw spring-boot:run
# Or run CalorieTrackerApplication.java from IDE

# Start React frontend
cd calorie-tracker-frontend
npm start
```

### Database Commands
```sql
-- Create database
CREATE DATABASE calorie_tracker;

-- Use database
USE calorie_tracker;

-- Show tables
SHOW TABLES;

-- View table structure
DESCRIBE users;

-- Query data
SELECT * FROM users;
SELECT * FROM foods WHERE user_id IS NULL; -- Default foods
SELECT * FROM meal_entries WHERE entry_date = CURDATE();

-- Delete all data (testing)
TRUNCATE TABLE meal_entries;
TRUNCATE TABLE weight_entries;
TRUNCATE TABLE foods;
TRUNCATE TABLE users;
```

### Maven Commands
```bash
# Clean and build
mvn clean install

# Run tests
mvn test

# Package without tests
mvn package -DskipTests

# Run application
mvn spring-boot:run
```

### npm Commands
```bash
# Install dependencies
npm install

# Start development server
npm start

# Build for production
npm run build

# Run tests
npm test
```

---

## Project Completion Checklist

### Core Functionality ☐
- [ ] User registration with metric calculation
- [ ] User login and authentication
- [ ] Dashboard with calorie tracking
- [ ] Food database management
- [ ] Meal logging system
- [ ] History view by date
- [ ] Weight tracking with graphs
- [ ] User profile management

### Technical Requirements ☐
- [ ] Backend: Spring Boot + MySQL
- [ ] Frontend: React
- [ ] REST API with proper endpoints
- [ ] JWT authentication
- [ ] Proper error handling
- [ ] Input validation
- [ ] CORS configuration
- [ ] Database relationships

### User Experience ☐
- [ ] Intuitive navigation
- [ ] Responsive design
- [ ] Loading indicators
- [ ] Error messages
- [ ] Success feedback
- [ ] Clean visual design
- [ ] Mobile-friendly interface

### Code Quality ☐
- [ ] Well-organized project structure
- [ ] Consistent naming conventions
- [ ] Comments for complex logic
- [ ] No console errors
- [ ] No security vulnerabilities
- [ ] README documentation

### Testing ☐
- [ ] All endpoints tested in Postman
- [ ] Complete user flow tested
- [ ] Edge cases handled
- [ ] Multiple users tested
- [ ] Mobile responsiveness tested

### Optional Enhancements ☐
- [ ] Edit meal entries
- [ ] Search/filter foods
- [ ] Export data
- [ ] Dashboard statistics
- [ ] Macro nutrients
- [ ] Dark mode
- [ ] Deployment to cloud

---

## Next Steps After Completion

### Learning Opportunities
1. **Add more features**: Meal planning, recipe calculator, social features
2. **Improve architecture**: Microservices, event-driven design
3. **Advanced frontend**: Redux, React Query, TypeScript
4. **Advanced backend**: Caching, message queues, GraphQL
5. **DevOps**: CI/CD pipelines, containerization, monitoring
6. **Mobile app**: React Native version
7. **Analytics**: User behavior tracking, insights dashboard

### Portfolio Presentation
- Deploy live version
- Create demo video
- Write blog post about development process
- Document challenges and solutions
- Include in GitHub with good README
- Add to personal website/portfolio

---

## Summary

This calorie tracking application demonstrates full-stack development skills using modern technologies. The implementation plan is designed to be beginner-friendly while building a production-quality application. Each session builds upon the previous one, gradually increasing in complexity while maintaining clear goals and success criteria.

The key to success is:
1. **Follow the plan step-by-step** - don't skip sessions
2. **Test each feature thoroughly** before moving on
3. **Ask for help when stuck** - debugging is part of learning
4. **Iterate and improve** - first version doesn't need to be perfect
5. **Document as you go** - comments and notes help future you

Remember: This is a learning project. Making mistakes and fixing them is how you learn. Take your time, understand each concept, and build something you're proud of!

**Good luck with your calorie tracking app! 🚀**