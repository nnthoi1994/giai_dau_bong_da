package com.example.premier_league.service;

import com.example.premier_league.dto.MatchLineupRequest;
import com.example.premier_league.dto.TrainingSessionRequest;
import com.example.premier_league.entity.*;
import com.example.premier_league.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoachService {

    private static final Logger log = LoggerFactory.getLogger(CoachService.class);

    private static final int DEFAULT_STARTING_PLAYERS = 11;

    private final CoachRepository coachRepository;
    private final PlayerRepository playerRepository;
    private final MatchScheduleRepository matchScheduleRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final MatchLineupRepository matchLineupRepository;

    public List<Player> getPlayersForCoach(Long coachId) {
        Team team = resolveCoachTeam(coachId);
        return playerRepository.findByTeamIdOrderByIdAsc(team.getId());
    }

    public List<MatchSchedule> getMatchesForCoach(Long coachId) {
        Team team = resolveCoachTeam(coachId);
        return matchScheduleRepository.findByHomeTeamIdOrAwayTeamId(team.getId(), team.getId())
                .stream()
                .sorted(Comparator.comparing(MatchSchedule::getMatchDate)
                        .thenComparing(MatchSchedule::getMatchTime, Comparator.nullsLast(LocalTime::compareTo)))
                .toList();
    }

    public List<TrainingSession> getTrainingSessions(Long coachId) {
        ensureCoachExists(coachId);
        return trainingSessionRepository.findByCoachIdOrderByStartAtAsc(coachId);
    }

    @Transactional
    public TrainingSession createTrainingSession(Long coachId, TrainingSessionRequest request) {
        Coach coach = ensureCoachExists(coachId);
        Team team = resolveCoachTeam(coach);
        TrainingSession session = TrainingSession.builder()
                .coach(coach)
                .team(team)
                .title(request.title())
                .description(request.description())
                .startAt(request.startAt())
                .endAt(request.endAt())
                .location(request.location())
                .build();
        return trainingSessionRepository.save(session);
    }

    @Transactional
    public TrainingSession updateTrainingSession(Long coachId, Long sessionId, TrainingSessionRequest request) {
        ensureCoachExists(coachId);
        TrainingSession session = trainingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training session not found"));
        if (!session.getCoach().getId().equals(coachId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Coach cannot modify this session");
        }
        session.setTitle(request.title());
        session.setDescription(request.description());
        session.setStartAt(request.startAt());
        session.setEndAt(request.endAt());
        session.setLocation(request.location());
        return trainingSessionRepository.save(session);
    }

    @Transactional
    public void deleteTrainingSession(Long coachId, Long sessionId) {
        ensureCoachExists(coachId);
        TrainingSession session = trainingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Training session not found"));
        if (!session.getCoach().getId().equals(coachId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Coach cannot delete this session");
        }
        trainingSessionRepository.delete(session);
    }

    @Transactional
    public MatchLineup upsertLineup(Long coachId, Long matchId, MatchLineupRequest request) {
        Coach coach = ensureCoachExists(coachId);
        MatchSchedule matchSchedule = matchScheduleRepository.findById(matchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found"));

        Team team = resolveCoachTeam(coach);
        if (!isCoachTeamParticipating(matchSchedule, team)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Coach team does not participate in this match");
        }

        MatchLineup lineup = matchLineupRepository
                .findByMatchScheduleIdAndCoachId(matchId, coachId)
                .orElse(MatchLineup.builder()
                        .coach(coach)
                        .team(team)
                        .matchSchedule(matchSchedule)
                        .status(LineupStatus.DRAFT)
                        .build());

        if (lineup.getStatus() == LineupStatus.LOCKED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Lineup already locked");
        }

        validatePlayersBelongToTeam(request.startingPlayerIds(), team);
        if (request.substitutePlayerIds() != null) {
            validatePlayersBelongToTeam(request.substitutePlayerIds(), team);
        }

        lineup.setStartingPlayerIds(new ArrayList<>(request.startingPlayerIds()));
        lineup.setSubstitutePlayerIds(request.substitutePlayerIds() == null
                ? new ArrayList<>()
                : new ArrayList<>(request.substitutePlayerIds()));

        return matchLineupRepository.save(lineup);
    }

    public MatchLineup getLineup(Long coachId, Long matchId) {
        ensureCoachExists(coachId);
        return matchLineupRepository.findByMatchScheduleIdAndCoachId(matchId, coachId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lineup not found"));
    }

    @Transactional
    public void lockDueLineups() {
        List<MatchLineup> drafts = matchLineupRepository.findByStatus(LineupStatus.DRAFT);
        LocalDateTime now = LocalDateTime.now();
        for (MatchLineup lineup : drafts) {
            LocalDateTime matchStart = resolveMatchStart(lineup.getMatchSchedule());
            if (matchStart == null) {
                continue;
            }
            if (!matchStart.isBefore(now.minusMinutes(1)) && !matchStart.isAfter(now.plusMinutes(30))) {
                finalizeLineup(lineup);
            }
        }
    }

    private void finalizeLineup(MatchLineup lineup) {
        if (lineup.getStartingPlayerIds().isEmpty()) {
            List<Player> players = playerRepository.findByTeamIdOrderByIdAsc(lineup.getTeam().getId());
            List<Long> starterIds = players.stream()
                    .limit(DEFAULT_STARTING_PLAYERS)
                    .map(Player::getId)
                    .toList();
            List<Long> subs = players.stream()
                    .skip(DEFAULT_STARTING_PLAYERS)
                    .map(Player::getId)
                    .toList();
            lineup.setStartingPlayerIds(new ArrayList<>(starterIds));
            lineup.setSubstitutePlayerIds(new ArrayList<>(subs));
            log.warn("Lineup for match {} auto-filled with default players due to missing selection", lineup.getMatchSchedule().getId());
        }
        lineup.setStatus(LineupStatus.LOCKED);
        matchLineupRepository.save(lineup);
        log.info("Lineup {} locked for match {}", lineup.getId(), lineup.getMatchSchedule().getId());
    }

    private void validatePlayersBelongToTeam(List<Long> playerIds, Team team) {
        if (playerIds == null || playerIds.isEmpty()) {
            return;
        }
        Set<Long> teamPlayerIds = playerRepository.findByTeamIdOrderByIdAsc(team.getId())
                .stream()
                .map(Player::getId)
                .collect(Collectors.toSet());
        List<Long> invalid = playerIds.stream()
                .filter(id -> !teamPlayerIds.contains(id))
                .toList();
        if (!invalid.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Players do not belong to coach team: " + invalid);
        }
    }

    private Team resolveCoachTeam(Long coachId) {
        Coach coach = ensureCoachExists(coachId);
        return resolveCoachTeam(coach);
    }

    private Team resolveCoachTeam(Coach coach) {
        Team team = coach.getTeam();
        if (team == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Coach is not assigned to any team");
        }
        return team;
    }

    private Coach ensureCoachExists(Long coachId) {
        return coachRepository.findById(coachId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coach not found"));
    }

    private boolean isCoachTeamParticipating(MatchSchedule matchSchedule, Team team) {
        return (matchSchedule.getHomeTeam() != null && team.getId().equals(matchSchedule.getHomeTeam().getId()))
                || (matchSchedule.getAwayTeam() != null && team.getId().equals(matchSchedule.getAwayTeam().getId()));
    }

    private LocalDateTime resolveMatchStart(MatchSchedule matchSchedule) {
        if (matchSchedule.getMatchDate() == null || matchSchedule.getMatchTime() == null) {
            return null;
        }
        return matchSchedule.getMatchDate().atTime(matchSchedule.getMatchTime());
    }
}
