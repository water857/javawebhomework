package com.smartcommunity.servlet;

import com.google.gson.Gson;
import com.smartcommunity.dao.SecondHandItemDAO;
import com.smartcommunity.dao.impl.SecondHandItemDAOImpl;
import com.smartcommunity.entity.SecondHandItem;
import com.smartcommunity.entity.User;
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

@WebServlet(name = "SecondHandServlet", urlPatterns = "/api/second-hand/*")
public class SecondHandServlet extends HttpServlet {
    private final SecondHandItemDAO itemDAO = new SecondHandItemDAOImpl();
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

            if (!relativeURI.equals("/api/second-hand/list")) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write(gson.toJson(new Response("error", "未知的接口")));
                return;
            }
            String status = req.getParameter("status");
            resp.setStatus(HttpServletResponse.SC_OK);
            out.write(gson.toJson(new Response("success", "获取二手商品列表成功", itemDAO.getItems(status))));
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

            String username = (String) req.getAttribute("username");
            User currentUser = userService.getUserByUsername(username);
            String role = (String) req.getAttribute("role");

            if (relativeURI.equals("/api/second-hand/publish")) {
                PublishRequest publishRequest = gson.fromJson(req.getReader(), PublishRequest.class);
                if (publishRequest == null || publishRequest.getTitle() == null || publishRequest.getTitle().isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "参数不完整")));
                    return;
                }
                SecondHandItem item = new SecondHandItem();
                item.setUserId(currentUser.getId());
                item.setTitle(publishRequest.getTitle());
                item.setDescription(publishRequest.getDescription());
                item.setPrice(publishRequest.getPrice());
                item.setStatus("available");
                item.setCreateTime(new Timestamp(System.currentTimeMillis()));
                int itemId = itemDAO.createItem(item);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "发布成功", Map.of("itemId", itemId))));
                return;
            }

            if (relativeURI.equals("/api/second-hand/mark-sold")) {
                MarkSoldRequest request = gson.fromJson(req.getReader(), MarkSoldRequest.class);
                if (request == null || request.getItemId() <= 0) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "参数不完整")));
                    return;
                }
                SecondHandItem item = itemDAO.getItemById(request.getItemId());
                if (item == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.write(gson.toJson(new Response("error", "商品不存在")));
                    return;
                }
                if (!RoleConstants.PROPERTY_ADMIN.equals(role) && item.getUserId() != currentUser.getId()) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(new Response("error", "无权限")));
                    return;
                }
                int updated = itemDAO.updateStatus(item.getId(), "sold");
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "已标记售出", Map.of("updated", updated))));
                return;
            }

            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.write(gson.toJson(new Response("error", "未知的接口")));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(new Response("error", "服务器内部错误: " + e.getMessage())));
        } finally {
            out.flush();
            out.close();
        }
    }

    private static class PublishRequest {
        private String title;
        private String description;
        private java.math.BigDecimal price;

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public java.math.BigDecimal getPrice() {
            return price;
        }
    }

    private static class MarkSoldRequest {
        private int itemId;

        public int getItemId() {
            return itemId;
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
