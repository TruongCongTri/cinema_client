package com.example.cinema_client.models;

import lombok.Data;

/**
 * @author tritcse00526x
 */
@Data
public class PromotionDTO {
    private Integer id;
    private String title;

    private String horizontalImgUrl;
    private String verticalImgUrl;

    private String longDesc;

    private int isActive;
}
