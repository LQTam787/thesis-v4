package com.calorietracker.service;

import com.calorietracker.dto.response.ReviewResponse;
import com.calorietracker.model.MealEntry;
import com.calorietracker.model.Plan;
import com.calorietracker.model.Review;
import com.calorietracker.model.User;
import com.calorietracker.model.WeightEntry;
import com.calorietracker.repository.MealEntryRepository;
import com.calorietracker.repository.PlanRepository;
import com.calorietracker.repository.ReviewRepository;
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

@Service
@Slf4j
public class ReviewService {

    private final WebClient webClient;
    private final ReviewRepository reviewRepository;
    private final PlanRepository planRepository;
    private final MealEntryRepository mealEntryRepository;
    private final WeightEntryRepository weightEntryRepository;
    private final String apiKey;
    private final String model;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final String SYSTEM_PROMPT_TEMPLATE = """
            You are a professional nutritionist reviewing a user's progress. Your task is to provide a comprehensive review of their meal plan adherence and overall progress.
            
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
            %s
            
            Based on this information, provide a detailed review that:
            1. Evaluates how well the user followed their meal plan
            2. Analyzes their calorie intake patterns
            3. Reviews their weight progress toward their goal
            4. Identifies strengths and areas for improvement
            5. Provides specific, actionable recommendations
            
            Be encouraging but honest. Focus on progress and practical advice.
            Trim all superfluous text, reply with only a bulleted list. Do not use markdown, reply in plain text.
            """;

    public ReviewService(
            WebClient.Builder webClientBuilder,
            ReviewRepository reviewRepository,
            PlanRepository planRepository,
            MealEntryRepository mealEntryRepository,
            WeightEntryRepository weightEntryRepository,
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.api.model}") String model) {
        this.webClient = webClientBuilder.baseUrl(GEMINI_API_URL).build();
        this.reviewRepository = reviewRepository;
        this.planRepository = planRepository;
        this.mealEntryRepository = mealEntryRepository;
        this.weightEntryRepository = weightEntryRepository;
        this.apiKey = apiKey;
        this.model = model;
    }

    public ReviewResponse generateReview(User user) {
        log.debug("Generating review for user: {}", user.getId());

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

            String reviewText = extractResponseText(response);

            Review review = reviewRepository.findByUserId(user.getId())
                    .orElse(Review.builder().user(user).build());
            review.setText(reviewText);
            Review savedReview = reviewRepository.save(review);

            return ReviewResponse.builder()
                    .text(reviewText)
                    .createdAt(savedReview.getCreatedAt())
                    .build();

        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage(), e);
            return ReviewResponse.builder()
                    .text("I apologize, but I'm unable to generate your review at the moment. Please try again later.")
                    .build();
        }
    }

    public ReviewResponse getReview(User user) {
        return reviewRepository.findByUserId(user.getId())
                .map(review -> ReviewResponse.builder()
                        .text(review.getText())
                        .createdAt(review.getCreatedAt())
                        .build())
                .orElse(ReviewResponse.builder().text(null).createdAt(null).build());
    }

    public void deleteReview(Long userId) {
        reviewRepository.findByUserId(userId)
                .ifPresent(reviewRepository::delete);
    }

    private String buildPrompt(User user) {
        String planSection = buildPlanSection(user.getId());
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
                planSection,
                mealHistory,
                weightHistory
        );
    }

    private String buildPlanSection(Long userId) {
        return planRepository.findByUserId(userId)
                .map(plan -> "Current Meal Plan:\n" + plan.getText())
                .orElse("Current Meal Plan: No meal plan generated yet.");
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
