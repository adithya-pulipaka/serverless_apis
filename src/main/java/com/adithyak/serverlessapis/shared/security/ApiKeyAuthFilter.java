package com.adithyak.serverlessapis.shared.security;

import com.adithyak.serverlessapis.shared.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";

    // Paths that do not require authentication
    private static final List<String> PUBLIC_PREFIXES = List.of(
            "/api/v1/ping",
            "/swagger-ui",
            "/v3/api-docs",
            "/api-docs",
            "/actuator/health"
    );

    @Value("${api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper;

    public ApiKeyAuthFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (isPublic(path)) {
            chain.doFilter(request, response);
            return;
        }

        String provided = request.getHeader(API_KEY_HEADER);
        if (apiKey.equals(provided)) {
            chain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "UNAUTHORIZED",
                "Missing or invalid API key. Provide a valid X-API-Key header.",
                Instant.now(),
                path
        ));
    }

    private boolean isPublic(String path) {
        return PUBLIC_PREFIXES.stream().anyMatch(path::startsWith);
    }
}
