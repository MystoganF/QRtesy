package com.qrtesy.shared.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppTimeConfig {

    @Bean
    public ZoneId manilaZoneId(@Value("${app.timezone:Asia/Manila}") String timezone) {
        return ZoneId.of(timezone);
    }

    @Bean
    public Clock manilaClock(ZoneId manilaZoneId) {
        return Clock.system(manilaZoneId);
    }
}