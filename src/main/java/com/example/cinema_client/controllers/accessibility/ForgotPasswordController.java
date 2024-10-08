package com.example.cinema_client.controllers.accessibility;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.cinema_client.constants.API;

/**
 * @author tritcse00526x
 */
@Controller
@RequestMapping("/forgot-password")
public class ForgotPasswordController {
    @Autowired
    private RestTemplate restTemplate;

    public static String API_FORGOT_PASSWORD = API.baseURL + "/api/forgot-password";
    public static String API_VALIDATE_TOKEN_PASSWORD = API.baseURL + "/api/forgot-password/validate-token";

    /**TODO: FORGOT PASSWORD page*/
    // access page
    @GetMapping
    public String displayForgotPasswordPage(HttpServletRequest request, Model model) {
        return "accessibility/forgot-password";
    }

    // verify email
    @PostMapping
    public String sendEmailToResetPassword(@RequestParam String email, Model model, HttpServletRequest request){
        try {
            ResponseEntity<String> response = restTemplate
                    .getForEntity(API_FORGOT_PASSWORD + "?email=" + email,String.class);
            model.addAttribute("status", 1);
            model.addAttribute("message", response.getBody());
        } catch (Exception e) {
            model.addAttribute("status", 0);
            model.addAttribute("message", e.getMessage());
        }
        return "accessibility/forgot-password";
    }

    /**TODO: RESET PASSWORD page*/
    // access page via link
    @GetMapping("/reset-password")
    public String displayResetPasswordPage(
            @RequestParam String token,
            @RequestParam String email,
            Model model, HttpServletRequest request){
        try { //if reset password token is correct
            ResponseEntity<String> response = restTemplate
                    .getForEntity(API_VALIDATE_TOKEN_PASSWORD + "?token=" + token,String.class);
            model.addAttribute("status", 1);
            model.addAttribute("message", response.getBody());
        } catch (Exception e) {
            System.out.println(e);
            model.addAttribute("status", 0);
            model.addAttribute("message", e.getMessage());
            return "accessibility/forgot-password";
        }
        model.addAttribute("token",token);
        model.addAttribute("email",email);

        return "accessibility/reset-password";
    }

    // verify password
    @PostMapping("/reset-password")
    public String validatePassword(
            @RequestParam String token,
            @RequestParam String password,
            Model model, HttpServletRequest request){
        //call API set up new password
        String urlTemplate = UriComponentsBuilder.fromHttpUrl(API_FORGOT_PASSWORD)
                .queryParam("token", "{token}")
                .queryParam("password", "{password}")
                .encode()
                .toUriString();
        Map<String, String> params = new HashMap<>();
        params.put("token", token);
        params.put("password", password);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(urlTemplate, null, String.class, params);
            model.addAttribute("statusResetPassword", true);
            model.addAttribute("messageStatus", response.getBody());
        } catch (Exception e) {
            //.addAttribute("status", 0);
            model.addAttribute("messageStatus", e.getMessage());
            model.addAttribute("statusResetPassword",false);
        }

        return "accessibility/reset-password";
    }

}
