package com.example.cinema_client.controllers.admin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.cinema_client.constants.API ;
import com.example.cinema_client.models.JwtResponseDTO;
import com.example.cinema_client.models.Role;
import com.example.cinema_client.models.User;

/**
 * @author tritcse00526x
 */
@Controller
@RequestMapping("/admin/accounts")
public class ManageAccountsController {
    @Autowired
    private RestTemplate restTemplate;

    private static final String API_ACCOUNTS = API.baseURL + "/api/admin/accounts";
    private static final String API_GET_ROLES = API.baseURL + "/api/admin/roles";
    private static final String API_GET_USER = API.baseURL + "/api/account";

    @GetMapping
    public String displayManageAccountPage(HttpSession session,Model model){
        System.out.println("LOG: accessing manage accounts page");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO)session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION,"Bearer "+jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        // call API get all accounts
        ResponseEntity<User[]> response = restTemplate.exchange(API_ACCOUNTS,HttpMethod.GET,entity,User[].class);
        User[] users = response.getBody();
        // call API get all roles
        ResponseEntity<Role[]> responseRole = restTemplate.exchange(API_GET_ROLES,HttpMethod.GET,entity,Role[].class);
        Role[] roles = responseRole.getBody();

        model.addAttribute("users", users);
        model.addAttribute("roles", roles);
        model.addAttribute("modelUser", new User());
        return "admin/account/manage-account";
    }

    @GetMapping("/add")
    public String displayAddAccountPage(HttpSession session,Model model){
        System.out.println("LOG: accessing add new account page");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO)session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION,"Bearer "+jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        // call API get all roles
        ResponseEntity<Role[]> responseRole = restTemplate.exchange(API_GET_ROLES,HttpMethod.GET,entity,Role[].class);
        Role[] roles = responseRole.getBody();
        model.addAttribute("roles", roles);
        model.addAttribute("user", new User());
        return "admin/account/add-account";
    }
    @PostMapping("/add")
    public String addAccountPage(
            @ModelAttribute("user") User user,
            @RequestParam(name = "role", required = false) List<String> role,
            HttpSession session,Model model){
        System.out.println("LOG: trying to create new account");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO)session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION,"Bearer "+jwtResponseDTO.getAccessToken());

        try { //check roles selected
            Set<Role> roles = new HashSet<>();
            if(role != null) {
                for (String r : role) {
                    Role ro = new Role();
                    ro.setName(r);
                    roles.add(ro);
                }
            } else {
                Role ro = new Role();
                ro.setName("ROLE_CLIENT");
                roles.add(ro);
            }
            // set roles for user
            user.setRoles(roles);
            HttpEntity<User> entity = new HttpEntity<>(user,headers);
            ResponseEntity<String> response = restTemplate.exchange(API_ACCOUNTS,HttpMethod.POST, entity, String.class);
            System.out.println("SUCCESS: Add new account");
        } catch (Exception e) {
            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<Role[]> responseRole = restTemplate.exchange(API_GET_ROLES,HttpMethod.GET,entity,Role[].class);
            Role[] roles = responseRole.getBody();
            model.addAttribute("roles",roles);
            model.addAttribute("error",e.getMessage());
            System.out.println("FAIL: Add new account - " + e.getMessage());
            return "admin/account/add-account";
        }
        return "redirect:/admin/accounts";
    }

    @GetMapping("/update")
    public String displayUpdateAccountPage(
            @RequestParam Integer userId,
            HttpSession session,Model model){
        System.out.println("LOG: accessing update account page");
        HttpHeaders headers = new HttpHeaders();
        // attach the JWT access token to the header to send it with the request
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO)session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION,"Bearer "+jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        // call API get selected user
        String urlTemplate = UriComponentsBuilder.fromHttpUrl(API_GET_USER)
                .queryParam("userId", "{userId}")
                .encode()
                .toUriString();
        Map<String,String> listRequestParam = new HashMap<>();
        listRequestParam.put("userId", userId + "");

        ResponseEntity<User> response = restTemplate
                .exchange(urlTemplate,HttpMethod.GET,entity,User.class,listRequestParam);
        User user=response.getBody();
        model.addAttribute("user",user);

        // call API get all roles
        ResponseEntity<Role[]> responseRole = restTemplate.exchange(API_GET_ROLES,HttpMethod.GET,entity,Role[].class);
        Role[] roles = responseRole.getBody();
        model.addAttribute("roles",roles);
        return "admin/account/update-account";
    }
    @PostMapping("/update")
    public String updateAccountPage(
            @ModelAttribute("user") User user,
            @RequestParam(name = "role", required = false) List<String> role,
            @RequestParam String newPassword,
            HttpSession session,Model model){
        System.out.println("LOG: trying to update account");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO)session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION,"Bearer "+jwtResponseDTO.getAccessToken());

        try { // check roles selected
            Set<Role> roles = new HashSet<>();
            if(role != null) {
                for (String r : role) {
                    Role ro = new Role();
                    ro.setName(r);
                    roles.add(ro);
                }
            } else {
                    Role ro = new Role();
                    ro.setName("ROLE_CLIENT");
                    roles.add(ro);
            }
            user.setRoles(roles);
            HttpEntity<User> entity = new HttpEntity<>(user,headers);

            String urlTemplate = UriComponentsBuilder.fromHttpUrl(API_ACCOUNTS)
                    .queryParam("newPassword", "{newPassword}")
                    .encode()
                    .toUriString();
            Map<String,String> listRequestParam = new HashMap<>();
            listRequestParam.put("newPassword", newPassword);
            ResponseEntity<String> response = restTemplate
                    .exchange(urlTemplate,HttpMethod.PUT,entity,String.class,listRequestParam);
            System.out.println("SUCCESS: Update account");
        } catch (Exception e) {
            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<Role[]> responseRole = restTemplate.exchange(API_GET_ROLES,HttpMethod.GET,entity,Role[].class);
            Role[] roles = responseRole.getBody();
            model.addAttribute("roles",roles);
            model.addAttribute("error",e.getMessage());
            System.out.println("FAIL: Update account - " + e.getMessage());
            return "admin/account/update-account";
        }
        return "redirect:/admin/accounts";
    }

}
