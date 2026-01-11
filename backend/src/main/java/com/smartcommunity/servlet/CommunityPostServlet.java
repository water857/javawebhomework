package com.smartcommunity.servlet;

import com.google.gson.Gson;
import com.smartcommunity.entity.CommunityPost;
import com.smartcommunity.entity.PostComment;
import com.smartcommunity.entity.PostImage;
import com.smartcommunity.entity.Tag;
import com.smartcommunity.entity.User;
import com.smartcommunity.service.CommunityPostService;
import com.smartcommunity.service.UserService;
import com.smartcommunity.service.impl.CommunityPostServiceImpl;
import com.smartcommunity.service.impl.UserServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2,  // 2MB
        maxFileSize = 1024 * 1024 * 10,       // 10MB
        maxRequestSize = 1024 * 1024 * 50      // 50MB
)public class CommunityPostServlet extends HttpServlet {
    private CommunityPostService postService = new CommunityPostServiceImpl();
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
            // 获取请求 URI
            String requestURI = req.getRequestURI();
            String contextPath = req.getContextPath();
            String relativeURI = requestURI.substring(contextPath.length());

            // 从 JWT 过滤器获取用户信息
            String username = (String) req.getAttribute("username");
            User currentUser = null;
            if (username != null) {
                currentUser = userService.getUserByUsername(username);
            }

            if (relativeURI.equals("/api/community/timeline")) {
                // 获取时间线
                String pageStr = req.getParameter("page");
                String pageSizeStr = req.getParameter("pageSize");
                int page = pageStr != null ? Integer.parseInt(pageStr) : 1;
                int pageSize = pageSizeStr != null ? Integer.parseInt(pageSizeStr) : 10;
                // 如果没有登录用户，使用默认用户ID或返回公共帖子
                int userId = currentUser != null ? currentUser.getId() : 1;
                List<CommunityPost> timeline = postService.getTimeline(userId, page, pageSize);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "获取时间线成功", timeline)));
                return;
            } else if (relativeURI.equals("/api/community/user-posts")) {
                // 获取用户动态
                int targetUserId = Integer.parseInt(req.getParameter("userId"));
                String pageStr = req.getParameter("page");
                String pageSizeStr = req.getParameter("pageSize");
                int page = pageStr != null ? Integer.parseInt(pageStr) : 1;
                int pageSize = pageSizeStr != null ? Integer.parseInt(pageSizeStr) : 10;
                List<CommunityPost> userPosts = postService.getUserPosts(targetUserId, page, pageSize);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "获取用户动态成功", userPosts)));
                return;
            } else if (relativeURI.startsWith("/api/community/post/comments/")) {
                // 获取动态评论
                String[] parts = relativeURI.split("/");
                // 正确的索引应该是最后一个部分，即评论ID
                if (parts.length < 6 || parts[5] == null || parts[5].isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "无效的动态ID")));
                    return;
                }
                int postId = Integer.parseInt(parts[5]);
                List<PostComment> comments = postService.getPostComments(postId);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "获取评论成功", comments)));
                return;
            } else if (relativeURI.startsWith("/api/community/post/")) {
                // 获取动态详情
                String[] parts = relativeURI.split("/");
                if (parts.length < 5 || parts[4] == null || parts[4].isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "无效的动态ID")));
                    return;
                }
                int postId = Integer.parseInt(parts[4]);
                // 如果没有登录用户，使用默认用户ID
                int userId = currentUser != null ? currentUser.getId() : 1;
                CommunityPost post = postService.getPostDetail(postId, userId);
                if (post == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.write(gson.toJson(new Response("error", "动态不存在")));
                    return;
                }
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "获取动态详情成功", post)));
                return;
            } else if (relativeURI.equals("/api/community/tags")) {
                // 获取所有标签
                List<Tag> tags = postService.getAllTags();
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "获取标签成功", tags)));
                return;
            } else if (relativeURI.startsWith("/api/community/tags/")) {
                // 获取标签下的动态
                String[] parts = relativeURI.split("/");
                if (parts.length < 5 || parts[4] == null || parts[4].isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "无效的标签ID")));
                    return;
                }
                int tagId = Integer.parseInt(parts[4]);
                String pageStr = req.getParameter("page");
                String pageSizeStr = req.getParameter("pageSize");
                int page = pageStr != null ? Integer.parseInt(pageStr) : 1;
                int pageSize = pageSizeStr != null ? Integer.parseInt(pageSizeStr) : 10;
                // 如果没有登录用户，使用默认用户ID
                int userId = currentUser != null ? currentUser.getId() : 1;
                List<CommunityPost> posts = postService.getPostsByTag(tagId, userId, page, pageSize);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "获取标签动态成功", posts)));
                return;
            } else if (relativeURI.equals("/api/community/notifications")) {
                // 获取用户通知
                String pageStr = req.getParameter("page");
                String pageSizeStr = req.getParameter("pageSize");
                int page = pageStr != null ? Integer.parseInt(pageStr) : 1;
                int pageSize = pageSizeStr != null ? Integer.parseInt(pageSizeStr) : 10;
                // 如果没有登录用户，使用默认用户ID
                int userId = currentUser != null ? currentUser.getId() : 1;
                List<com.smartcommunity.entity.Notification> notifications = postService.getUserNotifications(userId, page, pageSize);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "获取通知成功", notifications)));
                return;
            } else if (relativeURI.startsWith("/api/community/like/")) {
                // 检查是否点赞
                String[] parts = relativeURI.split("/");
                if (parts.length < 5 || parts[4] == null || parts[4].isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "无效的动态ID")));
                    return;
                }
                int postId = Integer.parseInt(parts[4]);
                // 如果没有登录用户，使用默认用户ID
                int userId = currentUser != null ? currentUser.getId() : 1;
                boolean isLiked = postService.isLiked(postId, userId);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "检查点赞状态成功", isLiked)));
                return;
            } else if (relativeURI.equals("/api/community/search")) {
                // 搜索动态
                String keyword = req.getParameter("keyword");
                String pageStr = req.getParameter("page");
                String pageSizeStr = req.getParameter("pageSize");
                int page = pageStr != null ? Integer.parseInt(pageStr) : 1;
                int pageSize = pageSizeStr != null ? Integer.parseInt(pageSizeStr) : 10;
                // 如果没有登录用户，使用默认用户ID
                int userId = currentUser != null ? currentUser.getId() : 1;
                List<CommunityPost> posts = postService.searchPosts(keyword, userId, page, pageSize);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "搜索动态成功", posts)));
                return;
            } else if (relativeURI.equals("/api/community/filter/time")) {
                // 按时间筛选动态
                String timeRange = req.getParameter("timeRange");
                String pageStr = req.getParameter("page");
                String pageSizeStr = req.getParameter("pageSize");
                int page = pageStr != null ? Integer.parseInt(pageStr) : 1;
                int pageSize = pageSizeStr != null ? Integer.parseInt(pageSizeStr) : 10;
                // 如果没有登录用户，使用默认用户ID
                int userId = currentUser != null ? currentUser.getId() : 1;
                List<CommunityPost> posts = postService.filterPostsByTime(userId, timeRange, page, pageSize);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "按时间筛选动态成功", posts)));
                return;
            } else if (relativeURI.equals("/api/community/filter/popularity")) {
                // 按热度筛选动态
                String pageStr = req.getParameter("page");
                String pageSizeStr = req.getParameter("pageSize");
                int page = pageStr != null ? Integer.parseInt(pageStr) : 1;
                int pageSize = pageSizeStr != null ? Integer.parseInt(pageSizeStr) : 10;
                // 如果没有登录用户，使用默认用户ID
                int userId = currentUser != null ? currentUser.getId() : 1;
                List<CommunityPost> posts = postService.filterPostsByPopularity(userId, page, pageSize);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "按热度筛选动态成功", posts)));
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
            // 从 JWT 过滤器获取用户信息
            String username = (String) req.getAttribute("username");
            User currentUser = userService.getUserByUsername(username);

            // 获取请求 URI
            String requestURI = req.getRequestURI();
            String contextPath = req.getContextPath();
            String relativeURI = requestURI.substring(contextPath.length());
            StringBuilder sb = new StringBuilder();

            // 处理图片上传 - 必须在读取请求体之前先检查
            if (relativeURI.equals("/api/community/upload")) {
                try {
                    Part filePart = req.getPart("image");
                    if (filePart == null || filePart.getSize() == 0) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.write(gson.toJson(new Response("error", "No image file found")));
                        return;
                    }

                    // 生成仅包含安全字符的唯一文件名
                    String originalFileName = getFileName(filePart);
                    // 使用UUID生成唯一前缀，避免文件名冲突，同时保留文件扩展名
                    String extension = "";
                    int lastDotIndex = originalFileName.lastIndexOf('.');
                    if (lastDotIndex != -1) {
                        extension = originalFileName.substring(lastDotIndex);
                    }
                    String fileName = UUID.randomUUID().toString() + extension;

                    // 定义上传目录
                    String uploadDir = getServletContext().getRealPath("/uploads");
                    File directory = new File(uploadDir);
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }

                    // 保存文件
                    String filePath = uploadDir + File.separator + fileName;
                    try (InputStream input = filePart.getInputStream();
                         OutputStream output = new FileOutputStream(filePath)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = input.read(buffer)) != -1) {
                            output.write(buffer, 0, bytesRead);
                        }
                    }

                    // 返回带前端代理路径的图片 URL，使用 UUID 无需编码
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
            } else {
                // 读取非 multipart 请求体
                BufferedReader reader = req.getReader();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
            }

            if (relativeURI.equals("/api/community/publish")) {
                // 发布动态
                PublishPostRequest publishRequest = gson.fromJson(sb.toString(), PublishPostRequest.class);
                
                // 验证必填字段
                if (publishRequest.getContent() == null || publishRequest.getContent().trim().isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "内容不能为空")));
                    return;
                }
                
                // 创建动态对象
                CommunityPost post = new CommunityPost();
                post.setUserId(currentUser.getId());
                post.setContent(publishRequest.getContent().trim());
                post.setPrivacy(publishRequest.getPrivacy() != null ? publishRequest.getPrivacy() : "public");
                
                // 保存动态
                CommunityPost savedPost = postService.publishPost(
                        post,
                        publishRequest.getImages(),
                        publishRequest.getTagIds(),
                        publishRequest.getVisibleUserIds()
                );
                
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "动态发布成功", savedPost)));
                return;
            } else if (relativeURI.startsWith("/api/community/like/")) {
                // 点赞/取消点赞
                String[] parts = relativeURI.split("/");
                if (parts.length < 5 || parts[4] == null || parts[4].isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "无效的动态ID")));
                    return;
                }
                int postId = Integer.parseInt(parts[4]);
                boolean result = postService.toggleLike(postId, currentUser.getId());
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", result ? "点赞成功" : "取消点赞成功", result)));
                return;
            } else if (relativeURI.startsWith("/api/community/comment/")) {
                // 添加评论
                String[] parts = relativeURI.split("/");
                if (parts.length < 5 || parts[4] == null || parts[4].isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "无效的动态ID")));
                    return;
                }
                int postId = Integer.parseInt(parts[4]);
                CommentRequest commentRequest = gson.fromJson(sb.toString(), CommentRequest.class);
                
                if (commentRequest.getContent() == null || commentRequest.getContent().trim().isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "评论内容不能为空")));
                    return;
                }
                
                PostComment comment = postService.addComment(
                        postId,
                        currentUser.getId(),
                        commentRequest.getContent().trim(),
                        commentRequest.getParentCommentId()
                );
                
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "评论添加成功", comment)));
                return;
            } else if (relativeURI.startsWith("/api/community/notification/read/")) {
                // 标记通知已读
                String[] parts = relativeURI.split("/");
                if (parts.length < 6 || parts[5] == null || parts[5].isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "无效的通知ID")));
                    return;
                }
                int notificationId = Integer.parseInt(parts[5]);
                boolean result = postService.markNotificationAsRead(notificationId, currentUser.getId());
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "标记通知已读成功", result)));
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
            // 从 JWT 过滤器获取用户信息
            String username = (String) req.getAttribute("username");
            User currentUser = userService.getUserByUsername(username);

            // 读取请求体
            BufferedReader reader = req.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            // 获取请求 URI
            String requestURI = req.getRequestURI();
            String contextPath = req.getContextPath();
            String relativeURI = requestURI.substring(contextPath.length());

            if (relativeURI.startsWith("/api/community/post/privacy/")) {
                // 更新动态隐私设置
                String[] parts = relativeURI.split("/");
                if (parts.length < 6 || parts[5] == null || parts[5].isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "无效的动态ID")));
                    return;
                }
                int postId = Integer.parseInt(parts[5]);
                PrivacyRequest privacyRequest = gson.fromJson(sb.toString(), PrivacyRequest.class);
                
                boolean result = postService.updatePostPrivacy(postId, currentUser.getId(), privacyRequest.getPrivacy());
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "隐私设置更新成功", result)));
                return;
            } else if (relativeURI.startsWith("/api/community/post/visible-users/")) {
                // 更新动态可见用户
                String[] parts = relativeURI.split("/");
                if (parts.length < 7 || parts[6] == null || parts[6].isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "无效的动态ID")));
                    return;
                }
                int postId = Integer.parseInt(parts[6]);
                VisibleUsersRequest visibleUsersRequest = gson.fromJson(sb.toString(), VisibleUsersRequest.class);
                
                boolean result = postService.updatePostVisibleUsers(postId, currentUser.getId(), visibleUsersRequest.getVisibleUserIds());
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "可见用户设置更新成功", result)));
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

    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition == null) {
            return "";
        }
        
        // 使用正则表达式安全地提取文件名
        String fileName = "";
        String[] parts = contentDisposition.split(";");
        for (String partStr : parts) {
            partStr = partStr.trim();
            if (partStr.startsWith("filename")) {
                // 处理filename="xxx"或filename=xxx格式
                int index = partStr.indexOf('=');
                if (index != -1) {
                    fileName = partStr.substring(index + 1).trim();
                    // 移除可能的引号
                    if (fileName.startsWith("\"")) {
                        fileName = fileName.substring(1);
                    }
                    if (fileName.endsWith("\"")) {
                        fileName = fileName.substring(0, fileName.length() - 1);
                    }
                    break;
                }
            }
        }
        return fileName;
    }

    // 辅助类
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
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }

    private static class PublishPostRequest {
        private String content;
        private String privacy;
        private List<String> images;
        private List<Integer> tagIds;
        private List<Integer> visibleUserIds;

        // 访问器与设置器方法
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public String getPrivacy() { return privacy; }
        public void setPrivacy(String privacy) { this.privacy = privacy; }
        public List<String> getImages() { return images; }
        public void setImages(List<String> images) { this.images = images; }
        public List<Integer> getTagIds() { return tagIds; }
        public void setTagIds(List<Integer> tagIds) { this.tagIds = tagIds; }
        public List<Integer> getVisibleUserIds() { return visibleUserIds; }
        public void setVisibleUserIds(List<Integer> visibleUserIds) { this.visibleUserIds = visibleUserIds; }
    }

    private static class CommentRequest {
        private String content;
        private Integer parentCommentId;

        // 访问器与设置器方法
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public Integer getParentCommentId() { return parentCommentId; }
        public void setParentCommentId(Integer parentCommentId) { this.parentCommentId = parentCommentId; }
    }

    private static class PrivacyRequest {
        private String privacy;

        // 访问器与设置器方法
        public String getPrivacy() { return privacy; }
        public void setPrivacy(String privacy) { this.privacy = privacy; }
    }

    private static class VisibleUsersRequest {
        private List<Integer> visibleUserIds;

        // 访问器与设置器方法
        public List<Integer> getVisibleUserIds() { return visibleUserIds; }
        public void setVisibleUserIds(List<Integer> visibleUserIds) { this.visibleUserIds = visibleUserIds; }
    }
}
