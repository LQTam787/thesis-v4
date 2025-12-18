package com.calorietracker.dto.response;

import com.calorietracker.model.WeightEntry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeightEntryResponse {

    private Long id;
    private LocalDate entryDate;
    private BigDecimal weight;
    private LocalDateTime createdAt;

    public static WeightEntryResponse fromEntity(WeightEntry weightEntry) {
        return WeightEntryResponse.builder()
                .id(weightEntry.getId())
                .entryDate(weightEntry.getEntryDate())
                .weight(weightEntry.getWeight())
                .createdAt(weightEntry.getCreatedAt())
                .build();
    }
}
