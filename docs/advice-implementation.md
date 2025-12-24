# Advice Feature Implementation

## Overview
The Advice feature allows users to chat with an AI diet advisor powered by Google's Gemini API (gemini-2.5-flash model). The system provides personalized nutrition advice using the user's profile data, meal history (last 7 days), and weight history (last month).

## Architecture

### Backend Stack
- **Framework**: Spring Boot 3.2.1
- **HTTP Client**: WebClient (Spring WebFlux)
- **External API**: Google Gemini API (gemini-2.5-flash)
- **Authentication**: JWT Bearer tokens via Spring Security

### Frontend Stack
- **Framework**: React 18.2.0
- **UI Library**: Material-UI 5.15.4
- **HTTP Client**: Axios with interceptors
- **Routing**: React Router v6

## Backend Components

### Configuration
- **File**: `backend/src/main/resources/application.properties`
- **Properties**:
  - `gemini.api.key`: API key for Gemini API (set to actual key, default was 'placeholder')
  - `gemini.api.model`: Model identifier (gemini-2.5-flash)

### DTOs

#### AdviceRequest
- **File**: `backend/src/main/java/com/calorietracker/dto/request/AdviceRequest.java`
- **Fields**:
  - `message` (String, required): User's question or message
  - `history` (List<ChatMessage>, optional): Conversation history for context
    - Each ChatMessage contains:
      - `role` (String): "user" or "model"
      - `content` (String): Message text
- **Validation**: Message is required and must not be blank

#### AdviceResponse
- **File**: `backend/src/main/java/com/calorietracker/dto/response/AdviceResponse.java`
- **Fields**:
  - `message` (String): Original user message
  - `response` (String): AI advisor's response

### Service Layer

#### AdviceService
- **File**: `backend/src/main/java/com/calorietracker/service/AdviceService.java`
- **Dependencies**:
  - `WebClient`: For HTTP calls to Gemini API
  - `MealEntryRepository`: To fetch user's meal history
  - `WeightEntryRepository`: To fetch user's weight history
- **Key Methods**:
  - `chat(AdviceRequest request, User user)`: Main entry point for chat requests
  - `buildSystemPrompt(User user)`: Creates personalized system prompt with user profile
  - `buildMealHistorySection(Long userId)`: Fetches and formats meal entries from last 7 days
  - `buildWeightHistorySection(Long userId)`: Fetches and formats weight entries from last month
  - `buildContentsWithHistory(AdviceRequest request, User user)`: Constructs full Gemini API request with conversation context
  - `truncateHistoryToTokenLimit(List<ChatMessage> history)`: Limits conversation history to ~2000 tokens
  - `extractResponseText(Map<String, Object> response)`: Parses Gemini API response

**System Prompt Template**:
The system prompt includes:
- Role definition: Professional diet advisor and nutritionist
- User profile data: Name, age, sex, height, weight, BMI, activity level, target weight, pace, daily calorie allowance
- Meal history: Last 7 days of meals organized by date with calorie totals
- Weight history: Last month of weight entries with overall trend (gained/lost/maintained)
- Instructions: Use plain text, no markdown, use user's information when making recommendations

**Token Management**:
- Max history tokens: 2000 (approximately 8000 characters)
- Estimation: 4 characters per token
- History truncation: Keeps most recent messages within token limit, removes oldest messages first

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
      "parts": [{"text": "System prompt with user profile and history"}]
    },
    {
      "role": "model",
      "parts": [{"text": "I understand. I'm ready to help with diet and nutrition advice."}]
    },
    // ... previous conversation messages ...
    {
      "role": "user",
      "parts": [{"text": "Current user message"}]
    }
  ]
}
```

### Controller

#### AdviceController
- **File**: `backend/src/main/java/com/calorietracker/controller/AdviceController.java`
- **Authentication**: Requires JWT Bearer token (Spring Security)
- **Endpoints**:
  - `POST /api/advice/chat`: Chat with AI diet advisor
    - **Authentication**: Required (@AuthenticationPrincipal UserDetails)
    - **Request Body**: AdviceRequest (message + optional history)
    - **Response**: AdviceResponse (message + response)
    - **Error Handling**: Returns 401 if user not authenticated, 400 if validation fails
- **User Extraction**: Fetches authenticated user from UserRepository using email from UserDetails

### Repositories
- `MealEntryRepository.findByUserIdAndEntryDateBetween(userId, startDate, endDate)`: Fetch meals within date range
- `WeightEntryRepository.findByUserIdAndEntryDateBetweenOrderByEntryDateAsc(userId, startDate, endDate)`: Fetch weight entries within date range

## Frontend Components

### API Service
- **File**: `frontend/src/services/api.js`
- **Method**: `adviceService.chat(message, history)`
  - Sends POST request to `/api/advice/chat`
  - Includes JWT token in Authorization header
  - Parameters:
    - `message` (String): User's message
    - `history` (Array): Conversation history with role and content

### Advice Component
- **File**: `frontend/src/components/advice/Advice.js`
- **Features**:
  - Chat interface with message bubbles
  - User messages: Right-aligned, blue background, PersonIcon avatar
  - AI messages: Left-aligned, gray background, SmartToyIcon avatar
  - Loading state: Shows spinner while waiting for response
  - Auto-scroll: Scrolls to latest message
  - Message history: Maintains all messages in state
- **Key Functions**:
  - `buildHistory(currentMessages)`: Converts frontend messages to API format (role + content)
  - `handleSend()`: Sends message with history to backend
  - `handleKeyPress()`: Allows Enter key to send (Shift+Enter for new line)
- **Initial Message**: Greeting message with ID 1 (filtered out when building history)
- **Disclaimer**: Reminds users to consult healthcare professionals

### Navigation Integration
- **File**: `frontend/src/components/layout/Layout.js`
- Menu item: "Advice" with SmartToyIcon
- Route: `/advice`
- **File**: `frontend/src/App.js`
- Route definition: `<Route path="advice" element={<Advice />} />`

## Data Flow

1. **User sends message**:
   - Frontend builds history from previous messages (excluding initial greeting)
   - Sends POST request with message and history

2. **Backend processes**:
   - AdviceController extracts authenticated user
   - AdviceService builds system prompt with:
     - User profile (from User entity)
     - Meal history (from MealEntryRepository, last 7 days)
     - Weight history (from WeightEntryRepository, last month)
   - Truncates conversation history to 2000 tokens
   - Constructs Gemini API request with full context

3. **Gemini API**:
   - Receives request with system prompt and conversation
   - Generates response based on user's profile and history
   - Returns response text

4. **Response to frontend**:
   - AdviceService extracts text from Gemini response
   - Returns AdviceResponse with original message and AI response
   - Frontend displays AI message in chat

## Dependencies Added
- `spring-boot-starter-webflux`: WebClient for HTTP calls to Gemini API

## Error Handling

**Backend**:
- API call failures: Returns error message "I apologize, but I'm unable to process your request at the moment. Please try again later."
- Response parsing errors: Logs error, returns "Unable to parse AI response."
- User not found: Throws RuntimeException (caught by Spring Security)

**Frontend**:
- Network errors: Displays error message in chat
- Validation errors: Message field is required, send button disabled if empty

## Setup Instructions
1. Obtain Gemini API key from Google AI Studio (https://makersuite.google.com/app/apikey)
2. Update `backend/src/main/resources/application.properties`:
   - Set `gemini.api.key` to your actual API key
   - Verify `gemini.api.model=gemini-2.5-flash`
3. Ensure JWT authentication is configured in Spring Security
4. Frontend automatically includes JWT token in requests via axios interceptor

## Testing Considerations
- Mock Gemini API responses for unit tests
- Test token truncation with various conversation lengths
- Verify date formatting with different locales
- Test with users having no meal/weight history
- Verify conversation context is maintained across multiple messages
