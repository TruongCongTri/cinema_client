package com.example.cinema_client.controllers.admin;

import javax.servlet.http.HttpSession;

import com.example.cinema_client.models.RoomDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.example.cinema_client.constants.API;
import com.example.cinema_client.models.BranchDTO;
import com.example.cinema_client.models.JwtResponseDTO;

/**
 * @author tritcse00526x
 */

@Controller
@RequestMapping("/admin/rooms")
public class ManageRoomsController {
    @Autowired
    private RestTemplate restTemplate;
    private static final String API_GET_ACTIVE_BRANCHES = API.baseURL + "/api/admin/branches/active";
    private static final String API_GET_ROOMS = API.baseURL + "/api/admin/rooms";
    private static final String API_GET_ROOM_DETAIL = API.baseURL + "/api/admin/rooms/detail";

    @GetMapping
    public String updateRoomPage(HttpSession session, Model model){
        System.out.println("LOG: accessing manage rooms page");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO)session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        System.out.println("LOG: loading branches");
        // call API get all branches
        ResponseEntity<BranchDTO[]> response = restTemplate.exchange(API_GET_ACTIVE_BRANCHES,HttpMethod.GET,entity,BranchDTO[].class);
        BranchDTO[] branches = response.getBody();
        model.addAttribute("branches",branches);
        return "admin/room/manage-room";
    }

    @GetMapping("/add")
    public String displayAddRoomPage(HttpSession session, Model model){
        System.out.println("LOG: accessing add new room page");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO)session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION,"Bearer "+jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        System.out.println("LOG: loading branches");
        //call API get all branches
        ResponseEntity<BranchDTO[]> response = restTemplate.exchange(API_GET_ACTIVE_BRANCHES,HttpMethod.GET,entity,BranchDTO[].class);
        BranchDTO[] branches = response.getBody();
        model.addAttribute("branches",branches);
        return "admin/room/add-room";
    }
}
