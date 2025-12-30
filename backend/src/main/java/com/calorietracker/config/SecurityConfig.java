package com.calorietracker.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security configuration for the Calorie Tracker application.
 * 
 * <p>This configuration implements a stateless JWT-based authentication system,
 * which is ideal for REST APIs consumed by single-page applications (SPAs).</p>
 * 
 * <h2>Security Features:</h2>
 * <ul>
 *   <li><b>JWT Authentication:</b> Stateless token-based auth via Bearer tokens</li>
 *   <li><b>BCrypt Password Encoding:</b> Industry-standard password hashing</li>
 *   <li><b>CORS Configuration:</b> Allows requests from React frontend (localhost:3000)</li>
 *   <li><b>CSRF Disabled:</b> Not needed for stateless JWT authentication</li>
 *   <li><b>Stateless Sessions:</b> No server-side session storage</li>
 * </ul>
 * 
 * <h2>Public Endpoints:</h2>
 * <ul>
 *   <li>/api/auth/** - Registration and login endpoints</li>
 *   <li>/api/test/** - Testing endpoints (development only)</li>
 * </ul>
 * 
 * <h2>Design Pattern:</h2>
 * <p>Implements the Filter Chain pattern where the JwtAuthenticationFilter
 * intercepts requests before the standard UsernamePasswordAuthenticationFilter.</p>
 * 
 * @author Calorie Tracker Team
 * @version 1.0.0
 * @see JwtAuthenticationFilter
 * @see com.calorietracker.service.JwtService
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(request -> {
                var corsConfig = new org.springframework.web.cors.CorsConfiguration();
                corsConfig.setAllowedOrigins(java.util.List.of("http://localhost:3000"));
                corsConfig.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                corsConfig.setAllowedHeaders(java.util.List.of("*"));
                corsConfig.setAllowCredentials(true);
                return corsConfig;
            }))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/test/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
