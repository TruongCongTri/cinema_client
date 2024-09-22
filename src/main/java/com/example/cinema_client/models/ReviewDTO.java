package com.example.cinema_client.models;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * @author tritcse00526x
 */
@Data
public class ReviewDTO {
    private Integer id;
    @Max(10)
    @Min(1)
    private float rating;
    private String comment;
    private LocalDate commentDate;
    @NotNull
    private Integer movieId;
    private MovieDTO movie;
    private User user;
}
