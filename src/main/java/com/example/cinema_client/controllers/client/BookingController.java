package com.example.cinema_client.controllers.client;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.cinema_client.constants.API;
import com.example.cinema_client.models.JwtResponseDTO;
import com.example.cinema_client.models.ScheduleDTO;
import com.example.cinema_client.models.SeatDTO;


/**
 * @author tritcse00526x
 */
@Controller
@RequestMapping("/booking")
public class BookingController {
    @Autowired
    private RestTemplate restTemplate;

    @Qualifier("bookingSeatStore")
    @Autowired
    Map<Integer, Map<Integer, LocalDateTime>> bookingSeatStore;

    public static String API_GET_SCHEDULE = API.baseURL+"/api/schedule";
    public static String API_GET_SEATS = API.baseURL+"/api/seats";


    /**TODO: CHOOSE SEATS page*/
    // access room page with seats layout
    @GetMapping("/{scheduleId}")
    public String displayRoomAndSeat(
            @PathVariable("scheduleId") Integer scheduleId,
            HttpServletRequest request, Model model) {
        System.out.println("LOG: accessing select seats page");
        HttpSession session = request.getSession();
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO)session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION,"Bearer " + jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        // call API get selected schedule
        String urlTemplate = UriComponentsBuilder.fromHttpUrl(API_GET_SCHEDULE)
                .queryParam("scheduleId", "{scheduleId}")
                .encode()
                .toUriString();
        Map<String,String> listRequestParam = new HashMap<>();
        listRequestParam.put("scheduleId", scheduleId.toString());

        ResponseEntity<ScheduleDTO> response = restTemplate
                .exchange(urlTemplate, HttpMethod.GET,entity,ScheduleDTO.class,listRequestParam);
        model.addAttribute("schedule",response.getBody());

        // call API get seats layout within the room of the selected schedule
        urlTemplate = UriComponentsBuilder.fromHttpUrl(API_GET_SEATS)
                .queryParam("scheduleId", "{scheduleId}")
                .queryParam("userId", "{userId}")
                .encode()
                .toUriString();
        listRequestParam.put("userId", jwtResponseDTO.getId().toString());
        ResponseEntity<SeatDTO[]> responseSeats = restTemplate
                .exchange(urlTemplate, HttpMethod.GET,entity,SeatDTO[].class,listRequestParam);

        // check if selected seats are currently withhold by other
        // who is currently in the payment process
        LocalDateTime now = LocalDateTime.now();
        Map<Integer, LocalDateTime> checkedSeats = bookingSeatStore.getOrDefault(scheduleId, new HashMap<Integer, LocalDateTime>());
        model.addAttribute("seats", Arrays
                .stream(responseSeats.getBody())
                .map(seat->{seat.setIsOccupied((checkedSeats.containsKey(seat.getId())
                        && checkedSeats.get(seat.getId()).compareTo(now)>0) ? 1 : seat.getIsOccupied());
                    return seat;
                })
                .collect(Collectors.toList()));

        return "client/booking-seat";
    }

}