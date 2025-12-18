# History Page Frontend Implementation Documentation

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
9. [Usage](#usage)

---

## Overview

This document describes the History page frontend implementation for the Calorie Tracker application. The History page provides:

- **Date Navigation** - Browse meal entries by day with previous/next buttons
- **Smart Date Display** - Shows "Today", "Yesterday", "Tomorrow" for relative dates
- **Meal Grouping** - Entries organized by meal type (Breakfast, Lunch, Dinner, Snacks, Other)
- **Daily Summary** - Total calories and meal count for the selected day
- **Subtotals** - Calorie subtotals per meal type

---

## File Structure

```
frontend/src/
├── components/
│   └── history/
│       └── History.js          # Main history page component
├── services/
│   └── api.js                  # API service (mealEntryService)
└── App.js                      # Route configuration (/history)
```

---

## API Endpoints Used

The History page uses the following backend endpoint:

### GET `/api/meal-entries/date/{date}`

Get meal entries for a specific date.

**Path Parameters:**
- `date` - Date in ISO format (YYYY-MM-DD)

**Example:** `GET /api/meal-entries/date/2025-12-18`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "entryDate": "2025-12-18",
    "entryTime": "08:30:00",
    "createdAt": "2025-12-18T08:30:00",
    "foodId": 1,
    "foodName": "Oatmeal",
    "foodImage": null,
    "mealType": "BREAKFAST",
    "calories": 150
  },
  {
    "id": 2,
    "entryDate": "2025-12-18",
    "entryTime": "12:00:00",
    "createdAt": "2025-12-18T12:00:00",
    "foodId": 5,
    "foodName": "Grilled Chicken Salad",
    "foodImage": "https://example.com/salad.jpg",
    "mealType": "LUNCH",
    "calories": 350
  }
]
```

**API Service Call:**
```javascript
import { mealEntryService } from '../../services/api';

// Fetch meals for a specific date
const response = await mealEntryService.getMealsByDate('2025-12-18');
```

---

## Component Architecture

### History.js

```javascript
import React, { useState, useEffect, useCallback } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  CircularProgress,
  Alert,
  IconButton,
  Divider,
} from '@mui/material';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import TodayIcon from '@mui/icons-material/Today';
import { mealEntryService } from '../../services/api';

const MEAL_TYPE_ORDER = ['BREAKFAST', 'LUNCH', 'DINNER', 'SNACKS', 'OTHER'];

const History = () => {
  // Component implementation
};

export default History;
```

---

## State Management

The component uses React's `useState` hook for local state management:

| State Variable | Type | Initial Value | Description |
|----------------|------|---------------|-------------|
| `selectedDate` | Date | `new Date()` | Currently selected date for viewing meals |
| `loading` | boolean | `true` | Loading state during API calls |
| `error` | string | `''` | Error message if API call fails |
| `mealEntries` | array | `[]` | Array of meal entries for selected date |

```javascript
const [selectedDate, setSelectedDate] = useState(new Date());
const [loading, setLoading] = useState(true);
const [error, setError] = useState('');
const [mealEntries, setMealEntries] = useState([]);
```

---

## Key Features

### 1. Date Navigation

Users can navigate between dates using:
- **Previous Day Button** - ChevronLeftIcon to go back one day
- **Next Day Button** - ChevronRightIcon to go forward one day
- **Today Button** - TodayIcon to jump to current date (only visible when not on today)

```javascript
const handlePreviousDay = () => {
  setSelectedDate((prev) => {
    const newDate = new Date(prev);
    newDate.setDate(newDate.getDate() - 1);
    return newDate;
  });
};

const handleNextDay = () => {
  setSelectedDate((prev) => {
    const newDate = new Date(prev);
    newDate.setDate(newDate.getDate() + 1);
    return newDate;
  });
};

const handleToday = () => {
  setSelectedDate(new Date());
};
```

### 2. Smart Date Display

The date display shows relative labels for today, yesterday, and tomorrow:

```javascript
const getDateDisplayText = () => {
  if (isToday()) return 'Today';
  if (isYesterday()) return 'Yesterday';
  if (isTomorrow()) return 'Tomorrow';
  return selectedDate.toLocaleDateString('en-US', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
};
```

### 3. Meal Type Grouping

Meals are grouped and sorted by type in a specific order:

```javascript
const MEAL_TYPE_ORDER = ['BREAKFAST', 'LUNCH', 'DINNER', 'SNACKS', 'OTHER'];

const groupMealsByType = (meals) => {
  const grouped = {};
  meals.forEach((meal) => {
    const type = meal.mealType || 'OTHER';
    if (!grouped[type]) {
      grouped[type] = [];
    }
    grouped[type].push(meal);
  });

  // Sort by meal type order
  const sortedGrouped = {};
  MEAL_TYPE_ORDER.forEach((type) => {
    if (grouped[type]) {
      sortedGrouped[type] = grouped[type];
    }
  });

  return sortedGrouped;
};
```

### 4. Calorie Calculations

Total and subtotal calorie calculations:

```javascript
// Total calories for the day
const getTotalCalories = () => {
  return mealEntries.reduce((sum, meal) => sum + (meal.calories || 0), 0);
};

// Subtotal per meal type (inline in JSX)
meals.reduce((sum, m) => sum + (m.calories || 0), 0)
```

---

## Helper Functions

### Date Comparison Functions

```javascript
const isSameDay = (date1, date2) => {
  return (
    date1.getDate() === date2.getDate() &&
    date1.getMonth() === date2.getMonth() &&
    date1.getFullYear() === date2.getFullYear()
  );
};

const isToday = () => isSameDay(selectedDate, new Date());

const isYesterday = () => {
  const yesterday = new Date();
  yesterday.setDate(yesterday.getDate() - 1);
  return isSameDay(selectedDate, yesterday);
};

const isTomorrow = () => {
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  return isSameDay(selectedDate, tomorrow);
};
```

### Date Formatting

```javascript
const formatDateForApi = (date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

const formatMealType = (type) => {
  return type.charAt(0) + type.slice(1).toLowerCase();
};
```

---

## UI Components

### 1. Date Navigation Card

```jsx
<Card sx={{ mb: 3 }}>
  <CardContent>
    <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
      <Box sx={{ width: 40 }} /> {/* Spacer for centering */}
      
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
        <IconButton onClick={handlePreviousDay}>
          <ChevronLeftIcon />
        </IconButton>
        
        <Box sx={{ textAlign: 'center', width: 300 }}>
          <Typography variant="h6">{getDateDisplayText()}</Typography>
        </Box>
        
        <IconButton onClick={handleNextDay}>
          <ChevronRightIcon />
        </IconButton>
      </Box>
      
      <Box sx={{ width: 40 }}>
        {!isToday() && (
          <IconButton onClick={handleToday} color="primary">
            <TodayIcon />
          </IconButton>
        )}
      </Box>
    </Box>
  </CardContent>
</Card>
```

### 2. Daily Summary Card

```jsx
<Card sx={{ mb: 3 }}>
  <CardContent>
    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
      <Typography variant="h6">Daily Summary</Typography>
      <Box sx={{ textAlign: 'right' }}>
        <Typography variant="h5" color="primary">
          {getTotalCalories()} cal
        </Typography>
        <Typography variant="caption" color="textSecondary">
          {mealEntries.length} meal{mealEntries.length !== 1 ? 's' : ''} logged
        </Typography>
      </Box>
    </Box>
  </CardContent>
</Card>
```

### 3. Meal Entry Item

```jsx
<Box
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
```

### 4. Empty State

```jsx
<Card>
  <CardContent>
    <Typography color="textSecondary" sx={{ textAlign: 'center', py: 4 }}>
      No meals logged for this day.
    </Typography>
  </CardContent>
</Card>
```

---

## Usage

### Route Configuration (App.js)

```javascript
import History from './components/history/History';

// Inside Routes
<Route path="history" element={<History />} />
```

### Navigation Menu (Layout.js)

The History page is accessible from the sidebar navigation:

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
| `@mui/material` | UI components (Box, Card, Typography, etc.) |
| `@mui/icons-material` | Icons (ChevronLeft, ChevronRight, Today) |
| `axios` (via api.js) | HTTP requests |

---

## Data Flow

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  History.js     │────►│  api.js          │────►│  Backend API    │
│  (Date Change)  │     │  (getMealsByDate)│     │  /meal-entries/ │
└─────────────────┘     └──────────────────┘     │  date/{date}    │
                                                 └─────────────────┘
                                                          │
                                                          ▼
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  Render Meals   │◄────│  Group by Type   │◄────│  Response Data  │
│  by Category    │     │  & Calculate     │     │  (MealEntry[])  │
└─────────────────┘     └──────────────────┘     └─────────────────┘
```

---

## Related Features

- **Dashboard** - Shows today's meals summary
- **Log Meal Modal** - Add new meal entries
- **Foods** - Manage food items used in meal entries
