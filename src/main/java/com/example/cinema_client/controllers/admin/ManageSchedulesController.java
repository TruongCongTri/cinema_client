package com.example.cinema_client.controllers.admin;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.cinema_client.constants.API;
import com.example.cinema_client.models.BranchDTO;
import com.example.cinema_client.models.JwtResponseDTO;
import com.example.cinema_client.models.MovieDTO;
import com.example.cinema_client.models.RoomDTO;
import com.example.cinema_client.models.ScheduleDTO;

/**
 * @author tritcse00526x
 */
@Controller
@RequestMapping("/admin/schedules")
public class ManageSchedulesController {
    @Autowired
    private RestTemplate restTemplate;

    private static final String API_GET_SCHEDULES = API.baseURL+"/api/admin/schedules";

    private static final String API_GET_ACTIVE_MOVIES = API.baseURL+"/api/admin/movies/active";
    private static final String API_GET_ACTIVE_BRANCHES = API.baseURL+"/api/admin/branches/active";
    private static final String API_GET_ACTIVE_ROOMS = API.baseURL+"/api/admin/rooms/active";

    private static final String API_GET_SCHEDULE_DETAIL = API.baseURL+"/api/admin/schedules/detail";


    @GetMapping
    public String displayManageSchedulePage(HttpSession session,Model model){
        System.out.println("LOG: accessing manage schedules page");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);


        System.out.println("LOG: loading schedules");
        // call API get all schedules
        ResponseEntity<ScheduleDTO[]> responseSchedules = restTemplate.exchange(API_GET_SCHEDULES,HttpMethod.GET,entity,ScheduleDTO[].class);
        ScheduleDTO[] schedules = responseSchedules.getBody();
        model.addAttribute("schedules",schedules);
        return "admin/schedule/manage-schedule";
    }

    @GetMapping("/add")
    public String displayAddSchedulePage(HttpSession session,Model model){
        System.out.println("LOG: accessing add new schedule page");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        System.out.println("LOG: loading active branches");
        // call API get all branches
        ResponseEntity<BranchDTO[]> response = restTemplate.exchange(API_GET_ACTIVE_BRANCHES,HttpMethod.GET,entity,BranchDTO[].class);
        BranchDTO[] branches = response.getBody();
        System.out.println("LOG: loading active movies");
        // call API get all movies
        ResponseEntity<MovieDTO[]> responseMovie = restTemplate.exchange(API_GET_ACTIVE_MOVIES,HttpMethod.GET,entity,MovieDTO[].class);
        MovieDTO[] movies = responseMovie.getBody();

        model.addAttribute("branches",branches);
        model.addAttribute("rooms",null);
        model.addAttribute("movies",movies);
        System.out.println("LOG: creating new empty schedule");
        model.addAttribute("schedule",new ScheduleDTO());
        return "admin/schedule/update-schedule";
    }
    @PostMapping("/add")
    public String addSchedulePage(
            @ModelAttribute("schedule") ScheduleDTO schedule,
            HttpSession session,Model model){
        System.out.println("LOG: trying to create new schedule");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());

        HttpEntity<ScheduleDTO> entity = new HttpEntity<>(schedule,headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(API_GET_SCHEDULES,HttpMethod.POST, entity, String.class);
            System.out.println("SUCCESS: Add new schedule");
        } catch (Exception e) {
            System.out.println("FAIL: Add new schedule - " + e.getMessage());
        }
        return "redirect:/admin/schedules";
    }

    @GetMapping("/update")
    public String displayUpdateSchedulePage(
            @RequestParam Integer scheduleId,
            HttpSession session,Model model){
        System.out.println("LOG: accessing update schedule page");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        System.out.println("LOG: loading schedule by id " + scheduleId);
        // call API get selected schedule
        String urlTemplateSchedule = UriComponentsBuilder.fromHttpUrl(API_GET_SCHEDULE_DETAIL)
                .queryParam("scheduleId", "{scheduleId}")
                .encode()
                .toUriString();
        Map<String, String> listRequestParam = new HashMap<>();
        listRequestParam.put("scheduleId", scheduleId + "");

        ResponseEntity<ScheduleDTO> responseSchedule = restTemplate
                .exchange(urlTemplateSchedule,HttpMethod.GET,entity,ScheduleDTO.class,listRequestParam);
        ScheduleDTO schedule = responseSchedule.getBody();

        System.out.println("LOG: loading active branches");
        ResponseEntity<BranchDTO[]> responseBranch = restTemplate.exchange(API_GET_ACTIVE_BRANCHES,HttpMethod.GET,entity,BranchDTO[].class);
        BranchDTO[] branches = responseBranch.getBody();
        System.out.println("LOG: loading active movies");
        ResponseEntity<MovieDTO[]> responseMovie = restTemplate.exchange(API_GET_ACTIVE_MOVIES,HttpMethod.GET,entity,MovieDTO[].class);
        MovieDTO[] movies = responseMovie.getBody();

        System.out.println("LOG: loading active rooms of selected branch id " + schedule.getBranch().getId());
        // call API get list of rooms by selected branch
        String urlTemplateBranch = UriComponentsBuilder.fromHttpUrl(API_GET_ACTIVE_ROOMS)
                .queryParam("branchId", "{branchId}")
                .encode()
                .toUriString();
        Map<String, Integer> params = new HashMap<>();
        params.put("branchId", schedule.getBranch().getId());
        ResponseEntity<RoomDTO[]> responseRoom = restTemplate
                .exchange(urlTemplateBranch,HttpMethod.POST,entity,RoomDTO[].class,params);
        RoomDTO[] rooms = responseRoom.getBody();

        model.addAttribute("branches",branches);
        model.addAttribute("movies",movies);
        model.addAttribute("rooms",rooms);
        model.addAttribute("schedule",schedule);
        return "admin/schedule/update-schedule";
    }
    @PostMapping("/update")
    public String updateSchedulePage(
            @ModelAttribute("schedule") ScheduleDTO schedule,
            HttpSession session, Model model){
        System.out.println("LOG: trying to update schedule");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());

        HttpEntity<?> entity = new HttpEntity<>(schedule,headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(API_GET_SCHEDULES,HttpMethod.PUT,entity,String.class);
            System.out.println("SUCCESS: Update schedule");
        } catch (Exception e) {
            System.out.println("FAIL: Update schedule - " + e.getMessage());
        }
        return "redirect:/admin/schedules";
    }
}
