package com.example.cinema_client.models;

import lombok.Data;

import java.util.List;

/**
 * @author tritcse00526x
 */
@Data
public class BranchDTO {
    private int id;
    private String mapURL;
    private String name;
    private String address;
    private String hotline;

    private List<ScheduleDTO> schedules;
    private List<MovieDTO> movies;
    private CityDTO city;

    private Long total;
    private Long totalTicket;

    private int isActive;
}
