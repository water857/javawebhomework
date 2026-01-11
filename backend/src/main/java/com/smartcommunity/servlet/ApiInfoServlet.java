package com.smartcommunity.servlet;

import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class ApiInfoServlet extends HttpServlet {
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=utf-8");
        PrintWriter out = resp.getWriter();

        try {
            // 准备 API 信息响应
            Map<String, Object> apiInfo = new HashMap<>();
            apiInfo.put("version", "1.0.0");
            apiInfo.put("name", "Smart Community Service API");
            apiInfo.put("description", "Smart Community Service Platform Backend API");
            
            Map<String, String> endpoints = new HashMap<>();
            endpoints.put("login", "/api/login");
            endpoints.put("register", "/api/register");
            endpoints.put("user info", "/api/user/*");
            
            apiInfo.put("endpoints", endpoints);
            
            Response response = new Response("success", "API information retrieved successfully", apiInfo);
            
            resp.setStatus(HttpServletResponse.SC_OK);
            out.write(gson.toJson(response));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Response response = new Response("error", "Internal server error");
            out.write(gson.toJson(response));
            e.printStackTrace();
        } finally {
            out.flush();
            out.close();
        }
    }

    // 响应类
    private static class Response {
        private String code;
        private String message;
        private Object data;

        public Response(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public Response(String code, String message, Object data) {
            this.code = code;
            this.message = message;
            this.data = data;
        }

        // 访问器与设置器方法
        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }
    }
}
