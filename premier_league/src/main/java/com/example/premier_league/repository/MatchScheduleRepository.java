package com.example.premier_league.repository;

import com.example.premier_league.entity.MatchSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface MatchScheduleRepository extends JpaRepository<MatchSchedule, Long> {

    List<MatchSchedule> findByHomeTeamIdOrAwayTeamId(Integer homeTeamId, Integer awayTeamId);

    List<MatchSchedule> findByMatchDateAndMatchTime(LocalDate date, LocalTime time);
}
