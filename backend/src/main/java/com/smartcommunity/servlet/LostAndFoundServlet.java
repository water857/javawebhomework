package com.smartcommunity.servlet;

import com.google.gson.Gson;
import com.smartcommunity.dao.LostAndFoundDAO;
import com.smartcommunity.dao.impl.LostAndFoundDAOImpl;
import com.smartcommunity.entity.LostAndFound;
import com.smartcommunity.entity.User;
import com.smartcommunity.service.UserService;
import com.smartcommunity.service.impl.UserServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Map;

@WebServlet(name = "LostAndFoundServlet", urlPatterns = "/api/lost-found/*")
public class LostAndFoundServlet extends HttpServlet {
    private final LostAndFoundDAO lostAndFoundDAO = new LostAndFoundDAOImpl();
    private final UserService userService = new UserServiceImpl();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=utf-8");
        PrintWriter out = resp.getWriter();
        try {
            String requestURI = req.getRequestURI();
            String contextPath = req.getContextPath();
            String relativeURI = requestURI.substring(contextPath.length());

            if (!relativeURI.equals("/api/lost-found/list")) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write(gson.toJson(new Response("error", "未知的接口")));
                return;
            }

            String type = req.getParameter("type");
            resp.setStatus(HttpServletResponse.SC_OK);
            out.write(gson.toJson(new Response("success", "获取失物招领列表成功", lostAndFoundDAO.getRecords(type))));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(new Response("error", "服务器内部错误: " + e.getMessage())));
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
            String requestURI = req.getRequestURI();
            String contextPath = req.getContextPath();
            String relativeURI = requestURI.substring(contextPath.length());

            if (!relativeURI.equals("/api/lost-found/publish")) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write(gson.toJson(new Response("error", "未知的接口")));
                return;
            }

            PublishRequest publishRequest = gson.fromJson(req.getReader(), PublishRequest.class);
            if (publishRequest == null || publishRequest.getTitle() == null || publishRequest.getTitle().isEmpty() || publishRequest.getType() == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(gson.toJson(new Response("error", "参数不完整")));
                return;
            }

            String username = (String) req.getAttribute("username");
            User currentUser = userService.getUserByUsername(username);
            if (currentUser == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.write(gson.toJson(new Response("error", "用户未登录")));
                return;
            }

            LostAndFound record = new LostAndFound();
            record.setUserId(currentUser.getId());
            record.setType(publishRequest.getType());
            record.setTitle(publishRequest.getTitle());
            record.setDescription(publishRequest.getDescription());
            record.setContact(publishRequest.getContact());
            record.setCreateTime(new Timestamp(System.currentTimeMillis()));
            int recordId = lostAndFoundDAO.createRecord(record);
            resp.setStatus(HttpServletResponse.SC_OK);
            out.write(gson.toJson(new Response("success", "发布成功", Map.of("recordId", recordId))));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(new Response("error", "服务器内部错误: " + e.getMessage())));
        } finally {
            out.flush();
            out.close();
        }
    }

    private static class PublishRequest {
        private String type;
        private String title;
        private String description;
        private String contact;

        public String getType() {
            return type;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getContact() {
            return contact;
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
    }
}
