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
