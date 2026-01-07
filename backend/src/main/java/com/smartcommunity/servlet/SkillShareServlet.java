package com.smartcommunity.servlet;

import com.google.gson.Gson;
import com.smartcommunity.dao.SkillShareDAO;
import com.smartcommunity.dao.impl.SkillShareDAOImpl;
import com.smartcommunity.entity.SkillShare;
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

@WebServlet(name = "SkillShareServlet", urlPatterns = "/api/skills/*")
public class SkillShareServlet extends HttpServlet {
    private final SkillShareDAO skillShareDAO = new SkillShareDAOImpl();
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

            if (!relativeURI.equals("/api/skills/list")) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write(gson.toJson(new Response("error", "未知的接口")));
                return;
            }
            resp.setStatus(HttpServletResponse.SC_OK);
            out.write(gson.toJson(new Response("success", "获取技能交换列表成功", skillShareDAO.getSkills())));
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

            if (!relativeURI.equals("/api/skills/publish")) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write(gson.toJson(new Response("error", "未知的接口")));
                return;
            }

            String username = (String) req.getAttribute("username");
            User currentUser = userService.getUserByUsername(username);

            PublishRequest publishRequest = gson.fromJson(req.getReader(), PublishRequest.class);
            if (publishRequest == null || publishRequest.getSkillName() == null || publishRequest.getSkillName().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write(gson.toJson(new Response("error", "参数不完整")));
                return;
            }

            SkillShare skill = new SkillShare();
            skill.setUserId(currentUser.getId());
            skill.setSkillName(publishRequest.getSkillName());
            skill.setDescription(publishRequest.getDescription());
            skill.setContact(publishRequest.getContact());
            skill.setCreateTime(new Timestamp(System.currentTimeMillis()));
            int skillId = skillShareDAO.createSkill(skill);
            resp.setStatus(HttpServletResponse.SC_OK);
            out.write(gson.toJson(new Response("success", "发布成功", Map.of("skillId", skillId))));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(new Response("error", "服务器内部错误: " + e.getMessage())));
        } finally {
            out.flush();
            out.close();
        }
    }

    private static class PublishRequest {
        private String skillName;
        private String description;
        private String contact;

        public String getSkillName() {
            return skillName;
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
