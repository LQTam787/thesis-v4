package com.calorietracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Calorie Tracker Application.
 * 
 * <p>This is a full-stack web application built with Spring Boot that helps users
 * track their daily calorie intake, manage meals, monitor weight progress, and
 * receive AI-powered nutritional advice using Google's Gemini API.</p>
 * 
 * <h2>Key Features:</h2>
 * <ul>
 *   <li>User authentication with JWT tokens</li>
 *   <li>Daily calorie tracking and meal logging</li>
 *   <li>Weight progress monitoring with visualization</li>
 *   <li>AI-powered meal planning and dietary advice</li>
 *   <li>BMI and TDEE calculations using Mifflin-St Jeor equation</li>
 * </ul>
 * 
 * <h2>Technology Stack:</h2>
 * <ul>
 *   <li>Backend: Spring Boot 3.2, Spring Security, Spring Data JPA</li>
 *   <li>Database: MySQL 8.0</li>
 *   <li>Authentication: JWT (JSON Web Tokens)</li>
 *   <li>AI Integration: Google Gemini API via WebFlux</li>
 * </ul>
 * 
 * @author Calorie Tracker Team
 * @version 1.0.0
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 */
@SpringBootApplication
public class CalorieTrackerApplication {

    /**
     * Application entry point.
     * 
     * <p>Bootstraps the Spring Boot application, initializing all auto-configured
     * beans, component scanning, and embedded Tomcat server on port 8080.</p>
     * 
     * @param args command line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(CalorieTrackerApplication.class, args);
    }
}
