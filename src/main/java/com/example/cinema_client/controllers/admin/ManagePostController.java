package com.example.cinema_client.controllers.admin;

import com.example.cinema_client.constants.API;
import com.example.cinema_client.models.JwtResponseDTO;
import com.example.cinema_client.models.PostDTO;
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
@RequestMapping("/admin/posts")
public class ManagePostController {
    @Autowired
    private RestTemplate restTemplate;
    public static final String API_GET_POSTS = API.baseURL + "/api/admin/posts";
    public static final String API_GET_POST_DETAIL = API.baseURL + "/api/admin/posts/detail";

    @GetMapping
    public String displayManagePage(HttpSession session, Model model) {
        System.out.println("LOG: accessing manage posts page");
        HttpHeaders headers = getHttpHeaders(session);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        System.out.println("LOG: loading posts");
        ResponseEntity<PostDTO[]> response = restTemplate.exchange(API_GET_POSTS,HttpMethod.GET,entity,PostDTO[].class);
        PostDTO[] posts = response.getBody();
        model.addAttribute("posts", posts);
        return "admin/post/manage-post";
    }

    @GetMapping("/add")
    public String displayAddPage(
            HttpSession session, Model model) {
        System.out.println("LOG: accessing add new post page");
        HttpHeaders headers = getHttpHeaders(session);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        System.out.println("LOG: creating new empty post");
        model.addAttribute("post", new PostDTO());
        return "admin/post/update-post";
    }

    @PostMapping("/add")
    public String addPage(
            @ModelAttribute("post") PostDTO post,
            @RequestParam(name = "checkActive", required = false, defaultValue = "false") boolean checkActive,
            @RequestParam(name = "checkType", required = false, defaultValue = "false") boolean checkType,
            HttpSession session, Model model) {
        System.out.println("LOG: trying to create new post");
        HttpHeaders headers = getHttpHeaders(session);

        //check if post status is active or not
        if(checkActive)
            post.setIsActive(1);
        else
            post.setIsActive(0);
        //check if post type
        if(checkType)
            post.setType(1);
        else
            post.setType(2);

        HttpEntity<PostDTO> entity = new HttpEntity<>(post, headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(API_GET_POSTS,HttpMethod.POST,entity,String.class);
            System.out.println("SUCCESS: Add new post");
        } catch (Exception e) {
            System.out.println("FAIL: Add new post - " + e.getMessage());
        }
        return "redirect:/admin/posts";
    }

    @GetMapping("/update")
    public String displayPromotionPage(
            @RequestParam Integer postId,
            HttpSession session, Model model) {
        System.out.println("LOG: accessing update post page");
        HttpHeaders headers = getHttpHeaders(session);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        System.out.println("LOG: loading post by id " + postId);
        // call API get selected post
        String urlTemplate = UriComponentsBuilder.fromHttpUrl(API_GET_POST_DETAIL)
                .queryParam("postId", "{postId}")
                .encode()
                .toUriString();
        Map<String, String> listRequestParam = new HashMap<>();
        listRequestParam.put("postId", postId + "");
        ResponseEntity<PostDTO> response = restTemplate.exchange(
                urlTemplate, HttpMethod.GET, entity, PostDTO.class, listRequestParam);
        PostDTO post = response.getBody();

        model.addAttribute("post", post);
        return "admin/post/update-post";
    }

    @PostMapping("/update")
    public String updatePage(
            @ModelAttribute("post") PostDTO post,
            @RequestParam(name = "checkActive", required = false, defaultValue = "false") boolean checkActive,
            @RequestParam(name = "checkType", required = false, defaultValue = "false") boolean checkType,
            HttpSession session, Model model) {
        System.out.println("LOG: trying to update post");
        HttpHeaders headers = getHttpHeaders(session);

        //check if post status is active or not
        if (checkActive)
            post.setIsActive(1);
        else
            post.setIsActive(0);
        // check post type
        if(checkType)
            post.setType(1);
        else
            post.setType(2);

        HttpEntity<PostDTO> entity = new HttpEntity<>(post, headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(API_GET_POSTS,HttpMethod.PUT,entity,String.class);
            System.out.println("SUCCESS: Update post");
        } catch (Exception e) {
            System.out.println("FAIL: Update post - " + e.getMessage());
        }

        return "redirect:/admin/posts";
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
