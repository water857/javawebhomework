package com.smartcommunity.servlet;

import com.google.gson.Gson;
import com.smartcommunity.entity.User;
import com.smartcommunity.service.UserService;
import com.smartcommunity.service.impl.UserServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class RegisterServlet extends HttpServlet {
    private UserService userService = new UserServiceImpl();
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=utf-8");
        PrintWriter out = resp.getWriter();

        try {
            // 读取请求体
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            // 解析 JSON 为 User 对象
            User user = gson.fromJson(sb.toString(), User.class);

            // 校验必填字段
            if (user == null || user.getUsername() == null || user.getUsername().isEmpty()
                    || user.getPassword() == null || user.getPassword().isEmpty()
                    || user.getRole() == null || user.getRole().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(gson.toJson(new Response("error", "Missing required fields")));
                return;
            }

            // 校验角色
            if (!user.getRole().equals("resident") && !user.getRole().equals("property_admin") && !user.getRole().equals("service_provider")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(gson.toJson(new Response("error", "Invalid role")));
                return;
            }

            // 注册用户
            int userId = userService.register(user);
            if (userId > 0) {
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "Registration successful", userId)));
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write(gson.toJson(new Response("error", "Registration failed")));
            }
        } catch (RuntimeException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(gson.toJson(new Response("error", e.getMessage())));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(new Response("error", "Internal server error")));
            e.printStackTrace();
        } finally {
            out.flush();
            out.close();
        }
    }

    // JSON 输出响应类
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
