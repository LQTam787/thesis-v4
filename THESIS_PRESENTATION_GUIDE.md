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

*This document serves as a comprehensive guide for presenting the Calorie Tracker thesis project to instructors and panel members.*
