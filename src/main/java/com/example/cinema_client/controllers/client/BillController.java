package com.example.cinema_client.controllers.client;

import com.example.cinema_client.constants.API;
import com.example.cinema_client.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author tritcse00526xd
 */
@Controller
@RequestMapping("/webhook/bill")
public class BillController {
    @Autowired
    private RestTemplate restTemplate;
    public static String API_CREATE_BILL = API.baseURL + "/api/bills/create-new-bill";

    /**TODO: create bill*/
    @GetMapping
    public String createBill(
            @RequestParam(required = true) String orderId,
            @RequestParam(required = false) String requestId,
            @RequestParam(required = true) Integer resultCode,
            @RequestParam(required = true) Integer amount,
            HttpServletRequest request, Model model) {
        System.out.println("LOG: trying to create new bill");
        HttpSession session = request.getSession();
        if (resultCode == 0) {
            System.out.println("LOG: creating new bill - " + orderId);
            // attach the JWT access token to the header to send it with the request
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());

            String[] objects = orderId.split("P");

            // pack user id, schedule id and list of seat id to request then send
            BookingRequestDTO body = new BookingRequestDTO();
            body.setUserId(jwtResponseDTO.getId());

            body.setScheduleId(Integer.parseInt(objects[0]));

            // change from seat id list to list of Integer
            List<Integer> listSeatIds = Arrays.stream(objects[1].split("T")[0].split("-")).map(Integer::parseInt)
                    .collect(Collectors.toList());
            body.setSeatIds(listSeatIds);
            body.setAmount(amount);
            body.setOrderId(orderId);
            model.addAttribute("user", new User());

            String message = "Ghế đã được đặt, vui lòng chọn lại ghế khác!";
            try {
                HttpEntity<?> entity = new HttpEntity<>(body, headers);
                ResponseEntity<String> response = restTemplate.exchange(API_CREATE_BILL, HttpMethod.POST, entity,
                        String.class);
                System.out.println("SUCCESS: Create new bill");
            } catch (HttpClientErrorException ex) { // if seats were booked
                // then return to booking page
                ex.printStackTrace();
                System.out.println("FAIL: Create new bill - unavailable seats");
                message = ex.getResponseBodyAsString();
                session.setAttribute("bookedError", message);
                return "redirect:/booking/"+Integer.parseInt(objects[0]);
            }
        }
        return "redirect:/account/profile";
    }

}
