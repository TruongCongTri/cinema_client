package com.example.cinema_client.controllers.admin;

import com.example.cinema_client.constants.API;
import com.example.cinema_client.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * @author tritcse00526x
 */
@Controller
@RequestMapping("/admin/movies")
public class ManageMoviesController {
    private static final String API_GET_MOVIES = API.baseURL + "/api/admin/movies";

    private static final String API_GET_ACTIVE_DIRECTORS = API.baseURL + "/api/admin/directors/active";
    private static final String API_GET_ACTIVE_ACTORS = API.baseURL + "/api/admin/actors/active";
    private static final String API_GET_ACTIVE_GENRES = API.baseURL + "/api/admin/genres/active";
    private static final String API_GET_ACTIVE_CERTIFICATIONS = API.baseURL + "/api/admin/certifications/active";

    private static final String API_GET_MOVIE_DETAIL = API.baseURL + "/api/admin/movies/detail";
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping
    public String displayManageMoviePage(HttpSession session, Model model) {
        System.out.println("LOG: accessing manage movies page");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        System.out.println("LOG: loading movies");
        // call API get all movies
        ResponseEntity<MovieDTO[]> response = restTemplate.exchange(API_GET_MOVIES,HttpMethod.GET,entity,MovieDTO[].class);
        MovieDTO[] movies = response.getBody();
        model.addAttribute("movies", movies);
        return "admin/movie/manage-movie";
    }

    @GetMapping("/add")
    public String displayAddMoviePage(HttpSession session, Model model) {
        System.out.println("LOG: accessing add new movie page");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        System.out.println("LOG: loading active directors");
        // call API get all directors
        ResponseEntity<DirectorDTO[]> response = restTemplate.exchange(API_GET_ACTIVE_DIRECTORS,HttpMethod.GET,entity,DirectorDTO[].class);
        DirectorDTO[] directors = response.getBody();
        System.out.println("LOG: loading active actors");
        // call API get all actors
        ResponseEntity<ActorDTO[]> responseActors = restTemplate.exchange(API_GET_ACTIVE_ACTORS,HttpMethod.GET,entity,ActorDTO[].class);
        ActorDTO[] actors = responseActors.getBody();
        System.out.println("LOG: loading active certifications");
        // call API get all certificates
        ResponseEntity<RatedDTO[]> responseCertificates = restTemplate.exchange(API_GET_ACTIVE_CERTIFICATIONS,HttpMethod.GET,entity,RatedDTO[].class);
        RatedDTO[] certificates = responseCertificates.getBody();
        System.out.println("LOG: loading active genres");
        // call API get all genres
        ResponseEntity<GenreDTO[]> responseGenres = restTemplate.exchange(API_GET_ACTIVE_GENRES,HttpMethod.GET,entity,GenreDTO[].class);
        GenreDTO[] genres = responseGenres.getBody();

        model.addAttribute("directors", directors);
        model.addAttribute("actors", actors);
        model.addAttribute("certificates", certificates);
        model.addAttribute("genres", genres);

        System.out.println("LOG: creating new empty movie");
        model.addAttribute("movie", new MovieDTO());
        return "admin/movie/update-movie";
    }

    @PostMapping("/add")
    public String addMoviePage(
            @RequestBody @ModelAttribute("movie") MovieDTO movie,
            @RequestParam(name = "checkShowing", required = false, defaultValue = "false") boolean checkShowing,
            @RequestParam(name = "checkActive", required = false, defaultValue = "false") boolean checkActive,
            @RequestParam(name = "director") List<Integer> director,
            @RequestParam(name = "actor") List<Integer> actor,
            @RequestParam(name = "genre") List<Integer> category,
            HttpSession session, Model model) {
        System.out.println("LOG: trying to create new movie");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());

        //check if new movie is currently showing or will be shown
        if (checkShowing)
            movie.setIsShowing(1);
        else
            movie.setIsShowing(0);
        //check if new movie is active or not
        if (checkActive)
            movie.setIsActive(1);
        else
            movie.setIsActive(0);

        try {
            Set<DirectorDTO> dirs = new HashSet<>();
            if (director != null) {
                for (Integer d : director) {
                    DirectorDTO dir = new DirectorDTO();
                    dir.setId(d);
                    dirs.add(dir);
                }
            }
            movie.setDirectors(dirs);
            Set<ActorDTO> actors = new HashSet<>();
            if (actor != null) {
                for (Integer a : actor) {
                    ActorDTO act = new ActorDTO();
                    act.setId(a);
                    actors.add(act);
                }
            }
            movie.setActors(actors);
            Set<GenreDTO> genres = new HashSet<>();
            if (category != null) {
                for (Integer c : category) {
                    GenreDTO gen = new GenreDTO();
                    gen.setId(c);
                    genres.add(gen);
                }
            }
            movie.setCategories(genres);
            HttpEntity<MovieDTO> entity = new HttpEntity<>(movie,headers);
            ResponseEntity<String> response = restTemplate.exchange(API_GET_MOVIES,HttpMethod.POST,entity,String.class);
            System.out.println("SUCCESS: Add new movie");
        } catch (Exception e) {
            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<DirectorDTO[]> responseDirs = restTemplate.exchange(API_GET_ACTIVE_DIRECTORS,HttpMethod.GET,entity,DirectorDTO[].class);
            DirectorDTO[] dirs = responseDirs.getBody();
            ResponseEntity<ActorDTO[]> responseActors = restTemplate.exchange(API_GET_ACTIVE_ACTORS,HttpMethod.GET,entity,ActorDTO[].class);
            ActorDTO[] actors = responseActors.getBody();
            ResponseEntity<GenreDTO[]> responseGenres = restTemplate.exchange(API_GET_ACTIVE_GENRES,HttpMethod.GET,entity,GenreDTO[].class);
            GenreDTO[] genres = responseGenres.getBody();

            model.addAttribute("directors", dirs);
            model.addAttribute("actors", actors);
            model.addAttribute("genres", genres);
            model.addAttribute("error", e.getMessage());
            System.out.println("FAIL: Add new movie - " + e.getMessage());
            return "admin/movie/update-movie";
        }
        return "redirect:/admin/movies";
    }

    @GetMapping("/update")
    public String displayUpdateSchedulePage(
            @RequestParam Integer movieId,
            HttpSession session, Model model) {
        System.out.println("LOG: accessing update movie page");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        System.out.println("LOG: loading movie by id " + movieId);
        // call API get selected movie detail
        String urlTemplate = UriComponentsBuilder.fromHttpUrl(API_GET_MOVIE_DETAIL)
                .queryParam("movieId", "{movieId}")
                .encode()
                .toUriString();
        Map<String, String> listRequestParam = new HashMap<>();
        listRequestParam.put("movieId", movieId + "");

        ResponseEntity<MovieDTO> response = restTemplate.exchange(
                urlTemplate,HttpMethod.GET,entity,MovieDTO.class,listRequestParam);
        MovieDTO movie = response.getBody();

        System.out.println("LOG: loading active directors");
        ResponseEntity<DirectorDTO[]> responseDir = restTemplate.exchange(API_GET_ACTIVE_DIRECTORS,HttpMethod.GET,entity,DirectorDTO[].class);
        DirectorDTO[] directors = responseDir.getBody();
        System.out.println("LOG: loading active actors");
        ResponseEntity<ActorDTO[]> responseActors = restTemplate.exchange(API_GET_ACTIVE_ACTORS,HttpMethod.GET,entity,ActorDTO[].class);
        ActorDTO[] actors = responseActors.getBody();
        System.out.println("LOG: loading active certifications");
        ResponseEntity<RatedDTO[]> responseCertificates = restTemplate.exchange(API_GET_ACTIVE_CERTIFICATIONS,HttpMethod.GET,entity,RatedDTO[].class);
        RatedDTO[] certificates = responseCertificates.getBody();
        System.out.println("LOG: loading active genres");
        ResponseEntity<GenreDTO[]> responseGenres = restTemplate.exchange(API_GET_ACTIVE_GENRES,HttpMethod.GET,entity,GenreDTO[].class);
        GenreDTO[] genres = responseGenres.getBody();

        model.addAttribute("directors", directors);
        model.addAttribute("actors", actors);
        model.addAttribute("certificates", certificates);
        model.addAttribute("genres", genres);
        model.addAttribute("movie", movie);
        return "admin/movie/update-movie";
    }

    @PostMapping("/update")
    public String updateMoviePage(
            @ModelAttribute("movie") MovieDTO movie,
            @RequestParam(name = "checkShowing", required = false, defaultValue = "false") boolean checkShowing,
            @RequestParam(name = "checkActive", required = false, defaultValue = "false") boolean checkActive,
            @RequestParam(name = "director") List<Integer> director,
            @RequestParam(name = "actor") List<Integer> actor,
            @RequestParam(name = "genre") List<Integer> genre,
            HttpSession session, Model model) {
        System.out.println("LOG: trying to update movie");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());

        //check if movie is currently showing or will be shown
        if (checkShowing)
            movie.setIsShowing(1);
        else
            movie.setIsShowing(0);
        //check if movie is active or not
        if (checkActive)
            movie.setIsActive(1);
        else
            movie.setIsActive(0);

        try {
            Set<DirectorDTO> dirs = new HashSet<>();
            if (director != null) {
                for (Integer d : director) {
                    DirectorDTO dir = new DirectorDTO();
                    dir.setId(d);
                    dirs.add(dir);
                }
            }
            Set<ActorDTO> actors = new HashSet<>();
            if (actor != null) {
                for (Integer a : actor) {
                    ActorDTO act = new ActorDTO();
                    act.setId(a);
                    actors.add(act);
                }
            }
            Set<GenreDTO> genres = new HashSet<>();
            if (genre != null) {
                for (Integer c : genre) {
                    GenreDTO gen = new GenreDTO();
                    gen.setId(c);
                    genres.add(gen);
                }
            }
            movie.setDirectors(dirs);
            movie.setActors(actors);
            movie.setCategories(genres);

            HttpEntity<MovieDTO> entity = new HttpEntity<>(movie,headers);
            ResponseEntity<String> response = restTemplate.exchange(API_GET_MOVIES,HttpMethod.PUT,entity,String.class);
            System.out.println("SUCCESS: Update movie");
        } catch (Exception e) {
            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<DirectorDTO[]> responseDirs = restTemplate.exchange(API_GET_ACTIVE_DIRECTORS,HttpMethod.GET,entity,DirectorDTO[].class);
            DirectorDTO[] dirs = responseDirs.getBody();
            model.addAttribute("directors", dirs);
            ResponseEntity<ActorDTO[]> responseActors = restTemplate.exchange(API_GET_ACTIVE_ACTORS,HttpMethod.GET,entity,ActorDTO[].class);
            ActorDTO[] actors = responseActors.getBody();
            model.addAttribute("actors", actors);
            ResponseEntity<GenreDTO[]> responseGenres = restTemplate.exchange(API_GET_ACTIVE_GENRES,HttpMethod.GET,entity,GenreDTO[].class);
            GenreDTO[] genres = responseGenres.getBody();
            model.addAttribute("genres", genres);
            model.addAttribute("error", e.getMessage());
            System.out.println("FAIL: Update movie - " + e.getMessage());
            return "admin/movie/update-movie";
        }
        return "redirect:/admin/movies";
    }
}
