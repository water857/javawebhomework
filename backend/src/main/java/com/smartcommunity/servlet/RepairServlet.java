package com.smartcommunity.servlet;

import com.google.gson.Gson;
import com.smartcommunity.entity.Repair;
import com.smartcommunity.entity.RepairEvaluation;
import com.smartcommunity.entity.User;
import com.smartcommunity.service.RepairService;
import com.smartcommunity.service.UserService;
import com.smartcommunity.service.impl.RepairServiceImpl;
import com.smartcommunity.service.impl.UserServiceImpl;
import com.smartcommunity.util.RoleConstants;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.util.List;
import java.util.UUID;

@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
    maxFileSize = 1024 * 1024 * 10,       // 10MB
    maxRequestSize = 1024 * 1024 * 50      // 50MB
)public class RepairServlet extends HttpServlet {
    private RepairService repairService = new RepairServiceImpl();
    private UserService userService = new UserServiceImpl();
    private Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 设置CORS响应头
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization, Content-Disposition");
        resp.setHeader("Access-Control-Max-Age", "3600");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 设置CORS响应头
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setContentType("application/json;charset=utf-8");
        PrintWriter out = resp.getWriter();

        try {
            // Get request URI
            String requestURI = req.getRequestURI();
            String contextPath = req.getContextPath();
            String relativeURI = requestURI.substring(contextPath.length());

            // Get user information from JWT filter
            String username = (String) req.getAttribute("username");
            String role = (String) req.getAttribute("role");
            User currentUser = userService.getUserByUsername(username);

            if (relativeURI.equals("/api/repair/list")) {
                // 获取搜索参数
                String searchKeyword = req.getParameter("searchKeyword");
                String status = req.getParameter("status");
                String residentIdParam = req.getParameter("residentId");
                Integer residentId = null;
                if (residentIdParam != null && !residentIdParam.isEmpty()) {
                    residentId = Integer.parseInt(residentIdParam);
                }
                
                List<Repair> repairs;
                if (RoleConstants.RESIDENT.equals(role)) {
                    repairs = repairService.getRepairsByResidentId(currentUser.getId(), searchKeyword, status);
                } else if (RoleConstants.SERVICE_PROVIDER.equals(role)) {
                    repairs = repairService.getRepairsByServiceProviderId(currentUser.getId(), searchKeyword, status);
                } else if (RoleConstants.PROPERTY_ADMIN.equals(role)) {
                    repairs = repairService.getAllRepairs(searchKeyword, status, residentId);
                } else {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(new Response("error", "Access denied")));
                    return;
                }
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "Repairs retrieved successfully", repairs)));
                return;
            } else if (relativeURI.matches("^/api/repair/\\d+$") ) {
                // Handle /api/repair/{id} - direct repair ID access
                String[] parts = relativeURI.split("/");
                int repairId = Integer.parseInt(parts[parts.length - 1]);
                Repair repair = repairService.getRepairById(repairId);
                if (repair == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.write(gson.toJson(new Response("error", "Repair not found")));
                    return;
                }

                // Check permission
                if (!hasPermission(repair, currentUser.getId(), role)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(new Response("error", "Access denied")));
                    return;
                }

                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "Repair retrieved successfully", repair)));
                return;
            } else if (relativeURI.startsWith("/api/repair/detail/")) {
                // Get repair detail by ID
                String[] parts = relativeURI.split("/");
                if (parts.length < 5 || parts[4] == null || parts[4].isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "Invalid repair ID")));
                    return;
                }

                int repairId = Integer.parseInt(parts[4]);
                Repair repair = repairService.getRepairById(repairId);
                if (repair == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.write(gson.toJson(new Response("error", "Repair not found")));
                    return;
                }

                // Check permission
                if (!hasPermission(repair, currentUser.getId(), role)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(new Response("error", "Access denied")));
                    return;
                }

                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "Repair detail retrieved successfully", repair)));
                return;
            }

            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.write(gson.toJson(new Response("error", "API not found")));
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
        // 设置CORS响应头
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setContentType("application/json;charset=utf-8");
        PrintWriter out = resp.getWriter();

        try {
            // Get user information from JWT filter
            String username = (String) req.getAttribute("username");
            String role = (String) req.getAttribute("role");
            User currentUser = userService.getUserByUsername(username);

            // Get request URI
            String requestURI = req.getRequestURI();
            String contextPath = req.getContextPath();
            String relativeURI = requestURI.substring(contextPath.length());

            // Handle image upload
            if (relativeURI.equals("/api/repair/upload")) {
                try {
                    Part filePart = req.getPart("image");
                    if (filePart == null || filePart.getSize() == 0) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.write(gson.toJson(new Response("error", "No image file found")));
                        return;
                    }

                    // Generate unique filename with safe characters only
                    String originalFileName = getFileName(filePart);
                    // 使用UUID生成唯一前缀，避免文件名冲突，同时保留文件扩展名
                    String extension = "";
                    int lastDotIndex = originalFileName.lastIndexOf('.');
                    if (lastDotIndex != -1) {
                        extension = originalFileName.substring(lastDotIndex);
                    }
                    String fileName = UUID.randomUUID().toString() + extension;

                    // Define upload directory
                    String uploadDir = getServletContext().getRealPath("/uploads");
                    File directory = new File(uploadDir);
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }

                    // Save file
                    String filePath = uploadDir + File.separator + fileName;
                    try (InputStream input = filePart.getInputStream();
                         OutputStream output = new FileOutputStream(filePath)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = input.read(buffer)) != -1) {
                            output.write(buffer, 0, bytesRead);
                        }
                    }

                    // Return image URL with frontend proxy path - no need for encoding since we're using UUID
                    String imageUrl = "/uploads/" + fileName;
                    resp.setStatus(HttpServletResponse.SC_OK);
                    out.write(gson.toJson(new Response("success", "Image uploaded successfully", imageUrl)));
                    return;
                } catch (Exception e) {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.write(gson.toJson(new Response("error", "Image upload failed: " + e.getMessage())));
                    e.printStackTrace();
                    return;
                }
            }

            // Read request body for non-multipart requests
            BufferedReader reader = req.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            if (relativeURI.equals("/api/repair/submit")) {
                // Submit repair request
                if (!RoleConstants.RESIDENT.equals(role)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(new Response("error", "Only residents can submit repair requests")));
                    return;
                }

                SubmitRepairRequest submitRequest = gson.fromJson(sb.toString(), SubmitRepairRequest.class);
                
                // Validate required fields
                if (submitRequest.getTitle() == null || submitRequest.getTitle().trim().isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "Title is required")));
                    return;
                }
                
                if (submitRequest.getContent() == null || submitRequest.getContent().trim().isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "Content is required")));
                    return;
                }
                
                // Create repair object
                Repair repair = new Repair();
                repair.setResidentId(currentUser.getId());
                repair.setTitle(submitRequest.getTitle().trim());
                repair.setContent(submitRequest.getContent().trim());

                // Save repair
                Repair savedRepair = repairService.submitRepair(repair, submitRequest.getImages());
                
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "Repair request submitted successfully", savedRepair)));
                return;
            } else if (relativeURI.startsWith("/api/repair/evaluate/")) {
                // Add repair evaluation
                String[] parts = relativeURI.split("/");
                // For path /api/repair/evaluate/13, split results in ["", "api", "repair", "evaluate", "13"] (length 5)
                if (parts.length < 5 || parts[4] == null || parts[4].isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "Invalid repair ID")));
                    return;
                }

                int repairId = Integer.parseInt(parts[4]);
                RepairEvaluationRequest evalRequest = gson.fromJson(sb.toString(), RepairEvaluationRequest.class);

                // Add evaluation
                RepairEvaluation evaluation = repairService.addRepairEvaluation(
                        repairId,
                        currentUser.getId(),
                        evalRequest.getRating(),
                        evalRequest.getContent()
                );

                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "Repair evaluation added successfully", evaluation)));
                return;
            }

            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.write(gson.toJson(new Response("error", "API not found")));
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

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 设置CORS响应头
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setContentType("application/json;charset=utf-8");
        PrintWriter out = resp.getWriter();

        try {
            // Get user information from JWT filter
            String username = (String) req.getAttribute("username");
            String role = (String) req.getAttribute("role");
            User currentUser = userService.getUserByUsername(username);

            // Get request URI
            String requestURI = req.getRequestURI();
            String contextPath = req.getContextPath();
            String relativeURI = requestURI.substring(contextPath.length());

            if (relativeURI.startsWith("/api/repair/")) {
                // Withdraw repair request
                String[] parts = relativeURI.split("/");
                if (parts.length < 4) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "Invalid repair ID")));
                    return;
                }

                int repairId = Integer.parseInt(parts[parts.length - 1]);
                
                // Check if repair exists and belongs to current user
                Repair repair = repairService.getRepairById(repairId);
                if (repair == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.write(gson.toJson(new Response("error", "Repair not found")));
                    return;
                }

                // Only residents can withdraw their own repairs and only if status is pending
                if (!RoleConstants.RESIDENT.equals(role) || repair.getResidentId() != currentUser.getId()) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(new Response("error", "Access denied")));
                    return;
                }

                if (!"pending".equals(repair.getStatus())) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "Only pending repairs can be withdrawn")));
                    return;
                }

                // Delete repair from database
                repairService.deleteRepair(repairId, currentUser.getId());
                
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "Repair request withdrawn successfully")));
                return;
            }

            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.write(gson.toJson(new Response("error", "API not found")));
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

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 设置CORS响应头
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setContentType("application/json;charset=utf-8");
        PrintWriter out = resp.getWriter();

        try {
            // Get user information from JWT filter
            String username = (String) req.getAttribute("username");
            String role = (String) req.getAttribute("role");
            User currentUser = userService.getUserByUsername(username);

            // Read request body
            BufferedReader reader = req.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            // Get request URI
            String requestURI = req.getRequestURI();
            String contextPath = req.getContextPath();
            String relativeURI = requestURI.substring(contextPath.length());

            if (relativeURI.startsWith("/api/repair/status/")) {
                // Update repair status
                String[] parts = relativeURI.split("/");
                if (parts.length < 5) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "Invalid repair ID")));
                    return;
                }

                int repairId = Integer.parseInt(parts[parts.length - 1]);
                UpdateStatusRequest statusRequest = gson.fromJson(sb.toString(), UpdateStatusRequest.class);

                // Update status
                Repair updatedRepair = repairService.updateRepairStatus(
                        repairId,
                        statusRequest.getStatus(),
                        currentUser.getId(),
                        role
                );

                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "Repair status updated successfully", updatedRepair)));
                return;
            } else if (relativeURI.startsWith("/api/repair/assign/")) {
                // Assign repair to service provider
                if (!RoleConstants.PROPERTY_ADMIN.equals(role)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(new Response("error", "Only property admins can assign repair tasks")));
                    return;
                }

                String[] parts = relativeURI.split("/");
                if (parts.length < 5) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "Invalid repair ID")));
                    return;
                }

                int repairId = Integer.parseInt(parts[parts.length - 1]);
                AssignRepairRequest assignRequest = gson.fromJson(sb.toString(), AssignRepairRequest.class);

                // Assign repair
                Repair assignedRepair = repairService.assignRepair(
                        repairId,
                        assignRequest.getProviderId(),
                        currentUser.getId()
                );

                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "Repair assigned successfully", assignedRepair)));
                return;
            }

            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.write(gson.toJson(new Response("error", "API not found")));
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

    // Helper method to get filename from Content-Disposition header
    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition == null) {
            return ""; // Fallback if header is missing
        }
        for (String cd : contentDisposition.split("[;]", -1)) {
            cd = cd.trim();
            if (cd.startsWith("filename=")) {
                String filename = cd.substring("filename=".length());
                // Remove surrounding quotes
                filename = filename.replaceAll("^\"+|\"+$|^'+|'+$", "");
                // Extract just the filename without path (for security)
                int lastSeparator = filename.lastIndexOf(File.separator);
                if (lastSeparator != -1) {
                    filename = filename.substring(lastSeparator + 1);
                }
                return filename;
            }
        }
        return ""; // Fallback if filename not found
    }

    // Check if user has permission to access repair
    private boolean hasPermission(Repair repair, int userId, String role) {
        if (RoleConstants.RESIDENT.equals(role)) {
            return repair.getResidentId() == userId;
        } else if (RoleConstants.SERVICE_PROVIDER.equals(role)) {
            return repair.getAssignedTo() != null && repair.getAssignedTo() == userId;
        } else if (RoleConstants.PROPERTY_ADMIN.equals(role)) {
            return true;
        }
        return false;
    }

    // Request and Response classes
    private static class SubmitRepairRequest {
        private String title;
        private String content;
        private List<String> images;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public List<String> getImages() {
            return images;
        }

        public void setImages(List<String> images) {
            this.images = images;
        }
    }

    private static class UpdateStatusRequest {
        private String status;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    private static class AssignRepairRequest {
        private int providerId;

        public int getProviderId() {
            return providerId;
        }

        public void setProviderId(int providerId) {
            this.providerId = providerId;
        }
    }

    private static class RepairEvaluationRequest {
        private int rating;
        private String content;

        public int getRating() {
            return rating;
        }

        public void setRating(int rating) {
            this.rating = rating;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
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