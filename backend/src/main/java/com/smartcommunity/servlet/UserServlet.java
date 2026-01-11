package com.smartcommunity.servlet;

import com.google.gson.Gson;
import com.smartcommunity.entity.User;
import com.smartcommunity.service.UserService;
import com.smartcommunity.service.impl.UserServiceImpl;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class UserServlet extends HttpServlet {
    private UserService userService = new UserServiceImpl();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=utf-8");
        PrintWriter out = resp.getWriter();

        try {
            // 获取请求 URI 以确定执行的操作
            String requestURI = req.getRequestURI();
            String contextPath = req.getContextPath();
            String relativeURI = requestURI.substring(contextPath.length());
            
            // 判断是否为所有住户请求（含或不含结尾斜杠）
            if (relativeURI.equals("/api/residents") || relativeURI.equals("/api/residents/")) {
                // 获取搜索参数
                String searchKeyword = req.getParameter("searchKeyword");
                String statusParam = req.getParameter("status");
                Integer status = null;
                if (statusParam != null && !statusParam.isEmpty()) {
                    try {
                        status = Integer.parseInt(statusParam);
                    } catch (NumberFormatException e) {
                        // 状态参数无效，忽略
                    }
                }
                
                // 按条件获取住户
                List<User> residents = userService.getResidents(searchKeyword, status);
                List<UserResponse> residentResponses = new ArrayList<>();
                
                // 转换为响应对象
                for (User resident : residents) {
                    UserResponse userResponse = new UserResponse();
                    userResponse.setId(resident.getId());
                    userResponse.setUsername(resident.getUsername());
                    userResponse.setRealName(resident.getRealName());
                    userResponse.setPhone(resident.getPhone());
                    userResponse.setEmail(resident.getEmail());
                    userResponse.setIdCard(resident.getIdCard());
                    userResponse.setAddress(resident.getAddress());
                    userResponse.setRole(resident.getRole());
                    userResponse.setStatus(resident.getStatus());
                    userResponse.setCreatedAt(resident.getCreatedAt());
                    userResponse.setUpdatedAt(resident.getUpdatedAt());
                    residentResponses.add(userResponse);
                }
                
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "Residents retrieved successfully", residentResponses)));
                return;
            // 判断是否为所有服务商请求
            } else if (relativeURI.equals("/api/providers") || relativeURI.equals("/api/providers/")) {
                // 获取搜索参数
                String searchKeyword = req.getParameter("searchKeyword");
                String statusParam = req.getParameter("status");
                Integer status = null;
                if (statusParam != null && !statusParam.isEmpty()) {
                    try {
                        status = Integer.parseInt(statusParam);
                    } catch (NumberFormatException e) {
                        // 状态参数无效，忽略
                    }
                }
                
                // 按条件获取服务商
                List<User> providers = userService.getProviders(searchKeyword, status);
                List<UserResponse> providerResponses = new ArrayList<>();
                
                // 转换为响应对象
                for (User provider : providers) {
                    UserResponse userResponse = new UserResponse();
                    userResponse.setId(provider.getId());
                    userResponse.setUsername(provider.getUsername());
                    userResponse.setRealName(provider.getRealName());
                    userResponse.setPhone(provider.getPhone());
                    userResponse.setEmail(provider.getEmail());
                    userResponse.setIdCard(provider.getIdCard());
                    userResponse.setAddress(provider.getAddress());
                    userResponse.setRole(provider.getRole());
                    userResponse.setStatus(provider.getStatus());
                    userResponse.setCreatedAt(provider.getCreatedAt());
                    userResponse.setUpdatedAt(provider.getUpdatedAt());
                    providerResponses.add(userResponse);
                }
                
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "Providers retrieved successfully", providerResponses)));
                return;
            } else if (relativeURI.startsWith("/api/residents/")) {
                // 根据 ID 获取指定住户
                String[] parts = relativeURI.split("/");
                if (parts.length != 4) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "Invalid resident ID")));
                    return;
                }
                
                int residentId = Integer.parseInt(parts[3]);
                User user = userService.getUserById(residentId);
                if (user == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.write(gson.toJson(new Response("error", "Resident not found")));
                    return;
                }
                
                // 准备响应数据
                UserResponse userResponse = new UserResponse();
                userResponse.setId(user.getId());
                userResponse.setUsername(user.getUsername());
                userResponse.setRealName(user.getRealName());
                userResponse.setPhone(user.getPhone());
                userResponse.setEmail(user.getEmail());
                userResponse.setIdCard(user.getIdCard());
                userResponse.setAddress(user.getAddress());
                userResponse.setRole(user.getRole());
                userResponse.setStatus(user.getStatus());
                userResponse.setCreatedAt(user.getCreatedAt());
                userResponse.setUpdatedAt(user.getUpdatedAt());
                
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "Resident information retrieved successfully", userResponse)));
                return;
            }
            
            // 兼容按用户名获取用户的原有逻辑
            // 从请求参数或会话获取用户名
            String username = req.getParameter("username");
            if (username == null || username.isEmpty()) {
                username = (String) req.getAttribute("username"); // 由 JwtAuthFilter 设置
            }

            if (username == null || username.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(gson.toJson(new Response("error", "Username is required")));
                return;
            }

            // 获取用户信息
            User user = userService.getUserByUsername(username);
            if (user == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write(gson.toJson(new Response("error", "User not found")));
                return;
            }

            // 准备响应数据
            UserResponse userResponse = new UserResponse();
            userResponse.setId(user.getId());
            userResponse.setUsername(user.getUsername());
            userResponse.setRealName(user.getRealName());
            userResponse.setPhone(user.getPhone());
            userResponse.setEmail(user.getEmail());
            userResponse.setIdCard(user.getIdCard());
            userResponse.setAddress(user.getAddress());
            userResponse.setRole(user.getRole());
            userResponse.setStatus(user.getStatus());
            userResponse.setCreatedAt(user.getCreatedAt());
            userResponse.setUpdatedAt(user.getUpdatedAt());

            resp.setStatus(HttpServletResponse.SC_OK);
            out.write(gson.toJson(new Response("success", "User information retrieved successfully", userResponse)));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(new Response("error", "Internal server error: " + e.getMessage())));
            e.printStackTrace();
        } finally {
            out.flush();
            out.close();
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=utf-8");
        PrintWriter out = resp.getWriter();

        try {
            // 从 JWT 过滤器获取用户名
            String username = (String) req.getAttribute("username");
            if (username == null || username.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write(gson.toJson(new Response("error", "Unauthorized")));
                return;
            }

            // 读取请求体
            BufferedReader reader = req.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            // 获取请求 URI 以确定更新操作
            String requestURI = req.getRequestURI();
            String contextPath = req.getContextPath();
            String relativeURI = requestURI.substring(contextPath.length());

            if (relativeURI.equals("/api/profile/update")) {
                // 更新个人资料信息
                ProfileUpdateRequest profileUpdate = gson.fromJson(sb.toString(), ProfileUpdateRequest.class);

                // 使用更新信息创建用户对象
                User user = new User();
                user.setUsername(username);
                user.setRealName(profileUpdate.getRealName());
                user.setPhone(profileUpdate.getPhone());
                user.setEmail(profileUpdate.getEmail());
                user.setAddress(profileUpdate.getAddress());

                // 更新数据库中的用户
                int result = userService.updateUser(user);
                if (result > 0) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    out.write(gson.toJson(new Response("success", "Profile updated successfully")));
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.write(gson.toJson(new Response("error", "Failed to update profile")));
                }
            } else if (relativeURI.equals("/api/profile/update-idcard")) {
                // 更新身份证信息
                IdCardUpdateRequest idCardUpdate = gson.fromJson(sb.toString(), IdCardUpdateRequest.class);

                // 使用更新后的身份证创建用户对象
                User user = new User();
                user.setUsername(username);
                user.setIdCard(idCardUpdate.getIdCard());

                // 更新数据库中的用户
                int result = userService.updateUser(user);
                if (result > 0) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    out.write(gson.toJson(new Response("success", "ID card updated successfully")));
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.write(gson.toJson(new Response("error", "Failed to update ID card")));
                }
            } else if (relativeURI.startsWith("/api/residents/")) {
                // 获取指定住户 ID
                String[] parts = relativeURI.split("/");
                if (parts.length != 4) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "Invalid resident ID")));
                    return;
                }
                
                int residentId = Integer.parseInt(parts[3]);
                
                // 判断是否为状态更新请求
                if (req.getParameter("action") != null && req.getParameter("action").equals("status")) {
                    // 更新用户状态（启用/禁用）
                    UpdateStatusRequest updateStatusRequest = gson.fromJson(sb.toString(), UpdateStatusRequest.class);
                    int status = updateStatusRequest.getStatus();
                    
                    // 更新数据库状态
                    int result = userService.updateUserStatus(residentId, status);
                    if (result > 0) {
                        resp.setStatus(HttpServletResponse.SC_OK);
                        out.write(gson.toJson(new Response("success", "Resident status updated successfully")));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        out.write(gson.toJson(new Response("error", "Failed to update resident status")));
                    }
                } else {
                    // 更新住户信息
                    UpdateResidentRequest updateResidentRequest = gson.fromJson(sb.toString(), UpdateResidentRequest.class);
                    
                    // 获取现有用户
                    User user = userService.getUserById(residentId);
                    if (user == null) {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.write(gson.toJson(new Response("error", "Resident not found")));
                        return;
                    }
                    
                    // 更新用户字段
                    user.setRealName(updateResidentRequest.getRealName());
                    user.setPhone(updateResidentRequest.getPhone());
                    user.setEmail(updateResidentRequest.getEmail());
                    user.setIdCard(updateResidentRequest.getIdCard());
                    user.setAddress(updateResidentRequest.getAddress());
                    
                    // 更新数据库中的用户
                    int result = userService.updateUser(user);
                    if (result > 0) {
                        resp.setStatus(HttpServletResponse.SC_OK);
                        out.write(gson.toJson(new Response("success", "Resident updated successfully")));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        out.write(gson.toJson(new Response("error", "Failed to update resident")));
                    }
                }
            } else if (relativeURI.startsWith("/api/providers/")) {
                // 获取指定服务商 ID
                String[] parts = relativeURI.split("/");
                if (parts.length != 4) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "Invalid provider ID")));
                    return;
                }
                
                int providerId = Integer.parseInt(parts[3]);
                
                // 判断是否为状态更新请求
                if (req.getParameter("action") != null && req.getParameter("action").equals("status")) {
                    // 更新用户状态（启用/禁用）
                    UpdateStatusRequest updateStatusRequest = gson.fromJson(sb.toString(), UpdateStatusRequest.class);
                    int status = updateStatusRequest.getStatus();
                    
                    // 更新数据库状态
                    int result = userService.updateUserStatus(providerId, status);
                    if (result > 0) {
                        resp.setStatus(HttpServletResponse.SC_OK);
                        out.write(gson.toJson(new Response("success", "Provider status updated successfully")));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        out.write(gson.toJson(new Response("error", "Failed to update provider status")));
                    }
                } else {
                    // 更新服务商信息
                    UpdateResidentRequest updateProviderRequest = gson.fromJson(sb.toString(), UpdateResidentRequest.class);
                    
                    // 获取现有用户
                    User user = userService.getUserById(providerId);
                    if (user == null) {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.write(gson.toJson(new Response("error", "Provider not found")));
                        return;
                    }
                    
                    // 更新用户字段
                    user.setRealName(updateProviderRequest.getRealName());
                    user.setPhone(updateProviderRequest.getPhone());
                    user.setEmail(updateProviderRequest.getEmail());
                    user.setIdCard(updateProviderRequest.getIdCard());
                    user.setAddress(updateProviderRequest.getAddress());
                    
                    // 更新数据库中的用户
                    int result = userService.updateUser(user);
                    if (result > 0) {
                        resp.setStatus(HttpServletResponse.SC_OK);
                        out.write(gson.toJson(new Response("success", "Provider updated successfully")));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        out.write(gson.toJson(new Response("error", "Failed to update provider")));
                    }
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write(gson.toJson(new Response("error", "API not found")));
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(new Response("error", "Internal server error: " + e.getMessage())));
            e.printStackTrace();
        } finally {
            out.flush();
            out.close();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=utf-8");
        PrintWriter out = resp.getWriter();

        try {
            // 从 JWT 过滤器获取用户名
            String username = (String) req.getAttribute("username");
            if (username == null || username.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write(gson.toJson(new Response("error", "Unauthorized")));
                return;
            }

            // 读取请求体
            BufferedReader reader = req.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            // 判断是否为修改密码请求
            String requestURI = req.getRequestURI();
            String contextPath = req.getContextPath();
            String relativeURI = requestURI.substring(contextPath.length());
            if (relativeURI.equals("/api/change-password")) {
                // 修改密码
                PasswordChangeRequest passwordChange = gson.fromJson(sb.toString(), PasswordChangeRequest.class);

                // 校验请求
                if (passwordChange.getOldPassword() == null || passwordChange.getOldPassword().isEmpty() ||
                        passwordChange.getNewPassword() == null || passwordChange.getNewPassword().isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "Old password and new password are required")));
                    return;
                }

                // 修改密码
                boolean result = userService.changePassword(username, passwordChange.getOldPassword(), passwordChange.getNewPassword());
                if (result) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    out.write(gson.toJson(new Response("success", "Password changed successfully")));
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "Failed to change password")));
                }
            } else if (relativeURI.equals("/api/residents")) {
                // 添加新住户
                AddResidentRequest addResidentRequest = gson.fromJson(sb.toString(), AddResidentRequest.class);
                
                // 创建用户对象
                User user = new User();
                user.setUsername(addResidentRequest.getUsername());
                user.setPassword(addResidentRequest.getPassword());
                user.setRealName(addResidentRequest.getRealName());
                user.setPhone(addResidentRequest.getPhone());
                user.setEmail(addResidentRequest.getEmail());
                user.setIdCard(addResidentRequest.getIdCard());
                user.setAddress(addResidentRequest.getAddress());
                user.setRole("resident"); // 设置角色为住户
                
                // 添加用户
                int result = userService.addUser(user);
                if (result > 0) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    out.write(gson.toJson(new Response("success", "Resident added successfully")));
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.write(gson.toJson(new Response("error", "Failed to add resident")));
                }
            } else if (relativeURI.equals("/api/providers")) {
                // 添加新服务商
                AddResidentRequest addProviderRequest = gson.fromJson(sb.toString(), AddResidentRequest.class);
                
                // 创建用户对象
                User user = new User();
                user.setUsername(addProviderRequest.getUsername());
                user.setPassword(addProviderRequest.getPassword());
                user.setRealName(addProviderRequest.getRealName());
                user.setPhone(addProviderRequest.getPhone());
                user.setEmail(addProviderRequest.getEmail());
                user.setIdCard(addProviderRequest.getIdCard());
                user.setAddress(addProviderRequest.getAddress());
                user.setRole("service_provider"); // 设置角色为服务商
                
                // 添加用户
                int result = userService.addUser(user);
                if (result > 0) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    out.write(gson.toJson(new Response("success", "Provider added successfully")));
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.write(gson.toJson(new Response("error", "Failed to add provider")));
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write(gson.toJson(new Response("error", "API not found")));
            }

        } catch (RuntimeException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(gson.toJson(new Response("error", e.getMessage())));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(new Response("error", "Internal server error: " + e.getMessage())));
            e.printStackTrace();
        } finally {
            out.flush();
            out.close();
        }
    }

    // 请求类
    private static class ProfileUpdateRequest {
        private String realName;
        private String phone;
        private String email;
        private String address;

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

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

    private static class IdCardUpdateRequest {
        private String idCard;

        public String getIdCard() {
            return idCard;
        }

        public void setIdCard(String idCard) {
            this.idCard = idCard;
        }
    }

    private static class PasswordChangeRequest {
        private String oldPassword;
        private String newPassword;

        public String getOldPassword() {
            return oldPassword;
        }

        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }

    private static class AddResidentRequest {
        private String username;
        private String password;
        private String realName;
        private String phone;
        private String email;
        private String idCard;
        private String address;

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

        public String getIdCard() {
            return idCard;
        }

        public void setIdCard(String idCard) {
            this.idCard = idCard;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

    private static class UpdateStatusRequest {
        private int status;

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }

    private static class UpdateResidentRequest {
        private String realName;
        private String phone;
        private String email;
        private String idCard;
        private String address;

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

        public String getIdCard() {
            return idCard;
        }

        public void setIdCard(String idCard) {
            this.idCard = idCard;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

    // 响应类
        private static class UserResponse {
            private int id;
            private String username;
            private String realName;
            private String phone;
            private String email;
            private String idCard;
            private String address;
            private String role;
            private int status;
            private Object createdAt;
            private Object updatedAt;

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

            public String getIdCard() {
                return idCard;
            }

            public void setIdCard(String idCard) {
                this.idCard = idCard;
            }

            public String getAddress() {
                return address;
            }

            public void setAddress(String address) {
                this.address = address;
            }

            public String getRole() {
                return role;
            }

            public void setRole(String role) {
                this.role = role;
            }

            public int getStatus() {
                return status;
            }

            public void setStatus(int status) {
                this.status = status;
            }

            public Object getCreatedAt() {
                return createdAt;
            }

            public void setCreatedAt(Object createdAt) {
                this.createdAt = createdAt;
            }

            public Object getUpdatedAt() {
                return updatedAt;
            }

            public void setUpdatedAt(Object updatedAt) {
                this.updatedAt = updatedAt;
            }
        }

        private static class Response {
            private String status;
            private String message;
            private Object data;

            public Response(String status, String message) {
                this.status = status;
                this.message = message;
            }

            public Response(String status, String message, Object data) {
                this.status = status;
                this.message = message;
                this.data = data;
            }

            public String getStatus() {
                return status;
            }

            public void setStatus(String status) {
                this.status = status;
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
