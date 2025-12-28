# Plan Feature Implementation

## Overview
The Plan feature generates personalized 7-day meal plans for users using Google's Gemini API (gemini-2.5-flash model). The system automatically generates a plan when the user first accesses the feature or when the existing plan is older than 7 days. Plans are stored in the database and associated with users in a 1-1 relationship.

## Architecture

### Backend Stack
- **Framework**: Spring Boot 3.2.1
- **HTTP Client**: WebClient (Spring WebFlux)
- **External API**: Google Gemini API (gemini-2.5-flash)
- **Authentication**: JWT Bearer tokens via Spring Security
- **Database**: JPA/Hibernate with relational database

### Frontend Stack
- **Framework**: React 18.2.0
- **UI Library**: Material-UI 5.15.4
- **HTTP Client**: Axios with interceptors
- **Routing**: React Router v6

## Database Schema

### Plan Entity
- **Table**: `plans`
- **Relationship**: One-to-One with User (bidirectional)
- **Fields**:
  - `id` (Long, Primary Key, Auto-generated)
  - `text` (String, TEXT column type): Stores the generated meal plan text
  - `user_id` (Long, Foreign Key, NOT NULL, UNIQUE): References users table
  - `created_at` (LocalDateTime, NOT NULL): Timestamp when plan was created
  - `updated_at` (LocalDateTime): Timestamp when plan was last updated

### User Entity Updates
- **New Field**: `plan` (Plan, OneToOne relationship)
  - Mapped by "user" in Plan entity
  - Cascade: ALL
  - Orphan removal: true
  - Fetch type: LAZY

## Backend Components

### Configuration
- **File**: `backend/src/main/resources/application.properties`
- **Properties**:
  - `gemini.api.key`: API key for Gemini API
  - `gemini.api.model`: Model identifier (gemini-2.5-flash)

### Model

#### Plan
- **File**: `backend/src/main/java/com/calorietracker/model/Plan.java`
- **Annotations**:
  - `@Entity`: JPA entity
  - `@Table(name = "plans")`: Database table mapping
  - `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`: Lombok annotations
- **Fields**:
  - `id`: Primary key with auto-generation strategy
  - `text`: Meal plan content (TEXT column)
  - `user`: OneToOne relationship with User (LAZY fetch, NOT NULL, UNIQUE constraint on user_id)
  - `createdAt`: Auto-populated on creation via @PrePersist
  - `updatedAt`: Auto-updated via @PreUpdate
- **Lifecycle Callbacks**:
  - `onCreate()`: Sets createdAt and updatedAt to current time
  - `onUpdate()`: Updates updatedAt to current time

### DTOs

#### PlanResponse
- **File**: `backend/src/main/java/com/calorietracker/dto/response/PlanResponse.java`
- **Fields**:
  - `text` (String): The generated meal plan text
  - `createdAt` (LocalDateTime): When the plan was created (used by frontend to check age)
- **Usage**: Returned by both GET and POST endpoints

### Repository

#### PlanRepository
- **File**: `backend/src/main/java/com/calorietracker/repository/PlanRepository.java`
- **Interface**: Extends JpaRepository<Plan, Long>
- **Custom Methods**:
  - `Optional<Plan> findByUserId(Long userId)`: Finds plan by user ID

### Service Layer

#### PlanService
- **File**: `backend/src/main/java/com/calorietracker/service/PlanService.java`
- **Dependencies**:
  - `WebClient`: For HTTP calls to Gemini API
  - `PlanRepository`: To save and retrieve plans
  - `MealEntryRepository`: To fetch user's meal history
  - `WeightEntryRepository`: To fetch user's weight history
- **Key Methods**:
  - `generatePlan(User user)`: Generates new meal plan and saves to database
  - `getPlan(User user)`: Retrieves existing plan for user
  - `buildPrompt(User user)`: Creates personalized prompt with user profile and history
  - `buildMealHistorySection(Long userId)`: Fetches and formats meal entries from last 7 days
  - `buildWeightHistorySection(Long userId)`: Fetches and formats weight entries from last month
  - `extractResponseText(Map<String, Object> response)`: Parses Gemini API response

**System Prompt Template**:
The system prompt includes:
- Role definition: Professional nutritionist and meal planner
- Task: Create personalized meal plan for the next week
- User profile data: Name, age, sex, height, weight, BMI, activity level, goal, pace, daily calorie allowance
- Meal history: Last 7 days of meals organized by date with calorie totals
- Weight history: Last month of weight entries with overall trend (gained/lost/maintained)
- Requirements:
  1. Stay within daily calorie allowance
  2. Include breakfast, lunch, and dinner
  3. Balance proteins, carbs, and healthy fats
  4. Consider user's goal (weight loss/gain/maintenance)
  5. Use practical, common ingredients
- Format instructions: Bulleted list, plain text (no markdown), trim superfluous text

**Date/Time Formatting**:
- Weight history dates: "MMM d" format with US locale (e.g., "Dec 24")
- Meal history dates: "EEE, MMM d" format with US locale (e.g., "Tue, Dec 24")
- Meal times: "hh:mm a" format with US locale (e.g., "08:30 AM")

**API Request Structure**:
```
POST https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key={apiKey}
Content-Type: application/json

{
  "contents": [
    {
      "role": "user",
      "parts": [{"text": "Full prompt with user profile, meal history, weight history, and instructions"}]
    }
  ]
}
```

**Plan Generation Logic**:
1. Build personalized prompt with user data
2. Send request to Gemini API
3. Extract response text from API response
4. Check if user already has a plan (findByUserId)
5. If plan exists: Update text field
6. If no plan: Create new Plan entity with user reference
7. Save plan to database
8. Return PlanResponse with text and createdAt

**Plan Retrieval Logic**:
1. Query database for plan by user ID
2. If found: Return PlanResponse with text and createdAt
3. If not found: Return PlanResponse with null values

### Controller

#### PlanController
- **File**: `backend/src/main/java/com/calorietracker/controller/PlanController.java`
- **Authentication**: Requires JWT Bearer token (Spring Security)
- **Endpoints**:
  - `GET /api/plan`: Get current meal plan for user
    - **Authentication**: Required (@AuthenticationPrincipal UserDetails)
    - **Response**: PlanResponse (text + createdAt)
    - **Behavior**: Returns existing plan or null if none exists
  - `POST /api/plan/generate`: Generate new meal plan
    - **Authentication**: Required (@AuthenticationPrincipal UserDetails)
    - **Response**: PlanResponse (text + createdAt)
    - **Behavior**: Generates new plan via Gemini API, saves to database, returns response
- **User Extraction**: Fetches authenticated user from UserRepository using email from UserDetails

### Repositories Used
- `PlanRepository.findByUserId(userId)`: Fetch plan by user ID
- `PlanRepository.save(plan)`: Save or update plan
- `MealEntryRepository.findByUserIdAndEntryDateBetween(userId, startDate, endDate)`: Fetch meals within date range
- `WeightEntryRepository.findByUserIdAndEntryDateBetweenOrderByEntryDateAsc(userId, startDate, endDate)`: Fetch weight entries within date range

## Frontend Components

### API Service
- **File**: `frontend/src/services/api.js`
- **Service**: `planService`
- **Methods**:
  - `getPlan()`: Sends GET request to `/api/plan`
  - `generatePlan()`: Sends POST request to `/api/plan/generate`
- **Authentication**: JWT token automatically included via axios interceptor

### Plan Component
- **File**: `frontend/src/components/plan/Plan.js`
- **State Management**:
  - `loading` (Boolean): Initial data fetch loading state
  - `generating` (Boolean): Plan generation loading state
  - `error` (String): Error message display
  - `planData` (Object): Contains `text` and `createdAt` fields
- **Key Functions**:
  - `isOlderThanOneWeek(dateString)`: Checks if plan is ≥7 days old
    - Returns true if dateString is null
    - Calculates difference between current date and createdAt
    - Returns true if difference ≥ 7 days
  - `fetchPlan()`: Fetches existing plan and triggers auto-generation if needed
    - Calls `planService.getPlan()`
    - If no plan exists (text is null) → calls `generateNewPlan()`
    - If plan is older than 7 days → calls `generateNewPlan()`
    - Otherwise → displays existing plan
  - `generateNewPlan()`: Generates new plan via API
    - Sets `generating` state to true
    - Calls `planService.generatePlan()`
    - Updates `planData` with response
    - Sets `generating` and `loading` to false
  - `formatDate(dateString)`: Formats createdAt for display
    - Format: "Weekday, Month Day, Year, HH:MM AM/PM"
    - Example: "Saturday, December 28, 2024, 05:15 PM"
- **UI Features**:
  - Header with title "Meal Plan" and subtitle
  - Generate New Plan button (currently commented out by user)
  - Loading spinner during fetch/generation with descriptive text
  - Error alert display
  - Plan creation date display
  - Plan content in Paper component with:
    - Gray background (grey.50)
    - Pre-wrap whitespace handling
    - Line height 1.8 for readability
    - RestaurantMenuIcon header
- **Auto-generation Logic**:
  - Runs on component mount (useEffect with fetchPlan)
  - Automatically generates if no plan exists
  - Automatically generates if plan is ≥7 days old
  - User cannot manually trigger generation (button commented out)

### Navigation Integration
- **File**: `frontend/src/components/layout/Layout.js`
- **Menu Item**: "Meal Plan" with RestaurantMenuIcon
- **Route**: `/plan`
- **Position**: Between "Advice" and "Profile" in sidebar
- **File**: `frontend/src/App.js`
- **Route Definition**: `<Route path="plan" element={<Plan />} />`
- **Import**: Plan component imported from `./components/plan/Plan`

## Data Flow

1. **User navigates to /plan**:
   - Plan component mounts
   - `fetchPlan()` called via useEffect

2. **Initial plan fetch**:
   - Frontend sends GET request to `/api/plan`
   - Backend queries database for user's plan
   - If plan exists: Returns PlanResponse with text and createdAt
   - If no plan: Returns PlanResponse with null values

3. **Auto-generation decision**:
   - Frontend checks if text is null OR createdAt is ≥7 days old
   - If true: Triggers `generateNewPlan()`
   - If false: Displays existing plan

4. **Plan generation process**:
   - Frontend sends POST request to `/api/plan/generate`
   - Backend PlanService builds prompt with:
     - User profile (from User entity)
     - Meal history (from MealEntryRepository, last 7 days)
     - Weight history (from WeightEntryRepository, last month)
   - Constructs Gemini API request
   - Gemini API generates 7-day meal plan
   - Backend saves/updates plan in database
   - Returns PlanResponse with text and createdAt

5. **Display plan**:
   - Frontend updates planData state
   - Displays plan text in formatted Paper component
   - Shows creation date

## Database Migrations
When deploying, ensure database schema includes:
- New `plans` table with columns: id, text, user_id (unique), created_at, updated_at
- Foreign key constraint from plans.user_id to users.id
- Unique constraint on plans.user_id (enforces 1-1 relationship)

## Error Handling

**Backend**:
- API call failures: Returns error message "I apologize, but I'm unable to generate your meal plan at the moment. Please try again later."
- Response parsing errors: Logs error, returns "Unable to parse AI response."
- User not found: Throws RuntimeException (caught by Spring Security)
- Database errors: Propagated as standard Spring exceptions

**Frontend**:
- Network errors: Displays error alert "Failed to load your meal plan. Please try again."
- Generation errors: Displays error alert "Failed to generate your meal plan. Please try again."
- Loading states: Shows spinner with descriptive text during operations
- Null plan handling: Gracefully handles null text and createdAt

## Key Differences from Advice Feature

1. **Conversation vs Single Generation**:
   - Advice: Maintains conversation history, supports back-and-forth chat
   - Plan: Single-shot generation, no conversation context

2. **Storage**:
   - Advice: No persistence, conversation exists only in frontend state
   - Plan: Persisted in database with timestamps

3. **Auto-generation**:
   - Advice: User-initiated only
   - Plan: Automatic on first visit and weekly refresh

4. **Prompt Structure**:
   - Advice: System prompt + conversation history + current message
   - Plan: Single comprehensive prompt with all context

5. **Output Format**:
   - Advice: Conversational responses, plain text
   - Plan: Structured 7-day meal plan, bulleted list format

6. **User Interaction**:
   - Advice: Chat interface with input field and message history
   - Plan: Read-only display with optional manual regeneration (currently disabled)

## Setup Instructions
1. Ensure Gemini API key is configured in `application.properties`
2. Run database migrations to create `plans` table
3. Verify JWT authentication is configured
4. Frontend automatically handles plan fetching and generation

## Testing Considerations
- Test auto-generation on first visit (no plan exists)
- Test auto-generation when plan is exactly 7 days old
- Test auto-generation when plan is older than 7 days
- Test display of existing plan less than 7 days old
- Mock Gemini API responses for unit tests
- Test with users having no meal/weight history
- Verify date formatting and age calculation
- Test database constraint enforcement (1-1 relationship)
- Test plan update vs create logic
- Verify createdAt is preserved when updating existing plan text

## Future Enhancements Considerations
- Manual regeneration button (currently commented out)
- Plan history/versioning
- Customization options (dietary restrictions, preferences)
- Export plan as PDF or email
- Shopping list generation from plan
- Integration with meal logging (quick-add from plan)
