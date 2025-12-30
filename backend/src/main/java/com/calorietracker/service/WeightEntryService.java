package com.calorietracker.service;

import com.calorietracker.dto.request.WeightEntryRequest;
import com.calorietracker.dto.response.WeightEntryResponse;
import com.calorietracker.exception.BadRequestException;
import com.calorietracker.exception.ResourceNotFoundException;
import com.calorietracker.model.User;
import com.calorietracker.model.WeightEntry;
import com.calorietracker.repository.UserRepository;
import com.calorietracker.repository.WeightEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer for weight tracking operations.
 * 
 * <p>This service manages weight entries for users, enabling progress tracking
 * toward their weight goals. It implements an upsert pattern where logging weight
 * for an existing date updates the entry rather than creating a duplicate.</p>
 * 
 * <h2>Key Features:</h2>
 * <ul>
 *   <li><b>Upsert Pattern:</b> One entry per user per day, updates replace existing</li>
 *   <li><b>Auto-sync:</b> Latest weight entry automatically updates user's current weight</li>
 *   <li><b>Cascade Updates:</b> Weight changes trigger BMI and calorie recalculation</li>
 *   <li><b>Historical Data:</b> Full history for progress charts and AI analysis</li>
 * </ul>
 * 
 * <h2>Business Logic:</h2>
 * <p>When a weight entry is created/updated and it's the chronologically latest entry,
 * the user's profile is automatically updated with the new weight, triggering
 * recalculation of BMI and daily calorie allowance.</p>
 * 
 * @author Calorie Tracker Team
 * @version 1.0.0
 * @see WeightEntry
 * @see UserService#updateUserWeight
 */
@Service
@RequiredArgsConstructor
public class WeightEntryService {

    private final WeightEntryRepository weightEntryRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Transactional
    public WeightEntryResponse createOrUpdateWeightEntry(WeightEntryRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check if entry already exists for this date
        Optional<WeightEntry> existingEntry = weightEntryRepository
                .findByUserIdAndEntryDate(userId, request.getEntryDate());

        WeightEntry weightEntry;
        if (existingEntry.isPresent()) {
            // Update existing entry
            weightEntry = existingEntry.get();
            weightEntry.setWeight(request.getWeight());
        } else {
            // Create new entry
            weightEntry = WeightEntry.builder()
                    .user(user)
                    .entryDate(request.getEntryDate())
                    .weight(request.getWeight())
                    .build();
        }

        WeightEntry savedEntry = weightEntryRepository.save(weightEntry);

        // If this is the latest weight entry, update user's current weight
        Optional<WeightEntry> latestEntry = weightEntryRepository.findFirstByUserIdOrderByEntryDateDesc(userId);
        if (latestEntry.isPresent() && latestEntry.get().getId().equals(savedEntry.getId())) {
            userService.updateUserWeight(userId, request.getWeight());
        }

        return WeightEntryResponse.fromEntity(savedEntry);
    }

    public List<WeightEntryResponse> getAllWeightEntries(Long userId) {
        return weightEntryRepository.findByUserIdOrderByEntryDateAsc(userId)
                .stream()
                .map(WeightEntryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public WeightEntryResponse getWeightEntryByDate(Long userId, LocalDate date) {
        WeightEntry weightEntry = weightEntryRepository.findByUserIdAndEntryDate(userId, date)
                .orElseThrow(() -> new ResourceNotFoundException("Weight entry not found for date: " + date));

        return WeightEntryResponse.fromEntity(weightEntry);
    }

    public WeightEntryResponse getLatestWeightEntry(Long userId) {
        WeightEntry weightEntry = weightEntryRepository.findFirstByUserIdOrderByEntryDateDesc(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No weight entries found"));

        return WeightEntryResponse.fromEntity(weightEntry);
    }

    public List<WeightEntryResponse> getWeightEntriesByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return weightEntryRepository.findByUserIdAndEntryDateBetweenOrderByEntryDateAsc(userId, startDate, endDate)
                .stream()
                .map(WeightEntryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public WeightEntryResponse getWeightEntryById(Long entryId, Long userId) {
        WeightEntry weightEntry = weightEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Weight entry not found with id: " + entryId));

        if (!weightEntry.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Weight entry not found with id: " + entryId);
        }

        return WeightEntryResponse.fromEntity(weightEntry);
    }

    @Transactional
    public void deleteWeightEntry(Long entryId, Long userId) {
        WeightEntry weightEntry = weightEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Weight entry not found with id: " + entryId));

        if (!weightEntry.getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only delete your own weight entries");
        }

        weightEntryRepository.delete(weightEntry);

        // If we deleted the latest entry, update user weight to the new latest
        Optional<WeightEntry> newLatest = weightEntryRepository.findFirstByUserIdOrderByEntryDateDesc(userId);
        if (newLatest.isPresent()) {
            userService.updateUserWeight(userId, newLatest.get().getWeight());
        }
    }
}
