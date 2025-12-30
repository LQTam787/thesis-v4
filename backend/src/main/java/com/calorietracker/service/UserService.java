package com.calorietracker.service;

import com.calorietracker.dto.request.UpdateProfileRequest;
import com.calorietracker.dto.request.UserRegistrationRequest;
import com.calorietracker.model.GoalType;
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

/**
 * Service layer for user management operations.
 * 
 * <p>This service handles user registration, profile management, and weight updates.
 * It automatically calculates derived values like BMI and daily calorie allowance
 * whenever relevant user data changes.</p>
 * 
 * <h2>Key Operations:</h2>
 * <ul>
 *   <li><b>Registration:</b> Creates new user with encrypted password and calculated metrics</li>
 *   <li><b>Profile Update:</b> Updates user info and recalculates BMI/calories</li>
 *   <li><b>Weight Update:</b> Updates weight and recalculates dependent values</li>
 * </ul>
 * 
 * <h2>Automatic Calculations:</h2>
 * <p>When weight, height, or goals change, the service automatically recalculates:</p>
 * <ul>
 *   <li>BMI using standard formula</li>
 *   <li>Daily calorie allowance using Mifflin-St Jeor equation</li>
 *   <li>Goal type derived from current vs target weight comparison</li>
 * </ul>
 * 
 * @author Calorie Tracker Team
 * @version 1.0.0
 * @see com.calorietracker.util.CalorieCalculator
 * @see User
 */
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
                request.getSex(),
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
                .sex(request.getSex())
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
                user.getSex(),
                user.getActivityLevel(),
                user.getWeeklyGoal(),
                user.getGoalType()
        );
        user.setAllowedDailyIntake(allowedDailyIntake);

        return userRepository.save(user);
    }

    @Transactional
    public User updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getUserById(userId);

        // Update basic fields
        user.setName(request.getName());
        user.setSex(request.getSex());
        user.setDob(request.getDob());
        user.setHeight(request.getHeight());
        user.setWeight(request.getWeight());
        user.setActivityLevel(request.getActivityLevel());
        user.setGoal(request.getGoal());
        user.setWeeklyGoal(request.getWeeklyGoal());

        // Derive goalType from comparing goal with new weight
        GoalType goalType = deriveGoalType(request.getWeight(), request.getGoal());
        user.setGoalType(goalType);

        // Recalculate BMI
        BigDecimal bmi = CalorieCalculator.calculateBMI(request.getWeight(), request.getHeight());
        user.setBmi(bmi);

        // Recalculate allowed daily intake
        int age = user.getAge();
        int allowedDailyIntake = CalorieCalculator.calculateAllowedDailyIntake(
                request.getWeight(),
                request.getHeight(),
                age,
                request.getSex(),
                request.getActivityLevel(),
                request.getWeeklyGoal(),
                goalType
        );
        user.setAllowedDailyIntake(allowedDailyIntake);

        return userRepository.save(user);
    }

    private GoalType deriveGoalType(BigDecimal currentWeight, BigDecimal goalWeight) {
        int comparison = goalWeight.compareTo(currentWeight);
        if (comparison < 0) {
            return GoalType.LOSE;
        } else if (comparison > 0) {
            return GoalType.GAIN;
        } else {
            return GoalType.MAINTAIN;
        }
    }
}
