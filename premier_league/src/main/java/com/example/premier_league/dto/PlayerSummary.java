package com.example.premier_league.dto;

import java.time.LocalDate;

public record PlayerSummary(
        Long id,
        String name,
        LocalDate dob,
        String experience,
        String position,
        String avatar
) {
}
