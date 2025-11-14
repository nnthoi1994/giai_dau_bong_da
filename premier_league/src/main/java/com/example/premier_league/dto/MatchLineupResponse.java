package com.example.premier_league.dto;

import com.example.premier_league.entity.LineupStatus;

import java.util.List;

public record MatchLineupResponse(
        Long lineupId,
        Long matchId,
        LineupStatus status,
        List<Long> startingPlayerIds,
        List<Long> substitutePlayerIds
) {
}
