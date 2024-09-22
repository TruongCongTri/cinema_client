package com.example.cinema_client.models;

import lombok.Data;

import java.util.List;

/**
 * @author tritcse00526x
 */
@Data
public class CityDTO {
    private int id;
    private String city;
    private String province;

    private List<BranchDTO> branches;

}
