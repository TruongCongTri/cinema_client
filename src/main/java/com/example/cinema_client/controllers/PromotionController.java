package com.example.cinema_client.controllers;

import com.example.cinema_client.constants.API;
import com.example.cinema_client.models.MovieDTO;
import com.example.cinema_client.models.PromotionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tritcse00526x
 */
@Controller
@RequestMapping("/promotions")
public class PromotionController {
    @Autowired
    private RestTemplate restTemplate;

    public static String API_GET_ACTIVE_PROMOS = API.baseURL + "/api/promotions/active";
    public static String API_GET_ACTIVE_PROMO_DETAIL = API.baseURL + "/api/promotions/active/detail";
    /*quick booking*/
    public static String API_GET_MOVIES_HAVE_ACTIVE_SCHEDULES = API.baseURL + "/api/movies/has-active-schedules";
    /**/

    /**TODO: PROMOTION page*/
    @GetMapping
    public String displayPromotionsPage(Model model, HttpServletRequest request){
        System.out.println("LOG: accessing promotions page");
        //call API get all promotions
        ResponseEntity<PromotionDTO[]> response = restTemplate.getForEntity(API_GET_ACTIVE_PROMOS, PromotionDTO[].class);
        PromotionDTO[] promos = response.getBody();
        model.addAttribute("promos",promos);

        /*START - quick booking*/
        ResponseEntity<MovieDTO[]> responseMoviesHaveBranches = restTemplate.getForEntity(API_GET_MOVIES_HAVE_ACTIVE_SCHEDULES, MovieDTO[].class);
        MovieDTO[] moviesHaveBranches = responseMoviesHaveBranches.getBody();
        model.addAttribute("selectMovies", moviesHaveBranches);
        /*END - quick booking*/

        return "promotions";
    }

    @GetMapping("/detail")
    public String displayPromotionDetailPage(@RequestParam Integer promoId, Model model) {
        System.out.println("LOG: accessing promotion detail page");
        // call API get selected promotion detail
        String urlTemplate = UriComponentsBuilder.fromHttpUrl(API_GET_ACTIVE_PROMO_DETAIL)
                .queryParam("promoId", "{promoId}")
                .encode()
                .toUriString();
        Map<String, Integer> params = new HashMap<>();
        params.put("promoId", promoId);

        HttpEntity<PromotionDTO> responseBranch = restTemplate.getForEntity(urlTemplate,PromotionDTO.class,params);
        PromotionDTO promo = responseBranch.getBody();

        model.addAttribute("promo",promo);

        /*START - quick booking*/
        ResponseEntity<MovieDTO[]> responseMoviesHaveBranches = restTemplate.getForEntity(API_GET_MOVIES_HAVE_ACTIVE_SCHEDULES, MovieDTO[].class);
        MovieDTO[] moviesHaveBranches = responseMoviesHaveBranches.getBody();
        model.addAttribute("selectMovies", moviesHaveBranches);
        /*END - quick booking*/

        return "promotion-detail";
    }

}
