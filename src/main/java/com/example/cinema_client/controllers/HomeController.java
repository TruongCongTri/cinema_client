package com.example.cinema_client.controllers;

import com.example.cinema_client.constants.API;
import com.example.cinema_client.models.MovieDTO;
import com.example.cinema_client.models.PostDTO;
import com.example.cinema_client.models.PromotionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

/**
 * @author tritcse00526x
 */
@Controller
@RequestMapping("/")
public class HomeController {
    @Autowired
    private RestTemplate restTemplate;

    public static String API_GET_ACTIVE_PROMOTIONS = API.baseURL+"/api/promotions/active";
    public static String API_GET_ACTIVE_NEWS = API.baseURL+"/api/posts/news/active";
    public static String API_GET_ACTIVE_REVIEWS = API.baseURL+"/api/posts/reviews/active";
    /*quick booking*/
    public static String API_GET_MOVIES_HAVE_ACTIVE_SCHEDULES = API.baseURL + "/api/movies/has-active-schedules";

    /*movies*/
    public static String API_GET_ACTIVE_SHOWING_MOVIES = API.baseURL+"/api/movies/showing/active";
    public static String API_GET_ACTIVE_COMING_MOVIES = API.baseURL+"/api/movies/coming/active";

    /**TODO: HOME page*/
    // access home page
    @GetMapping
    public String displayHomePage(Model model){
        System.out.println("LOG: accessing home page");
        /**/
        ResponseEntity<MovieDTO[]> responseShowingMoviesHeader = restTemplate.getForEntity(API_GET_ACTIVE_SHOWING_MOVIES,MovieDTO[].class);
        MovieDTO[] showingMovies = responseShowingMoviesHeader.getBody();
        ResponseEntity<MovieDTO[]> responseComingMoviesHeader = restTemplate.getForEntity(API_GET_ACTIVE_COMING_MOVIES,MovieDTO[].class);
        MovieDTO[] comingMovies = responseComingMoviesHeader.getBody();
        /**/
        ResponseEntity<MovieDTO[]> responseMoviesHaveBranches = restTemplate.getForEntity(API_GET_MOVIES_HAVE_ACTIVE_SCHEDULES, MovieDTO[].class);
        MovieDTO[] moviesHaveBranches = responseMoviesHaveBranches.getBody();
        /**/
        ResponseEntity<PromotionDTO[]> responseActivePromotions = restTemplate.getForEntity(API_GET_ACTIVE_PROMOTIONS,PromotionDTO[].class);
        PromotionDTO[] activePromos = responseActivePromotions.getBody();
        ResponseEntity<PostDTO[]> responseActiveNews = restTemplate.getForEntity(API_GET_ACTIVE_NEWS,PostDTO[].class);
        PostDTO[] activeNews = responseActiveNews.getBody();
        ResponseEntity<PostDTO[]> responseActiveReviews = restTemplate.getForEntity(API_GET_ACTIVE_REVIEWS,PostDTO[].class);
        PostDTO[] activeReviews = responseActiveReviews.getBody();

        /*movies*/
        model.addAttribute("showingMovies",showingMovies);
        model.addAttribute("comingMovies",comingMovies);
        /**/
        model.addAttribute("activePromos",activePromos);
        model.addAttribute("activeNews",activeNews);
        model.addAttribute("activeReviews",activeReviews);
        /*START - quick booking*/
        model.addAttribute("selectMovies", moviesHaveBranches);
        /*END - quick booking*/

        return "home";
    }

}
