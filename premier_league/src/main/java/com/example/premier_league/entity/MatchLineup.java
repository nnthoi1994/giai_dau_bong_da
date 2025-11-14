package com.example.premier_league.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "match_lineups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchLineup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private MatchSchedule matchSchedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    private Coach coach;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LineupStatus status;

    @ElementCollection
    @CollectionTable(name = "match_lineup_starters", joinColumns = @JoinColumn(name = "lineup_id"))
    @Column(name = "player_id")
    private List<Long> startingPlayerIds = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "match_lineup_subs", joinColumns = @JoinColumn(name = "lineup_id"))
    @Column(name = "player_id")
    private List<Long> substitutePlayerIds = new ArrayList<>();
}

