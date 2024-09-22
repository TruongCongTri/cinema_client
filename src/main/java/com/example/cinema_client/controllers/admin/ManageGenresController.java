package com.example.cinema_client.controllers.admin;

import com.example.cinema_client.constants.API;
import com.example.cinema_client.models.GenreDTO;
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
@RequestMapping("/admin/genres")
public class ManageGenresController {
    @Autowired
    private RestTemplate restTemplate;
    private static final String API_GENRES = API.baseURL + "/api/admin/genres";
    public static final String API_GET_GENRE = API.baseURL + "/api/admin/genres/detail";

    @GetMapping
    public String displayManageGenrePage(HttpSession session, Model model) {
        System.out.println("LOG: accessing manage genres page");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        System.out.println("LOG: loading genres");
        ResponseEntity<GenreDTO[]> response = restTemplate.exchange(API_GENRES,HttpMethod.GET,entity,GenreDTO[].class);
        GenreDTO[] genres = response.getBody();
        model.addAttribute("genres", genres);
        return "admin/genre/manage-genre";
    }

    @GetMapping("/add")
    public String displayAddGenrePage(HttpSession session, Model model){
        System.out.println("LOG: accessing add new genre page");

        System.out.println("LOG: creating new empty genre");
        model.addAttribute("genre",new GenreDTO());
        return "admin/genre/update-genre";
    }
    @PostMapping("/add")
    public String addGenrePage(
            @ModelAttribute("genre") GenreDTO genre,
            @RequestParam(name = "checkActive", required = false, defaultValue = "false") boolean checkActive,
            HttpSession session, Model model){
        System.out.println("LOG: trying to create new genre");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());

        // check if genre status is active or not
        if(checkActive)
            genre.setIsActive(1);
        else
            genre.setIsActive(0);

        HttpEntity<GenreDTO> entity = new HttpEntity<>(genre, headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(API_GENRES,HttpMethod.POST,entity,String.class);
            System.out.println("SUCCESS: Add new genre");
        } catch (Exception e) {
            System.out.println("FAIL: Add new genre - " + e.getMessage());
        }

        return "redirect:/admin/genres";
    }

    @GetMapping("/update")
    public String displayUpdateGenrePage(
            @RequestParam Integer genreId,
            HttpSession session, Model model){
        System.out.println("LOG: accessing update genre page");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO)session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        System.out.println("LOG: loading genre by id " + genreId);
        // call API get selected genre
        String urlTemplate = UriComponentsBuilder.fromHttpUrl(API_GET_GENRE)
                .queryParam("genreId", "{genreId}")
                .encode()
                .toUriString();
        Map<String, String> listRequestParam = new HashMap<>();
        listRequestParam.put("genreId", genreId + "");

        ResponseEntity<GenreDTO> response = restTemplate.exchange(
                urlTemplate,HttpMethod.GET,entity,GenreDTO.class,listRequestParam);
        GenreDTO genre = response.getBody();

        model.addAttribute("genre",genre);
        return "admin/genre/update-genre";
    }

    @PostMapping("/update")
    public String genreMoviePage(
            @ModelAttribute("genre") GenreDTO genre,
            @RequestParam(name = "checkActive", required = false, defaultValue = "false") boolean checkActive,
            HttpSession session, Model model){
        System.out.println("LOG: trying to update genre");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());

        // check if genre status is active or not
        if (checkActive)
            genre.setIsActive(1);
        else
            genre.setIsActive(0);

        HttpEntity<?> entity = new HttpEntity<>(genre,headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(API_GENRES,HttpMethod.PUT,entity,String.class);
            System.out.println("SUCCESS: Update genre");
        } catch (Exception e) {
            System.out.println("FAIL: Update genre - " + e.getMessage());
        }

        return "redirect:/admin/genres";
    }

}
