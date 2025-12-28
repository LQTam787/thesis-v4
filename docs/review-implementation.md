# Review Feature Implementation

## Overview
The Review feature generates AI-powered reviews of user progress using Google's Gemini API. It evaluates the user's meal plan adherence, calorie intake patterns, weight progress, and provides actionable recommendations. Reviews are automatically generated when a week passes and a new plan is about to be generated. Reviews are stored in the database with a 1-1 relationship to User.

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

### Review Entity
- **Table**: `reviews`
- **Relationship**: One-to-One with User (bidirectional)
- **Fields**:
  - `id` (Long, Primary Key, Auto-generated)
  - `text` (String, TEXT column type): Stores the generated review text
  - `user_id` (Long, Foreign Key, NOT NULL, UNIQUE): References users table
  - `created_at` (LocalDateTime, NOT NULL): Timestamp when review was created
  - `updated_at` (LocalDateTime): Timestamp when review was last updated

### User Entity Updates
- **New Field**: `review` (Review, OneToOne relationship)
  - Mapped by "user" in Review entity
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

#### Review
- **File**: `backend/src/main/java/com/calorietracker/model/Review.java`
- **Annotations**:
  - `@Entity`: JPA entity
  - `@Table(name = "reviews")`: Database table mapping
  - `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`: Lombok annotations
- **Fields**:
  - `id`: Primary key with auto-generation strategy
  - `text`: Review content (TEXT column)
  - `user`: OneToOne relationship with User (LAZY fetch, NOT NULL, UNIQUE constraint on user_id)
  - `createdAt`: Auto-populated on creation via @PrePersist
  - `updatedAt`: Auto-updated via @PreUpdate
- **Lifecycle Callbacks**:
  - `onCreate()`: Sets createdAt and updatedAt to current time
  - `onUpdate()`: Updates updatedAt to current time

### DTOs

#### ReviewResponse
- **File**: `backend/src/main/java/com/calorietracker/dto/response/ReviewResponse.java`
- **Fields**:
  - `text` (String): The generated review text
  - `createdAt` (LocalDateTime): When the review was created
- **Usage**: Returned by both GET and POST endpoints

### Repository

#### ReviewRepository
- **File**: `backend/src/main/java/com/calorietracker/repository/ReviewRepository.java`
- **Interface**: Extends JpaRepository<Review, Long>
- **Custom Methods**:
  - `Optional<Review> findByUserId(Long userId)`: Finds review by user ID

### Service Layer

#### ReviewService
- **File**: `backend/src/main/java/com/calorietracker/service/ReviewService.java`
- **Dependencies**:
  - `WebClient`: For HTTP calls to Gemini API
  - `ReviewRepository`: To save and retrieve reviews
  - `PlanRepository`: To fetch user's current meal plan
  - `MealEntryRepository`: To fetch user's meal history
  - `WeightEntryRepository`: To fetch user's weight history
- **Key Methods**:
  - `generateReview(User user)`: Generates new review and saves to database
  - `getReview(User user)`: Retrieves existing review for user
  - `deleteReview(Long userId)`: Deletes review for user
  - `buildPrompt(User user)`: Creates personalized prompt with user profile, plan, and history
  - `buildPlanSection(Long userId)`: Fetches and formats current meal plan
  - `buildMealHistorySection(Long userId)`: Fetches and formats meal entries from last 7 days
  - `buildWeightHistorySection(Long userId)`: Fetches and formats weight entries from last month
  - `extractResponseText(Map<String, Object> response)`: Parses Gemini API response

**System Prompt Template**:
The system prompt includes:
- Role definition: Professional nutritionist reviewing user's progress
- Task: Provide comprehensive review of meal plan adherence and overall progress
- User profile data: Name, age, sex, height, weight, BMI, activity level, goal, pace, daily calorie allowance
- Current meal plan: The user's generated 7-day meal plan
- Meal history: Last 7 days of meals organized by date with calorie totals
- Weight history: Last month of weight entries with overall trend (gained/lost/maintained)
- Review requirements:
  1. Evaluate how well the user followed their meal plan
  2. Analyze their calorie intake patterns
  3. Review their weight progress toward their goal
  4. Identify strengths and areas for improvement
  5. Provide specific, actionable recommendations
- Tone: Encouraging but honest, focus on progress and practical advice
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
      "parts": [{"text": "Full prompt with user profile, meal plan, meal history, weight history, and instructions"}]
    }
  ]
}
```

**Review Generation Logic**:
1. Build personalized prompt with user data including current plan
2. Send request to Gemini API
3. Extract response text from API response
4. Check if user already has a review (findByUserId)
5. If review exists: Update text field
6. If no review: Create new Review entity with user reference
7. Save review to database
8. Return ReviewResponse with text and createdAt

**Review Retrieval Logic**:
1. Query database for review by user ID
2. If found: Return ReviewResponse with text and createdAt
3. If not found: Return ReviewResponse with null values

### Controller

#### ReviewController
- **File**: `backend/src/main/java/com/calorietracker/controller/ReviewController.java`
- **Authentication**: Requires JWT Bearer token (Spring Security)
- **Endpoints**:
  - `GET /api/review`: Get current review for user
    - **Authentication**: Required (@AuthenticationPrincipal UserDetails)
    - **Response**: ReviewResponse (text + createdAt)
    - **Behavior**: Returns existing review or null if none exists
  - `POST /api/review/generate`: Generate new review
    - **Authentication**: Required (@AuthenticationPrincipal UserDetails)
    - **Response**: ReviewResponse (text + createdAt)
    - **Behavior**: Generates new review via Gemini API, saves to database, returns response
  - `DELETE /api/review`: Delete review for user
    - **Authentication**: Required (@AuthenticationPrincipal UserDetails)
    - **Response**: JSON with message "Review deleted successfully"
    - **Behavior**: Deletes review from database if exists
- **User Extraction**: Fetches authenticated user from UserRepository using email from UserDetails

### Repositories Used
- `ReviewRepository.findByUserId(userId)`: Fetch review by user ID
- `ReviewRepository.save(review)`: Save or update review
- `ReviewRepository.delete(review)`: Delete review
- `PlanRepository.findByUserId(userId)`: Fetch user's current meal plan
- `MealEntryRepository.findByUserIdAndEntryDateBetween(userId, startDate, endDate)`: Fetch meals within date range
- `WeightEntryRepository.findByUserIdAndEntryDateBetweenOrderByEntryDateAsc(userId, startDate, endDate)`: Fetch weight entries within date range

## Frontend Components

### API Service
- **File**: `frontend/src/services/api.js`
- **Service**: `reviewService`
- **Methods**:
  - `getReview()`: Sends GET request to `/api/review`
  - `generateReview()`: Sends POST request to `/api/review/generate`
- **Authentication**: JWT token automatically included via axios interceptor

### Plan Component (Review Integration)
- **File**: `frontend/src/components/plan/Plan.js`
- **Additional State Management**:
  - `generatingReview` (Boolean): Review generation loading state
  - `reviewData` (Object): Contains `text` and `createdAt` fields
- **Key Functions**:
  - `fetchReview()`: Fetches existing review from API
    - Calls `reviewService.getReview()`
    - Updates `reviewData` state
    - Returns review data for use in fetchPlan logic
  - `generateNewReview()`: Generates new review via API
    - Sets `generatingReview` state to true
    - Calls `reviewService.generateReview()`
    - Updates `reviewData` with response
    - Sets `generatingReview` to false
    - Returns response data
- **Modified fetchPlan() Logic**:
  1. Fetch plan and review in parallel using Promise.all
  2. Check if plan needs regeneration (null text OR ≥7 days old)
  3. If plan needs regeneration AND existing plan exists:
     - First generate a review of the old plan
     - Then generate a new plan
  4. If plan is current: Display existing plan and review
- **UI Features**:
  - Review section displayed ABOVE plan section
  - Review section only visible when `reviewData.text` exists
  - Review card styling:
    - Secondary color theme (purple/secondary.50 background)
    - Left border accent (4px, secondary.main color)
    - RateReviewIcon header
    - Title: "Last Week's Review"
    - Creation date display
    - Pre-wrap whitespace handling
    - Line height 1.8 for readability
  - Loading spinner shows "Generating your weekly review..." during review generation

### Component Hierarchy
```
Plan Component
├── Loading State (shows spinner with context-aware message)
├── Error Alert (if error exists)
├── Plan Creation Date
├── Review Card (conditional: only if reviewData.text exists)
│   ├── Header with RateReviewIcon
│   ├── Review Creation Date
│   └── Review Text in Paper component
└── Plan Card
    ├── Header with RestaurantMenuIcon
    └── Plan Text in Paper component
```

## Data Flow

1. **User navigates to /plan**:
   - Plan component mounts
   - `fetchPlan()` called via useEffect

2. **Initial data fetch**:
   - Frontend sends parallel requests:
     - GET `/api/plan` for meal plan
     - GET `/api/review` for existing review
   - Backend queries database for both entities
   - Returns responses with text and createdAt (or null values)

3. **Auto-generation decision**:
   - Frontend checks if plan text is null OR createdAt is ≥7 days old
   - If plan needs regeneration:
     - If old plan exists (text is not null): Generate review first
     - Then generate new plan
   - If plan is current: Display existing plan and review

4. **Review generation process** (when triggered):
   - Frontend sends POST request to `/api/review/generate`
   - Backend ReviewService builds prompt with:
     - User profile (from User entity)
     - Current meal plan (from PlanRepository)
     - Meal history (from MealEntryRepository, last 7 days)
     - Weight history (from WeightEntryRepository, last month)
   - Constructs Gemini API request
   - Gemini API generates progress review
   - Backend saves/updates review in database
   - Returns ReviewResponse with text and createdAt

5. **Plan generation process** (after review if needed):
   - Frontend sends POST request to `/api/plan/generate`
   - Backend generates new 7-day meal plan
   - Saves to database, returns response

6. **Display**:
   - Frontend updates reviewData and planData states
   - If review exists: Displays review card above plan
   - Displays plan card with meal plan content

## Database Migrations
When deploying, ensure database schema includes:
- New `reviews` table with columns: id, text, user_id (unique), created_at, updated_at
- Foreign key constraint from reviews.user_id to users.id
- Unique constraint on reviews.user_id (enforces 1-1 relationship)

## Error Handling

**Backend**:
- API call failures: Returns error message "I apologize, but I'm unable to generate your review at the moment. Please try again later."
- Response parsing errors: Logs error, returns "Unable to parse AI response."
- User not found: Throws RuntimeException (caught by Spring Security)
- Database errors: Propagated as standard Spring exceptions
- No plan exists: Returns "Current Meal Plan: No meal plan generated yet." in prompt

**Frontend**:
- Network errors: Displays error alert "Failed to load your meal plan. Please try again."
- Review generation errors: Displays error alert "Failed to generate your review. Please try again."
- Loading states: Shows spinner with descriptive text during operations
- Null review handling: Review section simply not rendered (no error)

## Key Differences from Plan Feature

1. **Trigger Mechanism**:
   - Plan: Auto-generated on first visit or when ≥7 days old
   - Review: Auto-generated only when plan is being regenerated (and old plan exists)

2. **Display Behavior**:
   - Plan: Always displayed (or shows "no plan available" message)
   - Review: Only displayed when review text exists, otherwise hidden

3. **Prompt Content**:
   - Plan: User profile + meal history + weight history → generates meal plan
   - Review: User profile + current plan + meal history + weight history → generates progress review

4. **Purpose**:
   - Plan: Forward-looking, provides meal recommendations for next week
   - Review: Backward-looking, evaluates adherence and progress from past week

5. **UI Position**:
   - Plan: Main content area
   - Review: Displayed above plan when available

## Relationship with Plan Feature

The Review feature is tightly integrated with the Plan feature:

1. **Timing**: Review is generated immediately before a new plan when:
   - User visits /plan page
   - Existing plan is ≥7 days old
   - Existing plan text is not null (user has had a plan before)

2. **Data Dependency**: Review prompt includes the current plan text to evaluate adherence

3. **UI Integration**: Both displayed on same page (/plan), review above plan

4. **Workflow**:
   ```
   User visits /plan
   ├── Plan < 7 days old → Show existing plan + existing review (if any)
   └── Plan ≥ 7 days old (or null)
       ├── Old plan exists → Generate review of old plan
       └── Generate new plan
   ```

## File Structure

### Backend Files
```
backend/src/main/java/com/calorietracker/
├── model/
│   └── Review.java                    # Review entity
├── repository/
│   └── ReviewRepository.java          # Review data access
├── service/
│   └── ReviewService.java             # Review business logic + Gemini API
├── controller/
│   └── ReviewController.java          # REST endpoints
└── dto/response/
    └── ReviewResponse.java            # Response DTO
```

### Frontend Files
```
frontend/src/
├── services/
│   └── api.js                         # reviewService added
└── components/plan/
    └── Plan.js                        # Review integration
```

## API Endpoints Summary

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| GET | `/api/review` | Get user's review | None | ReviewResponse |
| POST | `/api/review/generate` | Generate new review | None | ReviewResponse |
| DELETE | `/api/review` | Delete user's review | None | { message: string } |

## Testing Considerations
- Test review generation when plan exists and is ≥7 days old
- Test no review generation when plan is null (first-time user)
- Test review display when review exists
- Test review hidden when no review exists
- Test review generation with no meal history
- Test review generation with no weight history
- Test review generation with no plan (edge case)
- Mock Gemini API responses for unit tests
- Verify date formatting and display
- Test database constraint enforcement (1-1 relationship)
- Test review update vs create logic
- Test parallel fetch of plan and review
- Test loading states for both review and plan generation

## Future Enhancements Considerations
- Manual review generation button
- Review history/versioning
- Comparison between reviews over time
- Export review as PDF
- Share review with nutritionist/trainer
- More detailed metrics in review (macro breakdown, consistency scores)
- Customizable review focus areas
