package com.calorietracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdviceChatRequest {
    
    @NotBlank(message = "Message cannot be empty")
    private String message;
}
