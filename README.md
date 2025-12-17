# Calorie Tracker Application

A full-stack calorie tracking web application built with React (Frontend) and Spring Boot (Backend).

## Tech Stack

- **Frontend**: React 18, Material-UI, React Router, Axios, Chart.js
- **Backend**: Spring Boot 3.2, Spring Security, Spring Data JPA
- **Database**: MySQL 8.0
- **Authentication**: JWT (JSON Web Tokens)

## Project Structure

```
thesis-v4/
├── backend/                 # Spring Boot backend
│   ├── src/main/java/com/calorietracker/
│   │   ├── config/         # Security & web configuration
│   │   ├── controller/     # REST API endpoints
│   │   ├── model/          # JPA entities
│   │   ├── repository/     # Data access layer
│   │   ├── service/        # Business logic
│   │   ├── exception/      # Custom exceptions
│   │   └── util/           # Utility classes
│   └── src/main/resources/
│       └── application.properties
├── frontend/                # React frontend
│   ├── public/
│   └── src/
│       ├── components/     # React components
│       ├── context/        # React context (Auth)
│       └── services/       # API services
└── docs/                   # Documentation
```

## Prerequisites

- Java 17 or higher
- Node.js 18.x or 20.x
- MySQL 8.0
- Maven 3.8+

## Setup Instructions

### 1. Database Setup

Create a MySQL database (the app will auto-create if configured):

```sql
CREATE DATABASE calorie_tracker;
```

### 2. Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Update `src/main/resources/application.properties` with your MySQL credentials:
   ```properties
   spring.datasource.username=root
   spring.datasource.password=your_mysql_password
   ```

3. Run the Spring Boot application:
   ```bash
   ./mvnw spring-boot:run
   ```
   
   Or on Windows:
   ```bash
   mvnw.cmd spring-boot:run
   ```

   The backend will start at `http://localhost:8080`

### 3. Frontend Setup

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm start
   ```

   The frontend will start at `http://localhost:3000`

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login

### Dashboard
- `GET /api/dashboard/today` - Get today's calorie summary

### Foods
- `GET /api/foods` - Get all available foods
- `POST /api/foods` - Add custom food
- `DELETE /api/foods/{id}` - Delete custom food

### Meal Entries
- `POST /api/meal-entries` - Log a meal
- `GET /api/meal-entries/date/{date}` - Get meals by date
- `DELETE /api/meal-entries/{id}` - Delete meal entry

### Weight Entries
- `POST /api/weight-entries` - Log weight
- `GET /api/weight-entries` - Get all weight entries

## Development Ports

- Frontend (React): `http://localhost:3000`
- Backend (Spring Boot): `http://localhost:8080`
- Database (MySQL): `localhost:3306`

## Features

- User registration with BMI and calorie calculation
- Daily calorie tracking
- Food database with custom food entries
- Meal logging by meal type
- Weight tracking with progress visualization
- Historical data viewing

## License

This project is for educational purposes.
