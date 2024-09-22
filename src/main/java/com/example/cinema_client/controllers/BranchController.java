package com.example.cinema_client.controllers;

import com.example.cinema_client.constants.API;
import com.example.cinema_client.models.BranchDTO;
import com.example.cinema_client.models.CityDTO;
import com.example.cinema_client.models.MovieDTO;
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

import java.util.HashMap;
import java.util.Map;

/**
 * @author tritcse00526x
 */
@Controller
@RequestMapping("/branches")
public class BranchController {
    @Autowired
    private RestTemplate restTemplate;
    public static  String API_GET_ACTIVE_BRANCHES = API.baseURL + "/api/branches/active";
    public static String API_GET_ACTIVE_BRANCH_DETAIL = API.baseURL + "/api/branches/active/detail";
    public static String API_GET_CITIES_HAVE_ACTIVE_BRANCHES = API.baseURL + "/api/cities/have-active-branches";
    /*quick booking*/
    public static String API_GET_MOVIES_BY_BRANCH = API.baseURL + "/api/movies/by-branch";
    public static String API_GET_ALL_START_DATE = API.baseURL+"/api/schedule/active/all-schedule-dates";
    /**/

    /**TODO: BRANCH page*/
    // access branch detail page
    @GetMapping("/detail")
    public String displayBranchDetailPage(
            @RequestParam Integer cityId,
            @RequestParam Integer branchId,
            Model model) {
        System.out.println("LOG: accessing branch detail page");
        // call API get branch detail by its city ID and its ID
        String urlTemplate = UriComponentsBuilder.fromHttpUrl(API_GET_ACTIVE_BRANCH_DETAIL)
                .queryParam("cityId", "{cityId}")
                .queryParam("branchId", "{branchId}")
                .encode()
                .toUriString();
        Map<String, Integer> params = new HashMap<>();
        params.put("cityId", cityId);
        params.put("branchId", branchId);

        HttpEntity<BranchDTO> responseBranch = restTemplate.getForEntity(urlTemplate,BranchDTO.class,params);
        BranchDTO preSelectBranch = responseBranch.getBody();

        ResponseEntity<BranchDTO[]> responseBranches = restTemplate.getForEntity(API_GET_ACTIVE_BRANCHES,BranchDTO[].class);
        BranchDTO[] branches = responseBranches.getBody();
        ResponseEntity<CityDTO[]> responseCities = restTemplate.getForEntity(API_GET_CITIES_HAVE_ACTIVE_BRANCHES,CityDTO[].class);
        CityDTO[] cities = responseCities.getBody();

        model.addAttribute("preSelectBranch",preSelectBranch);
        model.addAttribute("branches",branches);
        model.addAttribute("cities",cities);

        // call API get list of movies shown in the selected branch
        urlTemplate = UriComponentsBuilder.fromHttpUrl(API_GET_MOVIES_BY_BRANCH)
                .queryParam("branchId", "{branchId}")
                .encode().toUriString();
        HttpEntity<MovieDTO[]> responseMovies = restTemplate.getForEntity(urlTemplate,MovieDTO[].class,params);
        MovieDTO[] movies = responseMovies.getBody();

        ResponseEntity<String[]> responseStartDate = restTemplate.getForEntity(API_GET_ALL_START_DATE,String[].class);
        String[] allStartDate = responseStartDate.getBody();

        model.addAttribute("movies", movies);
        model.addAttribute("allStartDate",allStartDate);

        return "branch-detail";
    }


 }
