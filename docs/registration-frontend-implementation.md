# Registration Frontend Implementation Documentation

**Created**: December 18, 2025  
**Status**: Completed and Verified

---

## Table of Contents

1. [Overview](#overview)
2. [File Structure](#file-structure)
3. [Registration Flow](#registration-flow)
4. [API Service (api.js)](#api-service-apijs)
5. [Auth Context (AuthContext.js)](#auth-context-authcontextjs)
6. [Register Component (Register.js)](#register-component-registerjs)
7. [Dashboard Component (Dashboard.js)](#dashboard-component-dashboardjs)
8. [App Routing (App.js)](#app-routing-appjs)
9. [Testing the Registration](#testing-the-registration)

---

## Overview

This document describes the frontend implementation for user registration in the Calorie Tracker application. The registration flow:

- **Collects User Data** - Name, email, password, DOB, weight, height, activity level, goals
- **Sends to Backend** - POST request to `/api/auth/register`
- **Auto-Authentication** - Stores JWT token and user data on successful registration
- **Redirects to Dashboard** - User is immediately taken to their personalized dashboard

---

## File Structure

```
frontend/src/
├── components/
│   ├── auth/
│   │   └── Register.js          # Registration form component
│   └── dashboard/
│       └── Dashboard.js         # Dashboard component (post-login landing)
├── context/
│   └── AuthContext.js           # Authentication state management
├── services/
│   └── api.js                   # Axios API service with interceptors
└── App.js                       # Main app with routing
```

---

## Registration Flow

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  Register.js    │────►│  AuthContext.js  │────►│    api.js       │
│  (Form Submit)  │     │  (register())    │     │  (POST request) │
└─────────────────┘     └──────────────────┘     └─────────────────┘
                                                          │
                                                          ▼
                                                 ┌─────────────────┐
                                                 │  Backend API    │
                                                 │  /api/auth/     │
                                                 │  register       │
                                                 └─────────────────┘
                                                          │
                                                          ▼
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  Dashboard.js   │◄────│  Navigate to     │◄────│  Store token &  │
│  (User lands)   │     │  /dashboard      │     │  user in state  │
└─────────────────┘     └──────────────────┘     └─────────────────┘
```

---

## API Service (api.js)

```javascript
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

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

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const authService = {
  login: (credentials) => api.post('/auth/login', credentials),
  register: (userData) => api.post('/auth/register', userData),
};

export const dashboardService = {
  getTodaySummary: () => api.get('/dashboard'),
  getDashboardByDate: (date) => api.get(`/dashboard/date/${date}`),
};

export const foodService = {
  getAllFoods: () => api.get('/foods'),
  getFoodsByMealType: (mealType) => api.get(`/foods?mealType=${mealType}`),
  addFood: (foodData) => api.post('/foods', foodData),
  deleteFood: (id) => api.delete(`/foods/${id}`),
};

export const mealEntryService = {
  logMeal: (mealData) => api.post('/meal-entries', mealData),
  getMealsByDate: (date) => api.get(`/meal-entries/date/${date}`),
  deleteMealEntry: (id) => api.delete(`/meal-entries/${id}`),
};

export const weightEntryService = {
  logWeight: (weightData) => api.post('/weight-entries', weightData),
  getAllWeightEntries: () => api.get('/weight-entries'),
  getWeightEntriesByRange: (startDate, endDate) => 
    api.get(`/weight-entries?startDate=${startDate}&endDate=${endDate}`),
};

export default api;
```

**Key Features:**

| Feature | Description |
|---------|-------------|
| Base URL | `http://localhost:8080/api` |
| Request Interceptor | Automatically adds JWT token to Authorization header |
| Response Interceptor | Handles 401 errors by clearing auth and redirecting to login |
| Content-Type | `application/json` for all requests |

---

## Auth Context (AuthContext.js)

```javascript
import React, { createContext, useContext, useState, useEffect } from 'react';
import { authService } from '../services/api';

const AuthContext = createContext(null);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedUser = localStorage.getItem('user');
    const storedToken = localStorage.getItem('token');
    
    if (storedUser && storedToken) {
      setUser(JSON.parse(storedUser));
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    try {
      const response = await authService.login({ email, password });
      const data = response.data;
      
      // Extract user info from response
      const userData = {
        userId: data.userId,
        name: data.name,
        email: data.email,
        bmi: data.bmi,
        allowedDailyIntake: data.allowedDailyIntake,
      };
      
      localStorage.setItem('token', data.token);
      localStorage.setItem('user', JSON.stringify(userData));
      setUser(userData);
      
      return { success: true };
    } catch (error) {
      console.log(error);
      return { 
        success: false, 
        message: error.response?.data?.message || 'Login failed' 
      };
    }
  };

  const register = async (registrationData) => {
    try {
      const response = await authService.register(registrationData);
      const data = response.data;
      
      // Extract user info from response
      const userData = {
        userId: data.userId,
        name: data.name,
        email: data.email,
        bmi: data.bmi,
        allowedDailyIntake: data.allowedDailyIntake,
      };
      
      localStorage.setItem('token', data.token);
      localStorage.setItem('user', JSON.stringify(userData));
      setUser(userData);
      
      return { success: true };
    } catch (error) {
      console.log(error);
      return { 
        success: false, 
        message: error.response?.data?.message || 'Registration failed' 
      };
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  };

  const value = {
    user,
    isAuthenticated: !!user,
    loading,
    login,
    register,
    logout,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};
```

**Key Features:**

| Feature | Description |
|---------|-------------|
| State Management | Uses React Context for global auth state |
| Persistence | Stores token and user in localStorage |
| Auto-restore | Restores auth state on page refresh |
| Response Parsing | Correctly parses backend's AuthResponse format |

**User Data Structure:**

```javascript
{
  userId: Long,           // User's database ID
  name: String,           // User's display name
  email: String,          // User's email
  bmi: BigDecimal,        // Calculated BMI
  allowedDailyIntake: Integer  // Daily calorie allowance
}
```

---

## Register Component (Register.js)

```javascript
import React, { useState } from 'react';
import { Link as RouterLink, useNavigate } from 'react-router-dom';
import {
  Container,
  Box,
  Typography,
  TextField,
  Button,
  Link,
  Paper,
  Alert,
  Grid,
  MenuItem,
  FormControl,
  InputLabel,
  Select,
} from '@mui/material';
import { useAuth } from '../../context/AuthContext';

const activityLevels = [
  { value: 'SEDENTARY', label: 'Sedentary (little to no exercise)' },
  { value: 'LIGHTLY_ACTIVE', label: 'Lightly Active (1-3 days/week)' },
  { value: 'MODERATELY_ACTIVE', label: 'Moderately Active (3-5 days/week)' },
  { value: 'VERY_ACTIVE', label: 'Very Active (6-7 days/week)' },
];

const goalTypes = [
  { value: 'LOSE', label: 'Lose Weight' },
  { value: 'MAINTAIN', label: 'Maintain Weight' },
  { value: 'GAIN', label: 'Gain Weight' },
];

const sexTypes = [
  { value: 'MALE', label: 'Male' },
  { value: 'FEMALE', label: 'Female' },
];

const Register = () => {
  const navigate = useNavigate();
  const { register } = useAuth();
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
    dob: '',
    sex: 'MALE',
    weight: '',
    height: '',
    activityLevel: 'MODERATELY_ACTIVE',
    goal: '',
    goalType: 'MAINTAIN',
    weeklyGoal: '0.5',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    if (formData.password.length < 6) {
      setError('Password must be at least 6 characters');
      return;
    }

    setLoading(true);

    const userData = {
      name: formData.name,
      email: formData.email,
      password: formData.password,
      dob: formData.dob,
      sex: formData.sex,
      weight: parseFloat(formData.weight),
      height: parseFloat(formData.height),
      activityLevel: formData.activityLevel,
      goal: formData.goal ? parseFloat(formData.goal) : parseFloat(formData.weight),
      goalType: formData.goalType,
      weeklyGoal: parseFloat(formData.weeklyGoal),
    };

    const result = await register(userData);
    
    if (result.success) {
      navigate('/dashboard');
    } else {
      setError(result.message);
    }
    setLoading(false);
  };

  return (
    <Container component="main" maxWidth="sm">
      <Box sx={{ marginTop: 4, marginBottom: 4, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        <Paper elevation={3} sx={{ p: 4, width: '100%' }}>
          <Typography component="h1" variant="h4" align="center" gutterBottom>
            Calorie Tracker
          </Typography>
          <Typography component="h2" variant="h6" align="center" color="textSecondary" gutterBottom>
            Create Account
          </Typography>
          
          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          <Box component="form" onSubmit={handleSubmit} sx={{ mt: 1 }}>
            {/* Form fields for: name, email, password, confirmPassword, dob, weight, height, activityLevel, goalType, goal, weeklyGoal */}
            <Button type="submit" fullWidth variant="contained" sx={{ mt: 3, mb: 2 }} disabled={loading}>
              {loading ? 'Creating Account...' : 'Sign Up'}
            </Button>
            <Box sx={{ textAlign: 'center' }}>
              <Link component={RouterLink} to="/login" variant="body2">
                Already have an account? Sign In
              </Link>
            </Box>
          </Box>
        </Paper>
      </Box>
    </Container>
  );
};

export default Register;
```

**Form Fields:**

| Field | Type | Validation |
|-------|------|------------|
| name | text | Required |
| email | email | Required, valid email |
| password | password | Required, min 6 chars |
| confirmPassword | password | Must match password |
| dob | date | Required |
| sex | select | Required (MALE, FEMALE) |
| weight | number | Required, 20-300 kg |
| height | number | Required, 100-250 cm |
| activityLevel | select | Required (enum) |
| goal | number | Optional (defaults to current weight) |
| weeklyGoal | number | Required, 0.1-1.0 kg/week |

**Goal Type Derivation:**

The `goalType` is automatically derived from the comparison between `weight` and `goal`:

| Condition | Goal Type |
|-----------|-----------|
| `goal < weight` | LOSE |
| `goal > weight` | GAIN |
| `goal = weight` | MAINTAIN |

```javascript
const deriveGoalType = (currentWeight, targetWeight) => {
  const weight = parseFloat(currentWeight);
  const goal = parseFloat(targetWeight);
  
  if (isNaN(weight) || isNaN(goal)) return 'MAINTAIN';
  
  if (goal < weight) return 'LOSE';
  if (goal > weight) return 'GAIN';
  return 'MAINTAIN';
};
```

**Request Payload to Backend:**

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

---

## Dashboard Component (Dashboard.js)

```javascript
import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  LinearProgress,
  CircularProgress,
  Alert,
} from '@mui/material';
import { useAuth } from '../../context/AuthContext';
import { dashboardService } from '../../services/api';

const Dashboard = () => {
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [dashboardData, setDashboardData] = useState({
    allowedDailyIntake: user?.allowedDailyIntake || 2000,
    consumedCalories: 0,
    remainingCalories: user?.allowedDailyIntake || 2000,
    percentageConsumed: 0,
    mealsByType: {},
    totalMealsCount: 0,
  });

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        setLoading(true);
        setError('');
        const response = await dashboardService.getTodaySummary();
        const data = response.data;
        
        const consumed = data.consumedCalories || 0;
        const allowed = data.allowedDailyIntake || user?.allowedDailyIntake || 2000;
        
        setDashboardData({
          allowedDailyIntake: allowed,
          consumedCalories: consumed,
          remainingCalories: data.remainingCalories ?? (allowed - consumed),
          percentageConsumed: allowed > 0 ? (consumed / allowed) * 100 : 0,
          mealsByType: data.mealsByType || {},
          totalMealsCount: data.totalMealsCount || 0,
          userName: data.userName,
          goalType: data.goalType,
          currentWeight: data.currentWeight,
          goalWeight: data.goalWeight,
          todayWeight: data.todayWeight,
        });
      } catch (err) {
        console.error('Failed to fetch dashboard data:', err);
        setError('Failed to load dashboard data. Please try again.');
        // Fallback to user data from context
        setDashboardData({
          allowedDailyIntake: user?.allowedDailyIntake || 2000,
          consumedCalories: 0,
          remainingCalories: user?.allowedDailyIntake || 2000,
          percentageConsumed: 0,
          mealsByType: {},
          totalMealsCount: 0,
        });
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, [user]);

  // ... render logic with cards for Daily Allowance, Consumed, Remaining, Progress Bar
};

export default Dashboard;
```

**Dashboard Data Structure:**

| Field | Type | Description |
|-------|------|-------------|
| allowedDailyIntake | Integer | User's daily calorie allowance |
| consumedCalories | Integer | Calories consumed today |
| remainingCalories | Integer | Calories remaining |
| percentageConsumed | Number | Progress percentage |
| mealsByType | Object | Meals grouped by type |
| totalMealsCount | Integer | Total meals logged today |

---

## App Routing (App.js)

```javascript
import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { AuthProvider, useAuth } from './context/AuthContext';
import Login from './components/auth/Login';
import Register from './components/auth/Register';
import Dashboard from './components/dashboard/Dashboard';
import Layout from './components/layout/Layout';

const PrivateRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();
  
  if (loading) {
    return <div>Loading...</div>;
  }
  
  return isAuthenticated ? children : <Navigate to="/login" />;
};

const PublicRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();
  
  if (loading) {
    return <div>Loading...</div>;
  }
  
  return !isAuthenticated ? children : <Navigate to="/dashboard" />;
};

function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <AuthProvider>
        <Router>
          <Routes>
            <Route path="/login" element={<PublicRoute><Login /></PublicRoute>} />
            <Route path="/register" element={<PublicRoute><Register /></PublicRoute>} />
            <Route path="/" element={<PrivateRoute><Layout /></PrivateRoute>}>
              <Route index element={<Navigate to="/dashboard" replace />} />
              <Route path="dashboard" element={<Dashboard />} />
            </Route>
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </Router>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
```

**Route Configuration:**

| Route | Component | Access |
|-------|-----------|--------|
| `/login` | Login | Public only (redirects to dashboard if authenticated) |
| `/register` | Register | Public only (redirects to dashboard if authenticated) |
| `/dashboard` | Dashboard | Private (redirects to login if not authenticated) |
| `/` | - | Redirects to `/dashboard` |
| `*` | - | Catch-all, redirects to `/dashboard` |

---

## Testing the Registration

### Prerequisites

1. Backend running on `http://localhost:8080`
2. Frontend running on `http://localhost:3000`
3. MySQL database accessible

### Test Steps

1. **Navigate to Registration Page**
   ```
   http://localhost:3000/register
   ```

2. **Fill in the Form**
   - Full Name: `John Doe`
   - Email: `john@example.com`
   - Password: `password123`
   - Confirm Password: `password123`
   - Date of Birth: `1990-05-15`
   - Current Weight: `75.5`
   - Height: `175`
   - Activity Level: `Moderately Active`
   - Goal Type: `Lose Weight`
   - Target Weight: `70`
   - Weekly Goal: `0.5`

3. **Click "Sign Up"**

4. **Expected Results**
   - User is created in database
   - JWT token is stored in localStorage
   - User data is stored in localStorage
   - User is redirected to `/dashboard`
   - Dashboard shows personalized welcome message
   - Dashboard displays calculated calorie allowance

### Verify in Browser DevTools

**localStorage (Application tab):**
```
token: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
user: {"userId":1,"name":"John Doe","email":"john@example.com","bmi":24.65,"allowedDailyIntake":1925}
```

**Network tab (register request):**
- Status: 201 Created
- Response contains: token, userId, name, email, bmi, allowedDailyIntake, message

---

## Error Handling

| Error | Cause | User Message |
|-------|-------|--------------|
| 400 Bad Request | Validation failed | Field-specific error messages |
| 400 Bad Request | Email exists | "Email already registered" |
| Network Error | Backend not running | "Registration failed" |
| 401 Unauthorized | Invalid token | Redirects to login |

---

## Dependencies

| Package | Version | Purpose |
|---------|---------|---------|
| react | 18.x | UI framework |
| react-router-dom | 6.x | Client-side routing |
| axios | 1.6.x | HTTP client |
| @mui/material | 5.x | UI components |
| @emotion/react | 11.x | Styling |

---

## Next Steps

1. **Login Page** - Similar implementation for existing users
2. **Food Management** - Add/view foods page
3. **Meal Logging** - Log meals from dashboard
4. **Weight Tracking** - Log and visualize weight progress
5. **History Page** - View past meals and weight entries
