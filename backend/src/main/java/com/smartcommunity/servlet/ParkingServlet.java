package com.smartcommunity.servlet;

import com.google.gson.Gson;
import com.smartcommunity.dao.ParkingApplicationDAO;
import com.smartcommunity.dao.ParkingSpaceDAO;
import com.smartcommunity.dao.impl.ParkingApplicationDAOImpl;
import com.smartcommunity.dao.impl.ParkingSpaceDAOImpl;
import com.smartcommunity.entity.ParkingApplication;
import com.smartcommunity.entity.ParkingSpace;
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

@WebServlet(name = "ParkingServlet", urlPatterns = "/api/parking/*")
public class ParkingServlet extends HttpServlet {
    private final ParkingSpaceDAO spaceDAO = new ParkingSpaceDAOImpl();
    private final ParkingApplicationDAO applicationDAO = new ParkingApplicationDAOImpl();
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

            String username = (String) req.getAttribute("username");
            User currentUser = userService.getUserByUsername(username);
            String role = (String) req.getAttribute("role");

            if (relativeURI.equals("/api/parking/space/list")) {
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "获取车位列表成功", spaceDAO.getAllSpaces())));
                return;
            }

            if (relativeURI.equals("/api/parking/application/list")) {
                if (RoleConstants.PROPERTY_ADMIN.equals(role)) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    out.write(gson.toJson(new Response("success", "获取申请列表成功", applicationDAO.getAllApplications())));
                } else {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    out.write(gson.toJson(new Response("success", "获取申请列表成功", applicationDAO.getApplicationsByUser(currentUser.getId()))));
                }
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

            if (relativeURI.equals("/api/parking/application")) {
                ApplyRequest applyRequest = gson.fromJson(req.getReader(), ApplyRequest.class);
                if (applyRequest == null || applyRequest.getParkingId() <= 0) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "参数不完整")));
                    return;
                }
                ParkingSpace space = spaceDAO.getSpaceById(applyRequest.getParkingId());
                if (space == null || !"available".equals(space.getStatus())) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "车位不可申请")));
                    return;
                }
                ParkingApplication application = new ParkingApplication();
                application.setUserId(currentUser.getId());
                application.setParkingId(applyRequest.getParkingId());
                application.setStatus("pending");
                application.setCreateTime(new Timestamp(System.currentTimeMillis()));
                int applicationId = applicationDAO.createApplication(application);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "申请提交成功", Map.of("applicationId", applicationId))));
                return;
            }

            if (relativeURI.equals("/api/parking/application/review")) {
                if (!RoleConstants.PROPERTY_ADMIN.equals(role)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(new Response("error", "无权限")));
                    return;
                }
                ReviewRequest reviewRequest = gson.fromJson(req.getReader(), ReviewRequest.class);
                if (reviewRequest == null || reviewRequest.getApplicationId() <= 0 || reviewRequest.getStatus() == null) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "参数不完整")));
                    return;
                }
                ParkingApplication application = applicationDAO.getApplicationById(reviewRequest.getApplicationId());
                if (application == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.write(gson.toJson(new Response("error", "申请不存在")));
                    return;
                }
                if ("approved".equals(reviewRequest.getStatus())) {
                    applicationDAO.updateStatus(application.getId(), "approved");
                    spaceDAO.updateSpaceStatus(application.getParkingId(), "occupied", application.getUserId());
                } else {
                    applicationDAO.updateStatus(application.getId(), "rejected");
                    spaceDAO.updateSpaceStatus(application.getParkingId(), "available", null);
                }
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "审核完成")));
                return;
            }

            if (relativeURI.equals("/api/parking/space/release")) {
                if (!RoleConstants.PROPERTY_ADMIN.equals(role)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(new Response("error", "无权限")));
                    return;
                }
                ReleaseRequest releaseRequest = gson.fromJson(req.getReader(), ReleaseRequest.class);
                if (releaseRequest == null || releaseRequest.getParkingId() <= 0) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "参数不完整")));
                    return;
                }
                int updated = spaceDAO.updateSpaceStatus(releaseRequest.getParkingId(), "available", null);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "车位已释放", Map.of("updated", updated))));
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

    private static class ApplyRequest {
        private int parkingId;

        public int getParkingId() {
            return parkingId;
        }
    }

    private static class ReviewRequest {
        private int applicationId;
        private String status;

        public int getApplicationId() {
            return applicationId;
        }

        public String getStatus() {
            return status;
        }
    }

    private static class ReleaseRequest {
        private int parkingId;

        public int getParkingId() {
            return parkingId;
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
