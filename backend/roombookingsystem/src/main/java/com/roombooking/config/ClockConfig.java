package com.roombooking.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Provides the system clock, so services can ask for the current time and tests can fix it. */
@Configuration
public class ClockConfig {

    /** Returns the clock that services use to decide what "now" is. */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
