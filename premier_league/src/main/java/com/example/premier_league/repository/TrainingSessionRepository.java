package com.example.premier_league.repository;

import com.example.premier_league.entity.TrainingSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {

    List<TrainingSession> findByCoachIdOrderByStartAtAsc(Long coachId);
}
