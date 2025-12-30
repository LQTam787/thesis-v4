package com.calorietracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Web MVC configuration for Cross-Origin Resource Sharing (CORS).
 * 
 * <p>This configuration enables the React frontend (running on localhost:3000)
 * to communicate with the Spring Boot backend (running on localhost:8080).
 * Without CORS configuration, browsers would block cross-origin requests
 * due to the Same-Origin Policy.</p>
 * 
 * <h2>CORS Settings:</h2>
 * <ul>
 *   <li><b>Allowed Origins:</b> http://localhost:3000 (React dev server)</li>
 *   <li><b>Allowed Methods:</b> GET, POST, PUT, DELETE, OPTIONS</li>
 *   <li><b>Allowed Headers:</b> All headers (*)</li>
 *   <li><b>Credentials:</b> Enabled (for cookies/auth headers)</li>
 *   <li><b>Max Age:</b> 3600 seconds (1 hour) for preflight cache</li>
 * </ul>
 * 
 * <h2>Note:</h2>
 * <p>For production deployment, the allowed origins should be updated to
 * include the actual production domain instead of localhost.</p>
 * 
 * @author Calorie Tracker Team
 * @version 1.0.0
 * @see WebMvcConfigurer
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
