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
        // 如有需要进行初始化
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 允许 CORS 预检请求的 OPTIONS 方法
        if (httpRequest.getMethod().equals("OPTIONS")) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // 获取请求 URI
        String requestURI = httpRequest.getRequestURI();
        
        // 允许登录、注册以及根 API 路径无需认证访问
        if (requestURI.contains("/api/login") || requestURI.contains("/api/register") || 
            (requestURI.endsWith("/api") || requestURI.matches(".*?/api$"))) {
            chain.doFilter(request, response);
            return;
        }
        
        // 检查是否为活动或社区 API 的 GET 请求
        boolean isPublicGetRequest = (requestURI.contains("/api/activities") || requestURI.contains("/api/community")) && httpRequest.getMethod().equals("GET");
        
        // 获取认证请求头
        String authHeader = httpRequest.getHeader("Authorization");
        
        // 非公开 GET 请求需要认证
        if (!isPublicGetRequest) {
            // 检查请求头是否存在且以 "Bearer " 开头
            if (authHeader == null || authHeader.trim().isEmpty() || !authHeader.startsWith("Bearer ")) {
                sendJsonResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid authorization header");
                return;
            }
        }
        
        // 若存在认证请求头则校验令牌并设置用户信息
        if (authHeader != null && authHeader.trim().length() > 7 && authHeader.startsWith("Bearer ")) {
            // 提取令牌
            String token = authHeader.substring(7);
            
            // 校验令牌
            String tokenValidationResult = JwtUtil.validateToken(token);
            if (tokenValidationResult == null) {
                // 从令牌中获取声明
                Claims claims = JwtUtil.getClaimsFromToken(token);
                String username = claims.getSubject();
                String role = (String) claims.get("role");
                
                // 在请求属性中设置用户信息
                httpRequest.setAttribute("username", username);
                httpRequest.setAttribute("role", role);
                
                // 继续过滤链
                chain.doFilter(request, response);
            } else {
                // 令牌无效
                sendJsonResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token: " + tokenValidationResult);
            }
        } else {
            // 受保护请求缺少或无效的认证请求头
            if (!isPublicGetRequest) {
                sendJsonResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid authorization header");
            } else {
                // 公共 GET 请求直接放行
                chain.doFilter(request, response);
            }
        }
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
