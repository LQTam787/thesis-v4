package com.calorietracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String type = "Bearer";
    private Long userId;
    private String name;
    private String email;
    private BigDecimal bmi;
    private Integer allowedDailyIntake;
    private String message;
}
