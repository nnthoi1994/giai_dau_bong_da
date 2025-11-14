package com.example.premier_league.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record MatchScheduleResponse(
        Long id,
        String name,
        LocalDate matchDate,
        LocalTime matchTime,
        Integer round,
        String note,
        Integer homeTeamId,
        String homeTeamName,
        Integer awayTeamId,
        String awayTeamName
) {
}
