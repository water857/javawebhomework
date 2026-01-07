package com.smartcommunity.filter;

import com.google.gson.Gson;
import com.smartcommunity.util.RoleConstants;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RoleFilter implements Filter {
    // Role-based access control configuration
    private Map<String, Set<String>> roleAccessMap;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialize role access configuration
        roleAccessMap = new HashMap<>();

        // Resident can access these endpoints
        roleAccessMap.put("/api/user", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN, RoleConstants.SERVICE_PROVIDER));
        roleAccessMap.put("/api/profile/update", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN, RoleConstants.SERVICE_PROVIDER));
        roleAccessMap.put("/api/profile/update-idcard", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN, RoleConstants.SERVICE_PROVIDER));
        roleAccessMap.put("/api/change-password", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN, RoleConstants.SERVICE_PROVIDER));
        roleAccessMap.put("/api/repair/submit", Set.of(RoleConstants.RESIDENT));
        roleAccessMap.put("/api/repair/list", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN, RoleConstants.SERVICE_PROVIDER));
        roleAccessMap.put("/api/repair/detail", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN, RoleConstants.SERVICE_PROVIDER));
        roleAccessMap.put("/api/property/fee/list", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/parking/space/list", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/visitor/register", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN));

        // Property admin can access these endpoints
        roleAccessMap.put("/api/residents", Set.of(RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/residents/", Set.of(RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/providers", Set.of(RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/providers/", Set.of(RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/user/manage", Set.of(RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/repair/manage", Set.of(RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/property/fee/manage", Set.of(RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/parking/manage", Set.of(RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/visitor/manage", Set.of(RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/system/monitor", Set.of(RoleConstants.PROPERTY_ADMIN));

        // Activity related endpoints - accessible to residents, property admins, and service providers
        roleAccessMap.put("/api/activities", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/activities/", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/activities/register", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/activities/checkin", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/activities/evaluate", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/activities/images", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/activities/qrcode", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/activities/cancel", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/activities/status", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/activities/participants", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN));

        // Service provider can access these endpoints
        roleAccessMap.put("/api/service/tasks", Set.of(RoleConstants.SERVICE_PROVIDER));
        roleAccessMap.put("/api/service/evaluations", Set.of(RoleConstants.SERVICE_PROVIDER));

        // Add community post access rules
        roleAccessMap.put("/api/community", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN, RoleConstants.SERVICE_PROVIDER));
        roleAccessMap.put("/api/community/", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN, RoleConstants.SERVICE_PROVIDER));

        // Add property fee endpoints
        roleAccessMap.put("/api/property-fee", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/property-fee/", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/admin/property-fee", Set.of(RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/admin/property-fee/", Set.of(RoleConstants.PROPERTY_ADMIN));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Get request URI and user role
        String requestURI = httpRequest.getRequestURI();
        String role = (String) httpRequest.getAttribute("role");

        // If no role is set (not authenticated), let JwtAuthFilter handle it
        if (role == null) {
            chain.doFilter(request, response);
            return;
        }

        // Check if the requested URI is in the access control list
        for (Map.Entry<String, Set<String>> entry : roleAccessMap.entrySet()) {
            String uriPattern = entry.getKey();
            Set<String> allowedRoles = entry.getValue();

            if (requestURI.startsWith(uriPattern)) {
                // Check if user's role is allowed
                if (!allowedRoles.contains(role)) {
                    sendJsonResponse(httpResponse, HttpServletResponse.SC_FORBIDDEN, "Access denied: insufficient permissions");
                    return;
                }
                break;
            }
        }

        // Continue filter chain if access is allowed
        chain.doFilter(request, response);
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