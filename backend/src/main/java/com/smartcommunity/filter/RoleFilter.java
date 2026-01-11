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
    // 角色权限控制配置
    private Map<String, Set<String>> roleAccessMap;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 初始化角色访问配置
        roleAccessMap = new HashMap<>();

        // 住户可访问这些接口
        roleAccessMap.put("/api/user", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN, RoleConstants.SERVICE_PROVIDER));
        roleAccessMap.put("/api/profile/update", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN, RoleConstants.SERVICE_PROVIDER));
        roleAccessMap.put("/api/profile/update-idcard", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN, RoleConstants.SERVICE_PROVIDER));
        roleAccessMap.put("/api/change-password", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN, RoleConstants.SERVICE_PROVIDER));
        roleAccessMap.put("/api/repair/submit", Set.of(RoleConstants.RESIDENT));
        roleAccessMap.put("/api/repair/list", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN, RoleConstants.SERVICE_PROVIDER));
        roleAccessMap.put("/api/repair/detail", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN, RoleConstants.SERVICE_PROVIDER));
        roleAccessMap.put("/api/property/fee/list", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/parking/space/list", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/parking/application", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/parking/application/list", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/parking/application/review", Set.of(RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/parking/space/release", Set.of(RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/visitor/register", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/visitor/list", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/message", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN, RoleConstants.SERVICE_PROVIDER));
        roleAccessMap.put("/api/message/", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN, RoleConstants.SERVICE_PROVIDER));

        // 物业管理员可访问这些接口
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

        // 活动相关接口（住户与物业管理员可访问）
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
        roleAccessMap.put("/api/activities/review", Set.of(RoleConstants.PROPERTY_ADMIN));

        // 服务商可访问这些接口
        roleAccessMap.put("/api/service/tasks", Set.of(RoleConstants.SERVICE_PROVIDER));
        roleAccessMap.put("/api/service/evaluations", Set.of(RoleConstants.SERVICE_PROVIDER));

        // 添加社区动态访问规则
        roleAccessMap.put("/api/community", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN, RoleConstants.SERVICE_PROVIDER));
        roleAccessMap.put("/api/community/", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN, RoleConstants.SERVICE_PROVIDER));

        roleAccessMap.put("/api/second-hand", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN, RoleConstants.SERVICE_PROVIDER));
        roleAccessMap.put("/api/second-hand/", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN, RoleConstants.SERVICE_PROVIDER));
        roleAccessMap.put("/api/skills", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN, RoleConstants.SERVICE_PROVIDER));
        roleAccessMap.put("/api/skills/", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN, RoleConstants.SERVICE_PROVIDER));
        roleAccessMap.put("/api/lost-found", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN, RoleConstants.SERVICE_PROVIDER));
        roleAccessMap.put("/api/lost-found/", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN, RoleConstants.SERVICE_PROVIDER));
        // 添加物业费接口
        roleAccessMap.put("/api/property-fee", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/property-fee/", Set.of(RoleConstants.RESIDENT, RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/admin/property-fee", Set.of(RoleConstants.PROPERTY_ADMIN));
        roleAccessMap.put("/api/admin/property-fee/", Set.of(RoleConstants.PROPERTY_ADMIN));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 获取请求 URI 和用户角色
        String requestURI = httpRequest.getRequestURI();
        String role = (String) httpRequest.getAttribute("role");

        // 未设置角色（未认证）时交给 JwtAuthFilter 处理
        if (role == null) {
            chain.doFilter(request, response);
            return;
        }

        // 检查请求 URI 是否在访问控制列表中
        for (Map.Entry<String, Set<String>> entry : roleAccessMap.entrySet()) {
            String uriPattern = entry.getKey();
            Set<String> allowedRoles = entry.getValue();

            if (requestURI.startsWith(uriPattern)) {
                // 检查用户角色是否允许
                if (!allowedRoles.contains(role)) {
                    sendJsonResponse(httpResponse, HttpServletResponse.SC_FORBIDDEN, "Access denied: insufficient permissions");
                    return;
                }
                break;
            }
        }

        // 访问允许时继续过滤链
        chain.doFilter(request, response);
    }

    // 发送 JSON 响应
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
        // 如有需要清理资源
    }
}
