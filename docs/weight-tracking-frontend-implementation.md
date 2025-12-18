# Weight Tracking Frontend Implementation Documentation

**Created**: December 18, 2025  
**Status**: Completed and Verified

---

## Table of Contents

1. [Overview](#overview)
2. [File Structure](#file-structure)
3. [Dependencies](#dependencies)
4. [Component Structure](#component-structure)
5. [API Integration](#api-integration)
6. [Features](#features)
7. [Component Code](#component-code)
8. [Styling and UI](#styling-and-ui)

---

## Overview

This document describes the Weight Tracking frontend implementation for the Calorie Tracker application. The Weight Tracking page provides:

- **Line Graph Visualization** - Interactive chart displaying weight progress over time with proper date spacing
- **Statistics Cards** - Current weight, total change, lowest, and highest weight values
- **Weight History Table** - Tabular view of all weight entries sorted by date
- **Log Weight Dialog** - Simple modal to add new weight entries (auto-dated to today)

---

## File Structure

```
frontend/src/
├── components/
│   └── weight/
│       └── WeightTracking.js      # Main weight tracking component
├── services/
│   └── api.js                     # API service with weightEntryService
└── App.js                         # Route configuration
```

---

## Dependencies

The Weight Tracking feature uses the following dependencies:

| Package | Version | Purpose |
|---------|---------|---------|
| `chart.js` | ^4.4.1 | Core charting library |
| `react-chartjs-2` | ^5.2.0 | React wrapper for Chart.js |
| `chartjs-adapter-luxon` | latest | Date adapter for time-based x-axis |
| `luxon` | latest | Date/time library for chart adapter |
| `@mui/material` | ^5.15.4 | UI components |
| `@mui/icons-material` | ^5.15.4 | Icons |

### Installation

```bash
npm install chartjs-adapter-luxon luxon
```

---

## Component Structure

### State Variables

| State | Type | Description |
|-------|------|-------------|
| `loading` | boolean | Loading state for data fetch |
| `error` | string | Error message display |
| `weightEntries` | array | Array of weight entry objects |
| `openDialog` | boolean | Controls Log Weight dialog visibility |
| `newWeight` | string | Input value for new weight |
| `submitting` | boolean | Form submission state |

### Weight Entry Object

```javascript
{
  id: 1,
  entryDate: "2025-12-18",
  weight: 75.5,
  createdAt: "2025-12-18T08:30:00"
}
```

---

## API Integration

### weightEntryService (api.js)

```javascript
export const weightEntryService = {
  logWeight: (weightData) => api.post('/weight-entries', weightData),
  getAllWeightEntries: () => api.get('/weight-entries'),
  getWeightEntriesByRange: (startDate, endDate) => 
    api.get(`/weight-entries/range?startDate=${startDate}&endDate=${endDate}`),
  getLatestWeightEntry: () => api.get('/weight-entries/latest'),
  deleteWeightEntry: (id) => api.delete(`/weight-entries/${id}`),
};
```

### API Endpoints Used

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/weight-entries` | Fetch all weight entries for user |
| POST | `/api/weight-entries` | Create new weight entry |

### Request Body (Log Weight)

```json
{
  "entryDate": "2025-12-18",
  "weight": 75.5
}
```

---

## Features

### 1. Line Graph with Time-Based X-Axis

The chart uses Chart.js with `TimeScale` for proper date spacing:

```javascript
import {
  Chart as ChartJS,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  TimeScale,
} from 'chart.js';
import 'chartjs-adapter-luxon';

ChartJS.register(
  TimeScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
);
```

**Chart Configuration:**

```javascript
const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    title: {
      display: true,
      text: 'Weight Progress',
      font: { size: 16, weight: 'bold' },
    },
    tooltip: {
      callbacks: {
        label: (context) => `${context.parsed.y} kg`,
      },
    },
  },
  scales: {
    y: {
      beginAtZero: false,
      title: { display: true, text: 'Weight (kg)' },
    },
    x: {
      type: 'time',
      time: {
        unit: 'day',
        displayFormats: { day: 'MMM d' },
        tooltipFormat: 'MMM d, yyyy',
      },
      title: { display: true, text: 'Date' },
    },
  },
};
```

**Data Format:**

```javascript
const getChartData = () => {
  const sortedEntries = [...weightEntries].sort(
    (a, b) => new Date(a.entryDate) - new Date(b.entryDate)
  );

  return {
    datasets: [
      {
        label: 'Weight (kg)',
        data: sortedEntries.map((entry) => ({
          x: entry.entryDate,  // ISO date string
          y: parseFloat(entry.weight),
        })),
        borderColor: '#1976d2',
        backgroundColor: 'rgba(25, 118, 210, 0.1)',
        tension: 0.3,
        fill: true,
        pointBackgroundColor: '#1976d2',
        pointBorderColor: '#fff',
        pointBorderWidth: 2,
        pointRadius: 5,
        pointHoverRadius: 7,
      },
    ],
  };
};
```

### 2. Statistics Cards

Displays four key metrics:

| Card | Description | Color Logic |
|------|-------------|-------------|
| Current Weight | Latest weight entry | Primary color |
| Total Change | Difference from first to latest | Green if ≤0, Red if >0 |
| Lowest | Minimum weight recorded | Default |
| Highest | Maximum weight recorded | Default |

```javascript
const getWeightStats = () => {
  if (weightEntries.length === 0) return null;

  const weights = weightEntries.map((e) => parseFloat(e.weight));
  const sortedByDate = [...weightEntries].sort(
    (a, b) => new Date(a.entryDate) - new Date(b.entryDate)
  );

  const firstWeight = parseFloat(sortedByDate[0].weight);
  const latestWeight = parseFloat(sortedByDate[sortedByDate.length - 1].weight);
  const weightChange = latestWeight - firstWeight;

  return {
    current: latestWeight,
    min: Math.min(...weights),
    max: Math.max(...weights),
    change: weightChange,
    entries: weightEntries.length,
  };
};
```

### 3. Weight History Table

Displays all entries in descending date order:

| Column | Description |
|--------|-------------|
| Date | Full date format (e.g., "December 18, 2025") |
| Weight (kg) | Weight value |

### 4. Log Weight Dialog

Simple modal with single input field:

- **Weight Input**: Number field with step 0.1, min 20, max 500
- **Auto-Date**: Entry date automatically set to today's date
- **Validation**: Requires weight value before submission
- **Upsert Behavior**: If entry exists for today, it will be updated

---

## Component Code

### WeightTracking.js

```javascript
import React, { useState, useEffect, useCallback } from 'react';
import {
  Box, Card, CardContent, Typography, CircularProgress, Alert,
  Button, TextField, Dialog, DialogTitle, DialogContent, DialogActions,
  Grid, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import {
  Chart as ChartJS, LinearScale, PointElement, LineElement,
  Title, Tooltip, Legend, TimeScale,
} from 'chart.js';
import 'chartjs-adapter-luxon';
import { Line } from 'react-chartjs-2';
import { weightEntryService } from '../../services/api';

ChartJS.register(TimeScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend);

const WeightTracking = () => {
  // Component implementation...
};

export default WeightTracking;
```

---

## Styling and UI

### Layout Structure

```
┌─────────────────────────────────────────────────────────────┐
│  Weight Tracking                           [Log Weight]     │
├─────────────────────────────────────────────────────────────┤
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │ Current  │ │  Total   │ │  Lowest  │ │ Highest  │       │
│  │  Weight  │ │  Change  │ │          │ │          │       │
│  │  75.5 kg │ │ -2.0 kg  │ │ 73.0 kg  │ │ 77.5 kg  │       │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘       │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│                    Weight Progress                          │
│                                                             │
│     78 ─┐                                                   │
│         │    •                                              │
│     76 ─┤         •                                         │
│         │              •    •                               │
│     74 ─┤                        •                          │
│         │                             •                     │
│     72 ─┴────┬────┬────┬────┬────┬────┬────                │
│           Dec 1  Dec 5  Dec 10 Dec 15 Dec 18               │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│  Weight History                                             │
│  ┌─────────────────────────────┬───────────────────┐       │
│  │ Date                        │ Weight (kg)       │       │
│  ├─────────────────────────────┼───────────────────┤       │
│  │ December 18, 2025           │ 74.0              │       │
│  │ December 15, 2025           │ 74.5              │       │
│  │ December 10, 2025           │ 75.0              │       │
│  └─────────────────────────────┴───────────────────┘       │
└─────────────────────────────────────────────────────────────┘
```

### Material-UI Components Used

| Component | Usage |
|-----------|-------|
| `Box` | Layout containers |
| `Card`, `CardContent` | Stats cards and chart container |
| `Typography` | Text elements |
| `Grid` | Responsive grid for stats cards |
| `Button` | Log Weight button |
| `TextField` | Weight input field |
| `Dialog`, `DialogTitle`, `DialogContent`, `DialogActions` | Log Weight modal |
| `Table`, `TableBody`, `TableCell`, `TableContainer`, `TableHead`, `TableRow` | History table |
| `Paper` | Table container |
| `CircularProgress` | Loading spinner |
| `Alert` | Error messages |

---

## Routing Configuration

### App.js

```javascript
import WeightTracking from './components/weight/WeightTracking';

// Inside Routes
<Route path="weight" element={<WeightTracking />} />
```

### Layout.js (Navigation)

```javascript
const menuItems = [
  { text: 'Dashboard', icon: <DashboardIcon />, path: '/dashboard' },
  { text: 'Foods', icon: <RestaurantIcon />, path: '/foods' },
  { text: 'History', icon: <HistoryIcon />, path: '/history' },
  { text: 'Weight Tracking', icon: <MonitorWeightIcon />, path: '/weight' },
];
```

---

## Error Handling

| Scenario | Handling |
|----------|----------|
| Failed to fetch entries | Display error alert, set empty array |
| Failed to log weight | Display API error message or generic error |
| Empty weight input | Show validation error |

---

## Business Rules

1. **Auto-Date**: Weight entries are always logged for today's date
2. **Upsert Behavior**: If an entry exists for today, it will be updated (backend handles this)
3. **Sorting**: Chart displays entries in ascending date order; table displays in descending order
4. **Weight Change Color**: Negative change (weight loss) shows green; positive change shows red
5. **Empty State**: Shows helpful message when no entries exist

---

## Related Backend Documentation

- [Weight Entry Implementation](./weight-entry-implementation.md) - Backend API documentation

---

## Future Enhancements

1. **Goal Line** - Display target weight as a horizontal line on the chart
2. **Date Range Filter** - Filter chart data by date range
3. **Export Data** - Export weight history to CSV
4. **BMI Display** - Show BMI calculation based on current weight
