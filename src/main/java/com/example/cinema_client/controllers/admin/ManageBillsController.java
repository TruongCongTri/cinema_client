package com.example.cinema_client.controllers.admin;

import javax.servlet.http.HttpSession;

import com.example.cinema_client.constants.API;
import com.example.cinema_client.models.BillDTO;
import com.example.cinema_client.models.JwtResponseDTO;
import com.example.cinema_client.models.TicketDTO;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tritcse00526x
 */
@Controller
@RequestMapping("/admin/bills")
public class ManageBillsController {
    @Autowired
    private RestTemplate restTemplate;

    private static final String API_GET_BILLS = API.baseURL + "/api/admin/bills";
    private static final String API_GET_TICKETS = API.baseURL + "/api/admin/tickets";

    @GetMapping
    public String displayManageBillPage(HttpSession session, Model model) {
        System.out.println("LOG: accessing manage bills page");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        // call API get all bills
        ResponseEntity<BillDTO[]> response = restTemplate.exchange(API_GET_BILLS,HttpMethod.GET,entity,BillDTO[].class);
        BillDTO[] bills = response.getBody();
        model.addAttribute("bills", bills);
        return "admin/bill/manage-bill";
    }

    @GetMapping("/view-ticket")
    public String displayTicket(@RequestParam Integer billId , HttpSession session, Model model){
        System.out.println("LOG: accessing tickets page of a bill");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO)session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        //call API get all tickets by selected bill
        String urlTemplate = UriComponentsBuilder.fromHttpUrl(API_GET_TICKETS)
                .queryParam("billId", "{billId}")
                .encode()
                .toUriString();
        Map<String,Integer> listRequestParam = new HashMap<>();
        listRequestParam.put("billId", billId);
        TicketDTO[] tickets = null;
        try {
            ResponseEntity<TicketDTO[]> responseTicket = restTemplate
                    .exchange(urlTemplate,HttpMethod.POST,entity,TicketDTO[].class,listRequestParam);
            tickets = responseTicket.getBody();
        } catch (Exception e) {
            System.out.println(e);
        }
        model.addAttribute("tickets", tickets);

        return "admin/bill/view-ticket";
    }
}
