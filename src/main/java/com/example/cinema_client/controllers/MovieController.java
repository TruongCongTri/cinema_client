package com.example.cinema_client.controllers;

import com.example.cinema_client.constants.API;
import com.example.cinema_client.models.BranchDTO;
import com.example.cinema_client.models.JwtResponseDTO;
import com.example.cinema_client.models.MovieDTO;
import com.example.cinema_client.models.ReviewDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tritcse00526x
 */
@Controller
@RequestMapping("/movies")
public class MovieController {
    @Autowired
    private RestTemplate restTemplate;

    public static String API_GET_ACTIVE_SHOWING_MOVIES = API.baseURL + "/api/movies/showing/active";
    public static String API_GET_ACTIVE_COMING_MOVIES = API.baseURL + "/api/movies/coming/active";
    public static String API_GET_ACTIVE_MOVIE_DETAIL = API.baseURL + "/api/movies/active/detail";
    /*quick booking*/
    public static String API_GET_BRANCHES_AND_SCHEDULES_BY_MOVIE = API.baseURL + "/api/branches/by-movie";
    public static String API_GET_ALL_START_DATE = API.baseURL+"/api/schedule/active/all-schedule-dates";
    /**/
    public static String API_POST_REVIEW = API.baseURL + "/api/reviews";

    /**TODO: MOVIE page*/
    // access Now Showing page
    @GetMapping("/showing")
    public String displayShowingMoviesPage(Model model) {
        System.out.println("LOG: accessing now showing movie page");
        ResponseEntity<MovieDTO[]> responseMovies = restTemplate.getForEntity(API_GET_ACTIVE_SHOWING_MOVIES, MovieDTO[].class);
        MovieDTO[] showingMovies = responseMovies.getBody();
        model.addAttribute("movies", showingMovies);
        return "movies";
    }
    // access Coming Soon page
    @GetMapping("/coming")
    public String displayComingMoviesPage(Model model) {
        System.out.println("LOG: accessing coming soon movie page");
        ResponseEntity<MovieDTO[]> responseMovies = restTemplate.getForEntity(API_GET_ACTIVE_COMING_MOVIES, MovieDTO[].class);
        MovieDTO[] comingMovies = responseMovies.getBody();
        model.addAttribute("movies", comingMovies);
        return "movies";
    }

    // access movie detail page
    @GetMapping("/detail")
    public String displayMovieDetailPage(@RequestParam Integer movieId, Model model){
        System.out.println("LOG: accessing movie detail page");
        // call API get selected movie detail
        String urlTemplate = UriComponentsBuilder.fromHttpUrl(API_GET_ACTIVE_MOVIE_DETAIL)
                .queryParam("movieId", "{movieId}")
                .encode()
                .toUriString();
        Map<String, Integer> params = new HashMap<>();
        params.put("movieId", movieId);
        ResponseEntity<MovieDTO> response = restTemplate.getForEntity(urlTemplate,MovieDTO.class,params);

        MovieDTO movie = response.getBody();

        // call API get list of branches show the selected movie
        urlTemplate = UriComponentsBuilder.fromHttpUrl(API_GET_BRANCHES_AND_SCHEDULES_BY_MOVIE)
                .queryParam("movieId", "{movieId}")
                .encode()
                .toUriString();

        HttpEntity<BranchDTO[]> responseBranchesByMovie = restTemplate.getForEntity(urlTemplate,BranchDTO[].class,params);
        BranchDTO[] branchDTOS = responseBranchesByMovie.getBody();

        ResponseEntity<String[]> responseStartDate = restTemplate.getForEntity(API_GET_ALL_START_DATE,String[].class);
        String[] allStartDate = responseStartDate.getBody();

        model.addAttribute("branchesByMovie",branchDTOS);
        model.addAttribute("movie",movie);
        model.addAttribute("allStartDate",allStartDate);

        // handle review comment
        double rating = 0;
        for(ReviewDTO r: movie.getReviews()) {
            rating += r.getRating();
        }
        model.addAttribute("rating", rating/movie.getReviews().size());
        model.addAttribute("review", new ReviewDTO());

        return "movie-detail";
    }

    // post comment for a specific movie
    @PostMapping("/review")
    public String postReview(
            @Valid @ModelAttribute("review") ReviewDTO review,
            BindingResult bindingResult,
            Model model, HttpServletRequest request) {
        System.out.println("LOG: accessing review session in movie detail page");
        HttpSession session = request.getSession();
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponse = (JwtResponseDTO) session.getAttribute("jwtResponse");

        if (jwtResponse == null) {
            return "redirect:/login";
        }

        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponse.getAccessToken());
        HttpEntity<ReviewDTO> entity = new HttpEntity<>(review, headers);

        // call API post a review
        if (!(bindingResult.hasErrors())) {
            ResponseEntity<ReviewDTO> response = restTemplate
                    .exchange(API_POST_REVIEW,HttpMethod.POST,entity,ReviewDTO.class);
        }
        return "redirect:/movies/detail?movieId=" + review.getMovieId() + "#pills-comment";
    }

}
