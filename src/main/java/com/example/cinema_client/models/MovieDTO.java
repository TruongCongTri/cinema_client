package com.example.cinema_client.models;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * @author tritcse00526x
 */
@Data
public class MovieDTO {
    private int id;
    private String name;
    private String longDescription;

    private LocalDate releaseDate;

    private int duration;
    private String trailerURL;

    private Integer isShowing;

    private String smallImageURL;
    private String largeImageURL;

    private String language;

    private Set<GenreDTO> categories;
    private Set<DirectorDTO> directors;
    private Set<ActorDTO> actors;
    private RatedDTO rated;
    private List<ScheduleDTO> schedules;
    private List<ReviewDTO> reviews;

    private Long total;
    private Long totalTicket;

    private int isActive;
}
