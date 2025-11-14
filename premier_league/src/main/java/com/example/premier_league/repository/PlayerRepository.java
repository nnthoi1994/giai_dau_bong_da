package com.example.premier_league.repository;

import com.example.premier_league.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    List<Player> findByTeamIdOrderByIdAsc(Integer teamId);
}
