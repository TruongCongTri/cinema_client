package com.example.cinema_client.controllers.admin;

import com.example.cinema_client.constants.API;
import com.example.cinema_client.constants.ValidateRandomString;
import com.example.cinema_client.models.JwtResponseDTO;
import com.example.cinema_client.models.TicketDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;

/**
 * @author tritcse00526x
 */
@Controller
@RequestMapping("/admin/tickets")
public class ManageTicketsController {
    @Autowired
    private RestTemplate restTemplate;

    private static final String API_GET_TICKET = API.baseURL + "/api/admin/tickets/";
    private static final String API_SEARCH_TICKET = API.baseURL + "/api/admin/tickets/search";

    @GetMapping
    public String displayTicketPage(
            @RequestParam(name = "code", required = false) String qrCode,
            HttpSession session, Model model) {
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        //check if ticket code is in the correct format
        if (qrCode != null) {
            System.out.println("LOG: Load check-in ticket page with ticket code");
            if (ValidateRandomString.validateUUIDV4(qrCode)) {
                try {
                    // Make API call to get ticket by code
                    ResponseEntity<TicketDTO> response = restTemplate.exchange(API_GET_TICKET + qrCode,HttpMethod.GET,entity,TicketDTO.class);
                    if (response.getStatusCode() == HttpStatus.OK) {
                        TicketDTO ticket = response.getBody();
                        model.addAttribute("ticket", ticket);
                        if (ticket.isCheck()) {
                            model.addAttribute("status",false);
                            model.addAttribute("messageStatus","Vé đã được sử dụng!");
                            System.out.println("FAIL: Check-in - used ticket code");
                        }
                    } else {
                        model.addAttribute("error", "Unexpected response status: " + response.getStatusCode());
                    }
                } catch (HttpClientErrorException.NotFound e) {
                    // Handle 404 error (ticket not found)
                    //model.addAttribute("error", "Ticket not found for code: " + qrCode);

                    model.addAttribute("status",false);
                    model.addAttribute("messageStatus","Không tìm được vé với mã " + qrCode);
                    System.out.println("FAIL: Check-in - error " + e.getMessage());
                } catch (HttpClientErrorException e) {
                    // Handle other HTTP errors
                    //model.addAttribute("error", "Error retrieving ticket: " + e.getStatusCode());
                    model.addAttribute("status",false);
                    model.addAttribute("messageStatus","Không tìm được vé với mã " + qrCode);
                    System.out.println("FAIL: Check-in - error " + e.getMessage());
                } catch (Exception e) {
                    // Handle any other exceptions
                    //model.addAttribute("error", "An unexpected error occurred: " + e.getMessage());
                    model.addAttribute("status",false);
                    model.addAttribute("messageStatus","Không tìm được vé với mã " + qrCode);
                    System.out.println("FAIL: Check-in - error " + e.getMessage());
                }
            } else {
                model.addAttribute("status",false);
                model.addAttribute("messageStatus","Mã vé không hợp lệ!");
                System.out.println("FAIL: Check-in - wrong ticket code");
            }
        } else {
            System.out.println("LOG: Load check-in ticket page without ticket code");
        }
        return "admin/ticket/checkin-ticket";
    }

    /*@GetMapping
    public String displayManageTicketPage(HttpSession session, Model model) {
        System.out.println("LOG: accessing check-in page");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        return "admin/bill/manage-ticket";
    }

    @GetMapping("/search")
    public String displayCheckinTicketPage(
            @RequestParam String qrCode,
            HttpSession session, Model model) {
        System.out.println("LOG: searching for ticket by qrcode " + qrCode);
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        //check if ticket code is in the correct format
        boolean isValid = ValidateRandomString.validateUUIDV4(qrCode);
        if (isValid) {
            // call API get selected ticket
            String urlTemplate = UriComponentsBuilder.fromHttpUrl(API_SEARCH_TICKET)
                    .queryParam("qrCode", "{qrCode}")
                    .encode()
                    .toUriString();
            Map<String, String> listRequestParam = new HashMap<>();
            listRequestParam.put("qrCode", qrCode + "");

            ResponseEntity<TicketDTO> response = restTemplate.exchange(
                    urlTemplate, HttpMethod.GET, entity, TicketDTO.class, listRequestParam);
            TicketDTO ticket = response.getBody();

            boolean isCheckIn = ticket.isCheck();
            if (isCheckIn) {
                model.addAttribute("status",false);
                model.addAttribute("messageStatus","Vé đã được sử dụng!");
                System.out.println("FAIL: Check-in - used ticket");
                return "admin/bill/manage-ticket";
            } else {
                model.addAttribute("ticket", ticket);
                System.out.println("SUCCESS: Check-in");
                return "admin/bill/checkin-ticket";
            }
        } else {
            model.addAttribute("status",false);
            model.addAttribute("messageStatus","Mã vé không hợp lệ!");
            System.out.println("FAIL: Check-in - wrong ticket code");
            return "admin/bill/manage-ticket";
        }
    }

    @PostMapping("/update")
    public String updateTicketPage(
            @ModelAttribute("ticket") TicketDTO ticket,
            HttpSession session, Model model){
        System.out.println("LOG: trying to update ticket check-in status");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());

        //check if ticket status is checked-in or not
        ticket.setCheck(true);

        HttpEntity<?> entity = new HttpEntity<>(ticket,headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(API_GET_TICKET,HttpMethod.PUT,entity,String.class);
            model.addAttribute("status",true);
            model.addAttribute("messageStatus","Soát vé thành công!");
            System.out.println("SUCCESS: Update ticket check-in status");
        } catch (Exception e) {
            model.addAttribute("status",false);
            model.addAttribute("messageStatus","Có lỗi xảy ra, Soát vé thất bại!");
            System.out.println("FAIL: Update ticket check-in status - " + e.getMessage());
        }
        return "admin/bill/manage-ticket";
    }*/
}
