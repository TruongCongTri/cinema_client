package com.example.cinema_client.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.example.cinema_client.models.JwtResponseDTO;
import com.example.cinema_client.models.Role;


/**
 * @author tritcse00526x
 */
@Component
@Order(1)
public class filterRequest implements Filter {

    private final static Logger LOG = LoggerFactory.getLogger(filterRequest.class);

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        LOG.info("Initializing filter :{}", this);
    }

    // Implement action to restrict access to a specific page for unauthorized users
    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession();
        JwtResponseDTO jwtResponseDTO = (JwtResponseDTO) session.getAttribute("jwtResponse");

        if (req.getRequestURI().contains("/admin")) { // when access to uri requires admin authorization
            if (jwtResponseDTO != null) { // if logged in
                for (Role role : jwtResponseDTO.getRoles()) {
                    if (role.equals("ROLE_ADMIN")) { // if has admin authorization
                        chain.doFilter(request, response);
                        return; // then gain access to requested page
                    }
                }
            }
            res.sendRedirect("/"); // otherwise deny access and return to home page
            return;
        } else {
            if (req.getRequestURI().contains("/booking")
                    || req.getRequestURI().contains("/payment")
                    || req.getRequestURI().contains("/account")) { // when access to uri requires member authorization
                if (jwtResponseDTO == null) { // if haven't logged in
                    res.sendRedirect("/login"); // then return login page
                    return;
                }
            }
        }
        LOG.info("Starting Transaction for req :{}", req.getRequestURI());
        chain.doFilter(request, response);
        LOG.info("Committing Transaction for req :{}", req.getRequestURI());
    }

    @Override
    public void destroy() {
        LOG.warn("Destructing filter :{}", this);
    }
}
