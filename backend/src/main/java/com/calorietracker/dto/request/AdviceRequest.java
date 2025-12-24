package com.calorietracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdviceRequest {
    
    @NotBlank(message = "Message is required")
    private String message;
    
    private List<ChatMessage> history;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage {
        private String role;
        private String content;
    }
}
