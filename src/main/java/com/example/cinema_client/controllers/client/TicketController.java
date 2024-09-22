package com.example.cinema_client.controllers.client;

import com.example.cinema_client.constants.API;
import com.example.cinema_client.models.BillDTO;
import com.example.cinema_client.models.JwtResponseDTO;
import com.example.cinema_client.models.TicketDTO;
import com.example.cinema_client.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tritcse00526x
 */
@Controller
@RequestMapping("/ticket")
public class TicketController {
    @Autowired
    private RestTemplate restTemplate;
    public static String API_BILL_DETAIL = API.baseURL+"/api/bills/detail";
    public static String API_TICKET_DETAIL = API.baseURL+"/api/tickets/detail";

    /**TODO: TICKET page*/
    // access ticket detail page
    @GetMapping
    public String displayTicketDetailPage(
            @RequestParam Integer ticketId,
            Model model, HttpServletRequest request){
        System.out.println("LOG: accessing ticket detail page");
        HttpSession session = request.getSession();
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        // call API get selected ticket detail
        String urlTemplate = UriComponentsBuilder.fromHttpUrl(API_TICKET_DETAIL)
                .queryParam("ticketId", "{ticketId}")
                .encode()
                .toUriString();
        Map<String, Integer> params = new HashMap<>();
        params.put("ticketId", ticketId);
        ResponseEntity<TicketDTO> response = restTemplate
                .exchange(urlTemplate,HttpMethod.GET,entity,TicketDTO.class,params);
        TicketDTO ticket = response.getBody();

        model.addAttribute("ticket",ticket);
        return "client/ticket";
    }
}
