package com.example.tracklybe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TracklyBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TracklyBeApplication.class, args);
    }

}
