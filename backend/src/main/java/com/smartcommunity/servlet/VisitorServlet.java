package com.smartcommunity.servlet;

import com.google.gson.Gson;
import com.smartcommunity.dao.VisitorRecordDAO;
import com.smartcommunity.dao.impl.VisitorRecordDAOImpl;
import com.smartcommunity.entity.User;
import com.smartcommunity.entity.VisitorRecord;
import com.smartcommunity.service.UserService;
import com.smartcommunity.service.impl.UserServiceImpl;
import com.smartcommunity.util.RoleConstants;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Map;

@WebServlet(name = "VisitorServlet", urlPatterns = "/api/visitor/*")
public class VisitorServlet extends HttpServlet {
    private final VisitorRecordDAO visitorRecordDAO = new VisitorRecordDAOImpl();
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

            if (!relativeURI.equals("/api/visitor/list")) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write(gson.toJson(new Response("error", "未知的接口")));
                return;
            }

            String username = (String) req.getAttribute("username");
            User currentUser = userService.getUserByUsername(username);
            String role = (String) req.getAttribute("role");

            if (RoleConstants.PROPERTY_ADMIN.equals(role)) {
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "获取访客记录成功", visitorRecordDAO.getAllRecords())));
            } else {
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "获取访客记录成功", visitorRecordDAO.getRecordsByHost(currentUser.getId()))));
            }
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

            if (!relativeURI.equals("/api/visitor/register")) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write(gson.toJson(new Response("error", "未知的接口")));
                return;
            }

            String username = (String) req.getAttribute("username");
            User currentUser = userService.getUserByUsername(username);

            RegisterRequest registerRequest = gson.fromJson(req.getReader(), RegisterRequest.class);
            if (registerRequest == null || registerRequest.getVisitorName() == null || registerRequest.getVisitorName().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(gson.toJson(new Response("error", "参数不完整")));
                return;
            }

            VisitorRecord record = new VisitorRecord();
            record.setVisitorName(registerRequest.getVisitorName());
            record.setPhone(registerRequest.getPhone());
            record.setVisitTime(new Timestamp(System.currentTimeMillis()));
            record.setHostUserId(currentUser.getId());
            record.setStatus("registered");
            int recordId = visitorRecordDAO.createRecord(record);

            resp.setStatus(HttpServletResponse.SC_OK);
            out.write(gson.toJson(new Response("success", "登记成功", Map.of("recordId", recordId))));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(new Response("error", "服务器内部错误: " + e.getMessage())));
        } finally {
            out.flush();
            out.close();
        }
    }

    private static class RegisterRequest {
        private String visitorName;
        private String phone;

        public String getVisitorName() {
            return visitorName;
        }

        public String getPhone() {
            return phone;
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
