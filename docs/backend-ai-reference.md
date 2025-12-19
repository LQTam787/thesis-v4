# Backend AI Reference Documentation

## Overview
- **Framework**: Spring Boot 3.2.1
- **Language**: Java 17
- **Database**: MySQL 8
- **Authentication**: JWT (jjwt 0.11.5)
- **Build Tool**: Maven
- **Package**: `com.calorietracker`
- **Server Port**: 8080

## Dependencies
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-security
- spring-boot-starter-validation
- mysql-connector-j
- lombok
- jjwt-api, jjwt-impl, jjwt-jackson (0.11.5)

## Configuration (application.properties)
```properties
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/calorie_tracker
spring.datasource.username=root
spring.datasource.password=1234
spring.jpa.hibernate.ddl-auto=update
jwt.secret=<base64-encoded-secret>
jwt.expiration=86400000 (24 hours)
```

---

## Database Schema (Entities)

### User (`users` table)
**File**: `model/User.java`

| Field | Type | Constraints |
|-------|------|-------------|
| id | Long | PK, auto-generated |
| name | String(100) | NOT NULL |
| email | String(100) | NOT NULL, UNIQUE |
| password | String | NOT NULL (BCrypt encoded) |
| dob | LocalDate | NOT NULL |
| sex | Sex (enum) | NOT NULL |
| weight | BigDecimal(5,2) | NOT NULL |
| height | BigDecimal(5,2) | NOT NULL |
| activityLevel | ActivityLevel (enum) | NOT NULL |
| goal | BigDecimal(5,2) | nullable (target weight) |
| goalType | GoalType (enum) | NOT NULL |
| weeklyGoal | BigDecimal(3,2) | NOT NULL |
| allowedDailyIntake | Integer | NOT NULL (calculated) |
| bmi | BigDecimal(4,2) | NOT NULL (calculated) |
| createdAt | LocalDateTime | auto-set |
| updatedAt | LocalDateTime | auto-updated |

**Relationships**:
- OneToMany → Food (customFoods)
- OneToMany → MealEntry (mealEntries)
- OneToMany → WeightEntry (weightEntries)

**Methods**:
- `getAge()`: Returns age calculated from dob

---

### Food (`foods` table)
**File**: `model/Food.java`

| Field | Type | Constraints |
|-------|------|-------------|
| id | Long | PK, auto-generated |
| name | String(100) | NOT NULL |
| image | String(255) | nullable |
| mealType | MealType (enum) | NOT NULL |
| calories | Integer | NOT NULL |
| user_id | Long (FK) | nullable (null = system food) |
| createdAt | LocalDateTime | auto-set |

**Relationships**:
- ManyToOne → User (nullable, if null = system food)

**Methods**:
- `isCustomFood()`: Returns true if user != null

---

### MealEntry (`meal_entries` table)
**File**: `model/MealEntry.java`

| Field | Type | Constraints |
|-------|------|-------------|
| id | Long | PK, auto-generated |
| user_id | Long (FK) | NOT NULL |
| food_id | Long (FK) | NOT NULL |
| entryDate | LocalDate | NOT NULL |
| entryTime | LocalTime | NOT NULL |
| createdAt | LocalDateTime | auto-set |

**Index**: `idx_user_date` on (user_id, entry_date)

**Relationships**:
- ManyToOne → User (LAZY)
- ManyToOne → Food (EAGER)

---

### WeightEntry (`weight_entries` table)
**File**: `model/WeightEntry.java`

| Field | Type | Constraints |
|-------|------|-------------|
| id | Long | PK, auto-generated |
| user_id | Long (FK) | NOT NULL |
| entryDate | LocalDate | NOT NULL |
| weight | BigDecimal(5,2) | NOT NULL |
| createdAt | LocalDateTime | auto-set |

**Unique Constraint**: (user_id, entry_date)

**Relationships**:
- ManyToOne → User (LAZY)

---

## Enums

### Sex (`model/Sex.java`)
- `MALE`
- `FEMALE`

### ActivityLevel (`model/ActivityLevel.java`)
| Value | Multiplier |
|-------|------------|
| SEDENTARY | 1.2 |
| LIGHTLY_ACTIVE | 1.375 |
| MODERATELY_ACTIVE | 1.55 |
| VERY_ACTIVE | 1.725 |

### GoalType (`model/GoalType.java`)
- `LOSE`
- `MAINTAIN`
- `GAIN`

### MealType (`model/MealType.java`)
- `BREAKFAST`
- `LUNCH`
- `SNACKS`
- `DINNER`
- `OTHER`

---

## Repositories

### UserRepository
**File**: `repository/UserRepository.java`
```java
Optional<User> findByEmail(String email)
boolean existsByEmail(String email)
```

### FoodRepository
**File**: `repository/FoodRepository.java`
```java
@Query("SELECT f FROM Food f WHERE f.user IS NULL OR f.user.id = :userId")
List<Food> findAvailableFoodsForUser(Long userId)

@Query("SELECT f FROM Food f WHERE (f.user IS NULL OR f.user.id = :userId) AND f.mealType = :mealType")
List<Food> findByMealTypeForUser(Long userId, MealType mealType)

List<Food> findByUserIsNull()  // system foods only
List<Food> findByUserId(Long userId)  // user's custom foods only
```

### MealEntryRepository
**File**: `repository/MealEntryRepository.java`
```java
List<MealEntry> findByUserIdAndEntryDate(Long userId, LocalDate entryDate)
List<MealEntry> findByUserIdAndEntryDateBetween(Long userId, LocalDate startDate, LocalDate endDate)
List<MealEntry> findByUserIdAndEntryDateOrderByEntryTimeAsc(Long userId, LocalDate entryDate)

@Query("SELECT COALESCE(SUM(f.calories), 0) FROM MealEntry me JOIN me.food f WHERE me.user.id = :userId AND me.entryDate = :date")
Integer sumCaloriesForUserAndDate(Long userId, LocalDate date)
```

### WeightEntryRepository
**File**: `repository/WeightEntryRepository.java`
```java
List<WeightEntry> findByUserIdOrderByEntryDateAsc(Long userId)
Optional<WeightEntry> findByUserIdAndEntryDate(Long userId, LocalDate entryDate)
Optional<WeightEntry> findFirstByUserIdOrderByEntryDateDesc(Long userId)
List<WeightEntry> findByUserIdAndEntryDateBetweenOrderByEntryDateAsc(Long userId, LocalDate startDate, LocalDate endDate)
```

---

## Services

### UserService
**File**: `service/UserService.java`

| Method | Description |
|--------|-------------|
| `registerUser(UserRegistrationRequest)` | Creates new user, calculates BMI and daily intake, encodes password |
| `getUserByEmail(String)` | Finds user by email or throws ResourceNotFoundException |
| `getUserById(Long)` | Finds user by ID or throws ResourceNotFoundException |
| `updateUserWeight(Long, BigDecimal)` | Updates user weight, recalculates BMI and daily intake |

---

### FoodService
**File**: `service/FoodService.java`

| Method | Description |
|--------|-------------|
| `getAvailableFoods(Long userId)` | Returns system foods + user's custom foods |
| `getFoodsByMealType(Long userId, MealType)` | Returns foods filtered by meal type |
| `getUserCustomFoods(Long userId)` | Returns only user's custom foods |
| `getFoodById(Long foodId, Long userId)` | Returns food if user has access |
| `createFood(FoodRequest, Long userId)` | Creates custom food for user |
| `updateFood(Long foodId, FoodRequest, Long userId)` | Updates custom food (owner only) |
| `deleteFood(Long foodId, Long userId)` | Deletes custom food (owner only) |

**Business Rules**:
- System foods (user=null) cannot be updated or deleted
- Users can only modify their own custom foods

---

### MealEntryService
**File**: `service/MealEntryService.java`

| Method | Description |
|--------|-------------|
| `createMealEntry(MealEntryRequest, Long userId)` | Creates meal entry, validates food access |
| `getMealEntriesByDate(Long userId, LocalDate)` | Returns entries for specific date, ordered by time |
| `getTodayMealEntries(Long userId)` | Returns today's entries |
| `getMealEntriesByDateRange(Long userId, LocalDate start, LocalDate end)` | Returns entries in date range |
| `getMealEntryById(Long entryId, Long userId)` | Returns entry if owned by user |
| `deleteMealEntry(Long entryId, Long userId)` | Deletes entry (owner only) |
| `getTotalCaloriesForDate(Long userId, LocalDate)` | Returns sum of calories for date |
| `getTodayTotalCalories(Long userId)` | Returns today's total calories |

---

### WeightEntryService
**File**: `service/WeightEntryService.java`

| Method | Description |
|--------|-------------|
| `createOrUpdateWeightEntry(WeightEntryRequest, Long userId)` | Creates or updates entry for date (upsert) |
| `getAllWeightEntries(Long userId)` | Returns all entries ordered by date ASC |
| `getWeightEntryByDate(Long userId, LocalDate)` | Returns entry for specific date |
| `getLatestWeightEntry(Long userId)` | Returns most recent entry |
| `getWeightEntriesByDateRange(Long userId, LocalDate start, LocalDate end)` | Returns entries in range |
| `getWeightEntryById(Long entryId, Long userId)` | Returns entry if owned by user |
| `deleteWeightEntry(Long entryId, Long userId)` | Deletes entry, updates user weight to new latest |

**Business Rules**:
- Only one weight entry per user per date (upsert behavior)
- When latest entry changes, user's current weight is auto-updated

---

### DashboardService
**File**: `service/DashboardService.java`

| Method | Description |
|--------|-------------|
| `getDashboardData(Long userId, LocalDate date)` | Returns complete dashboard data for date |
| `getTodayDashboard(Long userId)` | Returns dashboard for today |

**Returns**: DashboardResponse with:
- Calorie summary (allowed, consumed, remaining)
- User info (name, goalType, currentWeight, goalWeight)
- Today's weight entry (if exists)
- Meals grouped by type
- Total meals count

---

### JwtService
**File**: `service/JwtService.java`

| Method | Description |
|--------|-------------|
| `extractUsername(String token)` | Extracts email from JWT |
| `generateToken(UserDetails)` | Generates JWT with 24h expiration |
| `generateToken(Map claims, UserDetails)` | Generates JWT with extra claims |
| `isTokenValid(String token, UserDetails)` | Validates token signature and expiration |

---

### CustomUserDetailsService
**File**: `service/CustomUserDetailsService.java`
- Implements `UserDetailsService`
- Loads user by email for Spring Security

---

## Utility Classes

### CalorieCalculator
**File**: `util/CalorieCalculator.java`

| Method | Description |
|--------|-------------|
| `calculateBMI(BigDecimal weight, BigDecimal height)` | BMI = weight / (height in m)² |
| `calculateBMR(BigDecimal weight, BigDecimal height, int age, Sex)` | Mifflin-St Jeor equation |
| `calculateTDEE(double bmr, ActivityLevel)` | BMR × activity multiplier |
| `calculateDailyCalorieAllowance(int tdee, BigDecimal weeklyGoal, GoalType)` | TDEE ± (weeklyGoal × 1100) |
| `calculateAllowedDailyIntake(...)` | Full calculation pipeline |

**Mifflin-St Jeor Formula**:
- Male: (10 × weight) + (6.25 × height) - (5 × age) + 5
- Female: (10 × weight) + (6.25 × height) - (5 × age) - 161

---

## REST API Endpoints

### AuthController (`/api/auth`)
**File**: `controller/AuthController.java`

| Method | Endpoint | Request Body | Response |
|--------|----------|--------------|----------|
| POST | `/register` | UserRegistrationRequest | AuthResponse (201) |
| POST | `/login` | LoginRequest | AuthResponse (200) |

**Public endpoints** - no authentication required

---

### UserController (`/api/users`)
**File**: `controller/UserController.java`

| Method | Endpoint | Response |
|--------|----------|----------|
| GET | `/profile` | UserProfileResponse |

---

### FoodController (`/api/foods`)
**File**: `controller/FoodController.java`

| Method | Endpoint | Request | Response |
|--------|----------|---------|----------|
| GET | `/` | - | List<FoodResponse> |
| GET | `/meal-type/{mealType}` | - | List<FoodResponse> |
| GET | `/custom` | - | List<FoodResponse> |
| GET | `/{id}` | - | FoodResponse |
| POST | `/` | FoodRequest | FoodResponse (201) |
| PUT | `/{id}` | FoodRequest | FoodResponse |
| DELETE | `/{id}` | - | 204 No Content |

---

### MealEntryController (`/api/meal-entries`)
**File**: `controller/MealEntryController.java`

| Method | Endpoint | Request | Response |
|--------|----------|---------|----------|
| POST | `/` | MealEntryRequest | MealEntryResponse (201) |
| GET | `/today` | - | List<MealEntryResponse> |
| GET | `/date/{date}` | - | List<MealEntryResponse> |
| GET | `/range?startDate=&endDate=` | - | List<MealEntryResponse> |
| GET | `/{id}` | - | MealEntryResponse |
| DELETE | `/{id}` | - | 204 No Content |
| GET | `/today/calories` | - | Integer |
| GET | `/date/{date}/calories` | - | Integer |

**Date format**: ISO (YYYY-MM-DD)

---

### WeightEntryController (`/api/weight-entries`)
**File**: `controller/WeightEntryController.java`

| Method | Endpoint | Request | Response |
|--------|----------|---------|----------|
| POST | `/` | WeightEntryRequest | WeightEntryResponse (201) |
| GET | `/` | - | List<WeightEntryResponse> |
| GET | `/latest` | - | WeightEntryResponse |
| GET | `/date/{date}` | - | WeightEntryResponse |
| GET | `/range?startDate=&endDate=` | - | List<WeightEntryResponse> |
| GET | `/{id}` | - | WeightEntryResponse |
| DELETE | `/{id}` | - | 204 No Content |

---

### DashboardController (`/api/dashboard`)
**File**: `controller/DashboardController.java`

| Method | Endpoint | Response |
|--------|----------|----------|
| GET | `/` | DashboardResponse (today) |
| GET | `/date/{date}` | DashboardResponse |

---

## DTOs

### Request DTOs

#### UserRegistrationRequest
```java
String name          // @NotBlank, @Size(2-100)
String email         // @NotBlank, @Email
String password      // @NotBlank, @Size(min=6)
LocalDate dob        // @NotNull, @Past
Sex sex              // @NotNull
BigDecimal weight    // @NotNull, @DecimalMin(20), @DecimalMax(500)
BigDecimal height    // @NotNull, @DecimalMin(50), @DecimalMax(300)
ActivityLevel activityLevel  // @NotNull
BigDecimal goal      // @DecimalMin(20), @DecimalMax(500) (optional)
GoalType goalType    // @NotNull
BigDecimal weeklyGoal // @NotNull, @DecimalMin(0.1), @DecimalMax(1.0)
```

#### LoginRequest
```java
String email     // @NotBlank, @Email
String password  // @NotBlank
```

#### FoodRequest
```java
String name       // @NotBlank, @Size(2-100)
String image      // @Size(max=255) (optional)
MealType mealType // @NotNull
Integer calories  // @NotNull, @Min(0), @Max(10000)
```

#### MealEntryRequest
```java
Long foodId         // @NotNull
LocalDate entryDate // @NotNull
LocalTime entryTime // @NotNull
```

#### WeightEntryRequest
```java
LocalDate entryDate  // @NotNull
BigDecimal weight    // @NotNull, @DecimalMin(20), @DecimalMax(500)
```

---

### Response DTOs

#### AuthResponse
```java
String token
String type = "Bearer"
Long userId
String name
String email
BigDecimal bmi
Integer allowedDailyIntake
String message
```

#### UserProfileResponse
```java
Long userId
String name
String email
LocalDate dob
Sex sex
BigDecimal height
ActivityLevel activityLevel
BigDecimal weight
BigDecimal goal
GoalType goalType
BigDecimal weeklyGoal
BigDecimal bmi
Integer allowedDailyIntake
```

#### FoodResponse
```java
Long id
String name
String image
MealType mealType
Integer calories
boolean customFood
LocalDateTime createdAt
```
**Static method**: `fromEntity(Food)`

#### MealEntryResponse
```java
Long id
LocalDate entryDate
LocalTime entryTime
LocalDateTime createdAt
Long foodId
String foodName
String foodImage
MealType mealType
Integer calories
```
**Static method**: `fromEntity(MealEntry)`

#### WeightEntryResponse
```java
Long id
LocalDate entryDate
BigDecimal weight
LocalDateTime createdAt
```
**Static method**: `fromEntity(WeightEntry)`

#### DashboardResponse
```java
LocalDate date
Integer allowedDailyIntake
Integer consumedCalories
Integer remainingCalories
String userName
GoalType goalType
BigDecimal currentWeight
BigDecimal goalWeight
BigDecimal todayWeight
Map<String, List<MealEntryResponse>> mealsByType
Integer totalMealsCount
```

---

## Security Configuration

### SecurityConfig
**File**: `config/SecurityConfig.java`

- CORS: Allows `http://localhost:3000`
- CSRF: Disabled
- Session: Stateless
- Public endpoints: `/api/auth/**`, `/api/test/**`
- All other endpoints require authentication
- Password encoder: BCryptPasswordEncoder
- JWT filter added before UsernamePasswordAuthenticationFilter

### JwtAuthenticationFilter
**File**: `config/JwtAuthenticationFilter.java`

- Extends `OncePerRequestFilter`
- Extracts JWT from `Authorization: Bearer <token>` header
- Validates token and sets SecurityContext

### WebConfig
**File**: `config/WebConfig.java`

- CORS configuration for `/api/**`
- Allowed methods: GET, POST, PUT, DELETE, OPTIONS
- Credentials allowed
- Max age: 3600 seconds

---

## Exception Handling

### Custom Exceptions
- `ResourceNotFoundException` → 404 NOT_FOUND
- `BadRequestException` → 400 BAD_REQUEST

### GlobalExceptionHandler
**File**: `exception/GlobalExceptionHandler.java`

Error response format:
```json
{
  "success": false,
  "timestamp": "2024-01-01T12:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: 1"
}
```

---

## File Structure
```
backend/
├── src/main/java/com/calorietracker/
│   ├── CalorieTrackerApplication.java
│   ├── config/
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── SecurityConfig.java
│   │   └── WebConfig.java
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── DashboardController.java
│   │   ├── FoodController.java
│   │   ├── MealEntryController.java
│   │   ├── UserController.java
│   │   └── WeightEntryController.java
│   ├── dto/
│   │   ├── request/
│   │   │   ├── FoodRequest.java
│   │   │   ├── LoginRequest.java
│   │   │   ├── MealEntryRequest.java
│   │   │   ├── UserRegistrationRequest.java
│   │   │   └── WeightEntryRequest.java
│   │   └── response/
│   │       ├── AuthResponse.java
│   │       ├── DashboardResponse.java
│   │       ├── FoodResponse.java
│   │       ├── MealEntryResponse.java
│   │       ├── UserProfileResponse.java
│   │       └── WeightEntryResponse.java
│   ├── exception/
│   │   ├── BadRequestException.java
│   │   ├── GlobalExceptionHandler.java
│   │   └── ResourceNotFoundException.java
│   ├── model/
│   │   ├── ActivityLevel.java
│   │   ├── Food.java
│   │   ├── GoalType.java
│   │   ├── MealEntry.java
│   │   ├── MealType.java
│   │   ├── Sex.java
│   │   ├── User.java
│   │   └── WeightEntry.java
│   ├── repository/
│   │   ├── FoodRepository.java
│   │   ├── MealEntryRepository.java
│   │   ├── UserRepository.java
│   │   └── WeightEntryRepository.java
│   ├── service/
│   │   ├── CustomUserDetailsService.java
│   │   ├── DashboardService.java
│   │   ├── FoodService.java
│   │   ├── JwtService.java
│   │   ├── MealEntryService.java
│   │   ├── UserService.java
│   │   └── WeightEntryService.java
│   └── util/
│       └── CalorieCalculator.java
├── src/main/resources/
│   └── application.properties
└── pom.xml
```
