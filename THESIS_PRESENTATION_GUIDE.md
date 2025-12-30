# Calorie Tracker Application - Thesis Presentation Guide

## 1. Project Overview

**Calorie Tracker** is a full-stack web application designed to help users track their daily calorie intake, manage meals, monitor weight progress, and receive AI-powered nutritional advice. The system combines modern web technologies with artificial intelligence to provide personalized health management.

---

## 2. Tools and Platforms Used

### 2.1 Backend Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 17 | Primary programming language |
| **Spring Boot** | 3.2.1 | Application framework with auto-configuration |
| **Spring Security** | 6.x | Authentication and authorization |
| **Spring Data JPA** | 3.x | Database abstraction and ORM |
| **Spring WebFlux** | 3.x | Reactive HTTP client for AI API calls |
| **MySQL** | 8.0 | Relational database management system |
| **JWT (jjwt)** | 0.11.5 | JSON Web Token authentication |
| **Lombok** | Latest | Boilerplate code reduction |
| **Maven** | 3.8+ | Build and dependency management |

### 2.2 Frontend Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| **React** | 18.2.0 | UI component library |
| **Material-UI (MUI)** | 5.15.4 | Component design system |
| **React Router** | 6.21.2 | Client-side routing |
| **Axios** | 1.6.5 | HTTP client for API calls |
| **Chart.js** | 4.4.1 | Data visualization |
| **Luxon** | 3.7.2 | Date/time handling for charts |

### 2.3 External Services

| Service | Purpose |
|---------|---------|
| **Google Gemini API** | AI-powered meal planning, dietary advice, and progress reviews |

### 2.4 Benefits of Technology Choices

- **Spring Boot**: Rapid development with convention over configuration, embedded server, production-ready features
- **React**: Component-based architecture, virtual DOM for performance, large ecosystem
- **Material-UI**: Consistent design language, accessibility built-in, responsive components
- **JWT Authentication**: Stateless authentication ideal for REST APIs, scalable, no server-side session storage
- **MySQL**: ACID compliance, reliable, widely supported, excellent for relational data

---

## 3. System Architecture

### 3.1 Architecture Pattern: **Three-Tier Architecture**

```
┌─────────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                            │
│                    (React Frontend)                              │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐   │
│  │Dashboard│ │  Foods  │ │ Weight  │ │ Advice  │ │  Plan   │   │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              │ HTTP/REST (JSON)
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    BUSINESS LOGIC LAYER                          │
│                    (Spring Boot Backend)                         │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐                │
│  │ Controllers│──│  Services  │──│ Repositories│               │
│  └────────────┘  └────────────┘  └────────────┘                │
│         │              │                │                        │
│         │        ┌─────┴─────┐          │                        │
│         │        │ AI Service │          │                        │
│         │        │  (Gemini)  │          │                        │
│         │        └───────────┘          │                        │
└─────────────────────────────────────────────────────────────────┘
                              │ JPA/JDBC
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      DATA LAYER                                  │
│                      (MySQL 8.0)                                 │
│  ┌───────┐ ┌───────┐ ┌───────────┐ ┌──────────┐ ┌──────┐       │
│  │ users │ │ foods │ │meal_entries│ │weight_ent│ │plans │       │
│  └───────┘ └───────┘ └───────────┘ └──────────┘ └──────┘       │
└─────────────────────────────────────────────────────────────────┘
```

### 3.2 Design Patterns Used

| Pattern | Implementation | Location |
|---------|---------------|----------|
| **MVC (Model-View-Controller)** | Separates data, UI, and control logic | Entire application |
| **Repository Pattern** | Abstracts data access layer | `repository/` package |
| **Service Layer Pattern** | Encapsulates business logic | `service/` package |
| **DTO Pattern** | Data transfer between layers | `dto/` package |
| **Builder Pattern** | Object construction (via Lombok) | Entity classes |
| **Singleton Pattern** | Spring beans by default | All `@Service`, `@Component` |
| **Filter Chain Pattern** | Security request processing | `JwtAuthenticationFilter` |
| **Context Pattern** | Global state management | React `AuthContext` |
| **Provider Pattern** | Dependency injection | `AuthProvider`, `ThemeProvider` |

---

## 4. Algorithms and Calculations

### 4.1 BMI Calculation
```
BMI = weight (kg) / height² (m)
```

**BMI Categories:**
- Underweight: < 18.5
- Normal: 18.5 - 24.9
- Overweight: 25 - 29.9
- Obese: ≥ 30

### 4.2 BMR Calculation (Mifflin-St Jeor Equation)

The **Mifflin-St Jeor equation** (1990) is the most accurate for modern populations:

```
Men:   BMR = (10 × weight_kg) + (6.25 × height_cm) - (5 × age) + 5
Women: BMR = (10 × weight_kg) + (6.25 × height_cm) - (5 × age) - 161
```

### 4.3 TDEE Calculation (Total Daily Energy Expenditure)

```
TDEE = BMR × Activity Multiplier
```

**Activity Multipliers:**
| Level | Multiplier | Description |
|-------|------------|-------------|
| Sedentary | 1.2 | Little/no exercise |
| Lightly Active | 1.375 | Light exercise 1-3 days/week |
| Moderately Active | 1.55 | Moderate exercise 3-5 days/week |
| Very Active | 1.725 | Hard exercise 6-7 days/week |

### 4.4 Daily Calorie Allowance

```
Calorie Adjustment = weekly_goal_kg × 1100 calories

LOSE:     Allowance = TDEE - Calorie Adjustment
GAIN:     Allowance = TDEE + Calorie Adjustment
MAINTAIN: Allowance = TDEE
```

*Note: 1 kg of body fat ≈ 7700 calories, distributed over 7 days ≈ 1100 cal/day*

---

## 5. System Flows

### 5.1 User Registration Flow

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│  User    │───▶│ Frontend │───▶│ Backend  │───▶│ Database │
│  Input   │    │ Validate │    │ Process  │    │  Store   │
└──────────┘    └──────────┘    └──────────┘    └──────────┘
                                      │
                                      ▼
                              ┌──────────────┐
                              │ Calculate:   │
                              │ - BMI        │
                              │ - BMR        │
                              │ - TDEE       │
                              │ - Allowance  │
                              └──────────────┘
                                      │
                                      ▼
                              ┌──────────────┐
                              │ Generate JWT │
                              │ Return Token │
                              └──────────────┘
```

### 5.2 Authentication Flow

```
1. User submits credentials (email/password)
2. Backend validates against database (BCrypt comparison)
3. On success: Generate JWT token with user email as subject
4. Return token to frontend
5. Frontend stores token in localStorage
6. All subsequent requests include: Authorization: Bearer <token>
7. JwtAuthenticationFilter validates token on each request
8. On 401 error: Clear localStorage, redirect to login
```

### 5.3 Meal Logging Flow

```
User selects food → Creates MealEntry → Updates Dashboard
         │                  │                   │
         ▼                  ▼                   ▼
   Food selection    Stores: user_id,    Recalculates:
   from database     food_id, date,      - Consumed calories
   (system/custom)   time               - Remaining calories
                                        - Progress percentage
```

### 5.4 AI Integration Flow

```
┌────────────┐     ┌────────────┐     ┌────────────┐
│   User     │────▶│  Backend   │────▶│  Gemini    │
│  Request   │     │  Service   │     │    API     │
└────────────┘     └────────────┘     └────────────┘
                         │                   │
                         │ Build Prompt:     │
                         │ - User profile    │
                         │ - Meal history    │
                         │ - Weight history  │
                         │                   │
                         ▼                   ▼
                   ┌────────────┐     ┌────────────┐
                   │   Store    │◀────│  AI        │
                   │  Response  │     │ Response   │
                   └────────────┘     └────────────┘
```

---

## 6. System Functions (API Endpoints)

### 6.1 Authentication (`/api/auth`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/register` | Register new user |
| POST | `/login` | Authenticate user |

### 6.2 Dashboard (`/api/dashboard`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Get today's summary |
| GET | `/date/{date}` | Get summary for specific date |

### 6.3 Foods (`/api/foods`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Get all available foods |
| GET | `/meal-type/{type}` | Filter by meal type |
| GET | `/custom` | Get user's custom foods |
| POST | `/` | Create custom food |
| PUT | `/{id}` | Update custom food |
| DELETE | `/{id}` | Delete custom food |

### 6.4 Meal Entries (`/api/meal-entries`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Log a meal |
| GET | `/today` | Get today's meals |
| GET | `/date/{date}` | Get meals by date |
| GET | `/range` | Get meals in date range |
| DELETE | `/{id}` | Delete meal entry |

### 6.5 Weight Entries (`/api/weight-entries`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/` | Log/update weight |
| GET | `/` | Get all weight entries |
| GET | `/latest` | Get latest entry |
| DELETE | `/{id}` | Delete weight entry |

### 6.6 AI Services
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/advice/chat` | Chat with AI advisor |
| GET | `/api/plan` | Get meal plan |
| POST | `/api/plan/generate` | Generate AI meal plan |
| GET | `/api/review` | Get progress review |
| POST | `/api/review/generate` | Generate AI review |

---

## 7. Database Schema

### 7.1 Entity Relationship Diagram

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│    users    │       │    foods    │       │meal_entries │
├─────────────┤       ├─────────────┤       ├─────────────┤
│ id (PK)     │◀──┐   │ id (PK)     │◀──┐   │ id (PK)     │
│ name        │   │   │ name        │   │   │ user_id(FK) │──┐
│ email       │   │   │ calories    │   │   │ food_id(FK) │──┤
│ password    │   │   │ meal_type   │   │   │ entry_date  │  │
│ dob         │   │   │ user_id(FK) │───┘   │ entry_time  │  │
│ sex         │   │   └─────────────┘       └─────────────┘  │
│ weight      │   │                                          │
│ height      │   └──────────────────────────────────────────┘
│ bmi         │
│ goal        │       ┌─────────────┐       ┌─────────────┐
│ goal_type   │       │weight_entries│      │   plans     │
│ weekly_goal │       ├─────────────┤       ├─────────────┤
│ daily_intake│       │ id (PK)     │       │ id (PK)     │
└─────────────┘       │ user_id(FK) │───┐   │ user_id(FK) │───┐
       │              │ entry_date  │   │   │ text        │   │
       │              │ weight      │   │   └─────────────┘   │
       │              └─────────────┘   │                     │
       │                                │   ┌─────────────┐   │
       └────────────────────────────────┴───│  reviews    │───┘
                                            ├─────────────┤
                                            │ id (PK)     │
                                            │ user_id(FK) │
                                            │ text        │
                                            └─────────────┘
```

---

## 8. How to Make API Requests

### 8.1 Registration Request
```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "securePassword123",
  "dob": "1990-05-15",
  "sex": "MALE",
  "weight": 75.5,
  "height": 175,
  "activityLevel": "MODERATELY_ACTIVE",
  "goal": 70,
  "weeklyGoal": 0.5,
  "goalType": "LOSE"
}
```

### 8.2 Login Request
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "securePassword123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "bmi": 24.65,
  "allowedDailyIntake": 1850
}
```

### 8.3 Authenticated Request Example
```http
GET /api/dashboard
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 8.4 Log Meal Request
```http
POST /api/meal-entries
Authorization: Bearer <token>
Content-Type: application/json

{
  "foodId": 5,
  "entryDate": "2024-12-30",
  "entryTime": "12:30:00"
}
```

---

## 9. Practical Benefits and Advantages

### 9.1 Purpose of the System

1. **Health Awareness**: Helps users understand their caloric intake and nutritional habits
2. **Goal Achievement**: Provides calculated targets based on scientific formulas
3. **Progress Tracking**: Visual charts and statistics for motivation
4. **Personalized Guidance**: AI-powered advice tailored to individual profiles
5. **Convenience**: Easy meal logging with pre-defined food database

### 9.2 Advantages Over Similar Systems

| Feature | Calorie Tracker | Typical Apps |
|---------|-----------------|--------------|
| **AI Integration** | Gemini-powered personalized advice | Generic tips or none |
| **Scientific Calculations** | Mifflin-St Jeor equation | Often simplified |
| **Custom Foods** | User can add custom foods | Limited or premium |
| **Meal Planning** | AI-generated 7-day plans | Manual or templates |
| **Progress Reviews** | AI analyzes patterns and provides feedback | Basic statistics only |
| **Open Architecture** | Full-stack, customizable | Closed/proprietary |
| **No Subscription** | Self-hosted, no recurring fees | Monthly subscriptions |

### 9.3 Key Differentiators

1. **AI-Powered Personalization**: Unlike static calorie counters, this system uses Google Gemini to provide contextual advice based on user's actual eating patterns and progress.

2. **Scientific Accuracy**: Uses the Mifflin-St Jeor equation (most accurate for modern populations) rather than simplified calculations.

3. **Comprehensive Tracking**: Combines calorie tracking, weight monitoring, meal planning, and AI advice in one integrated system.

4. **Privacy-Focused**: Self-hosted solution means user data stays on their own infrastructure.

5. **Modern Tech Stack**: Built with current best practices (React 18, Spring Boot 3.2, JWT auth) ensuring maintainability and security.

---

## 10. Code Structure Summary

### Backend (`/backend/src/main/java/com/calorietracker/`)
```
├── CalorieTrackerApplication.java  # Main entry point
├── config/                         # Security, JWT, CORS configuration
├── controller/                     # REST API endpoints (10 controllers)
├── dto/                           # Request/Response data transfer objects
│   ├── request/                   # Incoming data validation
│   └── response/                  # Outgoing data formatting
├── exception/                     # Custom exception handling
├── model/                         # JPA entities (User, Food, MealEntry, etc.)
├── repository/                    # Data access layer (Spring Data JPA)
├── service/                       # Business logic (10 services)
└── util/                          # Utility classes (CalorieCalculator)
```

### Frontend (`/frontend/src/`)
```
├── App.js                         # Root component with routing
├── index.js                       # Application entry point
├── context/                       # React Context (AuthContext)
├── services/                      # API service layer (Axios)
└── components/                    # React components
    ├── auth/                      # Login, Register
    ├── dashboard/                 # Dashboard, LogMealModal
    ├── foods/                     # Food management
    ├── weight/                    # Weight tracking with charts
    ├── advice/                    # AI chatbot
    ├── plan/                      # AI meal planning
    ├── profile/                   # User profile management
    ├── history/                   # Historical data view
    └── layout/                    # Navigation layout
```

---

## 11. Running the Application

### Prerequisites
- Java 17+
- Node.js 18+
- MySQL 8.0
- Maven 3.8+

### Backend
```bash
cd backend
mvnw spring-boot:run
# Runs on http://localhost:8080
```

### Frontend
```bash
cd frontend
npm install
npm start
# Runs on http://localhost:3000
```

### Database
```sql
CREATE DATABASE calorie_tracker;
-- Tables are auto-created by JPA (ddl-auto=update)
```

---

## 12. Security Features

1. **Password Encryption**: BCrypt hashing (industry standard)
2. **JWT Authentication**: Stateless, scalable token-based auth
3. **CORS Protection**: Configured for specific origins
4. **Input Validation**: Jakarta Bean Validation on all inputs
5. **SQL Injection Prevention**: JPA parameterized queries
6. **XSS Protection**: React's built-in escaping

---

## 13. Live Coding Guide for Panel Demonstration

This section provides step-by-step instructions for manually coding key modules in real-time during your thesis defense. Each module includes talking points, code to type, and explanations.

---

### 13.1 Preparation Before the Presentation

**Environment Setup Checklist:**
- [ ] IDE open (IntelliJ IDEA / VS Code)
- [ ] MySQL running with `calorie_tracker` database created
- [ ] Terminal ready for running commands
- [ ] Browser open for testing API (Postman or browser dev tools)
- [ ] Have this guide open on a second screen or printed

**Recommended Demonstration Order:**
1. Backend Entity (User.java) - Shows JPA/ORM concepts
2. Repository Interface - Shows Spring Data magic
3. Service Layer - Shows business logic
4. REST Controller - Shows API endpoint creation
5. Frontend Component - Shows React integration

---

### 13.2 Module 1: Creating a JPA Entity (User.java)

**Talking Points:**
> "Let me demonstrate how we define database entities using JPA annotations. This User entity maps directly to our `users` table in MySQL."

**Step-by-Step Code:**

```java
// Step 1: Create the class with JPA annotations
package com.calorietracker.model;

import jakarta.persistence.*;
import lombok.*;

@Entity                          // Marks this as a JPA entity
@Table(name = "users")           // Maps to 'users' table
@Data                            // Lombok: generates getters, setters, toString
@NoArgsConstructor               // Lombok: generates no-arg constructor
@AllArgsConstructor              // Lombok: generates all-arg constructor
@Builder                         // Lombok: enables builder pattern
public class User {

    @Id                          // Primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)  // Unique constraint
    private String email;

    @Column(nullable = false)
    private String password;      // Will be BCrypt hashed

    private Double weight;        // in kg
    private Double height;        // in cm
    private Double bmi;           // Calculated field

    @Enumerated(EnumType.STRING)  // Store enum as string, not ordinal
    private ActivityLevel activityLevel;

    private Integer allowedDailyIntake;  // Calculated calories
}
```

**Explain While Typing:**
- `@Entity` tells JPA this class represents a database table
- `@Table(name = "users")` specifies the exact table name
- `@Id` and `@GeneratedValue` handle primary key auto-generation
- `@Column` constraints map to SQL constraints
- `@Enumerated(EnumType.STRING)` stores enums as readable strings
- Lombok annotations reduce boilerplate by 80%

---

### 13.3 Module 2: Creating a Repository Interface

**Talking Points:**
> "Spring Data JPA eliminates the need to write SQL. We just define an interface, and Spring generates the implementation automatically."

**Step-by-Step Code:**

```java
package com.calorietracker.repository;

import com.calorietracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Spring Data generates: SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);
    
    // Spring Data generates: SELECT EXISTS(SELECT 1 FROM users WHERE email = ?)
    boolean existsByEmail(String email);
}
```

**Explain While Typing:**
- `JpaRepository<User, Long>` - Entity type and ID type
- Method naming convention: `findBy` + `FieldName` = automatic query generation
- `Optional<User>` handles null safety elegantly
- No implementation needed - Spring creates it at runtime!

**Quick Demo:**
> "Let me show you how this works. When I call `userRepository.findByEmail("john@example.com")`, Spring automatically generates and executes the SQL query."

---

### 13.4 Module 3: Creating a Service Class

**Talking Points:**
> "The service layer contains our business logic. Here's where we implement the calorie calculation algorithms."

**Step-by-Step Code:**

```java
package com.calorietracker.service;

import com.calorietracker.model.User;
import com.calorietracker.repository.UserRepository;
import com.calorietracker.util.CalorieCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service                         // Marks as Spring-managed service bean
@RequiredArgsConstructor         // Lombok: constructor injection
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(User user) {
        // 1. Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // 2. Hash the password (never store plain text!)
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 3. Calculate BMI using our formula
        double bmi = CalorieCalculator.calculateBMI(
            user.getWeight(), 
            user.getHeight()
        );
        user.setBmi(bmi);

        // 4. Calculate daily calorie allowance
        int calories = CalorieCalculator.calculateAllowedDailyIntake(
            user.getWeight(),
            user.getHeight(),
            user.getAge(),
            user.getSex(),
            user.getActivityLevel(),
            user.getGoalType(),
            user.getWeeklyGoal()
        );
        user.setAllowedDailyIntake(calories);

        // 5. Save to database and return
        return userRepository.save(user);
    }
}
```

**Explain While Typing:**
- `@Service` registers this as a Spring bean (singleton by default)
- `@RequiredArgsConstructor` creates constructor for `final` fields (dependency injection)
- Password is hashed using BCrypt before storage
- Business logic (BMI, calorie calculation) lives here, not in controller
- `userRepository.save()` handles both INSERT and UPDATE

---

### 13.5 Module 4: Creating a REST Controller

**Talking Points:**
> "Controllers handle HTTP requests and responses. Let me show you how to create a registration endpoint."

**Step-by-Step Code:**

```java
package com.calorietracker.controller;

import com.calorietracker.dto.request.RegisterRequest;
import com.calorietracker.dto.response.AuthResponse;
import com.calorietracker.model.User;
import com.calorietracker.service.UserService;
import com.calorietracker.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController                  // REST API controller (returns JSON)
@RequestMapping("/api/auth")     // Base path for all endpoints
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/register")    // POST /api/auth/register
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        
        // 1. Convert DTO to entity
        User user = User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .password(request.getPassword())
            .weight(request.getWeight())
            .height(request.getHeight())
            .build();

        // 2. Register user (service handles business logic)
        User savedUser = userService.registerUser(user);

        // 3. Generate JWT token
        String token = jwtService.generateToken(savedUser.getEmail());

        // 4. Build and return response
        AuthResponse response = AuthResponse.builder()
            .token(token)
            .userId(savedUser.getId())
            .name(savedUser.getName())
            .email(savedUser.getEmail())
            .bmi(savedUser.getBmi())
            .allowedDailyIntake(savedUser.getAllowedDailyIntake())
            .build();

        return ResponseEntity.ok(response);
    }
}
```

**Explain While Typing:**
- `@RestController` = `@Controller` + `@ResponseBody` (returns JSON automatically)
- `@RequestMapping("/api/auth")` sets the base URL path
- `@PostMapping("/register")` handles POST requests to `/api/auth/register`
- `@Valid` triggers validation on the request DTO
- `@RequestBody` deserializes JSON to Java object
- `ResponseEntity.ok()` returns HTTP 200 with the response body

---

### 13.6 Module 5: Creating a React Component

**Talking Points:**
> "Now let me show you the frontend. This React component calls our API and manages authentication state."

**Step-by-Step Code:**

```javascript
// Login.js - React Login Component
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { TextField, Button, Alert, Paper, Typography } from '@mui/material';
import { useAuth } from '../../context/AuthContext';

const Login = () => {
  // React Router hook for navigation
  const navigate = useNavigate();
  
  // Custom hook for authentication
  const { login } = useAuth();
  
  // Component state using useState hook
  const [formData, setFormData] = useState({
    email: '',
    password: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // Handle input changes
  const handleChange = (e) => {
    setFormData({
      ...formData,                    // Spread existing data
      [e.target.name]: e.target.value // Update changed field
    });
  };

  // Handle form submission
  const handleSubmit = async (e) => {
    e.preventDefault();               // Prevent page reload
    setError('');
    setLoading(true);

    // Call login from AuthContext
    const result = await login(formData.email, formData.password);
    
    if (result.success) {
      navigate('/dashboard');         // Redirect on success
    } else {
      setError(result.message);       // Show error message
    }
    setLoading(false);
  };

  return (
    <Paper elevation={3} sx={{ p: 4, maxWidth: 400, mx: 'auto', mt: 8 }}>
      <Typography variant="h5" align="center" gutterBottom>
        Sign In
      </Typography>
      
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      
      <form onSubmit={handleSubmit}>
        <TextField
          fullWidth
          margin="normal"
          name="email"
          label="Email"
          value={formData.email}
          onChange={handleChange}
          required
        />
        <TextField
          fullWidth
          margin="normal"
          name="password"
          label="Password"
          type="password"
          value={formData.password}
          onChange={handleChange}
          required
        />
        <Button
          type="submit"
          fullWidth
          variant="contained"
          disabled={loading}
          sx={{ mt: 2 }}
        >
          {loading ? 'Signing In...' : 'Sign In'}
        </Button>
      </form>
    </Paper>
  );
};

export default Login;
```

**Explain While Typing:**
- `useState` hook manages component state
- `useNavigate` from React Router handles redirects
- `useAuth` is our custom context hook for authentication
- Spread operator (`...formData`) preserves existing state
- `async/await` handles the API call cleanly
- Material-UI components provide consistent styling

---

### 13.7 Module 6: Calorie Calculator Utility

**Talking Points:**
> "This is the core algorithm of our system - the Mifflin-St Jeor equation for calculating BMR and daily calorie needs."

**Step-by-Step Code:**

```java
package com.calorietracker.util;

import com.calorietracker.model.ActivityLevel;
import com.calorietracker.model.GoalType;
import com.calorietracker.model.Sex;

public class CalorieCalculator {

    /**
     * Calculate BMI (Body Mass Index)
     * Formula: weight(kg) / height(m)²
     */
    public static double calculateBMI(double weightKg, double heightCm) {
        double heightM = heightCm / 100.0;  // Convert cm to meters
        return weightKg / (heightM * heightM);
    }

    /**
     * Calculate BMR using Mifflin-St Jeor Equation (1990)
     * Most accurate formula for modern populations
     * 
     * Men:   BMR = (10 × weight) + (6.25 × height) - (5 × age) + 5
     * Women: BMR = (10 × weight) + (6.25 × height) - (5 × age) - 161
     */
    public static double calculateBMR(double weightKg, double heightCm, 
                                       int age, Sex sex) {
        double bmr = (10 * weightKg) + (6.25 * heightCm) - (5 * age);
        
        if (sex == Sex.MALE) {
            bmr += 5;
        } else {
            bmr -= 161;
        }
        
        return bmr;
    }

    /**
     * Calculate TDEE (Total Daily Energy Expenditure)
     * TDEE = BMR × Activity Multiplier
     */
    public static double calculateTDEE(double bmr, ActivityLevel activity) {
        return bmr * activity.getMultiplier();
    }

    /**
     * Calculate daily calorie allowance based on goal
     * 1 kg of fat ≈ 7700 calories
     * Weekly goal distributed over 7 days ≈ 1100 cal/day per kg
     */
    public static int calculateAllowedDailyIntake(
            double weightKg, double heightCm, int age, Sex sex,
            ActivityLevel activity, GoalType goal, double weeklyGoalKg) {
        
        double bmr = calculateBMR(weightKg, heightCm, age, sex);
        double tdee = calculateTDEE(bmr, activity);
        
        // Calorie adjustment: ~1100 calories per kg per week
        double adjustment = weeklyGoalKg * 1100;
        
        double allowance;
        switch (goal) {
            case LOSE:
                allowance = tdee - adjustment;  // Deficit
                break;
            case GAIN:
                allowance = tdee + adjustment;  // Surplus
                break;
            default:  // MAINTAIN
                allowance = tdee;
        }
        
        // Ensure minimum safe intake (1200 for women, 1500 for men)
        int minimum = (sex == Sex.FEMALE) ? 1200 : 1500;
        return Math.max((int) allowance, minimum);
    }
}
```

**Explain While Typing:**
- BMI formula: weight divided by height squared (in meters)
- Mifflin-St Jeor is the gold standard for BMR calculation
- Activity multipliers range from 1.2 (sedentary) to 1.725 (very active)
- 7700 calories ≈ 1 kg of body fat (scientific constant)
- Safety floor prevents dangerously low calorie targets

---

### 13.8 Module 7: JWT Authentication Filter

**Talking Points:**
> "This filter intercepts every request to validate the JWT token. It's the core of our stateless authentication."

**Step-by-Step Code:**

```java
package com.calorietracker.config;

import com.calorietracker.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        // 1. Extract Authorization header
        String authHeader = request.getHeader("Authorization");
        
        // 2. Check if header exists and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);  // Continue without auth
            return;
        }

        // 3. Extract token (remove "Bearer " prefix)
        String token = authHeader.substring(7);
        
        // 4. Extract username from token
        String username = jwtService.extractUsername(token);

        // 5. Validate and set authentication
        if (username != null && 
            SecurityContextHolder.getContext().getAuthentication() == null) {
            
            UserDetails userDetails = userDetailsService
                .loadUserByUsername(username);

            // 6. Verify token is valid
            if (jwtService.isTokenValid(token, userDetails)) {
                // 7. Create authentication token
                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                
                // 8. Set in security context
                SecurityContextHolder.getContext()
                    .setAuthentication(authToken);
            }
        }

        // 9. Continue filter chain
        filterChain.doFilter(request, response);
    }
}
```

**Explain While Typing:**
- `OncePerRequestFilter` ensures filter runs exactly once per request
- Authorization header format: `Bearer <token>`
- `substring(7)` removes "Bearer " prefix (7 characters)
- `SecurityContextHolder` stores the authenticated user for the request
- If token is invalid, request continues without authentication (will be blocked by security config)

---

### 13.9 Quick Demonstration Scripts

**Demo 1: Test Registration (5 minutes)**
```bash
# Terminal 1: Start backend
cd backend
mvnw spring-boot:run

# Terminal 2: Test API with curl
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Demo User",
    "email": "demo@test.com",
    "password": "password123",
    "dob": "1995-01-15",
    "sex": "MALE",
    "weight": 75,
    "height": 175,
    "activityLevel": "MODERATELY_ACTIVE",
    "goal": 70,
    "weeklyGoal": 0.5
  }'
```

**Demo 2: Test Login and Protected Endpoint (3 minutes)**
```bash
# Login and get token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@test.com","password":"password123"}' \
  | jq -r '.token')

# Access protected endpoint
curl http://localhost:8080/api/dashboard \
  -H "Authorization: Bearer $TOKEN"
```

**Demo 3: Show Database (2 minutes)**
```sql
-- Connect to MySQL and show data
USE calorie_tracker;
SELECT id, name, email, bmi, allowed_daily_intake FROM users;
```

---

### 13.10 Common Questions and Answers

**Q: Why use JWT instead of sessions?**
> "JWT is stateless - the server doesn't need to store session data. This makes the application horizontally scalable. Each request contains all the information needed for authentication."

**Q: Why Mifflin-St Jeor instead of Harris-Benedict?**
> "The Mifflin-St Jeor equation (1990) has been shown in studies to be more accurate for modern populations. Harris-Benedict (1918) tends to overestimate calorie needs by 5-15%."

**Q: How does Spring Data JPA generate queries?**
> "Spring parses the method name at startup. `findByEmail` becomes `SELECT * FROM users WHERE email = ?`. It uses reflection and proxy patterns to generate the implementation."

**Q: Why use React Context instead of Redux?**
> "For this application's scope, Context API is simpler and sufficient. Redux adds complexity that's only justified for larger applications with complex state management needs."

**Q: How is the password secured?**
> "We use BCrypt, which is a one-way hash with built-in salt. Even if the database is compromised, passwords cannot be reversed. BCrypt is also intentionally slow to prevent brute-force attacks."

---

### 13.11 Time Management for Live Coding

| Module | Time | Priority |
|--------|------|----------|
| Entity (User.java) | 5 min | High |
| Repository | 2 min | High |
| Service | 5 min | High |
| Controller | 5 min | High |
| CalorieCalculator | 5 min | Medium |
| JWT Filter | 5 min | Medium |
| React Component | 5 min | Medium |
| API Demo | 3 min | High |

**Total: ~35 minutes of live coding**

**Tips for Success:**
1. **Practice beforehand** - Type each module at least 3 times
2. **Have snippets ready** - For complex imports, have them in a notepad
3. **Explain as you type** - Don't code silently
4. **Handle errors gracefully** - If something breaks, explain why
5. **Keep IDE autocomplete on** - It's a legitimate tool, not cheating

---

*This document serves as a comprehensive guide for presenting the Calorie Tracker thesis project to instructors and panel members.*
