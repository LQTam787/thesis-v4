package com.calorietracker.service;

import com.calorietracker.dto.response.AdviceChatResponse;
import com.calorietracker.model.User;
import com.calorietracker.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdviceService {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();
    
    private static final String OLLAMA_URL = "http://localhost:11434/api/chat";
    private static final String MODEL_NAME = "deepseek-r1:8b-llama-distill-q4_K_M";
    
    private static final String SYSTEM_PROMPT = """
        You are a friendly and knowledgeable diet advisor assistant. Your role is to:
        - Provide helpful nutrition and diet advice
        - Answer questions about healthy eating habits
        - Suggest meal ideas and food alternatives
        - Help users understand their calorie and nutrition needs
        - Offer encouragement and support for their health goals
        
        Keep your responses concise, practical, and encouraging. 
        If asked about medical conditions or specific health issues, recommend consulting a healthcare professional.
        Base your advice on the user's profile information when available.
        """;

    public AdviceChatResponse chat(String userMessage, Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            String contextualPrompt = buildContextualPrompt(user);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", MODEL_NAME);
            requestBody.put("stream", false);
            
            List<Map<String, String>> messages = new ArrayList<>();
            
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", SYSTEM_PROMPT + "\n\n" + contextualPrompt);
            messages.add(systemMessage);
            
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);
            
            requestBody.put("messages", messages);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                OLLAMA_URL,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String assistantResponse = jsonNode.path("message").path("content").asText();
                
                return AdviceChatResponse.builder()
                        .response(assistantResponse)
                        .success(true)
                        .build();
            } else {
                return AdviceChatResponse.builder()
                        .success(false)
                        .error("Failed to get response from AI service")
                        .build();
            }
            
        } catch (Exception e) {
            log.error("Error communicating with Ollama: ", e);
            return AdviceChatResponse.builder()
                    .success(false)
                    .error("AI service is currently unavailable.")
                    .build();
        }
    }
    
    private String buildContextualPrompt(User user) {
        if (user == null) {
            return "No user profile information available.";
        }
        
        StringBuilder context = new StringBuilder("User Profile Information:\n");
        context.append("- Name: ").append(user.getName()).append("\n");
        context.append("- Age: ").append(user.getAge()).append(" years old\n");
        context.append("- Sex: ").append(user.getSex()).append("\n");
        context.append("- Current Weight: ").append(user.getWeight()).append(" kg\n");
        context.append("- Height: ").append(user.getHeight()).append(" cm\n");
        context.append("- BMI: ").append(user.getBmi()).append("\n");
        context.append("- Activity Level: ").append(user.getActivityLevel()).append("\n");
        context.append("- Goal Type: ").append(user.getGoalType()).append("\n");
        if (user.getGoal() != null) {
            context.append("- Target Weight: ").append(user.getGoal()).append(" kg\n");
        }
        context.append("- Pace: ").append(user.getWeeklyGoal()).append(" kg/week\n");
        context.append("- Daily Calorie Allowance: ").append(user.getAllowedDailyIntake()).append(" calories\n");
        
        return context.toString();
    }
}
