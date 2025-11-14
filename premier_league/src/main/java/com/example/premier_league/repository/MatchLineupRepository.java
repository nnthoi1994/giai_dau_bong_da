package com.example.premier_league.repository;

import com.example.premier_league.entity.LineupStatus;
import com.example.premier_league.entity.MatchLineup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MatchLineupRepository extends JpaRepository<MatchLineup, Long> {

    Optional<MatchLineup> findByMatchScheduleIdAndCoachId(Long matchId, Long coachId);

    List<MatchLineup> findByStatus(LineupStatus status);
}
