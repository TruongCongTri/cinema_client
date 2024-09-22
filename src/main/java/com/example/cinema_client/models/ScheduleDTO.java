package com.example.cinema_client.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * @author tritcse00526x
 */
@Data
@NoArgsConstructor
public class ScheduleDTO {
    private int id;
    private LocalDate startDate;
    private LocalTime startTime;
    private BranchDTO branch;
    private RoomDTO room;
    private MovieDTO movie;
    private Double price;
    private int isActive;
}
