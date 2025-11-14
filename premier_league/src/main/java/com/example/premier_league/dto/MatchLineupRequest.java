package com.example.premier_league.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MatchLineupRequest(
        @NotNull List<Long> startingPlayerIds,
        List<Long> substitutePlayerIds
) {
}
