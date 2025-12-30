package com.calorietracker.controller;

import com.calorietracker.dto.request.WeightEntryRequest;
import com.calorietracker.dto.response.WeightEntryResponse;
import com.calorietracker.model.User;
import com.calorietracker.repository.UserRepository;
import com.calorietracker.service.WeightEntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for weight tracking operations.
 * 
 * <p>Manages weight entries for progress tracking. Implements upsert pattern
 * where one entry per day is allowed - posting to existing date updates the entry.</p>
 * 
 * <h2>Endpoints:</h2>
 * <ul>
 *   <li><b>POST /api/weight-entries:</b> Log or update weight for a date</li>
 *   <li><b>GET /api/weight-entries:</b> Get all weight entries (for charts)</li>
 *   <li><b>GET /api/weight-entries/latest:</b> Get most recent weight entry</li>
 *   <li><b>GET /api/weight-entries/date/{date}:</b> Get weight for specific date</li>
 *   <li><b>GET /api/weight-entries/range:</b> Get weights for date range</li>
 *   <li><b>GET /api/weight-entries/{id}:</b> Get single weight entry</li>
 *   <li><b>DELETE /api/weight-entries/{id}:</b> Delete weight entry</li>
 * </ul>
 * 
 * @author Calorie Tracker Team
 * @version 1.0.0
 * @see WeightEntryService
 */
@RestController
@RequestMapping("/api/weight-entries")
@RequiredArgsConstructor
public class WeightEntryController {

    private final WeightEntryService weightEntryService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<WeightEntryResponse> createOrUpdateWeightEntry(
            @Valid @RequestBody WeightEntryRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        WeightEntryResponse weightEntry = weightEntryService.createOrUpdateWeightEntry(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(weightEntry);
    }

    @GetMapping
    public ResponseEntity<List<WeightEntryResponse>> getAllWeightEntries(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        List<WeightEntryResponse> entries = weightEntryService.getAllWeightEntries(userId);
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/latest")
    public ResponseEntity<WeightEntryResponse> getLatestWeightEntry(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        WeightEntryResponse entry = weightEntryService.getLatestWeightEntry(userId);
        return ResponseEntity.ok(entry);
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<WeightEntryResponse> getWeightEntryByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        WeightEntryResponse entry = weightEntryService.getWeightEntryByDate(userId, date);
        return ResponseEntity.ok(entry);
    }

    @GetMapping("/range")
    public ResponseEntity<List<WeightEntryResponse>> getWeightEntriesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        List<WeightEntryResponse> entries = weightEntryService.getWeightEntriesByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WeightEntryResponse> getWeightEntryById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        WeightEntryResponse entry = weightEntryService.getWeightEntryById(id, userId);
        return ResponseEntity.ok(entry);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWeightEntry(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserIdFromUserDetails(userDetails);
        weightEntryService.deleteWeightEntry(id, userId);
        return ResponseEntity.noContent().build();
    }

    private Long getUserIdFromUserDetails(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
