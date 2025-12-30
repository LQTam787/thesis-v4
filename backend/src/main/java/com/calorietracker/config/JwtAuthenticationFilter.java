package com.calorietracker.config;

import com.calorietracker.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter that intercepts every HTTP request to validate JWT tokens.
 * 
 * <p>This filter extends {@link OncePerRequestFilter} to ensure it executes exactly once
 * per request. It extracts the JWT token from the Authorization header, validates it,
 * and sets up the Spring Security context if valid.</p>
 * 
 * <h2>Authentication Flow:</h2>
 * <ol>
 *   <li>Extract Authorization header from request</li>
 *   <li>Check if header starts with "Bearer "</li>
 *   <li>Extract JWT token (substring after "Bearer ")</li>
 *   <li>Extract username (email) from token</li>
 *   <li>Load user details from database</li>
 *   <li>Validate token against user details</li>
 *   <li>Set authentication in SecurityContext if valid</li>
 * </ol>
 * 
 * <h2>Design Pattern:</h2>
 * <p>Implements the Intercepting Filter pattern, part of the security filter chain
 * that processes requests before they reach the controllers.</p>
 * 
 * @author Calorie Tracker Team
 * @version 1.0.0
 * @see com.calorietracker.service.JwtService
 * @see OncePerRequestFilter
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        userEmail = jwtService.extractUsername(jwt);

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
