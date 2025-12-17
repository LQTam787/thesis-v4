# Calorie Tracker - Project Setup Documentation

**Created**: December 17, 2025  
**Tech Stack**: React JS (Frontend) + Spring Boot (Backend) + MySQL (Database)

---

## Table of Contents

1. [Project Structure](#project-structure)
2. [Technology Versions](#technology-versions)
3. [Backend Configuration](#backend-configuration)
4. [Frontend Configuration](#frontend-configuration)
5. [Running the Application](#running-the-application)
6. [API Endpoints](#api-endpoints)

---

## Project Structure

```
thesis-v4/
├── backend/                          # Spring Boot Backend
│   ├── .mvn/wrapper/
│   │   └── maven-wrapper.properties
│   ├── src/main/java/com/calorietracker/
│   │   ├── CalorieTrackerApplication.java
│   │   ├── config/
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   ├── SecurityConfig.java
│   │   │   └── WebConfig.java
│   │   ├── controller/
│   │   │   └── TestController.java
│   │   ├── exception/
│   │   │   ├── BadRequestException.java
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   └── ResourceNotFoundException.java
│   │   ├── model/
│   │   │   ├── ActivityLevel.java (enum)
│   │   │   ├── Food.java
│   │   │   ├── GoalType.java (enum)
│   │   │   ├── MealEntry.java
│   │   │   ├── MealType.java (enum)
│   │   │   ├── User.java
│   │   │   └── WeightEntry.java
│   │   ├── repository/
│   │   │   ├── FoodRepository.java
│   │   │   ├── MealEntryRepository.java
│   │   │   ├── UserRepository.java
│   │   │   └── WeightEntryRepository.java
│   │   ├── service/
│   │   │   ├── CustomUserDetailsService.java
│   │   │   └── JwtService.java
│   │   └── util/
│   │       └── CalorieCalculator.java
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── src/test/java/
│   │   └── CalorieTrackerApplicationTests.java
│   ├── .gitignore
│   ├── mvnw.cmd
│   └── pom.xml
├── frontend/                         # React Frontend
│   ├── public/
│   │   └── index.html
│   ├── src/
│   │   ├── components/
│   │   │   ├── auth/
│   │   │   │   ├── Login.js
│   │   │   │   └── Register.js
│   │   │   ├── dashboard/
│   │   │   │   └── Dashboard.js
│   │   │   └── layout/
│   │   │       └── Layout.js
│   │   ├── context/
│   │   │   └── AuthContext.js
│   │   ├── services/
│   │   │   └── api.js
│   │   ├── App.js
│   │   ├── index.css
│   │   └── index.js
│   ├── .gitignore
│   └── package.json
├── docs/
└── README.md
```

---

## Technology Versions

### Backend
| Technology | Version |
|------------|---------|
| Java | 17 LTS |
| Spring Boot | 3.2.1 |
| MySQL | 8.0.x |
| Maven | 3.9.6 |
| JWT (jjwt) | 0.11.5 |

### Frontend
| Technology | Version |
|------------|---------|
| Node.js | 18.x or 20.x LTS |
| React | 18.2.0 |
| Material-UI | 5.15.4 |
| React Router | 6.21.2 |
| Axios | 1.6.5 |
| Chart.js | 4.4.1 |

---

## Backend Configuration

### application.properties
```properties
# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/calorie_tracker?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=1234
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.properties.hibernate.format_sql=true

# JWT Configuration (Base64 encoded secret key)
jwt.secret=dGhpcy1pcy1hLXZlcnktc2VjdXJlLXNlY3JldC1rZXktZm9yLWp3dC1hdXRoZW50aWNhdGlvbi1hdC1sZWFzdC0yNTYtYml0cw==
jwt.expiration=86400000

# File Upload Configuration
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB

# Logging
logging.level.org.springframework.web=INFO
logging.level.com.calorietracker=DEBUG
```

### Key Dependencies (pom.xml)
- `spring-boot-starter-web` - REST API
- `spring-boot-starter-data-jpa` - Database ORM
- `spring-boot-starter-security` - Authentication
- `spring-boot-starter-validation` - Input validation
- `mysql-connector-j` - MySQL driver
- `lombok` - Boilerplate reduction
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson` - JWT authentication

### Database Tables (Auto-created by JPA)
- **users** - User accounts with BMI and calorie calculations
- **foods** - Food items (system + custom)
- **meal_entries** - Daily meal logs
- **weight_entries** - Weight tracking history

### Security Configuration
- JWT-based stateless authentication
- BCrypt password encoding
- CORS enabled for `http://localhost:3000`
- Public endpoints: `/api/auth/**`, `/api/test/**`
- All other endpoints require authentication

---

## Frontend Configuration

### Key Dependencies (package.json)
- `react` & `react-dom` - Core React
- `react-router-dom` - Client-side routing
- `axios` - HTTP client
- `@mui/material` & `@mui/icons-material` - UI components
- `@emotion/react` & `@emotion/styled` - Styling
- `chart.js` & `react-chartjs-2` - Charts

### Application Structure
- **AuthContext** - Global authentication state management
- **API Service** - Axios instance with JWT interceptors
- **Protected Routes** - Redirect unauthenticated users to login
- **Material-UI Theme** - Consistent styling

### API Base URL
```javascript
const API_BASE_URL = 'http://localhost:8080/api';
```

---

## Running the Application

### Prerequisites
1. Java 17+ installed (`java -version`)
2. Node.js 18+ installed (`node -v`)
3. MySQL 8.0 running on port 3306

### Step 1: Start MySQL
Ensure MySQL is running and accessible with:
- Host: `localhost`
- Port: `3306`
- Username: `root`
- Password: `1234` (or update in application.properties)

### Step 2: Start Backend
```bash
cd backend
mvnw.cmd spring-boot:run
```
Backend runs at: `http://localhost:8080`

Test endpoint: `GET http://localhost:8080/api/test`

### Step 3: Start Frontend
```bash
cd frontend
npm install
npm start
```
Frontend runs at: `http://localhost:3000`

---

## API Endpoints

### Authentication (Public)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | User login |

### Dashboard (Protected)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/dashboard/today` | Today's calorie summary |

### Foods (Protected)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/foods` | Get all available foods |
| POST | `/api/foods` | Add custom food |
| DELETE | `/api/foods/{id}` | Delete custom food |

### Meal Entries (Protected)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/meal-entries` | Log a meal |
| GET | `/api/meal-entries/date/{date}` | Get meals by date |
| DELETE | `/api/meal-entries/{id}` | Delete meal entry |

### Weight Entries (Protected)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/weight-entries` | Log weight |
| GET | `/api/weight-entries` | Get all weight entries |

---

## Calorie Calculation Formulas

### BMI (Body Mass Index)
```
BMI = weight(kg) / (height(m))²
```

### BMR (Basal Metabolic Rate) - Mifflin-St Jeor
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

## Next Implementation Steps

1. **AuthController** - Register/Login endpoints
2. **UserService** - User registration with BMI/calorie calculation
3. **FoodController** - Food CRUD operations
4. **MealEntryController** - Meal logging
5. **WeightEntryController** - Weight tracking
6. **DashboardController** - Daily summary
7. **Frontend pages** - Foods, History, Weight Tracking
