package com.calorietracker.service;

import com.calorietracker.dto.request.AdviceRequest;
import com.calorietracker.dto.response.AdviceResponse;
import com.calorietracker.model.MealEntry;
import com.calorietracker.model.User;
import com.calorietracker.model.WeightEntry;
import com.calorietracker.repository.MealEntryRepository;
import com.calorietracker.repository.WeightEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for AI-powered diet advice chatbot using Google Gemini API.
 * 
 * <p>This service provides an interactive chat interface where users can ask
 * questions about nutrition, diet, and receive personalized advice based on
 * their profile and eating history.</p>
 * 
 * <h2>Features:</h2>
 * <ul>
 *   <li><b>Contextual Advice:</b> AI considers user's profile, goals, and history</li>
 *   <li><b>Conversation History:</b> Maintains chat context for follow-up questions</li>
 *   <li><b>Token Management:</b> Truncates history to stay within API limits</li>
 * </ul>
 * 
 * <h2>Prompt Context Includes:</h2>
 * <ul>
 *   <li>User profile (physical stats, goals, calorie allowance)</li>
 *   <li>Recent meal history (last 7 days)</li>
 *   <li>Weight progress (last month)</li>
 *   <li>Previous conversation messages</li>
 * </ul>
 * 
 * <h2>Token Optimization:</h2>
 * <p>The service implements a sliding window approach for conversation history,
 * keeping approximately 2000 tokens (~8000 characters) of recent messages
 * to balance context retention with API limits.</p>
 * 
 * @author Calorie Tracker Team
 * @version 1.0.0
 * @see com.calorietracker.controller.AdviceController
 */
@Service
@Slf4j
public class AdviceService {

    private final WebClient webClient;
    private final MealEntryRepository mealEntryRepository;
    private final WeightEntryRepository weightEntryRepository;
    private final String apiKey;
    private final String model;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final int MAX_HISTORY_TOKENS = 2000;
    private static final double AVG_CHARS_PER_TOKEN = 4.0;
    private static final String SYSTEM_PROMPT_TEMPLATE = """
            You are a professional diet advisor and nutritionist. Your role is to:
            - Provide helpful, accurate, and personalized diet advice
            - Answer questions about nutrition, calories, and healthy eating habits
            - Suggest meal plans and food alternatives when asked
            - Help users understand their dietary needs based on their goals
            - Keep responses concise but informative

            Use user's information when making recomendations.
            User Profile:
            - Name: %s
            - Age: %d years old
            - Sex: %s
            - Height: %.1f cm
            - Weight: %.1f kg
            - BMI: %.1f
            - Activity Level: %s
            - Target Weight: %s
            - Pace: %.2f kg/week
            - Daily Calorie Allowance: %d cal
            
            Do not use markdown. Reply in plain text.
            """;

    public AdviceService(
            WebClient.Builder webClientBuilder,
            MealEntryRepository mealEntryRepository,
            WeightEntryRepository weightEntryRepository,
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.api.model}") String model) {
        this.webClient = webClientBuilder.baseUrl(GEMINI_API_URL).build();
        this.mealEntryRepository = mealEntryRepository;
        this.weightEntryRepository = weightEntryRepository;
        this.apiKey = apiKey;
        this.model = model;
    }

    public AdviceResponse chat(AdviceRequest request, User user) {
        log.debug("Processing advice request: {}", request.getMessage());

        List<Map<String, Object>> contents = buildContentsWithHistory(request, user);

        Map<String, Object> requestBody = Map.of("contents", contents);

        try {
            Map<String, Object> response = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path(model + ":generateContent")
                            .queryParam("key", apiKey)
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            String aiResponse = extractResponseText(response);

            return AdviceResponse.builder()
                    .message(request.getMessage())
                    .response(aiResponse)
                    .build();

        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage(), e);
            return AdviceResponse.builder()
                    .message(request.getMessage())
                    .response("I apologize, but I'm unable to process your request at the moment. Please try again later.")
                    .build();
        }
    }

    private String buildSystemPrompt(User user) {
        String basePrompt = String.format(SYSTEM_PROMPT_TEMPLATE,
                user.getName(),
                user.getAge(),
                user.getSex().name(),
                user.getHeight().doubleValue(),
                user.getWeight().doubleValue(),
                user.getBmi().doubleValue(),
                user.getActivityLevel().name().replace("_", " "),
                user.getGoalType().name().replace("_", " "),
                user.getWeeklyGoal().doubleValue(),
                user.getAllowedDailyIntake()
        );
        
        String mealHistory = buildMealHistorySection(user.getId());
        String weightHistory = buildWeightHistorySection(user.getId());
        return basePrompt + mealHistory + weightHistory;
    }

    private String buildWeightHistorySection(Long userId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(1);
        
        List<WeightEntry> entries = weightEntryRepository.findByUserIdAndEntryDateBetweenOrderByEntryDateAsc(
                userId, startDate, endDate);
        
        if (entries.isEmpty()) {
            return "\nWeight History (Last Month): No weight entries logged.\n";
        }
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d").withLocale(java.util.Locale.US);
        
        StringBuilder sb = new StringBuilder("\nWeight History (Last Month):\n");
        
        for (WeightEntry entry : entries) {
            sb.append(String.format("  - %s: %.1f kg\n",
                    entry.getEntryDate().format(dateFormatter),
                    entry.getWeight().doubleValue()));
        }
        
        if (entries.size() >= 2) {
            double firstWeight = entries.get(0).getWeight().doubleValue();
            double lastWeight = entries.get(entries.size() - 1).getWeight().doubleValue();
            double change = lastWeight - firstWeight;
            String trend = change > 0 ? "gained" : (change < 0 ? "lost" : "maintained");
            sb.append(String.format("  Overall: %s %.1f kg\n", trend, Math.abs(change)));
        }
        
        return sb.toString();
    }

    private String buildMealHistorySection(Long userId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);
        
        List<MealEntry> entries = mealEntryRepository.findByUserIdAndEntryDateBetween(userId, startDate, endDate);
        
        if (entries.isEmpty()) {
            return "\nMeal History (Last 7 Days): No meals logged.\n";
        }
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d").withLocale(java.util.Locale.US);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a").withLocale(java.util.Locale.US);
        
        Map<LocalDate, List<MealEntry>> entriesByDate = entries.stream()
                .collect(Collectors.groupingBy(MealEntry::getEntryDate));
        
        StringBuilder sb = new StringBuilder("\nMeal History (Last 7 Days):\n");
        
        entriesByDate.entrySet().stream()
                .sorted((e1, e2) -> e2.getKey().compareTo(e1.getKey()))
                .forEach(entry -> {
                    LocalDate date = entry.getKey();
                    List<MealEntry> dayMeals = entry.getValue();
                    int dailyTotal = dayMeals.stream()
                            .mapToInt(m -> m.getFood().getCalories())
                            .sum();
                    
                    sb.append(String.format("\n%s (Total: %d cal):\n",
                            date.format(dateFormatter), dailyTotal));
                    
                    dayMeals.stream()
                            .sorted((m1, m2) -> m1.getEntryTime().compareTo(m2.getEntryTime()))
                            .forEach(meal -> {
                                sb.append(String.format("  - %s: %s (%d cal)\n",
                                        meal.getEntryTime().format(timeFormatter),
                                        meal.getFood().getName(),
                                        meal.getFood().getCalories()));
                            });
                });
        
        return sb.toString();
    }

    private List<Map<String, Object>> buildContentsWithHistory(AdviceRequest request, User user) {
        List<Map<String, Object>> contents = new ArrayList<>();
        
        String systemPrompt = buildSystemPrompt(user);
        
        contents.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", systemPrompt))
        ));
        contents.add(Map.of(
                "role", "model",
                "parts", List.of(Map.of("text", "I understand. I'm ready to help with diet and nutrition advice."))
        ));

        if (request.getHistory() != null && !request.getHistory().isEmpty()) {
            List<AdviceRequest.ChatMessage> truncatedHistory = truncateHistoryToTokenLimit(request.getHistory());
            
            for (AdviceRequest.ChatMessage msg : truncatedHistory) {
                String role = "user".equals(msg.getRole()) ? "user" : "model";
                contents.add(Map.of(
                        "role", role,
                        "parts", List.of(Map.of("text", msg.getContent()))
                ));
            }
        }

        contents.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", request.getMessage()))
        ));

        return contents;
    }

    private List<AdviceRequest.ChatMessage> truncateHistoryToTokenLimit(List<AdviceRequest.ChatMessage> history) {
        int maxChars = (int) (MAX_HISTORY_TOKENS * AVG_CHARS_PER_TOKEN);
        int totalChars = 0;
        
        List<AdviceRequest.ChatMessage> result = new ArrayList<>();
        
        for (int i = history.size() - 1; i >= 0; i--) {
            AdviceRequest.ChatMessage msg = history.get(i);
            int msgChars = msg.getContent() != null ? msg.getContent().length() : 0;
            
            if (totalChars + msgChars > maxChars) {
                break;
            }
            
            totalChars += msgChars;
            result.add(0, msg);
        }
        
        return result;
    }

    @SuppressWarnings("unchecked")
    private String extractResponseText(Map<String, Object> response) {
        if (response == null) {
            return "No response received from AI service.";
        }

        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                if (content != null) {
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        return (String) parts.get(0).get("text");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error parsing Gemini response: {}", e.getMessage(), e);
        }

        return "Unable to parse AI response.";
    }
}
