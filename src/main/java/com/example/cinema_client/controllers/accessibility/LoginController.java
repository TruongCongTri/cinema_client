package com.example.cinema_client.controllers.accessibility;

import com.example.cinema_client.constants.API;
import com.example.cinema_client.models.JwtResponseDTO;
import com.example.cinema_client.models.Role;
import com.example.cinema_client.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author tritcse00526x
 */
@Controller
@RequestMapping
public class LoginController {
    @Autowired
    private RestTemplate restTemplate;
    public static String API_LOGIN = API.baseURL + "/login";
    public static String API_REGISTER = API.baseURL + "/register";

    /**TODO: LOGIN page*/
    // access login page
    @GetMapping("/login")
    public String displayLoginPage(HttpSession session, Model model) {
        System.out.println("LOG: accessing login page");
        model.addAttribute("user", new User());
        session.removeAttribute("jwtResponse");
        session.removeAttribute("admin");
        return "accessibility/login";
    }
    // verify login input
    @PostMapping("/login")
    public String login(Model model, HttpServletRequest request, HttpSession session) {
        System.out.println("LOG: verifying login input");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        User user = new User();
        user.setUsername(request.getParameter("username"));
        user.setPassword(request.getParameter("password"));
        JwtResponseDTO jwt;
        try {
            HttpEntity<User> httpEntity = new HttpEntity<>(user, httpHeaders);
            ResponseEntity<JwtResponseDTO> jwtResponse = restTemplate
                    .exchange(API_LOGIN,HttpMethod.POST,httpEntity,JwtResponseDTO.class);
            jwt = jwtResponse.getBody();
            session.setAttribute("jwtResponse", (JwtResponseDTO) jwtResponse.getBody());
        } catch (HttpClientErrorException e){
            model.addAttribute("loginError", e.getResponseBodyAsString());
            model.addAttribute("hasErrors", true);
            model.addAttribute("user", user);
            System.out.println(e);
            return "accessibility/login";
        }
        for(Role role: jwt.getRoles()) {
            if(role.equals("ROLE_ADMIN")) {
                session.setAttribute("admin", true);
                return "redirect:/admin";
            }
        }
        return "redirect:/";
    }

    /**TODO: REGISTER page*/
    // access register page
    @GetMapping("/register")
    public String displayRegisterPage(HttpSession session, Model model) {
        System.out.println("LOG: accessing register page");
        model.addAttribute("user", new User());
        session.removeAttribute("jwtResponse");
        session.removeAttribute("admin");
        return "accessibility/register";
    }
    // verify register input
    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("user") User user,
            BindingResult bindingResult,
            Model model, HttpServletRequest request) {
        System.out.println("LOG: verifying register input");
        if (bindingResult.hasErrors()) {
            return "accessibility/register";
        } else {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            Set<Role> roles = new HashSet<>();
            Role roleClient = new Role();
            roleClient.setName("ROLE_CLIENT");
            roles.add(roleClient);
            user.setRoles(roles);
            try {
                HttpEntity<User> httpEntity = new HttpEntity<>(user, httpHeaders);
                ResponseEntity<JwtResponseDTO> jwtResponse
                        = restTemplate.exchange(API_REGISTER, HttpMethod.POST, httpEntity, JwtResponseDTO.class);
                request.getSession().setAttribute("jwtResponse", jwtResponse.getBody());
            } catch (HttpClientErrorException ex){
                model.addAttribute("registerError",ex.getResponseBodyAsString());
                model.addAttribute("hasErrors", true);
                model.addAttribute("user",user);
                return "accessibility/register";
            }
            return "redirect:/";
        }
    }

    /**TODO: LOG OUT*/
    @GetMapping("/sign-out")
    public String signOut(HttpSession session) {
        System.out.println("LOG: sign out");
        session.removeAttribute("jwtResponse");
        session.removeAttribute("admin");
        return "redirect:/";
    }
}
