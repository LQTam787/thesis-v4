package com.calorietracker.service;

import com.calorietracker.dto.request.UserRegistrationRequest;
import com.calorietracker.exception.BadRequestException;
import com.calorietracker.exception.ResourceNotFoundException;
import com.calorietracker.model.User;
import com.calorietracker.repository.UserRepository;
import com.calorietracker.util.CalorieCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(UserRegistrationRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        // Calculate age from DOB
        int age = LocalDate.now().getYear() - request.getDob().getYear();

        // Calculate BMI
        BigDecimal bmi = CalorieCalculator.calculateBMI(
                request.getWeight(),
                request.getHeight()
        );

        // Calculate allowed daily intake
        int allowedDailyIntake = CalorieCalculator.calculateAllowedDailyIntake(
                request.getWeight(),
                request.getHeight(),
                age,
                request.getActivityLevel(),
                request.getWeeklyGoal(),
                request.getGoalType()
        );

        // Build and save user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .dob(request.getDob())
                .weight(request.getWeight())
                .height(request.getHeight())
                .activityLevel(request.getActivityLevel())
                .goal(request.getGoal())
                .goalType(request.getGoalType())
                .weeklyGoal(request.getWeeklyGoal())
                .bmi(bmi)
                .allowedDailyIntake(allowedDailyIntake)
                .build();

        return userRepository.save(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    @Transactional
    public User updateUserWeight(Long userId, BigDecimal newWeight) {
        User user = getUserById(userId);

        // Update weight
        user.setWeight(newWeight);

        // Recalculate BMI
        BigDecimal bmi = CalorieCalculator.calculateBMI(newWeight, user.getHeight());
        user.setBmi(bmi);

        // Recalculate allowed daily intake
        int age = user.getAge();
        int allowedDailyIntake = CalorieCalculator.calculateAllowedDailyIntake(
                newWeight,
                user.getHeight(),
                age,
                user.getActivityLevel(),
                user.getWeeklyGoal(),
                user.getGoalType()
        );
        user.setAllowedDailyIntake(allowedDailyIntake);

        return userRepository.save(user);
    }
}
