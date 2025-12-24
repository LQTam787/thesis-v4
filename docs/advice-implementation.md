# Advice Feature Implementation

## Overview
The Advice feature provides users with an AI-powered diet advisor chat interface. Users can ask questions about nutrition, diet tips, meal suggestions, and receive personalized advice based on their profile information.

## Technology Stack
- **AI Model**: DeepSeek-R1 via Ollama
- **Ollama URL**: `http://localhost:11434`
- **API Endpoint**: `/api/advice/chat`

## Prerequisites
1. **Ollama** must be installed and running locally on port 11434
2. **DeepSeek-R1** model must be pulled: `ollama pull deepseek-r1`

---

## Backend Implementation

### DTOs

#### AdviceChatRequest
**File**: `dto/request/AdviceChatRequest.java`
```java
String message    // @NotBlank
```

#### AdviceChatResponse
**File**: `dto/response/AdviceChatResponse.java`
```java
String response   // AI response text
boolean success   // Whether the request succeeded
String error      // Error message if failed
```

---

### Service

#### AdviceService
**File**: `service/AdviceService.java`

| Method | Description |
|--------|-------------|
| `chat(String message, Long userId)` | Sends user message to Ollama and returns AI response |
| `buildContextualPrompt(User user)` | Builds user profile context for personalized advice |

**Configuration**:
- `OLLAMA_URL`: `http://localhost:11434/api/chat`
- `MODEL_NAME`: `deepseek-r1`

**System Prompt**:
The AI is configured as a friendly diet advisor that:
- Provides nutrition and diet advice
- Suggests meal ideas and food alternatives
- Helps users understand calorie and nutrition needs
- Offers encouragement for health goals
- Recommends consulting healthcare professionals for medical issues

**User Context**:
The service automatically includes user profile information:
- Name, Age, Sex
- Current Weight, Height, BMI
- Activity Level
- Goal Type, Target Weight, Weekly Goal
- Daily Calorie Allowance

---

### Controller

#### AdviceController
**File**: `controller/AdviceController.java`

| Method | Endpoint | Request | Response |
|--------|----------|---------|----------|
| POST | `/api/advice/chat` | AdviceChatRequest | AdviceChatResponse |

---

## Frontend Implementation

### API Service
**File**: `services/api.js`

```javascript
export const adviceService = {
  chat: (message) => api.post('/advice/chat', { message }),
};
```

---

### Advice Component
**File**: `components/advice/Advice.js`

**State**:
```javascript
messages: Array<{ role: 'user' | 'assistant', content: string }>
input: string
loading: boolean
error: string
```

**Features**:
1. **Chat Interface**: Modern chat UI with message bubbles
2. **Auto-scroll**: Automatically scrolls to latest message
3. **Loading State**: Shows "Thinking..." indicator while waiting for AI
4. **Error Handling**: Displays error alerts for failed requests
5. **Enter to Send**: Supports Enter key to send messages
6. **Multiline Input**: Supports multiline message input

**UI Elements**:
- Message list with user/assistant avatars
- Text input field with send button
- Loading indicator during AI response
- Error alert banner

---

### Routing

**File**: `App.js`
```javascript
<Route path="advice" element={<Advice />} />
```

---

### Navigation

**File**: `components/layout/Layout.js`

Added to menuItems:
```javascript
{ text: 'Advice', icon: <TipsAndUpdatesIcon />, path: '/advice' }
```

---

## File Structure

### Backend
```
backend/src/main/java/com/calorietracker/
├── controller/
│   └── AdviceController.java
├── dto/
│   ├── request/
│   │   └── AdviceChatRequest.java
│   └── response/
│       └── AdviceChatResponse.java
└── service/
    └── AdviceService.java
```

### Frontend
```
frontend/src/
├── components/
│   └── advice/
│       └── Advice.js
├── services/
│   └── api.js (updated)
└── App.js (updated)
```

---

## Usage

1. Start Ollama with DeepSeek-R1 model:
   ```bash
   ollama serve
   ollama pull deepseek-r1
   ```

2. Start the backend server (port 8080)

3. Start the frontend server (port 3000)

4. Navigate to the "Advice" page from the sidebar

5. Type questions about diet, nutrition, or meal suggestions

---

## Error Handling

| Scenario | User Message |
|----------|--------------|
| Ollama not running | "AI service is currently unavailable. Please ensure Ollama is running on localhost:11434" |
| API request fails | "Failed to connect to AI advisor. Please try again." |
| Empty response | "Failed to get response from AI service" |

---

## Security
- Endpoint requires JWT authentication
- User context is fetched server-side using authenticated user ID
- No sensitive data is sent to external services (Ollama runs locally)
