package com.example.cinema_client.controllers.admin;

import com.example.cinema_client.constants.API;
import com.example.cinema_client.models.DirectorDTO;
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
@RequestMapping("/admin/directors")
public class ManageDirectorsController {
    @Autowired
    private RestTemplate restTemplate;
    private static final String API_DIRECTORS = API.baseURL + "/api/admin/directors";
    public static final String API_GET_DIRECTOR = API.baseURL + "/api/admin/directors/detail";

    @GetMapping
    public String displayManageDirectorPage(HttpSession session, Model model) {
        System.out.println("LOG: accessing manage branches page");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        System.out.println("LOG: loading directors");
        ResponseEntity<DirectorDTO[]> response = restTemplate.exchange(API_DIRECTORS, HttpMethod.GET, entity, DirectorDTO[].class);
        DirectorDTO[] directors = response.getBody();
        model.addAttribute("directors", directors);
        return "admin/director/manage-director";
    }

    @GetMapping("/add")
    public String displayAddDirectorPage(HttpSession session, Model model){
        System.out.println("LOG: accessing add new director page");

        System.out.println("LOG: creating new empty director");
        model.addAttribute("director",new DirectorDTO());
        return "admin/director/update-director";
    }
    @PostMapping("/add")
    public String addDirectorPage(
            @ModelAttribute("director") DirectorDTO director,
            @RequestParam(name = "checkActive", required = false, defaultValue = "false") boolean checkActive,
            HttpSession session, Model model){
        System.out.println("LOG: trying to create new branch");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());

        //check if director status is active or not
        if(checkActive) {
            director.setIsActive(1);
        } else {director.setIsActive(0);
        }

        HttpEntity<DirectorDTO> entity = new HttpEntity<>(director, headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(API_DIRECTORS,HttpMethod.POST,entity,String.class);
            System.out.println("SUCCESS: Add new director");
        } catch (Exception e) {
            System.out.println("FAIL: Add new director - " + e.getMessage());
        }

        return "redirect:/admin/directors";
    }

    @GetMapping("/update")
    public String displayUpdateDirectorPage(
            @RequestParam Integer directorId,
            HttpSession session, Model model){
        System.out.println("LOG: accessing update director page");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO)session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        System.out.println("LOG: loading director by id " + directorId);
        // call API get selected director
        String urlTemplate = UriComponentsBuilder.fromHttpUrl(API_GET_DIRECTOR)
                .queryParam("directorId", "{directorId}")
                .encode()
                .toUriString();
        Map<String, String> listRequestParam = new HashMap<>();
        listRequestParam.put("directorId", directorId + "");

        ResponseEntity<DirectorDTO> response = restTemplate.exchange(
                urlTemplate,HttpMethod.GET,entity,DirectorDTO.class,listRequestParam);
        DirectorDTO director = response.getBody();

        model.addAttribute("director",director);
        return "admin/director/update-director";
    }

    @PostMapping("/update")
    public String updateDirectorPage(
            @ModelAttribute("director") DirectorDTO director,
            @RequestParam(name = "checkActive", required = false, defaultValue = "false") boolean checkActive,
            HttpSession session, Model model){
        System.out.println("LOG: trying to update director");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());

        //check if director status is active or not
        if (checkActive)
            director.setIsActive(1);
        else
            director.setIsActive(0);

        HttpEntity<?> entity = new HttpEntity<>(director,headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(API_DIRECTORS,HttpMethod.PUT,entity,String.class);
            System.out.println("SUCCESS: Update director");
        } catch (Exception e) {
            System.out.println("FAIL: Update director - " + e.getMessage());
        }
        return "redirect:/admin/directors";
    }

}
