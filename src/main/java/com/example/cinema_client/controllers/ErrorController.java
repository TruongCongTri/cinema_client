package com.example.cinema_client.controllers;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author tritcse00526x
 */
@Controller
@RequestMapping("/error")
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @GetMapping
    public ModelAndView renderErrorPage(HttpServletRequest httpRequest) {
        ModelAndView errorPage = new ModelAndView("errorPage");
        String errorMsg = "";
        int httpErrorCode = getErrorCode(httpRequest);

        // Determine the error message based on the HTTP status code
        switch (httpErrorCode) {
            case 400: {
                errorMsg = "Http Error Code: 400. Bad Request";
                break;
            }
            case 401: {
                errorMsg = "Http Error Code: 401. Unauthorized";
                break;
            }
            case 404: {
                errorMsg = "Http Error Code: 404. Resource not found";
                break;
            }
            case 405: {
                errorMsg = "Http Error Code: 405. Bad Request";
                break;
            }
            case 500: {
                errorMsg = "Http Error Code: 500. Internal Server Error";
                break;
            }
            default: {
                errorMsg = "Http Error Code: 500. Internal Server Error";
                break;
            }
        }

        // Add the error message and status code to the model
        errorPage.addObject("errorMsg", errorMsg);
        errorPage.addObject("errorStatus", httpErrorCode);
        return errorPage;
    }

    private int getErrorCode(HttpServletRequest httpRequest) {
        return (Integer) httpRequest.getAttribute("javax.servlet.error.status_code");
    }

    @RequestMapping(value = "500Error", method = RequestMethod.GET)
    public void throwRuntimeException() {
        throw new NullPointerException("Throwing a null pointer exception");
    }
}