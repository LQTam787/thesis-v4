package com.calorietracker.controller;

import com.calorietracker.dto.response.UserProfileResponse;
import com.calorietracker.model.User;
import com.calorietracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserByEmail(userDetails.getUsername());

        UserProfileResponse response = UserProfileResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .dob(user.getDob())
                .sex(user.getSex())
                .height(user.getHeight())
                .activityLevel(user.getActivityLevel())
                .weight(user.getWeight())
                .goal(user.getGoal())
                .goalType(user.getGoalType())
                .weeklyGoal(user.getWeeklyGoal())
                .bmi(user.getBmi())
                .allowedDailyIntake(user.getAllowedDailyIntake())
                .build();

        return ResponseEntity.ok(response);
    }
}
