package com.calorietracker.service;

import com.calorietracker.dto.response.PlanResponse;
import com.calorietracker.model.MealEntry;
import com.calorietracker.model.Plan;
import com.calorietracker.model.User;
import com.calorietracker.model.WeightEntry;
import com.calorietracker.repository.MealEntryRepository;
import com.calorietracker.repository.PlanRepository;
import com.calorietracker.repository.WeightEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for AI-powered meal plan generation using Google Gemini API.
 * 
 * <p>This service integrates with Google's Gemini AI to generate personalized
 * 7-day meal plans based on user profile, goals, and recent eating/weight history.</p>
 * 
 * <h2>AI Integration:</h2>
 * <ul>
 *   <li><b>API:</b> Google Gemini API (generativelanguage.googleapis.com)</li>
 *   <li><b>Model:</b> Configurable via gemini.api.model property</li>
 *   <li><b>Communication:</b> WebClient (non-blocking HTTP client)</li>
 * </ul>
 * 
 * <h2>Prompt Engineering:</h2>
 * <p>The service constructs detailed prompts including:</p>
 * <ul>
 *   <li>User profile (age, sex, height, weight, BMI, activity level)</li>
 *   <li>Goals (target weight, weekly pace, daily calorie allowance)</li>
 *   <li>Meal history (last 7 days with calories)</li>
 *   <li>Weight history (last month with trend analysis)</li>
 * </ul>
 * 
 * <h2>Design Pattern:</h2>
 * <p>Implements the Strategy pattern for AI communication, allowing easy
 * swapping of AI providers by changing the API endpoint and request format.</p>
 * 
 * @author Calorie Tracker Team
 * @version 1.0.0
 * @see Plan
 * @see com.calorietracker.controller.PlanController
 */
@Service
@Slf4j
public class PlanService {

    private final WebClient webClient;
    private final PlanRepository planRepository;
    private final MealEntryRepository mealEntryRepository;
    private final WeightEntryRepository weightEntryRepository;
    private final String apiKey;
    private final String model;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final String SYSTEM_PROMPT_TEMPLATE = """
            You are a professional nutritionist and meal planner. Your task is to create a personalized meal plan for the user for next week.
            
            User Profile:
            - Name: %s
            - Age: %d years old
            - Sex: %s
            - Height: %.1f cm
            - Weight: %.1f kg
            - BMI: %.1f
            - Activity Level: %s
            - Goal: %s
            - Pace: %.2f kg/week
            - Daily Calorie Allowance: %d cal
            
            %s
            %s
            
            Based on this information, create a detailed 7-day meal plan that:
            1. Stays within the daily calorie allowance
            2. Includes breakfast, lunch and dinner
            3. Is balanced with proteins, carbs, and healthy fats
            4. Considers the user's goal (weight loss/gain/maintenance)
            5. Is practical and uses common ingredients
            
            Format the plan clearly with each day and meal listed.
            Trim all superfluous text, reply should contain only a bulleted list. Do not use markdown, reply in plain text.
            """;

    public PlanService(
            WebClient.Builder webClientBuilder,
            PlanRepository planRepository,
            MealEntryRepository mealEntryRepository,
            WeightEntryRepository weightEntryRepository,
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.api.model}") String model) {
        this.webClient = webClientBuilder.baseUrl(GEMINI_API_URL).build();
        this.planRepository = planRepository;
        this.mealEntryRepository = mealEntryRepository;
        this.weightEntryRepository = weightEntryRepository;
        this.apiKey = apiKey;
        this.model = model;
    }

    public PlanResponse generatePlan(User user) {
        log.debug("Generating meal plan for user: {}", user.getId());

        String prompt = buildPrompt(user);
        
        List<Map<String, Object>> contents = List.of(
                Map.of("role", "user", "parts", List.of(Map.of("text", prompt)))
        );

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

            String planText = extractResponseText(response);

            Plan plan = planRepository.findByUserId(user.getId())
                    .orElse(Plan.builder().user(user).build());
            plan.setText(planText);
            Plan savedPlan = planRepository.save(plan);

            return PlanResponse.builder()
                    .text(planText)
                    .createdAt(savedPlan.getCreatedAt())
                    .build();

        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage(), e);
            return PlanResponse.builder()
                    .text("I apologize, but I'm unable to generate your meal plan at the moment. Please try again later.")
                    .build();
        }
    }

    public PlanResponse getPlan(User user) {
        return planRepository.findByUserId(user.getId())
                .map(plan -> PlanResponse.builder()
                        .text(plan.getText())
                        .createdAt(plan.getCreatedAt())
                        .build())
                .orElse(PlanResponse.builder().text(null).createdAt(null).build());
    }

    private String buildPrompt(User user) {
        String mealHistory = buildMealHistorySection(user.getId());
        String weightHistory = buildWeightHistorySection(user.getId());
        
        return String.format(SYSTEM_PROMPT_TEMPLATE,
                user.getName(),
                user.getAge(),
                user.getSex().name(),
                user.getHeight().doubleValue(),
                user.getWeight().doubleValue(),
                user.getBmi().doubleValue(),
                user.getActivityLevel().name().replace("_", " "),
                user.getGoalType().name().replace("_", " "),
                user.getWeeklyGoal().doubleValue(),
                user.getAllowedDailyIntake(),
                mealHistory,
                weightHistory
        );
    }

    private String buildWeightHistorySection(Long userId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(1);
        
        List<WeightEntry> entries = weightEntryRepository.findByUserIdAndEntryDateBetweenOrderByEntryDateAsc(
                userId, startDate, endDate);
        
        if (entries.isEmpty()) {
            return "Weight History (Last Month): No weight entries logged.";
        }
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d").withLocale(java.util.Locale.US);
        
        StringBuilder sb = new StringBuilder("Weight History (Last Month):\n");
        
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
            return "Meal History (Last 7 Days): No meals logged.";
        }
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d").withLocale(java.util.Locale.US);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a").withLocale(java.util.Locale.US);
        
        Map<LocalDate, List<MealEntry>> entriesByDate = entries.stream()
                .collect(Collectors.groupingBy(MealEntry::getEntryDate));
        
        StringBuilder sb = new StringBuilder("Meal History (Last 7 Days):\n");
        
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
