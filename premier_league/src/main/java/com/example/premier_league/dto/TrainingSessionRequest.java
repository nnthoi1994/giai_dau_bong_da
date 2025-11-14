package com.example.premier_league.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TrainingSessionRequest(
        @NotBlank String title,
        String description,
        @NotNull @FutureOrPresent LocalDateTime startAt,
        LocalDateTime endAt,
        String location
) {
}
