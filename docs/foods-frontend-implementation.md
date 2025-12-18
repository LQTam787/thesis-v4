# Foods Page Frontend Implementation Documentation

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
9. [Add Food Dialog](#add-food-dialog)
10. [Usage](#usage)

---

## Overview

This document describes the Foods page frontend implementation for the Calorie Tracker application. The Foods page provides:

- **Food Listing** - Display all available foods (system + custom) in a responsive card grid
- **Search Functionality** - Filter foods by name with real-time search
- **Meal Type Filtering** - Filter foods by meal type (Breakfast, Lunch, Snacks, Dinner, Other)
- **Add Custom Food** - Create new custom foods via a dialog form
- **Visual Indicators** - Color-coded meal type chips and calorie display

---

## File Structure

```
frontend/src/
├── components/
│   └── foods/
│       └── Foods.js              # Main foods page component
├── services/
│   └── api.js                    # API service (foodService)
└── App.js                        # Route configuration (/foods)
```

---

## API Endpoints Used

The Foods page uses the following backend endpoints:

### GET `/api/foods`

Get all available foods (system foods + user's custom foods).

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
  },
  {
    "id": 10,
    "name": "My Protein Shake",
    "image": "https://example.com/shake.jpg",
    "mealType": "SNACKS",
    "calories": 200,
    "customFood": true,
    "createdAt": "2025-12-18T09:30:00"
  }
]
```

**API Service Call:**
```javascript
import { foodService } from '../../services/api';

// Fetch all foods
const response = await foodService.getAllFoods();
```

### POST `/api/foods`

Create a new custom food.

**Request Body:**
```json
{
  "name": "My Protein Shake",
  "image": null,
  "mealType": "SNACKS",
  "calories": 200
}
```

**Response (201 Created):**
```json
{
  "id": 10,
  "name": "My Protein Shake",
  "image": null,
  "mealType": "SNACKS",
  "calories": 200,
  "customFood": true,
  "createdAt": "2025-12-18T09:30:00"
}
```

**API Service Call:**
```javascript
await foodService.addFood({
  name: 'My Protein Shake',
  mealType: 'SNACKS',
  calories: 200,
  image: null,
});
```

---

## Component Architecture

### Foods.js

```javascript
import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  CircularProgress,
  Alert,
  Chip,
  TextField,
  InputAdornment,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Avatar,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
} from '@mui/material';
import {
  Search as SearchIcon,
  Restaurant as RestaurantIcon,
  LocalFireDepartment as CaloriesIcon,
  Add as AddIcon,
} from '@mui/icons-material';
import { foodService } from '../../services/api';

const MEAL_TYPES = ['ALL', 'BREAKFAST', 'LUNCH', 'SNACKS', 'DINNER', 'OTHER'];
const FORM_MEAL_TYPES = ['BREAKFAST', 'LUNCH', 'SNACKS', 'DINNER', 'OTHER'];

const Foods = () => {
  // Component implementation
};

export default Foods;
```

---

## State Management

The component uses React's `useState` hook for local state management:

| State Variable | Type | Initial Value | Description |
|----------------|------|---------------|-------------|
| `foods` | array | `[]` | All foods fetched from API |
| `filteredFoods` | array | `[]` | Foods after applying search/filter |
| `loading` | boolean | `true` | Loading state during initial fetch |
| `error` | string | `''` | Error message if API call fails |
| `searchTerm` | string | `''` | Current search input value |
| `mealTypeFilter` | string | `'ALL'` | Selected meal type filter |
| `openDialog` | boolean | `false` | Add food dialog visibility |
| `submitting` | boolean | `false` | Form submission loading state |
| `formData` | object | `{name: '', mealType: '', calories: ''}` | Add food form data |
| `formError` | string | `''` | Form validation/submission error |

```javascript
const [foods, setFoods] = useState([]);
const [filteredFoods, setFilteredFoods] = useState([]);
const [loading, setLoading] = useState(true);
const [error, setError] = useState('');
const [searchTerm, setSearchTerm] = useState('');
const [mealTypeFilter, setMealTypeFilter] = useState('ALL');
const [openDialog, setOpenDialog] = useState(false);
const [submitting, setSubmitting] = useState(false);
const [formData, setFormData] = useState({
  name: '',
  mealType: '',
  calories: '',
});
const [formError, setFormError] = useState('');
```

---

## Key Features

### 1. Food Listing with Card Grid

Foods are displayed in a responsive grid layout with hover effects:

```javascript
<Grid container spacing={2}>
  {filteredFoods.map((food) => (
    <Grid item xs={12} sm={6} md={4} lg={3} key={food.id}>
      <Card
        sx={{
          height: '100%',
          display: 'flex',
          flexDirection: 'column',
          transition: 'transform 0.2s, box-shadow 0.2s',
          '&:hover': {
            transform: 'translateY(-4px)',
            boxShadow: 4,
          },
        }}
      >
        {/* Card content */}
      </Card>
    </Grid>
  ))}
</Grid>
```

### 2. Real-time Search

Search filters foods by name as user types:

```javascript
const filterFoods = () => {
  let result = [...foods];

  if (searchTerm) {
    result = result.filter((food) =>
      food.name.toLowerCase().includes(searchTerm.toLowerCase())
    );
  }

  if (mealTypeFilter !== 'ALL') {
    result = result.filter((food) => food.mealType === mealTypeFilter);
  }

  setFilteredFoods(result);
};
```

### 3. Meal Type Filtering

Dropdown filter for meal types:

```javascript
const MEAL_TYPES = ['ALL', 'BREAKFAST', 'LUNCH', 'SNACKS', 'DINNER', 'OTHER'];

<FormControl sx={{ minWidth: 150 }}>
  <InputLabel>Meal Type</InputLabel>
  <Select
    value={mealTypeFilter}
    label="Meal Type"
    onChange={(e) => setMealTypeFilter(e.target.value)}
  >
    {MEAL_TYPES.map((type) => (
      <MenuItem key={type} value={type}>
        {type === 'ALL' ? 'All Types' : type.charAt(0) + type.slice(1).toLowerCase()}
      </MenuItem>
    ))}
  </Select>
</FormControl>
```

### 4. Add Custom Food

Dialog form for creating new foods:

```javascript
const handleSubmit = async () => {
  // Validation
  if (!formData.name.trim()) {
    setFormError('Food name is required');
    return;
  }
  if (!formData.mealType) {
    setFormError('Meal type is required');
    return;
  }
  if (!formData.calories || formData.calories < 0) {
    setFormError('Valid calories value is required');
    return;
  }

  try {
    setSubmitting(true);
    setFormError('');
    await foodService.addFood({
      name: formData.name.trim(),
      mealType: formData.mealType,
      calories: parseInt(formData.calories, 10),
      image: null,
    });
    handleCloseDialog();
    fetchFoods(); // Refresh list
  } catch (err) {
    setFormError(err.response?.data?.message || 'Failed to add food. Please try again.');
  } finally {
    setSubmitting(false);
  }
};
```

---

## Helper Functions

### Meal Type Color Mapping

```javascript
const getMealTypeColor = (mealType) => {
  const colors = {
    BREAKFAST: '#FF9800',  // Orange
    LUNCH: '#4CAF50',      // Green
    SNACKS: '#9C27B0',     // Purple
    DINNER: '#2196F3',     // Blue
    OTHER: '#607D8B',      // Grey
  };
  return colors[mealType] || '#607D8B';
};
```

### Dialog Handlers

```javascript
const handleOpenDialog = () => {
  setOpenDialog(true);
  setFormError('');
};

const handleCloseDialog = () => {
  setOpenDialog(false);
  setFormData({ name: '', mealType: '', calories: '' });
  setFormError('');
};

const handleFormChange = (e) => {
  const { name, value } = e.target;
  setFormData((prev) => ({ ...prev, [name]: value }));
};
```

---

## UI Components

### 1. Page Header with Add Button

```jsx
<Typography variant="h4" gutterBottom>
  Foods
</Typography>
<Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
  <Typography variant="subtitle1" color="textSecondary">
    Browse available foods for meal tracking
  </Typography>
  <Button
    variant="contained"
    startIcon={<AddIcon />}
    onClick={handleOpenDialog}
  >
    Add Food
  </Button>
</Box>
```

### 2. Search and Filter Bar

```jsx
<Box sx={{ display: 'flex', gap: 2, mb: 3, flexWrap: 'wrap' }}>
  <TextField
    placeholder="Search foods..."
    value={searchTerm}
    onChange={(e) => setSearchTerm(e.target.value)}
    sx={{ flexGrow: 1, minWidth: 200 }}
    InputProps={{
      startAdornment: (
        <InputAdornment position="start">
          <SearchIcon />
        </InputAdornment>
      ),
    }}
  />
  <FormControl sx={{ minWidth: 150 }}>
    <InputLabel>Meal Type</InputLabel>
    <Select
      value={mealTypeFilter}
      label="Meal Type"
      onChange={(e) => setMealTypeFilter(e.target.value)}
    >
      {MEAL_TYPES.map((type) => (
        <MenuItem key={type} value={type}>
          {type === 'ALL' ? 'All Types' : type.charAt(0) + type.slice(1).toLowerCase()}
        </MenuItem>
      ))}
    </Select>
  </FormControl>
</Box>
```

### 3. Food Card

```jsx
<Card sx={{ height: '100%', /* ... hover styles */ }}>
  <CardContent sx={{ flexGrow: 1 }}>
    <Box display="flex" alignItems="center" mb={2}>
      <Avatar
        src={food.image}
        sx={{
          width: 48,
          height: 48,
          bgcolor: getMealTypeColor(food.mealType),
          mr: 2,
        }}
      >
        <RestaurantIcon />
      </Avatar>
      <Box sx={{ flexGrow: 1, minWidth: 0 }}>
        <Typography
          variant="h6"
          sx={{
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            whiteSpace: 'nowrap',
          }}
        >
          {food.name}
        </Typography>
        <Chip
          label={food.mealType}
          size="small"
          sx={{
            bgcolor: getMealTypeColor(food.mealType),
            color: 'white',
            fontSize: '0.7rem',
          }}
        />
      </Box>
    </Box>

    <Box display="flex" alignItems="center">
      <CaloriesIcon sx={{ color: 'warning.main', mr: 0.5, fontSize: 20 }} />
      <Typography variant="body1" fontWeight="bold">
        {food.calories}
      </Typography>
      <Typography variant="body2" color="textSecondary" sx={{ ml: 0.5 }}>
        cal
      </Typography>
    </Box>
  </CardContent>
</Card>
```

### 4. Empty State

```jsx
<Card>
  <CardContent>
    <Box display="flex" flexDirection="column" alignItems="center" py={4}>
      <RestaurantIcon sx={{ fontSize: 64, color: 'text.secondary', mb: 2 }} />
      <Typography variant="h6" color="textSecondary">
        No foods found
      </Typography>
      <Typography variant="body2" color="textSecondary">
        {searchTerm || mealTypeFilter !== 'ALL'
          ? 'Try adjusting your search or filter'
          : 'No foods available yet'}
      </Typography>
    </Box>
  </CardContent>
</Card>
```

### 5. Results Count

```jsx
<Typography variant="body2" color="textSecondary" sx={{ mb: 2 }}>
  Showing {filteredFoods.length} of {foods.length} foods
</Typography>
```

---

## Add Food Dialog

### Dialog Structure

```jsx
<Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
  <DialogTitle>Add New Food</DialogTitle>
  <DialogContent>
    {formError && (
      <Alert severity="error" sx={{ mb: 2, mt: 1 }}>
        {formError}
      </Alert>
    )}
    <TextField
      autoFocus
      margin="dense"
      name="name"
      label="Food Name"
      type="text"
      fullWidth
      value={formData.name}
      onChange={handleFormChange}
      sx={{ mb: 2 }}
    />
    <FormControl fullWidth sx={{ mb: 2 }}>
      <InputLabel>Meal Type</InputLabel>
      <Select
        name="mealType"
        value={formData.mealType}
        label="Meal Type"
        onChange={handleFormChange}
      >
        {FORM_MEAL_TYPES.map((type) => (
          <MenuItem key={type} value={type}>
            {type.charAt(0) + type.slice(1).toLowerCase()}
          </MenuItem>
        ))}
      </Select>
    </FormControl>
    <TextField
      margin="dense"
      name="calories"
      label="Calories"
      type="number"
      fullWidth
      value={formData.calories}
      onChange={handleFormChange}
      inputProps={{ min: 0, max: 10000 }}
    />
  </DialogContent>
  <DialogActions>
    <Button onClick={handleCloseDialog} disabled={submitting}>
      Cancel
    </Button>
    <Button onClick={handleSubmit} variant="contained" disabled={submitting}>
      {submitting ? 'Adding...' : 'Add Food'}
    </Button>
  </DialogActions>
</Dialog>
```

### Form Fields

| Field | Type | Validation | Description |
|-------|------|------------|-------------|
| name | text | Required, trimmed | Food name |
| mealType | select | Required | One of: BREAKFAST, LUNCH, SNACKS, DINNER, OTHER |
| calories | number | Required, min: 0, max: 10000 | Calorie count |
| image | - | Set to null | Image URL (not editable in current implementation) |

---

## Usage

### Route Configuration (App.js)

```javascript
import Foods from './components/foods/Foods';

// Inside Routes
<Route path="foods" element={<Foods />} />
```

### Navigation Menu (Layout.js)

The Foods page is accessible from the sidebar navigation:

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
| `@mui/material` | UI components (Box, Card, Grid, Dialog, etc.) |
| `@mui/icons-material` | Icons (Search, Restaurant, LocalFireDepartment, Add) |
| `axios` (via api.js) | HTTP requests |

---

## Data Flow

```
┌─────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  Foods.js       │────►│  api.js          │────►│  Backend API    │
│  (Component)    │     │  (foodService)   │     │  /api/foods     │
└─────────────────┘     └──────────────────┘     └─────────────────┘
        │                                                 │
        │ Filter locally                                  ▼
        ▼                                        ┌─────────────────┐
┌─────────────────┐                              │  Response Data  │
│  filteredFoods  │◄─────────────────────────────│  (Food[])       │
│  (search/type)  │                              └─────────────────┘
└─────────────────┘
        │
        ▼
┌─────────────────┐
│  Render Cards   │
│  in Grid        │
└─────────────────┘
```

---

## Meal Type Colors

| Meal Type | Color | Hex Code |
|-----------|-------|----------|
| BREAKFAST | Orange | `#FF9800` |
| LUNCH | Green | `#4CAF50` |
| SNACKS | Purple | `#9C27B0` |
| DINNER | Blue | `#2196F3` |
| OTHER | Grey | `#607D8B` |

---

## Related Features

- **Dashboard** - Shows today's calorie summary
- **History** - View meal entries by date
- **Log Meal** - Use foods to create meal entries
- **Backend Food API** - See `docs/food-implementation.md` for backend details
