package com.calorietracker.controller;

import com.calorietracker.dto.request.UpdateProfileRequest;
import com.calorietracker.dto.response.UserProfileResponse;
import com.calorietracker.model.User;
import com.calorietracker.service.UserService;
import jakarta.validation.Valid;
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

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        User currentUser = userService.getUserByEmail(userDetails.getUsername());
        User updatedUser = userService.updateProfile(currentUser.getId(), request);

        UserProfileResponse response = UserProfileResponse.builder()
                .userId(updatedUser.getId())
                .name(updatedUser.getName())
                .email(updatedUser.getEmail())
                .dob(updatedUser.getDob())
                .sex(updatedUser.getSex())
                .height(updatedUser.getHeight())
                .activityLevel(updatedUser.getActivityLevel())
                .weight(updatedUser.getWeight())
                .goal(updatedUser.getGoal())
                .goalType(updatedUser.getGoalType())
                .weeklyGoal(updatedUser.getWeeklyGoal())
                .bmi(updatedUser.getBmi())
                .allowedDailyIntake(updatedUser.getAllowedDailyIntake())
                .build();

        return ResponseEntity.ok(response);
    }
}
