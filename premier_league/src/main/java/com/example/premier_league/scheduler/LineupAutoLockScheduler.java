package com.example.premier_league.scheduler;

import com.example.premier_league.service.CoachService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LineupAutoLockScheduler {

    private final CoachService coachService;

    @Scheduled(fixedDelay = 60000)
    public void autoLockLineups() {
        coachService.lockDueLineups();
    }
}
