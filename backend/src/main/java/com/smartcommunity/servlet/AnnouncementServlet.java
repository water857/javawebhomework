package com.smartcommunity.servlet;

import com.google.gson.Gson;
import com.smartcommunity.entity.Announcement;
import com.smartcommunity.entity.User;
import com.smartcommunity.service.AnnouncementService;
import com.smartcommunity.service.UserService;
import com.smartcommunity.service.impl.AnnouncementServiceImpl;
import com.smartcommunity.service.impl.UserServiceImpl;
import com.smartcommunity.util.GsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.List;

@WebServlet(name = "AnnouncementServlet", urlPatterns = "/api/announcements/*")
public class AnnouncementServlet extends HttpServlet {
    private AnnouncementService announcementService = new AnnouncementServiceImpl();
    private UserService userService = new UserServiceImpl();
    private Gson gson = GsonUtil.getGson();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 设置CORS响应头
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization");
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
            // 获取请求URI
            String requestURI = req.getRequestURI();
            String contextPath = req.getContextPath();
            String relativeURI = requestURI.substring(contextPath.length());

            // 从JWT过滤器获取用户信息（可能为null，因为公告列表允许匿名访问）
            String username = (String) req.getAttribute("username");
            User currentUser = username != null ? userService.getUserByUsername(username) : null;

            if (relativeURI.equals("/api/announcements")) {
                // 获取公告列表
                String pageStr = req.getParameter("page");
                String pageSizeStr = req.getParameter("pageSize");
                String search = req.getParameter("search");
                String statusStr = req.getParameter("status");
                
                int page = pageStr != null ? Integer.parseInt(pageStr) : 1;
                int pageSize = pageSizeStr != null ? Integer.parseInt(pageSizeStr) : 10;
                Integer status = statusStr != null ? Integer.parseInt(statusStr) : null;
                
                List<Announcement> announcements = announcementService.getAnnouncements(page, pageSize, search, status);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "获取公告列表成功", announcements)));
            } else if (relativeURI.startsWith("/api/announcements/latest")) {
                // 获取最新发布的公告列表
                String limitStr = req.getParameter("limit");
                int limit = limitStr != null ? Integer.parseInt(limitStr) : 5;
                
                List<Announcement> announcements = announcementService.getLatestPublishedAnnouncements(limit);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "获取最新公告成功", announcements)));
            } else if (relativeURI.startsWith("/api/announcements/author/")) {
                // 获取发布者的公告列表
                String[] parts = relativeURI.split("/");
                if (parts.length < 5) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "无效的发布者ID")));
                    return;
                }
                
                int authorId = Integer.parseInt(parts[4]);
                String pageStr = req.getParameter("page");
                String pageSizeStr = req.getParameter("pageSize");
                String search = req.getParameter("search");
                String statusStr = req.getParameter("status");
                
                int page = pageStr != null ? Integer.parseInt(pageStr) : 1;
                int pageSize = pageSizeStr != null ? Integer.parseInt(pageSizeStr) : 10;
                Integer status = statusStr != null ? Integer.parseInt(statusStr) : null;
                
                List<Announcement> announcements = announcementService.getAuthorAnnouncements(authorId, page, pageSize, search, status);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "获取发布者公告列表成功", announcements)));
            } else if (relativeURI.startsWith("/api/announcements/")) {
                // 获取公告详情
                String[] parts = relativeURI.split("/");
                if (parts.length < 4) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "无效的公告ID")));
                    return;
                }
                
                int announcementId = Integer.parseInt(parts[3]);
                Announcement announcement = announcementService.getAnnouncementDetail(announcementId);
                
                if (announcement != null) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    out.write(gson.toJson(new Response("success", "获取公告详情成功", announcement)));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.write(gson.toJson(new Response("error", "公告不存在")));
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write(gson.toJson(new Response("error", "接口不存在")));
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(gson.toJson(new Response("error", "无效的数字参数")));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(new Response("error", "服务器内部错误: " + e.getMessage())));
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
            // 从JWT过滤器获取用户信息
            String username = (String) req.getAttribute("username");
            if (username == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write(gson.toJson(new Response("error", "未登录，无法发布公告")));
                return;
            }
            
            User currentUser = userService.getUserByUsername(username);
            if (currentUser == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write(gson.toJson(new Response("error", "用户不存在，无法发布公告")));
                return;
            }
            
            // 检查用户权限（只有物业管理员可以发布公告）
            if (!currentUser.getRole().equals("property_admin")) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.write(gson.toJson(new Response("error", "只有物业管理员可以发布公告")));
                return;
            }

            // 读取请求体
            StringBuilder jsonBuffer = new StringBuilder();
            BufferedReader reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuffer.append(line);
            }
            reader.close();

            // 解析请求体
            AnnouncementRequest announcementRequest = gson.fromJson(jsonBuffer.toString(), AnnouncementRequest.class);
            if (announcementRequest == null || announcementRequest.title == null || announcementRequest.content == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(gson.toJson(new Response("error", "缺少必要的公告信息")));
                return;
            }

            // 创建公告对象
            Announcement announcement = new Announcement();
            announcement.setTitle(announcementRequest.title);
            announcement.setContent(announcementRequest.content);
            announcement.setAuthorId(currentUser.getId());
            announcement.setAuthorName(currentUser.getRealName());

            Announcement result;
            if (announcementRequest.action != null && announcementRequest.action.equals("saveDraft")) {
                // 保存草稿
                result = announcementService.saveDraft(announcement);
            } else {
                // 发布公告
                result = announcementService.publishAnnouncement(announcement);
            }

            if (result != null) {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                out.write(gson.toJson(new Response("success", "公告操作成功", result)));
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write(gson.toJson(new Response("error", "公告操作失败")));
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(new Response("error", "服务器内部错误: " + e.getMessage())));
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
            // 从JWT过滤器获取用户信息
            String username = (String) req.getAttribute("username");
            if (username == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write(gson.toJson(new Response("error", "未登录，无法更新公告")));
                return;
            }
            
            User currentUser = userService.getUserByUsername(username);
            if (currentUser == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write(gson.toJson(new Response("error", "用户不存在，无法更新公告")));
                return;
            }

            // 获取请求URI
            String requestURI = req.getRequestURI();
            String contextPath = req.getContextPath();
            String relativeURI = requestURI.substring(contextPath.length());

            if (relativeURI.startsWith("/api/announcements/status/")) {
                // 更新公告状态
                String[] parts = relativeURI.split("/");
                if (parts.length < 5) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "无效的公告ID")));
                    return;
                }
                
                int announcementId = Integer.parseInt(parts[4]);
                
                // 读取请求体
                StringBuilder jsonBuffer = new StringBuilder();
                BufferedReader reader = req.getReader();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuffer.append(line);
                }
                reader.close();
                
                StatusUpdateRequest statusRequest = gson.fromJson(jsonBuffer.toString(), StatusUpdateRequest.class);
                if (statusRequest == null || statusRequest.status == null) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "缺少必要的状态信息")));
                    return;
                }
                
                // 检查用户是否有权限更新该公告（物业管理员可以更新所有公告，普通用户只能更新自己的公告）
                if (!currentUser.getRole().equals("property_admin") && !announcementService.isAuthor(announcementId, currentUser.getId())) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(new Response("error", "没有权限更新该公告")));
                    return;
                }
                
                boolean success = announcementService.updateAnnouncementStatus(announcementId, statusRequest.status, currentUser.getId());
                if (success) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    out.write(gson.toJson(new Response("success", "公告状态更新成功")));
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.write(gson.toJson(new Response("error", "公告状态更新失败")));
                }
            } else if (relativeURI.startsWith("/api/announcements/")) {
                // 更新公告详情
                String[] parts = relativeURI.split("/");
                if (parts.length < 4) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "无效的公告ID")));
                    return;
                }
                
                int announcementId = Integer.parseInt(parts[3]);
                
                // 读取请求体
                StringBuilder jsonBuffer = new StringBuilder();
                BufferedReader reader = req.getReader();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuffer.append(line);
                }
                reader.close();
                
                AnnouncementRequest announcementRequest = gson.fromJson(jsonBuffer.toString(), AnnouncementRequest.class);
                if (announcementRequest == null || announcementRequest.title == null || announcementRequest.content == null) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "缺少必要的公告信息")));
                    return;
                }
                
                // 获取现有公告
                Announcement existingAnnouncement = announcementService.getAnnouncementDetail(announcementId);
                if (existingAnnouncement == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.write(gson.toJson(new Response("error", "公告不存在")));
                    return;
                }
                
                // 检查用户是否有权限更新该公告（物业管理员可以更新所有公告，普通用户只能更新自己的公告）
                if (!currentUser.getRole().equals("property_admin") && !announcementService.isAuthor(announcementId, currentUser.getId())) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(new Response("error", "没有权限更新该公告")));
                    return;
                }
                
                // 更新公告信息
                existingAnnouncement.setTitle(announcementRequest.title);
                existingAnnouncement.setContent(announcementRequest.content);
                existingAnnouncement.setAuthorName(currentUser.getRealName());
                
                // 更新公告状态，如果从草稿变为已发布，设置发布时间
                if (announcementRequest.status != null) {
                    int oldStatus = existingAnnouncement.getStatus();
                    int newStatus = announcementRequest.status;
                    existingAnnouncement.setStatus(newStatus);
                    
                    // 如果从草稿变为已发布，设置发布时间
                    if (oldStatus == 0 && newStatus == 1) {
                        existingAnnouncement.setPublishedAt(new Timestamp(System.currentTimeMillis()));
                    }
                }
                
                Announcement updatedAnnouncement = announcementService.updateAnnouncement(existingAnnouncement);
                if (updatedAnnouncement != null) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    out.write(gson.toJson(new Response("success", "公告更新成功", updatedAnnouncement)));
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.write(gson.toJson(new Response("error", "公告更新失败")));
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write(gson.toJson(new Response("error", "接口不存在")));
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(gson.toJson(new Response("error", "无效的数字参数")));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(new Response("error", "服务器内部错误: " + e.getMessage())));
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
            // 从JWT过滤器获取用户信息
            String username = (String) req.getAttribute("username");
            if (username == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write(gson.toJson(new Response("error", "未登录，无法删除公告")));
                return;
            }
            
            User currentUser = userService.getUserByUsername(username);
            if (currentUser == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write(gson.toJson(new Response("error", "用户不存在，无法删除公告")));
                return;
            }

            // 获取请求URI
            String requestURI = req.getRequestURI();
            String contextPath = req.getContextPath();
            String relativeURI = requestURI.substring(contextPath.length());

            if (relativeURI.startsWith("/api/announcements/")) {
                // 删除公告
                String[] parts = relativeURI.split("/");
                if (parts.length < 4) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "无效的公告ID")));
                    return;
                }
                
                int announcementId = Integer.parseInt(parts[3]);
                
                // 检查用户是否有权限删除该公告（物业管理员可以删除所有公告，普通用户只能删除自己的公告）
                if (!currentUser.getRole().equals("property_admin") && !announcementService.isAuthor(announcementId, currentUser.getId())) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(new Response("error", "没有权限删除该公告")));
                    return;
                }
                
                boolean success = announcementService.deleteAnnouncement(announcementId, currentUser.getId());
                if (success) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    out.write(gson.toJson(new Response("success", "公告删除成功")));
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.write(gson.toJson(new Response("error", "公告删除失败")));
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write(gson.toJson(new Response("error", "接口不存在")));
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write(gson.toJson(new Response("error", "无效的数字参数")));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(new Response("error", "服务器内部错误: " + e.getMessage())));
            e.printStackTrace();
        } finally {
            out.flush();
            out.close();
        }
    }

    // 内部类：响应对象
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

    // 内部类：公告请求对象
    private static class AnnouncementRequest {
        private String title;
        private String content;
        private String action;
        private Integer status;

        // 访问器与设置器方法
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

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }

    // 内部类：状态更新请求对象
    private static class StatusUpdateRequest {
        private Integer status;

        // 访问器与设置器方法
        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }
}