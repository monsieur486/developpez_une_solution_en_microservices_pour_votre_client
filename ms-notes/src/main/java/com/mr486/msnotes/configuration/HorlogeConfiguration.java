package com.mr486.msnotes.configuration;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HorlogeConfiguration {

    @Bean
    public Clock horloge() {
        return Clock.systemDefaultZone();
    }
}
