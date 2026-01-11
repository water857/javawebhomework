package com.smartcommunity.servlet;

import com.google.gson.Gson;
import com.smartcommunity.entity.User;
import com.smartcommunity.service.UserService;
import com.smartcommunity.service.impl.UserServiceImpl;
import com.smartcommunity.util.JwtUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class LoginServlet extends HttpServlet {
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

            // 解析 JSON 为 LoginRequest 对象
            LoginRequest loginRequest = gson.fromJson(sb.toString(), LoginRequest.class);

            // 校验必填字段
            if (loginRequest == null || loginRequest.getUsername() == null || loginRequest.getUsername().isEmpty()
                    || loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(gson.toJson(new Response("error", "Missing username or password")));
                return;
            }

            // 用户登录
            User user = userService.login(loginRequest.getUsername(), loginRequest.getPassword());

            // 生成 JWT 令牌
            String token = JwtUtil.generateToken(user.getUsername(), user.getRole());

            // 准备响应数据
            UserResponse userResponse = new UserResponse();
            userResponse.setId(user.getId());
            userResponse.setUsername(user.getUsername());
            userResponse.setRealName(user.getRealName());
            userResponse.setPhone(user.getPhone());
            userResponse.setEmail(user.getEmail());
            userResponse.setRole(user.getRole());

            LoginResponse loginResponse = new LoginResponse(token, userResponse);

            resp.setStatus(HttpServletResponse.SC_OK);
            out.write(gson.toJson(new Response("success", "Login successful", loginResponse)));
        } catch (RuntimeException e) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
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

    // 请求与响应类
    private static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    private static class UserResponse {
        private int id;
        private String username;
        private String realName;
        private String phone;
        private String email;
        private String role;

        // 访问器与设置器方法
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getRealName() {
            return realName;
        }

        public void setRealName(String realName) {
            this.realName = realName;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    private static class LoginResponse {
        private String token;
        private UserResponse user;

        public LoginResponse(String token, UserResponse user) {
            this.token = token;
            this.user = user;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public UserResponse getUser() {
            return user;
        }

        public void setUser(UserResponse user) {
            this.user = user;
        }
    }

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
