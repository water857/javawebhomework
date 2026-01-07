package com.smartcommunity.servlet;

import com.google.gson.Gson;
import com.smartcommunity.dao.PrivateMessageDAO;
import com.smartcommunity.dao.impl.PrivateMessageDAOImpl;
import com.smartcommunity.entity.PrivateMessage;
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

@WebServlet(name = "MessageServlet", urlPatterns = "/api/message/*")
public class MessageServlet extends HttpServlet {
    private final PrivateMessageDAO messageDAO = new PrivateMessageDAOImpl();
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

            if (!relativeURI.equals("/api/message/list")) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write(gson.toJson(new Response("error", "未知的接口")));
                return;
            }

            String username = (String) req.getAttribute("username");
            User currentUser = userService.getUserByUsername(username);

            String withUserIdStr = req.getParameter("withUserId");
            if (withUserIdStr != null && !withUserIdStr.isEmpty()) {
                int withUserId = Integer.parseInt(withUserIdStr);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "获取对话成功", messageDAO.getConversation(currentUser.getId(), withUserId))));
                return;
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            out.write(gson.toJson(new Response("success", "获取会话列表成功", messageDAO.getConversationList(currentUser.getId()))));
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

            if (relativeURI.equals("/api/message/send")) {
                SendRequest sendRequest = gson.fromJson(req.getReader(), SendRequest.class);
                if (sendRequest == null || sendRequest.getToUserId() <= 0 || sendRequest.getContent() == null || sendRequest.getContent().isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "参数不完整")));
                    return;
                }
                PrivateMessage message = new PrivateMessage();
                message.setFromUserId(currentUser.getId());
                message.setToUserId(sendRequest.getToUserId());
                message.setContent(sendRequest.getContent());
                message.setCreateTime(new Timestamp(System.currentTimeMillis()));
                message.setRead(false);
                int messageId = messageDAO.sendMessage(message);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "发送成功", Map.of("messageId", messageId))));
                return;
            }

            if (relativeURI.equals("/api/message/read")) {
                ReadRequest readRequest = gson.fromJson(req.getReader(), ReadRequest.class);
                if (readRequest == null || (readRequest.getMessageId() == null && readRequest.getWithUserId() == null)) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "参数不完整")));
                    return;
                }
                int updated;
                if (readRequest.getMessageId() != null) {
                    updated = messageDAO.markReadByMessageId(readRequest.getMessageId(), currentUser.getId());
                } else {
                    updated = messageDAO.markReadByUser(currentUser.getId(), readRequest.getWithUserId());
                }
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "已更新" , Map.of("updated", updated))));
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

    private static class SendRequest {
        private int toUserId;
        private String content;

        public int getToUserId() {
            return toUserId;
        }

        public String getContent() {
            return content;
        }
    }

    private static class ReadRequest {
        private Integer messageId;
        private Integer withUserId;

        public Integer getMessageId() {
            return messageId;
        }

        public Integer getWithUserId() {
            return withUserId;
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
