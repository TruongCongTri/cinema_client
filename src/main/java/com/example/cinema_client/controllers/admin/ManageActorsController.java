package com.example.cinema_client.controllers.admin;

import com.example.cinema_client.constants.API;
import com.example.cinema_client.models.ActorDTO;
import com.example.cinema_client.models.JwtResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tritcse00526x
 */
@Controller
@RequestMapping("/admin/actors")
public class ManageActorsController {
    @Autowired
    private RestTemplate restTemplate;
    private static final String API_ACTORS = API.baseURL + "/api/admin/actors";
    public static final String API_GET_ACTOR = API.baseURL + "/api/admin/actors/detail";

    @GetMapping
    public String displayManageActorPage(HttpSession session, Model model) {
        System.out.println("LOG: accessing manage actors page");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<ActorDTO[]> response = restTemplate.exchange(API_ACTORS,HttpMethod.GET,entity,ActorDTO[].class);
        ActorDTO[] actors = response.getBody();
        model.addAttribute("actors", actors);
        return "admin/actor/manage-actor";
    }

    @GetMapping("/add")
    public String displayAddActorPage(HttpSession session, Model model){
        System.out.println("LOG: accessing add new user page");


        model.addAttribute("actor",new ActorDTO());
        return "admin/actor/update-actor";
    }
    @PostMapping("/add")
    public String addActorPage(
            @ModelAttribute("actor") ActorDTO actor,
            @RequestParam(name = "checkActive", required = false, defaultValue = "false") boolean checkActive,
            HttpSession session, Model model){
        System.out.println("LOG: trying to create new actor");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());

        // check if actor status is active or not
        if(checkActive) {
            actor.setIsActive(1);
        } else {
            actor.setIsActive(0);
        }

        HttpEntity<ActorDTO> entity = new HttpEntity<>(actor, headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(API_ACTORS,HttpMethod.POST,entity,String.class);
            System.out.println("SUCCESS: Add new actor");
        } catch (Exception e) {
            System.out.println("FAIL: Add new actor - " + e.getMessage());
        }

        return "redirect:/admin/actors";
    }

    @GetMapping("/update")
    public String displayUpdateActorPage(
            @RequestParam Integer actorId,
            HttpSession session, Model model){
        System.out.println("LOG: accessing update actor page");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO)session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        // call API get selected actor
        String urlTemplate = UriComponentsBuilder.fromHttpUrl(API_GET_ACTOR)
                .queryParam("actorId", "{actorId}")
                .encode()
                .toUriString();
        Map<String, String> listRequestParam = new HashMap<>();
        listRequestParam.put("actorId", actorId + "");

        ResponseEntity<ActorDTO> response = restTemplate.exchange(
                urlTemplate,HttpMethod.GET,entity,ActorDTO.class,listRequestParam);
        ActorDTO actor = response.getBody();

        model.addAttribute("actor",actor);
        return "admin/actor/update-actor";
    }

    @PostMapping("/update")
    public String updateActorPage(
            @ModelAttribute("actor") ActorDTO actor,
            @RequestParam(name = "checkActive", required = false, defaultValue = "false") boolean checkActive,
            HttpSession session, Model model){
        System.out.println("LOG: trying to update actor");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());

        // check if actor status is active or not
        if (checkActive)
            actor.setIsActive(1);
        else
            actor.setIsActive(0);
        HttpEntity<?> entity = new HttpEntity<>(actor,headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(API_ACTORS,HttpMethod.PUT,entity,String.class);
            System.out.println("SUCCESS: Update actor");
        } catch (Exception e) {
            System.out.println("FAIL: Update actor - " + e.getMessage());
        }
        return "redirect:/admin/actors";
    }

}
