# Dashboard Page Frontend Implementation Documentation

**Created**: December 18, 2025  
**Status**: Completed and Verified

---

## Table of Contents

1. [Overview](#overview)
2. [File Structure](#file-structure)
3. [API Endpoints Used](#api-endpoints-used)
4. [Component Architecture](#component-architecture)
5. [State Management](#state-management)
6. [Key Features](#key-features)
7. [Helper Functions](#helper-functions)
8. [UI Components](#ui-components)
9. [LogMealModal Component](#logmealmodal-component)
10. [Usage](#usage)

---

## Overview

This document describes the Dashboard page frontend implementation for the Calorie Tracker application. The Dashboard provides:

- **Daily Summary** - Overview of today's calorie intake with visual progress bar
- **Calorie Cards** - Daily allowance, consumed, and remaining calories
- **Progress Visualization** - Color-coded progress bar (green/yellow/red)
- **Today's Meals** - List of logged meals grouped by meal type
- **Log Meal** - Quick meal logging via modal dialog with food selection

---

## File Structure

```
frontend/src/
├── components/
│   └── dashboard/
│       ├── Dashboard.js       # Main dashboard page component
│       └── LogMealModal.js    # Modal for logging new meals
├── context/
│   └── AuthContext.js         # User authentication context
├── services/
│   └── api.js                 # API services (dashboardService, foodService, mealEntryService)
└── App.js                     # Route configuration (/dashboard)
```

---

## API Endpoints Used

The Dashboard page uses the following backend endpoints:

### GET `/api/dashboard`

Get today's dashboard summary for the authenticated user.

**Response (200 OK):**
```json
{
  "date": "2025-12-18",
  "allowedDailyIntake": 2000,
  "consumedCalories": 850,
  "remainingCalories": 1150,
  "userName": "John Doe",
  "goalType": "LOSE",
  "currentWeight": 75.50,
  "goalWeight": 70.00,
  "todayWeight": 75.50,
  "mealsByType": {
    "BREAKFAST": [
      {
        "id": 1,
        "entryDate": "2025-12-18",
        "entryTime": "08:30:00",
        "createdAt": "2025-12-18T08:30:00",
        "foodId": 1,
        "foodName": "Oatmeal",
        "foodImage": null,
        "mealType": "BREAKFAST",
        "calories": 300
      }
    ],
    "LUNCH": [...]
  },
  "totalMealsCount": 2
}
```

**API Service Call:**
```javascript
import { dashboardService } from '../../services/api';

// Fetch today's dashboard data
const response = await dashboardService.getTodaySummary();
```

### GET `/api/foods`

Get all available foods for the food selection dropdown.

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Oatmeal",
    "image": null,
    "mealType": "BREAKFAST",
    "calories": 150,
    "customFood": false,
    "createdAt": "2025-12-18T08:00:00"
  }
]
```

### POST `/api/meal-entries`

Create a new meal entry.

**Request Body:**
```json
{
  "foodId": 1,
  "entryDate": "2025-12-18",
  "entryTime": "12:30:00"
}
```

**Response (201 Created):**
```json
{
  "id": 5,
  "entryDate": "2025-12-18",
  "entryTime": "12:30:00",
  "createdAt": "2025-12-18T12:30:00",
  "foodId": 1,
  "foodName": "Oatmeal",
  "foodImage": null,
  "mealType": "BREAKFAST",
  "calories": 150
}
```

---

## Component Architecture

### Dashboard.js

```javascript
import React, { useState, useEffect, useCallback } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  LinearProgress,
  CircularProgress,
  Alert,
  Button,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import { useAuth } from '../../context/AuthContext';
import { dashboardService } from '../../services/api';
import LogMealModal from './LogMealModal';

const Dashboard = () => {
  // Component implementation
};

export default Dashboard;
```

---

## State Management

The component uses React's `useState` hook for local state management:

| State Variable | Type | Initial Value | Description |
|----------------|------|---------------|-------------|
| `loading` | boolean | `true` | Loading state during API calls |
| `error` | string | `''` | Error message if API call fails |
| `logMealOpen` | boolean | `false` | Controls Log Meal modal visibility |
| `dashboardData` | object | See below | Dashboard data from API |

### Dashboard Data Object

```javascript
const [dashboardData, setDashboardData] = useState({
  allowedDailyIntake: user?.allowedDailyIntake || 2000,
  consumedCalories: 0,
  remainingCalories: user?.allowedDailyIntake || 2000,
  percentageConsumed: 0,
  mealsByType: {},
  totalMealsCount: 0,
});
```

**Extended fields after API fetch:**
- `userName` - User's display name
- `goalType` - User's goal (LOSE, MAINTAIN, GAIN)
- `currentWeight` - User's current weight
- `goalWeight` - User's target weight
- `todayWeight` - Weight logged for today (if any)

---

## Key Features

### 1. Data Fetching with useCallback

```javascript
const fetchDashboardData = useCallback(async () => {
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
    // Fallback to default values
  } finally {
    setLoading(false);
  }
}, [user]);

useEffect(() => {
  fetchDashboardData();
}, [fetchDashboardData]);
```

### 2. Progress Bar Color Logic

The progress bar changes color based on consumption percentage:

```javascript
const getProgressColor = (percentage) => {
  if (percentage < 50) return 'success';  // Green
  if (percentage < 80) return 'warning';  // Yellow
  return 'error';                          // Red
};
```

### 3. Meal Logging Success Handler

After successfully logging a meal, the dashboard refreshes:

```javascript
const handleLogMealSuccess = () => {
  fetchDashboardData();
};
```

---

## Helper Functions

### Percentage Calculation

```javascript
percentageConsumed: allowed > 0 ? (consumed / allowed) * 100 : 0
```

### Meal Type Formatting

```javascript
// Convert "BREAKFAST" to "Breakfast"
mealType.charAt(0) + mealType.slice(1).toLowerCase()
```

### Time Formatting

```javascript
// Display time as HH:MM (e.g., "08:30")
meal.entryTime?.slice(0, 5)
```

---

## UI Components

### 1. Loading State

```jsx
if (loading) {
  return (
    <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh">
      <CircularProgress />
    </Box>
  );
}
```

### 2. Welcome Header

```jsx
<Typography variant="h4" gutterBottom>
  Welcome, {dashboardData.userName || user?.name || 'User'}!
</Typography>
<Typography variant="h5" gutterBottom>
  Today's Summary
</Typography>
<Typography variant="subtitle1" color="textSecondary" gutterBottom>
  {new Date().toLocaleDateString('en-US', { 
    weekday: 'long', 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric' 
  })}
</Typography>
```

### 3. Calorie Summary Cards

Three cards in a responsive grid:

```jsx
<Grid container spacing={3} sx={{ mt: 2 }}>
  {/* Daily Allowance Card */}
  <Grid item xs={12} md={4}>
    <Card>
      <CardContent>
        <Typography color="textSecondary" gutterBottom>Daily Allowance</Typography>
        <Typography variant="h4">{dashboardData.allowedDailyIntake}</Typography>
        <Typography color="textSecondary">calories</Typography>
      </CardContent>
    </Card>
  </Grid>

  {/* Consumed Card */}
  <Grid item xs={12} md={4}>
    <Card>
      <CardContent>
        <Typography color="textSecondary" gutterBottom>Consumed</Typography>
        <Typography variant="h4" color="primary">{dashboardData.consumedCalories}</Typography>
        <Typography color="textSecondary">calories</Typography>
      </CardContent>
    </Card>
  </Grid>

  {/* Remaining Card */}
  <Grid item xs={12} md={4}>
    <Card>
      <CardContent>
        <Typography color="textSecondary" gutterBottom>Remaining</Typography>
        <Typography 
          variant="h4" 
          color={dashboardData.remainingCalories >= 0 ? 'success.main' : 'error.main'}
        >
          {dashboardData.remainingCalories}
        </Typography>
        <Typography color="textSecondary">calories</Typography>
      </CardContent>
    </Card>
  </Grid>
</Grid>
```

### 4. Progress Bar

```jsx
<Card>
  <CardContent>
    <Typography color="textSecondary" gutterBottom>Daily Progress</Typography>
    <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
      <Box sx={{ width: '100%', mr: 1 }}>
        <LinearProgress 
          variant="determinate" 
          value={Math.min(dashboardData.percentageConsumed, 100)} 
          color={getProgressColor(dashboardData.percentageConsumed)}
          sx={{ height: 20, borderRadius: 5 }}
        />
      </Box>
      <Box sx={{ minWidth: 50 }}>
        <Typography variant="body2" color="textSecondary">
          {dashboardData.percentageConsumed.toFixed(1)}%
        </Typography>
      </Box>
    </Box>
    <Typography variant="body2" color="textSecondary">
      {dashboardData.consumedCalories} of {dashboardData.allowedDailyIntake} calories consumed
    </Typography>
  </CardContent>
</Card>
```

### 5. Today's Meals Section

```jsx
<Card>
  <CardContent>
    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
      <Typography variant="h6">Today's Meals</Typography>
      <Button
        variant="contained"
        startIcon={<AddIcon />}
        onClick={() => setLogMealOpen(true)}
      >
        Log Meal
      </Button>
    </Box>
    {dashboardData.totalMealsCount > 0 ? (
      <Box>
        {Object.entries(dashboardData.mealsByType).map(([mealType, meals]) => (
          <Box key={mealType} sx={{ mb: 2 }}>
            <Typography variant="subtitle1" color="primary" sx={{ fontWeight: 'bold', mb: 1 }}>
              {mealType.charAt(0) + mealType.slice(1).toLowerCase()}
            </Typography>
            {meals.map((meal) => (
              <Box
                key={meal.id}
                sx={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  py: 1,
                  px: 2,
                  bgcolor: 'grey.50',
                  borderRadius: 1,
                  mb: 1,
                }}
              >
                <Box>
                  <Typography variant="body1">{meal.foodName}</Typography>
                  <Typography variant="caption" color="textSecondary">
                    {meal.entryTime?.slice(0, 5)}
                  </Typography>
                </Box>
                <Typography variant="body2" color="primary" sx={{ fontWeight: 'bold' }}>
                  {meal.calories} cal
                </Typography>
              </Box>
            ))}
          </Box>
        ))}
      </Box>
    ) : (
      <Typography color="textSecondary">
        No meals logged yet. Start tracking your food intake!
      </Typography>
    )}
  </CardContent>
</Card>
```

---

## LogMealModal Component

### Overview

A modal dialog for logging new meals with food selection from the database.

### Props

| Prop | Type | Description |
|------|------|-------------|
| `open` | boolean | Controls modal visibility |
| `onClose` | function | Callback when modal is closed |
| `onSuccess` | function | Callback when meal is successfully logged |

### State Variables

| State Variable | Type | Description |
|----------------|------|-------------|
| `foods` | array | List of available foods |
| `loading` | boolean | Loading state for food fetch |
| `submitting` | boolean | Submitting state for meal creation |
| `error` | string | Error message |
| `selectedFood` | object | Currently selected food item |
| `mealTypeFilter` | string | Filter foods by meal type |

### Key Features

#### 1. Food Fetching on Open

```javascript
useEffect(() => {
  if (open) {
    fetchFoods();
    setSelectedFood(null);
    setError('');
  }
}, [open]);

const fetchFoods = async () => {
  try {
    setLoading(true);
    const response = await foodService.getAllFoods();
    setFoods(response.data);
  } catch (err) {
    setError('Failed to load foods. Please try again.');
  } finally {
    setLoading(false);
  }
};
```

#### 2. Meal Type Filtering

```javascript
const MEAL_TYPES = ['BREAKFAST', 'LUNCH', 'DINNER', 'SNACKS', 'OTHER'];

const filteredFoods = mealTypeFilter
  ? foods.filter((food) => food.mealType === mealTypeFilter)
  : foods;
```

#### 3. Auto Date/Time on Submit

Date and time are automatically set to the current moment when logging:

```javascript
const handleSubmit = async () => {
  if (!selectedFood) {
    setError('Please select a food item');
    return;
  }

  try {
    setSubmitting(true);
    setError('');

    const now = new Date();
    const mealData = {
      foodId: selectedFood.id,
      entryDate: now.toISOString().split('T')[0],
      entryTime: now.toTimeString().split(' ')[0],
    };

    await mealEntryService.logMeal(mealData);
    onSuccess?.();
    onClose();
  } catch (err) {
    setError(err.response?.data?.message || 'Failed to log meal. Please try again.');
  } finally {
    setSubmitting(false);
  }
};
```

### UI Components

#### Meal Type Filter Dropdown

```jsx
<FormControl fullWidth size="small">
  <InputLabel>Filter by Meal Type</InputLabel>
  <Select
    value={mealTypeFilter}
    label="Filter by Meal Type"
    onChange={(e) => setMealTypeFilter(e.target.value)}
  >
    <MenuItem value="">All Types</MenuItem>
    {MEAL_TYPES.map((type) => (
      <MenuItem key={type} value={type}>
        {type.charAt(0) + type.slice(1).toLowerCase()}
      </MenuItem>
    ))}
  </Select>
</FormControl>
```

#### Food Autocomplete

```jsx
<Autocomplete
  options={filteredFoods}
  getOptionLabel={(option) => option.name}
  value={selectedFood}
  onChange={(event, newValue) => setSelectedFood(newValue)}
  renderInput={(params) => (
    <TextField
      {...params}
      label="Select Food"
      placeholder="Search for a food..."
      required
    />
  )}
  renderOption={(props, option) => (
    <Box component="li" {...props}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', width: '100%', alignItems: 'center' }}>
        <Box>
          <Typography variant="body1">{option.name}</Typography>
          <Typography variant="caption" color="textSecondary">
            {option.mealType.charAt(0) + option.mealType.slice(1).toLowerCase()}
          </Typography>
        </Box>
        <Chip
          label={`${option.calories} cal`}
          size="small"
          color="primary"
          variant="outlined"
        />
      </Box>
    </Box>
  )}
  isOptionEqualToValue={(option, value) => option.id === value.id}
  noOptionsText="No foods found"
/>
```

#### Selected Food Preview

```jsx
{selectedFood && (
  <Box sx={{ p: 2, bgcolor: 'grey.100', borderRadius: 1 }}>
    <Typography variant="subtitle2" color="textSecondary">
      Selected Food
    </Typography>
    <Typography variant="h6">{selectedFood.name}</Typography>
    <Box sx={{ display: 'flex', gap: 2, mt: 1 }}>
      <Chip
        label={selectedFood.mealType.charAt(0) + selectedFood.mealType.slice(1).toLowerCase()}
        size="small"
      />
      <Chip
        label={`${selectedFood.calories} calories`}
        size="small"
        color="primary"
      />
    </Box>
  </Box>
)}
```

---

## Usage

### Route Configuration (App.js)

```javascript
import Dashboard from './components/dashboard/Dashboard';

// Inside Routes
<Route path="dashboard" element={<Dashboard />} />
```

### Navigation Menu (Layout.js)

The Dashboard is accessible from the sidebar navigation:

```javascript
const menuItems = [
  { text: 'Dashboard', icon: <DashboardIcon />, path: '/dashboard' },
  { text: 'Foods', icon: <RestaurantIcon />, path: '/foods' },
  { text: 'History', icon: <HistoryIcon />, path: '/history' },
  { text: 'Weight Tracking', icon: <MonitorWeightIcon />, path: '/weight' },
];
```

---

## Dependencies Used

| Dependency | Purpose |
|------------|---------|
| `react` | Component framework |
| `@mui/material` | UI components (Box, Card, Grid, Typography, etc.) |
| `@mui/icons-material` | Icons (Add) |
| `axios` (via api.js) | HTTP requests |

---

## Data Flow

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  Dashboard.js   │────►│  api.js          │────►│  Backend API    │
│  (Page Load)    │     │  (getTodaySummary)│    │  /api/dashboard │
└─────────────────┘     └──────────────────┘     └─────────────────┘
                                                          │
                                                          ▼
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  Render Cards   │◄────│  Calculate %     │◄────│  Response Data  │
│  & Progress Bar │     │  & Set State     │     │  (Dashboard)    │
└─────────────────┘     └──────────────────┘     └─────────────────┘

┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  LogMealModal   │────►│  api.js          │────►│  Backend API    │
│  (Log Meal)     │     │  (logMeal)       │     │  /meal-entries  │
└─────────────────┘     └──────────────────┘     └─────────────────┘
         │
         ▼
┌─────────────────┐
│  onSuccess()    │────► fetchDashboardData() ────► Refresh UI
└─────────────────┘
```

---

## Related Features

- **History** - View meal entries for any date
- **Foods** - Manage food items used in meal entries
- **Weight Tracking** - Track weight progress over time
