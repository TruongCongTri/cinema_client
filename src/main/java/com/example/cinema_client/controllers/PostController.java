package com.example.cinema_client.controllers;

import com.example.cinema_client.constants.API;
import com.example.cinema_client.models.MovieDTO;
import com.example.cinema_client.models.PostDTO;
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
@RequestMapping("/posts")
public class PostController {
    @Autowired
    private RestTemplate restTemplate;

    public static  String API_GET_ACTIVE_NEWS = API.baseURL + "/api/posts/news/active";
    public static  String API_GET_ACTIVE_REVIEWS = API.baseURL + "/api/posts/reviews/active";
    public static String API_GET_ACTIVE_POST_DETAIL = API.baseURL + "/api/posts/active/detail";
    /*quick booking*/
    public static String API_GET_MOVIES_HAVE_ACTIVE_SCHEDULES = API.baseURL + "/api/movies/has-active-schedules";

    /**TODO: POST page*/
    @GetMapping("/reviews")
    public String displayReviewPage(Model model, HttpServletRequest request){
        System.out.println("LOG: accessing reviews page");
        // call API get all reviews
        ResponseEntity<PostDTO[]> response = restTemplate.getForEntity(API_GET_ACTIVE_NEWS, PostDTO[].class);
        PostDTO[] posts = response.getBody();
        model.addAttribute("posts",posts);

        /*START - quick booking*/
        ResponseEntity<MovieDTO[]> responseMoviesHaveBranches = restTemplate.getForEntity(API_GET_MOVIES_HAVE_ACTIVE_SCHEDULES, MovieDTO[].class);
        MovieDTO[] moviesHaveBranches = responseMoviesHaveBranches.getBody();
        model.addAttribute("selectMovies", moviesHaveBranches);
        /*END - quick booking*/

        return "posts";
    }
    @GetMapping("/news")
    public String displayNewsPage(Model model, HttpServletRequest request){
        System.out.println("LOG: accessing news page");
        // call API get all news
        ResponseEntity<PostDTO[]> response = restTemplate.getForEntity(API_GET_ACTIVE_REVIEWS, PostDTO[].class);
        PostDTO[] posts = response.getBody();
        model.addAttribute("posts",posts);

        /*START - quick booking*/
        ResponseEntity<MovieDTO[]> responseMoviesHaveBranches = restTemplate.getForEntity(API_GET_MOVIES_HAVE_ACTIVE_SCHEDULES, MovieDTO[].class);
        MovieDTO[] moviesHaveBranches = responseMoviesHaveBranches.getBody();
        model.addAttribute("selectMovies", moviesHaveBranches);
        /*END - quick booking*/

        return "posts";
    }

    @GetMapping("/detail")
    public String displayDetailPage(@RequestParam Integer postId, Model model) {
        System.out.println("LOG: accessing post detail page");
        // call API get selected post
        String urlTemplate = UriComponentsBuilder.fromHttpUrl(API_GET_ACTIVE_POST_DETAIL)
                .queryParam("postId", "{postId}")
                .encode()
                .toUriString();
        Map<String, Integer> params = new HashMap<>();
        params.put("postId", postId);

        HttpEntity<PostDTO> responseNews = restTemplate.getForEntity(urlTemplate, PostDTO.class,params);
        PostDTO post = responseNews.getBody();

        model.addAttribute("post",post);

        /*START - quick booking*/
        ResponseEntity<MovieDTO[]> responseMoviesHaveBranches = restTemplate.getForEntity(API_GET_MOVIES_HAVE_ACTIVE_SCHEDULES, MovieDTO[].class);
        MovieDTO[] moviesHaveBranches = responseMoviesHaveBranches.getBody();
        model.addAttribute("selectMovies", moviesHaveBranches);
        /*END - quick booking*/

        return "post-detail";
    }

}
