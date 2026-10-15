package com.ecommerce.orders_service.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class AuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String token = request.getHeader("Authorization");

        System.out.println("=================================");
        System.out.println("ENTRO AL FILTRO");
        System.out.println("TOKEN: " + token);
        System.out.println("URL: " + request.getRequestURI());
        System.out.println("=================================");

        if (token == null || token.isBlank()) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED);

            response.getWriter().write(
                    "Authorization header is required");

            return;
        }

        filterChain.doFilter(request, response);
    }
}