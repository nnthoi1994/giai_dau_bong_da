package com.example.premier_league;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PremierLeagueApplication {

    public static void main(String[] args) {
        SpringApplication.run(PremierLeagueApplication.class, args);
    }

}
