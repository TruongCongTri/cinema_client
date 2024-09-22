package com.example.cinema_client.controllers.client;

import com.example.cinema_client.constants.API;
import com.example.cinema_client.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tritcse00526x
 */
@Controller
@RequestMapping("/account")
public class AccountController {
    @Autowired
    private RestTemplate restTemplate;
    public static String API_CHANGE_PASSWORD = API.baseURL+"/api/account/change-password";
    public static String API_UPDATE_INFO = API.baseURL+"/api/account/update-info";
    public static String API_GET_PROFILE = API.baseURL+"/api/account";
    public static String API_GET_TICKETS = API.baseURL+"/api/tickets";


    /**TODO: PROFILE page*/
    // access profile page
    @GetMapping("/profile")
    public String displayProfilePage(Model model, HttpServletRequest request){
        System.out.println("LOG: accessing profile page");
        HttpSession session = request.getSession();
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        // call API get tickets by user
        String urlTemplate = UriComponentsBuilder.fromHttpUrl(API_GET_TICKETS)
                .queryParam("userId", "{userId}")
                .encode()
                .toUriString();
        Map<String,String> listRequestParam = new HashMap<>();
        listRequestParam.put("userId", jwtResponseDTO.getId()+"");

        ResponseEntity<TicketDTO[]> response = restTemplate
                .exchange(urlTemplate,HttpMethod.GET,entity,TicketDTO[].class,listRequestParam);
        TicketDTO[] tickets = response.getBody();

        // call API get user profile
        urlTemplate = UriComponentsBuilder.fromHttpUrl(API_GET_PROFILE)
                .queryParam("userId", "{userId}")
                .encode()
                .toUriString();

        ResponseEntity<User> responseUser = restTemplate
                .exchange(urlTemplate,HttpMethod.GET,entity,User.class,listRequestParam);

        User user = responseUser.getBody();
        model.addAttribute("tickets",tickets);
        model.addAttribute("user",user);

        return "client/profile";
    }

    // update profile
    @PostMapping("/update-info")
    public String updateProfilePage(
            @Valid @ModelAttribute("user") User user,
            BindingResult bindingResult,
            Model model, HttpServletRequest request) {
        System.out.println("LOG: trying to update profile");
        HttpSession session = request.getSession();
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO)session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION,"Bearer "+jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        HttpEntity<User> entityUser = new HttpEntity<>(user,headers);
        if (user.getPhone().length() >= 9 && user.getPhone().length() <= 10 && !(bindingResult.hasErrors())) {
            // call API send data to update user
            if(!(bindingResult.hasErrors())) {
                ResponseEntity<String> response = restTemplate
                        .exchange(API_UPDATE_INFO + "?userId=" + jwtResponseDTO.getId().toString(),HttpMethod.POST,entityUser,String.class);
                model.addAttribute("statusChangedPassword",true);
                model.addAttribute("messageStatus","Thay đổi thông tin tài khoản thành công!");
                System.out.println("SUCCESS: Update profile");
            }
        } else {
            model.addAttribute("statusChangedPassword",false);
            model.addAttribute("messageStatus","Số điện thoại không hợp lệ!");
            System.out.println("FAIL: Update profile");
        }

        // call API get tickets of this user
        String urlTemplate = UriComponentsBuilder.fromHttpUrl(API_GET_TICKETS)
                .queryParam("userId", "{userId}")
                .encode()
                .toUriString();
        Map<String,String> listRequestParam = new HashMap<>();
        listRequestParam.put("userId", jwtResponseDTO.getId()+"");

        ResponseEntity<TicketDTO[]> responseTicket = restTemplate.exchange(urlTemplate, HttpMethod.GET,entity,TicketDTO[].class
                ,listRequestParam);
        TicketDTO[] listTickets = responseTicket.getBody();

        // call API get user profile
        urlTemplate = UriComponentsBuilder.fromHttpUrl(API_GET_PROFILE)
                .queryParam("userId", "{userId}")
                .encode()
                .toUriString();

        ResponseEntity<User> responseUser = restTemplate.exchange(urlTemplate, HttpMethod.GET,entity,User.class
                ,listRequestParam);
        User userInf = responseUser.getBody();
        model.addAttribute("tickets",listTickets);
        model.addAttribute("user",userInf);

        jwtResponseDTO.setName(user.getFullName());
        session.setAttribute("jwtResponse", jwtResponseDTO);

        return "client/profile";
    }

    // change password
    @PostMapping("/change-password")
    public String updatePassWord(
            @RequestParam String newPassword,
            @RequestParam String validNewPassword,
            Model model, HttpServletRequest request){
        System.out.println("LOG: trying to change password");
        HttpSession session = request.getSession();
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO)session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION,"Bearer "+jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        if(newPassword.equals(validNewPassword) && newPassword.length() >= 6 ) { // if password is valid
            Map<String,String> listRequestParam = new HashMap<>();
            listRequestParam.put("userId", jwtResponseDTO.getId()+"");
            listRequestParam.put("newPassword", newPassword);

            // call API sent data to update profile
            String urlTemplate = UriComponentsBuilder.fromHttpUrl(API_CHANGE_PASSWORD)
                    .queryParam("userId", "{userId}")
                    .queryParam("newPassword","{newPassword}")
                    .encode()
                    .toUriString();

            ResponseEntity<String> response = restTemplate
                    .exchange(urlTemplate,HttpMethod.POST,entity,String.class,listRequestParam);

            model.addAttribute("statusChangedPassword",true);
            model.addAttribute("messageStatus","Thay đổi mật khẩu thành công!");
            System.out.println("SUCCESS: Change password");
        } else { // if password is in-valid
            if(newPassword.length() < 6) {
                model.addAttribute("messageStatus", "Mật khẩu phải có ít nhất 6 ký tự!");
                System.out.println("FAIL: Change password - incorrect password");
            } else {
                model.addAttribute("messageStatus","Mật khẩu nhập không trùng khớp!");
                System.out.println("FAIL: Change password - incorrect re-password");
            }
            model.addAttribute("statusChangedPassword",false);
        }

        // call API get tickets of this user
        String urlTemplate = UriComponentsBuilder.fromHttpUrl(API_GET_TICKETS)
                .queryParam("userId", "{userId}")
                .encode()
                .toUriString();
        Map<String,String> listRequestParam = new HashMap<>();
        listRequestParam.put("userId", jwtResponseDTO.getId()+"");

        ResponseEntity<TicketDTO[]> response = restTemplate.exchange(urlTemplate, HttpMethod.GET,entity,TicketDTO[].class
                ,listRequestParam);
        TicketDTO[] listTickets = response.getBody();

        // call API get user profile
        urlTemplate = UriComponentsBuilder.fromHttpUrl(API_GET_PROFILE)
                .queryParam("userId", "{userId}")
                .encode()
                .toUriString();

        ResponseEntity<User> responseUser = restTemplate.exchange(urlTemplate, HttpMethod.GET,entity,User.class
                ,listRequestParam);

        User userInf = responseUser.getBody();
        model.addAttribute("tickets",listTickets);
        model.addAttribute("user",userInf);

        return "client/profile";
    }

}
