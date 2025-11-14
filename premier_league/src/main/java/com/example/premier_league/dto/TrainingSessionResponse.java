package com.example.premier_league.dto;

import java.time.LocalDateTime;

public record TrainingSessionResponse(
        Long id,
        String title,
        String description,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String location
) {
}
