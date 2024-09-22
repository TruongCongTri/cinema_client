package com.example.cinema_client.controllers.admin;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import com.example.cinema_client.constants.API;
import com.example.cinema_client.models.BranchDTO;
import com.example.cinema_client.models.JwtResponseDTO;
import com.example.cinema_client.models.MovieDTO;
/**
 * @author tritcse00526x
 */
@Controller
@RequestMapping("/admin")
public class AdminHomeController {
    @Autowired
    private RestTemplate restTemplate;

    //public static final String API_GET_REPORT_TICKETS = API.baseURL + "/api/admin/reports/tickets";
    public static final String API_GET_REPORT_BRANCHES = API.baseURL + "/api/admin/reports/branches";
    public static final String API_GET_REPORT_MOVIES = API.baseURL + "/api/admin/reports/movies";
    //public static final String API_GET_REPORT_SCHEDULES = API.baseURL + "/api/admin/schedules";

    @GetMapping
    public String displayHomePage(HttpSession session,Model model){
        System.out.println("LOG: accessing admin home page");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO)session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<MovieDTO[]> responses = restTemplate.exchange(API_GET_REPORT_MOVIES, HttpMethod.GET, entity, MovieDTO[].class);
        MovieDTO[] movies = responses.getBody();
        ResponseEntity<BranchDTO[]> response = restTemplate.exchange(API_GET_REPORT_BRANCHES, HttpMethod.GET, entity, BranchDTO[].class);
        BranchDTO[] branches = response.getBody();

        Long turnover = 0l;
        Long numTicket = 0l;
        for(MovieDTO movie: movies) {
            turnover += movie.getTotal();
            numTicket += movie.getTotalTicket();
        }
        model.addAttribute("turnover", turnover);
        model.addAttribute("numTicket", numTicket);
        model.addAttribute("movies", movies);
        model.addAttribute("branches", branches);
        return "admin/home-admin";
    }
}
