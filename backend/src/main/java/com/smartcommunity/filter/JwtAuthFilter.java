package com.smartcommunity.filter;

import com.google.gson.Gson;
import com.smartcommunity.util.JwtUtil;
import io.jsonwebtoken.Claims;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class JwtAuthFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Allow OPTIONS method for CORS preflight requests
        if (httpRequest.getMethod().equals("OPTIONS")) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // Get request URI
        String requestURI = httpRequest.getRequestURI();
        
        // Allow login, register, and root API path without authentication
        if (requestURI.contains("/api/login") || requestURI.contains("/api/register") || 
            (requestURI.endsWith("/api") || requestURI.matches(".*?/api$"))) {
            chain.doFilter(request, response);
            return;
        }
        
        // Check if this is a GET request to activities or community API
        boolean isPublicGetRequest = (requestURI.contains("/api/activities") || requestURI.contains("/api/community")) && httpRequest.getMethod().equals("GET");
        
        // Get authorization header
        String authHeader = httpRequest.getHeader("Authorization");
        
        // If it's not a public GET request, require authentication
        if (!isPublicGetRequest) {
            // Check if header is present and starts with "Bearer "
            if (authHeader == null || authHeader.trim().isEmpty() || !authHeader.startsWith("Bearer ")) {
                sendJsonResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid authorization header");
                return;
            }
        }
        
        // If authorization header is present, validate token and set user information
        if (authHeader != null && authHeader.trim().length() > 7 && authHeader.startsWith("Bearer ")) {
            // Extract token
            String token = authHeader.substring(7);
            
            // Validate token
            String tokenValidationResult = JwtUtil.validateToken(token);
            if (tokenValidationResult == null) {
                // Get claims from token
                Claims claims = JwtUtil.getClaimsFromToken(token);
                String username = claims.getSubject();
                String role = (String) claims.get("role");
                
                // Set user information in request attributes
                httpRequest.setAttribute("username", username);
                httpRequest.setAttribute("role", role);
                
                // Continue filter chain
                chain.doFilter(request, response);
            } else {
                // Invalid token
                sendJsonResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token: " + tokenValidationResult);
            }
        } else {
            // Missing or invalid authorization header for protected request
            if (!isPublicGetRequest) {
                sendJsonResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid authorization header");
            } else {
                // Continue for public GET requests
                chain.doFilter(request, response);
            }
        }
    }

    // Send JSON response
    private void sendJsonResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", statusCode);
        responseBody.put("error", message);
        
        Gson gson = new Gson();
        String json = gson.toJson(responseBody);
        
        PrintWriter writer = response.getWriter();
        writer.write(json);
        writer.flush();
    }
    
    @Override
    public void destroy() {
        // Cleanup resources if needed
    }
}