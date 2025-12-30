package com.calorietracker.controller;

import com.calorietracker.dto.request.AdviceRequest;
import com.calorietracker.dto.response.AdviceResponse;
import com.calorietracker.model.User;
import com.calorietracker.repository.UserRepository;
import com.calorietracker.service.AdviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for AI-powered diet advice chatbot.
 * 
 * <p>Provides an interactive chat interface where users can ask nutrition
 * questions and receive personalized advice from Google Gemini AI.</p>
 * 
 * <h2>Endpoints:</h2>
 * <ul>
 *   <li><b>POST /api/advice/chat:</b> Send message and get AI response</li>
 * </ul>
 * 
 * @author Calorie Tracker Team
 * @version 1.0.0
 * @see AdviceService
 */
@RestController
@RequestMapping("/api/advice")
@RequiredArgsConstructor
public class AdviceController {

    private final AdviceService adviceService;
    private final UserRepository userRepository;

    /**
     * Chat with AI diet advisor
     */
    @PostMapping("/chat")
    public ResponseEntity<AdviceResponse> chat(
            @Valid @RequestBody AdviceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        AdviceResponse response = adviceService.chat(request, user);
        return ResponseEntity.ok(response);
    }
}
