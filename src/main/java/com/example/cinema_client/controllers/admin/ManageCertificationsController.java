package com.example.cinema_client.controllers.admin;

import com.example.cinema_client.constants.API;
import com.example.cinema_client.models.DirectorDTO;
import com.example.cinema_client.models.JwtResponseDTO;
import com.example.cinema_client.models.RatedDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tritcse00526x
 */
@Controller
@RequestMapping("/admin/certifications")
public class ManageCertificationsController {
    @Autowired
    private RestTemplate restTemplate;
    private static final String API_CERTIFICATIONS = API.baseURL + "/api/admin/certifications";
    public static final String API_GET_CERTIFICATION = API.baseURL + "/api/admin/certifications/detail";

    @GetMapping
    public String displayManageCertificationPage(HttpSession session, Model model) {
        System.out.println("LOG: accessing manage certifications page");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        // call API get all certifications
        ResponseEntity<RatedDTO[]> response = restTemplate.exchange(API_CERTIFICATIONS,HttpMethod.GET,entity,RatedDTO[].class);
        RatedDTO[] certificates = response.getBody();
        model.addAttribute("certificates", certificates);
        return "admin/certification/manage-certification";
    }

    @GetMapping("/add")
    public String displayAddCertificationPage(HttpSession session, Model model){
        System.out.println("LOG: accessing add new certification page");

        model.addAttribute("certificate",new RatedDTO());
        return "admin/certification/update-certification";
    }
    @PostMapping("/add")
    public String addCertificationPage(
            @ModelAttribute("certificate") RatedDTO certificate,
            @RequestParam(name = "checkActive", required = false, defaultValue = "false") boolean checkActive,
            HttpSession session, Model model){
        System.out.println("LOG: trying to create new certification");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());

        //check if promotion status is active or not
        if(checkActive)
            certificate.setIsActive(1);
        else
            certificate.setIsActive(0);

        HttpEntity<RatedDTO> entity = new HttpEntity<>(certificate, headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(API_CERTIFICATIONS,HttpMethod.POST,entity,String.class);
            System.out.println("SUCCESS: Add new certification");
        } catch (Exception e) {
            System.out.println("FAIL: Add new certification - " + e.getMessage());
        }

        return "redirect:/admin/certifications";
    }

    @GetMapping("/update")
    public String displayUpdateCertificationPage(
            @RequestParam Integer certificateId,
            HttpSession session, Model model){
        System.out.println("LOG: accessing update certification page");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO)session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());
        HttpEntity<?> entity = new HttpEntity<>(headers);

        // call API get selected certificate detail
        String urlTemplate = UriComponentsBuilder.fromHttpUrl(API_GET_CERTIFICATION)
                .queryParam("certificateId", "{certificateId}")
                .encode()
                .toUriString();
        Map<String, String> listRequestParam = new HashMap<>();
        listRequestParam.put("certificateId", certificateId + "");

        ResponseEntity<RatedDTO> response = restTemplate.exchange(
                urlTemplate,HttpMethod.GET,entity,RatedDTO.class,listRequestParam);
        RatedDTO certificate = response.getBody();

        model.addAttribute("certificate",certificate);
        return "admin/certification/update-certification";
    }

    @PostMapping("/update")
    public String updateCertificationPage(
            @ModelAttribute("certificate") RatedDTO certificate,
            @RequestParam(name = "checkActive", required = false, defaultValue = "false") boolean checkActive,
            HttpSession session, Model model){
        System.out.println("LOG: trying to update certification");
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDTO.getAccessToken());

        //check if promotion status is active or not
        if (checkActive)
            certificate.setIsActive(1);
        else
            certificate.setIsActive(0);

        HttpEntity<?> entity = new HttpEntity<>(certificate,headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(API_CERTIFICATIONS,HttpMethod.PUT,entity,String.class);
            System.out.println("SUCCESS: Update certification");
        } catch (Exception e) {
            System.out.println("FAIL: Update certification - " + e.getMessage());
        }
        return "redirect:/admin/certifications";
    }

}
