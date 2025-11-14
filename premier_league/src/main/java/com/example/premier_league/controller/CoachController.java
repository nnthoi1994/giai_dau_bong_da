package com.example.premier_league.controller;

import com.example.premier_league.dto.*;
import com.example.premier_league.entity.MatchLineup;
import com.example.premier_league.entity.MatchSchedule;
import com.example.premier_league.entity.Player;
import com.example.premier_league.entity.TrainingSession;
import com.example.premier_league.service.CoachService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coach")
@RequiredArgsConstructor
public class CoachController {

    private final CoachService coachService;

    @GetMapping("/{coachId}/players")
    public List<PlayerSummary> getPlayers(@PathVariable Long coachId) {
        return coachService.getPlayersForCoach(coachId)
                .stream()
                .map(player -> new PlayerSummary(
                        player.getId(),
                        player.getName(),
                        player.getDob(),
                        player.getExperience(),
                        player.getPosition(),
                        player.getAvatar()
                ))
                .toList();
    }

    @GetMapping("/{coachId}/matches")
    public List<MatchScheduleResponse> getMatches(@PathVariable Long coachId) {
        return coachService.getMatchesForCoach(coachId)
                .stream()
                .map(match -> new MatchScheduleResponse(
                        match.getId(),
                        match.getName(),
                        match.getMatchDate(),
                        match.getMatchTime(),
                        match.getRound(),
                        match.getNote(),
                        match.getHomeTeam() != null ? match.getHomeTeam().getId() : null,
                        match.getHomeTeam() != null ? match.getHomeTeam().getName() : null,
                        match.getAwayTeam() != null ? match.getAwayTeam().getId() : null,
                        match.getAwayTeam() != null ? match.getAwayTeam().getName() : null
                ))
                .toList();
    }

    @GetMapping("/{coachId}/training-sessions")
    public List<TrainingSessionResponse> getTrainingSessions(@PathVariable Long coachId) {
        return coachService.getTrainingSessions(coachId)
                .stream()
                .map(session -> new TrainingSessionResponse(
                        session.getId(),
                        session.getTitle(),
                        session.getDescription(),
                        session.getStartAt(),
                        session.getEndAt(),
                        session.getLocation()
                ))
                .toList();
    }

    @PostMapping("/{coachId}/training-sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public TrainingSessionResponse createTrainingSession(@PathVariable Long coachId,
                                                         @Valid @RequestBody TrainingSessionRequest request) {
        TrainingSession session = coachService.createTrainingSession(coachId, request);
        return new TrainingSessionResponse(
                session.getId(),
                session.getTitle(),
                session.getDescription(),
                session.getStartAt(),
                session.getEndAt(),
                session.getLocation()
        );
    }

    @PutMapping("/{coachId}/training-sessions/{sessionId}")
    public TrainingSessionResponse updateTrainingSession(@PathVariable Long coachId,
                                                         @PathVariable Long sessionId,
                                                         @Valid @RequestBody TrainingSessionRequest request) {
        TrainingSession session = coachService.updateTrainingSession(coachId, sessionId, request);
        return new TrainingSessionResponse(
                session.getId(),
                session.getTitle(),
                session.getDescription(),
                session.getStartAt(),
                session.getEndAt(),
                session.getLocation()
        );
    }

    @DeleteMapping("/{coachId}/training-sessions/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTrainingSession(@PathVariable Long coachId, @PathVariable Long sessionId) {
        coachService.deleteTrainingSession(coachId, sessionId);
    }

    @GetMapping("/{coachId}/lineups/{matchId}")
    public MatchLineupResponse getLineup(@PathVariable Long coachId, @PathVariable Long matchId) {
        MatchLineup lineup = coachService.getLineup(coachId, matchId);
        return new MatchLineupResponse(
                lineup.getId(),
                lineup.getMatchSchedule().getId(),
                lineup.getStatus(),
                lineup.getStartingPlayerIds(),
                lineup.getSubstitutePlayerIds()
        );
    }

    @PutMapping("/{coachId}/lineups/{matchId}")
    public MatchLineupResponse upsertLineup(@PathVariable Long coachId,
                                            @PathVariable Long matchId,
                                            @Valid @RequestBody MatchLineupRequest request) {
        MatchLineup lineup = coachService.upsertLineup(coachId, matchId, request);
        return new MatchLineupResponse(
                lineup.getId(),
                lineup.getMatchSchedule().getId(),
                lineup.getStatus(),
                lineup.getStartingPlayerIds(),
                lineup.getSubstitutePlayerIds()
        );
    }
}
