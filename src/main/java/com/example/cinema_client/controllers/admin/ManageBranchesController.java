package com.example.cinema_client.controllers.admin;

import com.example.cinema_client.constants.API;
import com.example.cinema_client.models.BranchDTO;
import com.example.cinema_client.models.CityDTO;
import com.example.cinema_client.models.JwtResponseDTO;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tritcse00526x
 */
@Controller
@RequestMapping("/admin/branches")
public class ManageBranchesController {
    @Autowired
    private RestTemplate restTemplate;
    public static final String API_GET_BRANCHES = API.baseURL + "/api/admin/branches";
    public static final String API_GET_BRANCH_DETAIL = API.baseURL + "/api/admin/branches/detail";
    private static final String API_GET_CITIES = API.baseURL + "/api/admin/cities";
    @GetMapping
    public String displayManageBranchPage(HttpSession session, Model model) {
        System.out.println("LOG: accessing manage branches page");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        // call API get all branches
        ResponseEntity<BranchDTO[]> response = restTemplate.exchange(API_GET_BRANCHES,HttpMethod.GET,entity,BranchDTO[].class);
        BranchDTO[] branches = response.getBody();
        model.addAttribute("branches", branches);
        return "admin/branch/manage-branch";
    }

    @GetMapping("/add")
    public String displayAddBranchPage(HttpSession session,Model model){
        System.out.println("LOG: accessing add new branch page");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        // call API get all cities
        ResponseEntity<CityDTO[]> responseCities = restTemplate.exchange(API_GET_CITIES,HttpMethod.GET,entity,CityDTO[].class);
        CityDTO[] cities = responseCities.getBody();
        model.addAttribute("cities", cities);
        model.addAttribute("branch",new BranchDTO());
        return "admin/branch/update-branch";
    }
    @PostMapping("/add")
    public String addBranchPage(
            @ModelAttribute("branch") BranchDTO branch,
            @RequestParam(name = "checkActive", required = false, defaultValue = "false") boolean checkActive,
            HttpSession session, Model model){
        System.out.println("LOG: trying to create new branch");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO)session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION,"Bearer " + jwtResponseDTO.getAccessToken());

        //check if branch status is active or not
        if(checkActive)
            branch.setIsActive(1);
        else
            branch.setIsActive(0);

        HttpEntity<BranchDTO> entity = new HttpEntity<>(branch,headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(API_GET_BRANCHES,HttpMethod.POST,entity,String.class);
            System.out.println("SUCCESS: Add new branch");
        } catch (Exception e) {
            System.out.println("FAIL: Add new branch - " + e.getMessage());
        }

        return "redirect:/admin/branches";
    }

    @GetMapping("/update")
    public String displayUpdateBranchPage(
            @RequestParam Integer branchId,
            HttpSession session, Model model){
        System.out.println("LOG: accessing update branch page");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO)session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        // call API get selected branch detail
        String urlTemplate = UriComponentsBuilder.fromHttpUrl(API_GET_BRANCH_DETAIL)
                .queryParam("branchId", "{branchId}")
                .encode()
                .toUriString();
        Map<String,String> listRequestParam = new HashMap<>();
        listRequestParam.put("branchId", branchId + "");

        ResponseEntity<BranchDTO> response = restTemplate
                .exchange(urlTemplate,HttpMethod.GET,entity,BranchDTO.class,listRequestParam);
        BranchDTO branch = response.getBody();

        // call API get all cities
        ResponseEntity<CityDTO[]> responseCities = restTemplate.exchange(API_GET_CITIES,HttpMethod.GET,entity,CityDTO[].class);
        CityDTO[] cities = responseCities.getBody();
        model.addAttribute("cities", cities);
        model.addAttribute("branch",branch);
        return "admin/branch/update-branch";
    }

    @PostMapping("/update")
    public String updateBranchPage(
            @ModelAttribute("branch") BranchDTO branch,
            @RequestParam(name = "checkActive", required = false, defaultValue = "false") boolean checkActive,
            HttpSession session, Model model){
        System.out.println("LOG: trying to update branch");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO)session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());

        //check if branch status is active or not
        if(checkActive)
            branch.setIsActive(1);
        else
            branch.setIsActive(0);
        HttpEntity<?> entity = new HttpEntity<>(branch,headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(API_GET_BRANCHES,HttpMethod.PUT,entity,String.class);
            System.out.println("SUCCESS: Update branch");
        } catch (Exception e) {
            System.out.println("FAIL: Update branch - " + e.getMessage());
        }
        return "redirect:/admin/branches";
    }
}
