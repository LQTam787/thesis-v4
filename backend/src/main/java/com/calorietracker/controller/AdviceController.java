package com.calorietracker.controller;

import com.calorietracker.dto.request.AdviceChatRequest;
import com.calorietracker.dto.response.AdviceChatResponse;
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

    @PostMapping("/chat")
    public ResponseEntity<AdviceChatResponse> chat(
            @Valid @RequestBody AdviceChatRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        AdviceChatResponse response = adviceService.chat(request.getMessage(), userId);
        return ResponseEntity.ok(response);
    }

    private Long getUserIdFromUserDetails(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
