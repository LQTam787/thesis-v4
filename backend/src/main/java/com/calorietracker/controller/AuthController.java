package com.calorietracker.controller;

import com.calorietracker.dto.request.LoginRequest;
import com.calorietracker.dto.request.UserRegistrationRequest;
import com.calorietracker.dto.response.AuthResponse;
import com.calorietracker.model.User;
import com.calorietracker.service.JwtService;
import com.calorietracker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody UserRegistrationRequest request) {
        User user = userService.registerUser(request);

        // Generate JWT token for the newly registered user
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .bmi(user.getBmi())
                .allowedDailyIntake(user.getAllowedDailyIntake())
                .message("Registration successful")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder()
                            .message("Invalid email or password")
                            .build());
        }

        User user = userService.getUserByEmail(request.getEmail());
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtService.generateToken(userDetails);

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .bmi(user.getBmi())
                .allowedDailyIntake(user.getAllowedDailyIntake())
                .message("Login successful")
                .build();

        return ResponseEntity.ok(response);
    }
}
