# Frontend AI Reference Documentation

## Overview
- **Framework**: React 18.2.0
- **UI Library**: Material-UI (MUI) 5.15.4
- **Routing**: React Router DOM 6.21.2
- **HTTP Client**: Axios 1.6.5
- **Charts**: Chart.js 4.4.1 + react-chartjs-2 5.2.0
- **Date Handling**: Luxon 3.7.2 (for chart.js adapter)
- **Dev Server Port**: 3000
- **Backend API**: http://localhost:8080/api

## Dependencies (package.json)
```json
{
  "@emotion/react": "^11.11.3",
  "@emotion/styled": "^11.11.0",
  "@mui/icons-material": "^5.15.4",
  "@mui/material": "^5.15.4",
  "axios": "^1.6.5",
  "chart.js": "^4.4.1",
  "chartjs-adapter-luxon": "^1.3.1",
  "luxon": "^3.7.2",
  "react": "^18.2.0",
  "react-chartjs-2": "^5.2.0",
  "react-dom": "^18.2.0",
  "react-router-dom": "^6.21.2",
  "react-scripts": "5.0.1"
}
```

## Scripts
```bash
npm start   # Development server on port 3000
npm build   # Production build
npm test    # Run tests
```

---

## Application Structure

### Entry Point
**File**: `src/index.js`
- Renders `<App />` inside `<React.StrictMode>`

### Main App Component
**File**: `src/App.js`

**Theme Configuration**:
```javascript
palette: {
  primary: { main: '#1976d2' },
  secondary: { main: '#dc004e' },
  background: { default: '#f5f5f5' }
}
typography: { fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif' }
```

**Component Hierarchy**:
```
<ThemeProvider>
  <CssBaseline />
  <AuthProvider>
    <Router>
      <Routes>
        ...
      </Routes>
    </Router>
  </AuthProvider>
</ThemeProvider>
```

---

## Routing

### Route Configuration
| Path | Component | Access | Description |
|------|-----------|--------|-------------|
| `/login` | Login | Public | Login page |
| `/register` | Register | Public | Registration page |
| `/` | Layout (wrapper) | Private | Redirects to /dashboard |
| `/dashboard` | Dashboard | Private | Main dashboard |
| `/foods` | Foods | Private | Food catalog |
| `/history` | History | Private | Meal history |
| `/weight` | WeightTracking | Private | Weight tracking |
| `/profile` | Profile | Private | User profile |
| `*` | - | - | Redirects to /dashboard |

### Route Guards

#### PrivateRoute
- Checks `isAuthenticated` from AuthContext
- Shows loading state while checking auth
- Redirects to `/login` if not authenticated

#### PublicRoute
- Checks `isAuthenticated` from AuthContext
- Redirects to `/dashboard` if already authenticated

---

## Context

### AuthContext
**File**: `src/context/AuthContext.js`

**State**:
```javascript
{
  user: {
    userId: Long,
    name: String,
    email: String,
    bmi: BigDecimal,
    allowedDailyIntake: Integer
  } | null,
  isAuthenticated: boolean,
  loading: boolean
}
```

**Methods**:

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `login` | (email, password) | `{success, message?}` | Authenticates user, stores token |
| `register` | (registrationData) | `{success, message?}` | Registers user, stores token |
| `logout` | - | void | Clears token and user data |

**Storage**:
- `localStorage.token`: JWT token
- `localStorage.user`: JSON stringified user object

**Hook**: `useAuth()`
- Must be used within `<AuthProvider>`
- Returns: `{ user, isAuthenticated, loading, login, register, logout }`

---

## API Service

**File**: `src/services/api.js`

### Axios Instance Configuration
```javascript
baseURL: 'http://localhost:8080/api'
headers: { 'Content-Type': 'application/json' }
```

### Request Interceptor
- Adds `Authorization: Bearer <token>` header from localStorage

### Response Interceptor
- On 401 (non-auth endpoints): Clears storage, redirects to `/login`

### Service Objects

#### authService
```javascript
login(credentials)    // POST /auth/login
register(userData)    // POST /auth/register
```

#### dashboardService
```javascript
getTodaySummary()           // GET /dashboard
getDashboardByDate(date)    // GET /dashboard/date/{date}
```

#### foodService
```javascript
getAllFoods()                    // GET /foods
getFoodsByMealType(mealType)     // GET /foods/meal-type/{mealType}
getFoodById(id)                  // GET /foods/{id}
getCustomFoods()                 // GET /foods/custom
addFood(foodData)                // POST /foods
updateFood(id, foodData)         // PUT /foods/{id}
deleteFood(id)                   // DELETE /foods/{id}
```

#### mealEntryService
```javascript
logMeal(mealData)        // POST /meal-entries
getMealsByDate(date)     // GET /meal-entries/date/{date}
deleteMealEntry(id)      // DELETE /meal-entries/{id}
```

#### weightEntryService
```javascript
logWeight(weightData)                        // POST /weight-entries
getAllWeightEntries()                        // GET /weight-entries
getWeightEntriesByRange(startDate, endDate)  // GET /weight-entries/range?startDate=&endDate=
getLatestWeightEntry()                       // GET /weight-entries/latest
deleteWeightEntry(id)                        // DELETE /weight-entries/{id}
```

#### userService
```javascript
getProfile()    // GET /users/profile
```

---

## Components

### Layout Component
**File**: `src/components/layout/Layout.js`

**Features**:
- Responsive sidebar navigation (permanent on desktop, temporary drawer on mobile)
- AppBar with current page title and user greeting
- Uses `<Outlet />` for nested route rendering

**Navigation Items**:
| Text | Icon | Path |
|------|------|------|
| Dashboard | DashboardIcon | /dashboard |
| Foods | RestaurantIcon | /foods |
| History | HistoryIcon | /history |
| Weight Tracking | MonitorWeightIcon | /weight |
| Profile | PersonIcon | /profile |
| Logout | LogoutIcon | (action) |

**Drawer Width**: 240px

**Breakpoint**: `md` (mobile vs desktop)

---

### Login Component
**File**: `src/components/auth/Login.js`

**State**:
```javascript
formData: { email: '', password: '' }
error: string
loading: boolean
```

**Flow**:
1. User enters email/password
2. Calls `login()` from AuthContext
3. On success: navigates to `/dashboard`
4. On failure: displays error message

**UI Elements**:
- Email TextField (required)
- Password TextField (required, type=password)
- Submit Button (disabled while loading)
- Link to Register page

---

### Register Component
**File**: `src/components/auth/Register.js`

**State**:
```javascript
formData: {
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
  weeklyGoal: '0.5'
}
error: string
loading: boolean
```

**Constants**:
```javascript
activityLevels = [
  { value: 'SEDENTARY', label: 'Sedentary (little to no exercise)' },
  { value: 'LIGHTLY_ACTIVE', label: 'Lightly Active (1-3 days/week)' },
  { value: 'MODERATELY_ACTIVE', label: 'Moderately Active (3-5 days/week)' },
  { value: 'VERY_ACTIVE', label: 'Very Active (6-7 days/week)' }
]

sexOptions = [
  { value: 'MALE', label: 'Male' },
  { value: 'FEMALE', label: 'Female' }
]
```

**Helper Function**:
```javascript
deriveGoalType(currentWeight, targetWeight)
// Returns: 'LOSE' | 'GAIN' | 'MAINTAIN'
```

**Validation**:
- Passwords must match
- Password minimum 6 characters

**Flow**:
1. User fills form
2. goalType is derived from weight vs goal
3. Calls `register()` from AuthContext
4. On success: navigates to `/dashboard`

---

### Dashboard Component
**File**: `src/components/dashboard/Dashboard.js`

**State**:
```javascript
loading: boolean
error: string
logMealOpen: boolean
dashboardData: {
  allowedDailyIntake: number,
  consumedCalories: number,
  remainingCalories: number,
  percentageConsumed: number,
  mealsByType: { [mealType]: MealEntryResponse[] },
  totalMealsCount: number,
  userName: string,
  goalType: string,
  currentWeight: number,
  goalWeight: number,
  todayWeight: number
}
```

**API Call**: `dashboardService.getTodaySummary()`

**UI Sections**:
1. **Welcome Header**: User name + today's date
2. **Calorie Cards** (3 cards):
   - Daily Allowance
   - Consumed
   - Remaining (green if positive, red if negative)
3. **Progress Bar**: Visual percentage with color coding
   - < 50%: success (green)
   - < 80%: warning (yellow)
   - >= 80%: error (red)
4. **Today's Meals**: Grouped by meal type with "Log Meal" button

**Child Component**: `<LogMealModal />`

---

### LogMealModal Component
**File**: `src/components/dashboard/LogMealModal.js`

**Props**:
```javascript
open: boolean
onClose: () => void
onSuccess: () => void
```

**State**:
```javascript
foods: FoodResponse[]
loading: boolean
submitting: boolean
error: string
selectedFood: FoodResponse | null
mealTypeFilter: string
```

**Constants**:
```javascript
MEAL_TYPES = ['BREAKFAST', 'LUNCH', 'DINNER', 'SNACKS', 'OTHER']
```

**Flow**:
1. Fetches all foods on open
2. User can filter by meal type
3. User selects food via Autocomplete
4. Submit creates meal entry with current date/time
5. Calls `onSuccess()` to refresh dashboard

**API Calls**:
- `foodService.getAllFoods()`
- `mealEntryService.logMeal(mealData)`

**Meal Data Format**:
```javascript
{
  foodId: selectedFood.id,
  entryDate: 'YYYY-MM-DD',
  entryTime: 'HH:MM:SS'
}
```

---

### Foods Component
**File**: `src/components/foods/Foods.js`

**State**:
```javascript
foods: FoodResponse[]
filteredFoods: FoodResponse[]
loading: boolean
error: string
searchTerm: string
mealTypeFilter: 'ALL' | MealType
openDialog: boolean
submitting: boolean
formData: { name: '', mealType: '', calories: '' }
formError: string
```

**Constants**:
```javascript
MEAL_TYPES = ['ALL', 'BREAKFAST', 'LUNCH', 'SNACKS', 'DINNER', 'OTHER']
FORM_MEAL_TYPES = ['BREAKFAST', 'LUNCH', 'SNACKS', 'DINNER', 'OTHER']

getMealTypeColor(mealType) = {
  BREAKFAST: '#FF9800',
  LUNCH: '#4CAF50',
  SNACKS: '#9C27B0',
  DINNER: '#2196F3',
  OTHER: '#607D8B'
}
```

**Features**:
1. **Search**: Filter by food name (case-insensitive)
2. **Filter**: Filter by meal type dropdown
3. **Add Food Dialog**: Create custom food
4. **Food Cards**: Grid display with name, meal type chip, calories

**API Calls**:
- `foodService.getAllFoods()`
- `foodService.addFood(foodData)`

**Add Food Data Format**:
```javascript
{
  name: string,
  mealType: MealType,
  calories: number,
  image: null
}
```

---

### History Component
**File**: `src/components/history/History.js`

**State**:
```javascript
selectedDate: Date
loading: boolean
error: string
mealEntries: MealEntryResponse[]
```

**Constants**:
```javascript
MEAL_TYPE_ORDER = ['BREAKFAST', 'LUNCH', 'DINNER', 'SNACKS', 'OTHER']
```

**Helper Functions**:
```javascript
formatDateForApi(date)     // Returns 'YYYY-MM-DD'
groupMealsByType(meals)    // Groups and sorts by MEAL_TYPE_ORDER
getTotalCalories()         // Sum of all meal calories
getDateDisplayText()       // 'Today', 'Yesterday', 'Tomorrow', or full date
```

**Features**:
1. **Date Navigation**: Previous/Next day buttons + Today button
2. **Daily Summary Card**: Total calories + meal count
3. **Meals by Type**: Grouped display with subtotals

**API Call**: `mealEntryService.getMealsByDate(dateStr)`

---

### WeightTracking Component
**File**: `src/components/weight/WeightTracking.js`

**State**:
```javascript
loading: boolean
error: string
weightEntries: WeightEntryResponse[]
openDialog: boolean
newWeight: string
submitting: boolean
```

**Chart.js Registration**:
```javascript
ChartJS.register(
  TimeScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
)
```

**Chart Configuration**:
```javascript
{
  responsive: true,
  maintainAspectRatio: false,
  scales: {
    x: { type: 'time', time: { unit: 'day' } },
    y: { beginAtZero: false }
  }
}
```

**Helper Functions**:
```javascript
formatDate(dateString)      // 'MMM d'
formatFullDate(dateString)  // 'Month d, yyyy'
getChartData()              // Returns Chart.js dataset
getWeightStats()            // Returns { current, min, max, change, entries }
```

**Features**:
1. **Stats Cards** (4 cards):
   - Current Weight
   - Total Change (green if negative, red if positive)
   - Lowest
   - Highest
2. **Line Chart**: Weight progress over time
3. **History Table**: All entries sorted by date DESC
4. **Add Weight Dialog**: Log today's weight

**API Calls**:
- `weightEntryService.getAllWeightEntries()`
- `weightEntryService.logWeight(weightData)`

**Weight Data Format**:
```javascript
{
  entryDate: 'YYYY-MM-DD',
  weight: number
}
```

---

### Profile Component
**File**: `src/components/profile/Profile.js`

**State**:
```javascript
loading: boolean
error: string
profile: UserProfileResponse | null
```

**Helper Functions**:
```javascript
formatDate(dateString)           // 'Month d, yyyy'
formatActivityLevel(level)       // Human-readable label
formatGoalType(type)             // 'Lose Weight' | 'Maintain Weight' | 'Gain Weight'
formatSex(sex)                   // 'Male' | 'Female'
getBmiCategory(bmi)              // { label, color } based on BMI value
```

**BMI Categories**:
| Range | Label | Color |
|-------|-------|-------|
| < 18.5 | Underweight | info |
| 18.5 - 24.9 | Normal | success |
| 25 - 29.9 | Overweight | warning |
| >= 30 | Obese | error |

**UI Sections**:
1. **Profile Card**: Name, email, sex, birthdate, height, activity level
2. **Weight Goals Card**: Current weight, goal weight, goal type, weekly goal, daily calorie allowance
3. **BMI Card**: BMI value with category chip and reference scale

**API Call**: `userService.getProfile()`

---

## File Structure
```
frontend/
├── public/
│   └── index.html
├── src/
│   ├── components/
│   │   ├── auth/
│   │   │   ├── Login.js
│   │   │   └── Register.js
│   │   ├── dashboard/
│   │   │   ├── Dashboard.js
│   │   │   └── LogMealModal.js
│   │   ├── foods/
│   │   │   └── Foods.js
│   │   ├── history/
│   │   │   └── History.js
│   │   ├── layout/
│   │   │   └── Layout.js
│   │   ├── profile/
│   │   │   └── Profile.js
│   │   └── weight/
│   │       └── WeightTracking.js
│   ├── context/
│   │   └── AuthContext.js
│   ├── services/
│   │   └── api.js
│   ├── App.js
│   ├── index.css
│   └── index.js
├── package.json
└── package-lock.json
```

---

## Data Flow Patterns

### Authentication Flow
```
1. User submits login/register form
2. AuthContext calls authService
3. On success: stores token + user in localStorage
4. AuthContext updates state (user, isAuthenticated)
5. PrivateRoute allows access to protected routes
```

### API Request Flow
```
1. Component calls service method
2. Axios interceptor adds Authorization header
3. Backend validates JWT
4. Response returned to component
5. On 401: interceptor clears storage, redirects to login
```

### State Management Pattern
```
- Global state: AuthContext (user, auth status)
- Local state: useState in each component
- No Redux or other state management library
```

---

## Common Patterns

### Loading State
```javascript
if (loading) {
  return (
    <Box display="flex" justifyContent="center" alignItems="center" minHeight="50vh">
      <CircularProgress />
    </Box>
  );
}
```

### Error Display
```javascript
{error && (
  <Alert severity="error" sx={{ mb: 2 }}>
    {error}
  </Alert>
)}
```

### Form Handling
```javascript
const handleChange = (e) => {
  setFormData({
    ...formData,
    [e.target.name]: e.target.value,
  });
};
```

### Date Formatting for API
```javascript
const formatDateForApi = (date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};
```

### useCallback for Fetch Functions
```javascript
const fetchData = useCallback(async () => {
  try {
    setLoading(true);
    const response = await service.getData();
    setData(response.data);
  } catch (err) {
    setError('Failed to load data');
  } finally {
    setLoading(false);
  }
}, [dependencies]);

useEffect(() => {
  fetchData();
}, [fetchData]);
```

---

## MUI Components Used

### Layout
- Box, Container, Grid, Paper, Card, CardContent
- AppBar, Toolbar, Drawer
- Divider

### Navigation
- List, ListItem, ListItemButton, ListItemIcon, ListItemText

### Forms
- TextField, Select, MenuItem, FormControl, InputLabel
- Button, IconButton
- Autocomplete

### Feedback
- Alert, CircularProgress, LinearProgress
- Dialog, DialogTitle, DialogContent, DialogActions

### Data Display
- Typography, Chip, Avatar
- Table, TableContainer, TableHead, TableBody, TableRow, TableCell

### Icons (from @mui/icons-material)
- MenuIcon, DashboardIcon, RestaurantIcon, HistoryIcon
- MonitorWeightIcon, PersonIcon, LogoutIcon
- AddIcon, SearchIcon, ChevronLeftIcon, ChevronRightIcon
- TodayIcon, LocalFireDepartmentIcon, FitnessCenterIcon
