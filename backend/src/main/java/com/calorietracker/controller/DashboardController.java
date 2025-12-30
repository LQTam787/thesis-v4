package com.calorietracker.controller;

import com.calorietracker.dto.response.DashboardResponse;
import com.calorietracker.model.User;
import com.calorietracker.repository.UserRepository;
import com.calorietracker.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST Controller for dashboard data retrieval.
 * 
 * <p>Provides aggregated daily summary data including calorie consumption,
 * meal entries grouped by type, and weight information for the dashboard view.</p>
 * 
 * <h2>Endpoints:</h2>
 * <ul>
 *   <li><b>GET /api/dashboard:</b> Get today's dashboard summary</li>
 *   <li><b>GET /api/dashboard/date/{date}:</b> Get dashboard for specific date</li>
 * </ul>
 * 
 * @author Calorie Tracker Team
 * @version 1.0.0
 * @see DashboardService
 * @see DashboardResponse
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<DashboardResponse> getTodayDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        DashboardResponse dashboard = dashboardService.getTodayDashboard(userId);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<DashboardResponse> getDashboardByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        DashboardResponse dashboard = dashboardService.getDashboardData(userId, date);
        return ResponseEntity.ok(dashboard);
    }

    private Long getUserIdFromUserDetails(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
