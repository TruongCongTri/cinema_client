package com.example.cinema_client.controllers.admin;

import com.example.cinema_client.constants.API;
import com.example.cinema_client.models.JwtResponseDTO;
import com.example.cinema_client.models.PromotionDTO;
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
@RequestMapping("/admin/promotions")
public class ManagePromotionsController {
    @Autowired
    private RestTemplate restTemplate;
    public static final String API_GET_PROMOTIONS = API.baseURL + "/api/admin/promotions";
    public static final String API_GET_PROMOTION_DETAIL = API.baseURL + "/api/admin/promotions/detail";

    @GetMapping
    public String displayManagePromotionPage(HttpSession session, Model model) {
        System.out.println("LOG: accessing manage promotions page");
        HttpHeaders headers = getHttpHeaders(session);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        System.out.println("LOG: loading promotions");
        ResponseEntity<PromotionDTO[]> responsePromotions = restTemplate.exchange(
                API_GET_PROMOTIONS, HttpMethod.GET, entity, PromotionDTO[].class);
        PromotionDTO[] promotionList = responsePromotions.getBody();

        model.addAttribute("promotionList", promotionList);
        return "admin/promotion/manage-promotion";
    }

    @GetMapping("/add")
    public String displayAddPromotionPage(
            HttpSession session, Model model) {
        System.out.println("LOG: accessing add new promotion page");
        HttpHeaders headers = getHttpHeaders(session);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        System.out.println("LOG: creating new empty promotion");
        model.addAttribute("promotion", new PromotionDTO());
        return "admin/promotion/update-promotion";
    }

    @PostMapping("/add")
    public String addPromotionPage(
            @ModelAttribute("promotion") PromotionDTO promotion,
            @RequestParam(name = "checkActive", required = false, defaultValue = "false") boolean checkActive,
            HttpSession session, Model model) {
        System.out.println("LOG: trying to create new promotion");
        HttpHeaders headers = getHttpHeaders(session);
        //check if promotion status is active or not
        if(checkActive)
            promotion.setIsActive(1);
        else
            promotion.setIsActive(0);

        HttpEntity<PromotionDTO> promotionEntity = new HttpEntity<>(promotion, headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(API_GET_PROMOTIONS,HttpMethod.POST,promotionEntity,String.class);
            System.out.println("SUCCESS: Add new promotion");
        } catch (Exception e) {
            System.out.println("FAIL: Add new promotion - " + e.getMessage());
        }

        return "redirect:/admin/promotions";
    }

    @GetMapping("/update")
    public String displayUpdatePromotionPage(
            @RequestParam Integer promotionId,
            HttpSession session, Model model) {
        System.out.println("LOG: accessing update promotion page");
        HttpHeaders headers = getHttpHeaders(session);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        System.out.println("LOG: loading promotion by id " + promotionId);
        // call API get selected promotion
        String urlTemplate = UriComponentsBuilder.fromHttpUrl(API_GET_PROMOTION_DETAIL)
                .queryParam("promotionId", "{promotionId}")
                .encode()
                .toUriString();
        Map<String, String> listRequestParam = new HashMap<>();
        listRequestParam.put("promotionId", promotionId + "");

        ResponseEntity<PromotionDTO> response = restTemplate.exchange(
                urlTemplate, HttpMethod.GET, entity, PromotionDTO.class, listRequestParam);
        PromotionDTO promotion = response.getBody();

        model.addAttribute("promotion", promotion);
        return "admin/promotion/update-promotion";
    }

    @PostMapping("/update")
    public String updatePromotionPage(
            @ModelAttribute("promotion") PromotionDTO promotion,
            @RequestParam(name = "checkActive", required = false, defaultValue = "false") boolean checkActive,
            HttpSession session, Model model) {
        System.out.println("LOG: trying to update promotion");
        HttpHeaders headers = getHttpHeaders(session);
        // check if promotion status is active or not
        if (checkActive)
            promotion.setIsActive(1);
        else
            promotion.setIsActive(0);

        HttpEntity<PromotionDTO> promotionEntity = new HttpEntity<>(promotion, headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(API_GET_PROMOTIONS,HttpMethod.PUT,promotionEntity,String.class);
            System.out.println("SUCCESS: Update promotion");
        } catch (Exception e) {
            System.out.println("FAIL: Update promotion - " + e.getMessage());
        }

        return "redirect:/admin/promotions";
    }

    private static HttpHeaders getHttpHeaders(HttpSession session) {
        // attach the JWT access token to the header to send it with the request
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        JwtResponseDTO jwtResponse = (JwtResponseDTO) session.getAttribute("jwtResponse");
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponse.getAccessToken());
        return headers;
    }
}
