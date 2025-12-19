# Profile Frontend Implementation Documentation

**Created**: December 19, 2025  
**Status**: Completed

---

## Overview

This document describes the frontend implementation for the User Profile page in the Calorie Tracker application. The Profile page displays user information in three sections: Profile, Weight Goals, and BMI.

---

## File Structure

```
frontend/src/
├── components/
│   └── profile/
│       └── Profile.js           # Profile page component
├── services/
│   └── api.js                   # API service (userService.getProfile)
└── App.js                       # Route configuration (/profile)
```

---

## API Endpoint Used

### GET `/api/users/profile`

Fetches the authenticated user's profile data.

**API Service Call:**
```javascript
import { userService } from '../../services/api';

const response = await userService.getProfile();
```

**Response Data Structure:**
```json
{
  "userId": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "dob": "1990-05-15",
  "sex": "MALE",
  "height": 175.00,
  "activityLevel": "MODERATELY_ACTIVE",
  "weight": 75.50,
  "goal": 70.00,
  "goalType": "LOSE",
  "weeklyGoal": 0.50,
  "bmi": 24.65,
  "allowedDailyIntake": 1925
}
```

---

## API Service Definition

**File**: `src/services/api.js`

```javascript
export const userService = {
  getProfile: () => api.get('/users/profile'),
};
```

---

## Profile Component

**File**: `src/components/profile/Profile.js`

### Imports

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
  Divider,
  Chip,
} from '@mui/material';
import PersonIcon from '@mui/icons-material/Person';
import FitnessCenterIcon from '@mui/icons-material/FitnessCenter';
import MonitorWeightIcon from '@mui/icons-material/MonitorWeight';
import { userService } from '../../services/api';
```

### State Variables

| State Variable | Type | Initial Value | Description |
|----------------|------|---------------|-------------|
| `loading` | boolean | `true` | Loading state during API call |
| `error` | string | `''` | Error message if API call fails |
| `profile` | object | `null` | Profile data from API |

### Data Fetching

```javascript
useEffect(() => {
  const fetchProfile = async () => {
    try {
      setLoading(true);
      setError('');
      const response = await userService.getProfile();
      setProfile(response.data);
    } catch (err) {
      console.error('Failed to fetch profile:', err);
      setError('Failed to load profile data. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  fetchProfile();
}, []);
```

---

## Helper Functions

### formatDate
Formats ISO date string to human-readable format.

```javascript
const formatDate = (dateString) => {
  if (!dateString) return '-';
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
};
```

**Example**: `"1990-05-15"` → `"May 15, 1990"`

### formatActivityLevel
Converts activity level enum to display label.

```javascript
const formatActivityLevel = (level) => {
  if (!level) return '-';
  const labels = {
    SEDENTARY: 'Sedentary (little to no exercise)',
    LIGHTLY_ACTIVE: 'Lightly Active (1-3 days/week)',
    MODERATELY_ACTIVE: 'Moderately Active (3-5 days/week)',
    VERY_ACTIVE: 'Very Active (6-7 days/week)',
  };
  return labels[level] || level;
};
```

### formatGoalType
Converts goal type enum to display label.

```javascript
const formatGoalType = (type) => {
  if (!type) return '-';
  const labels = {
    LOSE: 'Lose Weight',
    MAINTAIN: 'Maintain Weight',
    GAIN: 'Gain Weight',
  };
  return labels[type] || type;
};
```

### formatSex
Capitalizes sex enum value.

```javascript
const formatSex = (sex) => {
  if (!sex) return '-';
  return sex.charAt(0) + sex.slice(1).toLowerCase();
};
```

**Example**: `"MALE"` → `"Male"`

### getBmiCategory
Returns BMI category label and color based on BMI value.

```javascript
const getBmiCategory = (bmi) => {
  if (!bmi) return { label: '-', color: 'default' };
  const value = parseFloat(bmi);
  if (value < 18.5) return { label: 'Underweight', color: 'info' };
  if (value < 25) return { label: 'Normal', color: 'success' };
  if (value < 30) return { label: 'Overweight', color: 'warning' };
  return { label: 'Obese', color: 'error' };
};
```

**BMI Categories:**
| Range | Label | Chip Color |
|-------|-------|------------|
| < 18.5 | Underweight | info (blue) |
| 18.5 - 24.9 | Normal | success (green) |
| 25 - 29.9 | Overweight | warning (yellow) |
| ≥ 30 | Obese | error (red) |

---

## UI Sections

### 1. Profile Section

Displays personal information in a Card component.

**Fields Displayed:**
| Field | Source | Formatting |
|-------|--------|------------|
| Name | `profile.name` | As-is |
| Email | `profile.email` | As-is |
| Sex | `profile.sex` | `formatSex()` |
| Birthdate | `profile.dob` | `formatDate()` |
| Height | `profile.height` | Append " cm" |
| Activity Level | `profile.activityLevel` | `formatActivityLevel()` |

**Grid**: `xs={12} md={6}` (full width on mobile, half on desktop)

### 2. Weight Goals Section

Displays weight-related goals and calorie allowance.

**Fields Displayed:**
| Field | Source | Formatting |
|-------|--------|------------|
| Current Weight | `profile.weight` | Append " kg" |
| Weight Goal | `profile.goal` | Append " kg" |
| Goal Type | `profile.goalType` | `formatGoalType()` with colored Chip |
| Weekly Goal | `profile.weeklyGoal` | Append " kg/week" |
| Daily Calorie Allowance | `profile.allowedDailyIntake` | Append " kcal", primary color |

**Goal Type Chip Colors:**
| Goal Type | Chip Color |
|-----------|------------|
| LOSE | error (red) |
| GAIN | success (green) |
| MAINTAIN | default (grey) |

**Grid**: `xs={12} md={6}` with `height: '100%'` to match Profile section height

### 3. BMI Section

Displays BMI value with category and reference ranges.

**Components:**
- Large BMI value display (Typography variant="h2")
- Category Chip with color based on BMI range
- Explanatory text
- Reference range display (Underweight, Normal, Overweight, Obese)

**Grid**: `xs={12} md={6}` with `mx: 'auto'` to center horizontally

---

## Layout Structure

```
┌─────────────────────────────────────────────────────────┐
│  My Profile (h4)                                        │
├───────────────────────────┬─────────────────────────────┤
│  Profile Card             │  Weight Goals Card          │
│  - Name                   │  - Current Weight           │
│  - Email                  │  - Weight Goal              │
│  - Sex                    │  - Goal Type (Chip)         │
│  - Birthdate              │  - Weekly Goal              │
│  - Height                 │  - Daily Calorie Allowance  │
│  - Activity Level         │                             │
├───────────────────────────┴─────────────────────────────┤
│                    BMI Card (centered)                  │
│                    ┌─────────────────┐                  │
│                    │      24.7       │                  │
│                    │    [Normal]     │                  │
│                    │                 │                  │
│                    │  BMI Reference  │                  │
│                    │  <18.5 | 18.5-  │                  │
│                    │  24.9 | 25-29.9 │                  │
│                    │  | ≥30          │                  │
│                    └─────────────────┘                  │
└─────────────────────────────────────────────────────────┘
```

---

## Routing Configuration

**File**: `src/App.js`

```javascript
import Profile from './components/profile/Profile';

// Inside Routes, under PrivateRoute Layout
<Route path="profile" element={<Profile />} />
```

**Route**: `/profile` (requires authentication)

---

## Navigation Menu

**File**: `src/components/layout/Layout.js`

```javascript
import PersonIcon from '@mui/icons-material/Person';

const menuItems = [
  { text: 'Dashboard', icon: <DashboardIcon />, path: '/dashboard' },
  { text: 'Foods', icon: <RestaurantIcon />, path: '/foods' },
  { text: 'History', icon: <HistoryIcon />, path: '/history' },
  { text: 'Weight Tracking', icon: <MonitorWeightIcon />, path: '/weight' },
  { text: 'Profile', icon: <PersonIcon />, path: '/profile' },
];
```

---

## Loading State

Displays centered CircularProgress while fetching data:

```jsx
if (loading) {
  return (
    <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh">
      <CircularProgress />
    </Box>
  );
}
```

---

## Error State

Displays Alert component on API error:

```jsx
if (error) {
  return (
    <Box sx={{ p: 2 }}>
      <Alert severity="error">{error}</Alert>
    </Box>
  );
}
```

---

## Dependencies

| Dependency | Purpose |
|------------|---------|
| `react` | Component framework |
| `@mui/material` | UI components (Box, Card, Grid, Typography, Chip, etc.) |
| `@mui/icons-material` | Icons (PersonIcon, FitnessCenterIcon, MonitorWeightIcon) |
| `axios` (via api.js) | HTTP requests |

---

## Complete Component Code

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
  Divider,
  Chip,
} from '@mui/material';
import PersonIcon from '@mui/icons-material/Person';
import FitnessCenterIcon from '@mui/icons-material/FitnessCenter';
import MonitorWeightIcon from '@mui/icons-material/MonitorWeight';
import { userService } from '../../services/api';

const Profile = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [profile, setProfile] = useState(null);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        setLoading(true);
        setError('');
        const response = await userService.getProfile();
        setProfile(response.data);
      } catch (err) {
        console.error('Failed to fetch profile:', err);
        setError('Failed to load profile data. Please try again.');
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, []);

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  const formatActivityLevel = (level) => {
    if (!level) return '-';
    const labels = {
      SEDENTARY: 'Sedentary (little to no exercise)',
      LIGHTLY_ACTIVE: 'Lightly Active (1-3 days/week)',
      MODERATELY_ACTIVE: 'Moderately Active (3-5 days/week)',
      VERY_ACTIVE: 'Very Active (6-7 days/week)',
    };
    return labels[level] || level;
  };

  const formatGoalType = (type) => {
    if (!type) return '-';
    const labels = {
      LOSE: 'Lose Weight',
      MAINTAIN: 'Maintain Weight',
      GAIN: 'Gain Weight',
    };
    return labels[type] || type;
  };

  const formatSex = (sex) => {
    if (!sex) return '-';
    return sex.charAt(0) + sex.slice(1).toLowerCase();
  };

  const getBmiCategory = (bmi) => {
    if (!bmi) return { label: '-', color: 'default' };
    const value = parseFloat(bmi);
    if (value < 18.5) return { label: 'Underweight', color: 'info' };
    if (value < 25) return { label: 'Normal', color: 'success' };
    if (value < 30) return { label: 'Overweight', color: 'warning' };
    return { label: 'Obese', color: 'error' };
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh">
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Box sx={{ p: 2 }}>
        <Alert severity="error">{error}</Alert>
      </Box>
    );
  }

  const bmiInfo = getBmiCategory(profile?.bmi);

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        My Profile
      </Typography>

      <Grid container spacing={3}>
        {/* Profile Section */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                <PersonIcon sx={{ mr: 1, color: 'primary.main' }} />
                <Typography variant="h6">Profile</Typography>
              </Box>
              <Divider sx={{ mb: 2 }} />

              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography color="textSecondary">Name</Typography>
                  <Typography fontWeight="medium">{profile?.name || '-'}</Typography>
                </Box>

                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography color="textSecondary">Email</Typography>
                  <Typography fontWeight="medium">{profile?.email || '-'}</Typography>
                </Box>

                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography color="textSecondary">Sex</Typography>
                  <Typography fontWeight="medium">{formatSex(profile?.sex)}</Typography>
                </Box>

                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography color="textSecondary">Birthdate</Typography>
                  <Typography fontWeight="medium">{formatDate(profile?.dob)}</Typography>
                </Box>

                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography color="textSecondary">Height</Typography>
                  <Typography fontWeight="medium">
                    {profile?.height ? `${profile.height} cm` : '-'}
                  </Typography>
                </Box>

                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography color="textSecondary">Activity Level</Typography>
                  <Typography fontWeight="medium" sx={{ textAlign: 'right', maxWidth: '60%' }}>
                    {formatActivityLevel(profile?.activityLevel)}
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Weight Goals Section */}
        <Grid item xs={12} md={6}>
          <Card sx={{ height: '100%' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                <FitnessCenterIcon sx={{ mr: 1, color: 'primary.main' }} />
                <Typography variant="h6">Weight Goals</Typography>
              </Box>
              <Divider sx={{ mb: 2 }} />

              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography color="textSecondary">Current Weight</Typography>
                  <Typography fontWeight="medium">
                    {profile?.weight ? `${profile.weight} kg` : '-'}
                  </Typography>
                </Box>

                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography color="textSecondary">Weight Goal</Typography>
                  <Typography fontWeight="medium">
                    {profile?.goal ? `${profile.goal} kg` : '-'}
                  </Typography>
                </Box>

                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography color="textSecondary">Goal Type</Typography>
                  <Chip
                    label={formatGoalType(profile?.goalType)}
                    size="small"
                    color={
                      profile?.goalType === 'LOSE'
                        ? 'error'
                        : profile?.goalType === 'GAIN'
                        ? 'success'
                        : 'default'
                    }
                    variant="outlined"
                  />
                </Box>

                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography color="textSecondary">Weekly Goal</Typography>
                  <Typography fontWeight="medium">
                    {profile?.weeklyGoal ? `${profile.weeklyGoal} kg/week` : '-'}
                  </Typography>
                </Box>

                <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                  <Typography color="textSecondary">Daily Calorie Allowance</Typography>
                  <Typography fontWeight="medium" color="primary">
                    {profile?.allowedDailyIntake ? `${profile.allowedDailyIntake} kcal` : '-'}
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* BMI Section */}
        <Grid item xs={12} md={6} sx={{ mx: 'auto' }}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                <MonitorWeightIcon sx={{ mr: 1, color: 'primary.main' }} />
                <Typography variant="h6">BMI</Typography>
              </Box>
              <Divider sx={{ mb: 2 }} />

              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', flexDirection: 'column', py: 2 }}>
                <Typography variant="h2" color="primary" fontWeight="bold">
                  {profile?.bmi ? parseFloat(profile.bmi).toFixed(1) : '-'}
                </Typography>
                <Chip
                  label={bmiInfo.label}
                  color={bmiInfo.color}
                  sx={{ mt: 1 }}
                />
                <Typography variant="body2" color="textSecondary" sx={{ mt: 2, textAlign: 'center' }}>
                  Body Mass Index (BMI) is calculated from your weight and height.
                </Typography>
              </Box>

              <Divider sx={{ my: 2 }} />

              <Box sx={{ display: 'flex', justifyContent: 'space-around' }}>
                <Box sx={{ textAlign: 'center' }}>
                  <Typography variant="caption" color="textSecondary">Underweight</Typography>
                  <Typography variant="body2">&lt; 18.5</Typography>
                </Box>
                <Box sx={{ textAlign: 'center' }}>
                  <Typography variant="caption" color="textSecondary">Normal</Typography>
                  <Typography variant="body2">18.5 - 24.9</Typography>
                </Box>
                <Box sx={{ textAlign: 'center' }}>
                  <Typography variant="caption" color="textSecondary">Overweight</Typography>
                  <Typography variant="body2">25 - 29.9</Typography>
                </Box>
                <Box sx={{ textAlign: 'center' }}>
                  <Typography variant="caption" color="textSecondary">Obese</Typography>
                  <Typography variant="body2">&ge; 30</Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Profile;
```

---

## Related Features

- **Registration** - Collects initial profile data
- **Weight Tracking** - Updates current weight
- **Dashboard** - Displays daily calorie allowance
