package com.example.cinema_client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tritcse00526x
 */
@Configuration
public class BookingConfig {
    // Map<scheduleId, Map<seatId, expired>>
    @Bean
    @Qualifier("bookingSeatStore")
    Map<Integer, Map<Integer, LocalDateTime>> bookingSeatStore(){
        return new HashMap<Integer, Map<Integer,LocalDateTime>>();
    };
}