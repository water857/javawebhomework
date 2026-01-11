package com.smartcommunity.servlet;

import com.google.gson.Gson;
import com.smartcommunity.entity.Activity;
import com.smartcommunity.entity.ActivityImage;
import com.smartcommunity.entity.ActivityParticipant;
import com.smartcommunity.entity.ActivityReview;
import com.smartcommunity.entity.User;
import com.smartcommunity.dao.ActivityReviewDAO;
import com.smartcommunity.dao.impl.ActivityReviewDAOImpl;
import com.smartcommunity.service.ActivityService;
import com.smartcommunity.service.UserService;
import com.smartcommunity.service.impl.ActivityServiceImpl;
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
import java.util.List;

@WebServlet(name = "ActivityServlet", urlPatterns = "/api/activities/*")
public class ActivityServlet extends HttpServlet {
    private ActivityService activityService = new ActivityServiceImpl();
    private ActivityReviewDAO reviewDAO = new ActivityReviewDAOImpl();
    private UserService userService = new UserServiceImpl();
    private Gson gson = new Gson();

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

            // 从JWT过滤器获取用户信息（可能为null，因为activities路径允许匿名访问）
            String username = (String) req.getAttribute("username");
            User currentUser = username != null ? userService.getUserByUsername(username) : null;
            String role = (String) req.getAttribute("role");

            if (relativeURI.equals("/api/activities/review")) {
                if (!RoleConstants.PROPERTY_ADMIN.equals(role)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(new Response("error", "无权限")));
                    return;
                }
                ReviewRequest reviewRequest = gson.fromJson(req.getReader(), ReviewRequest.class);
                if (reviewRequest == null || reviewRequest.getActivityId() <= 0) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "参数不完整")));
                    return;
                }
                ActivityReview review = new ActivityReview();
                review.setActivityId(reviewRequest.getActivityId());
                review.setSummary(reviewRequest.getSummary());
                review.setImages(reviewRequest.getImages());
                review.setCreateTime(new Timestamp(System.currentTimeMillis()));
                int updated = reviewDAO.saveReview(review);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "回顾已保存", java.util.Map.of("updated", updated))));
                return;
            }

            if (relativeURI.equals("/api/activities")) {
                // 获取活动列表
                String pageStr = req.getParameter("page");
                String pageSizeStr = req.getParameter("pageSize");
                String search = req.getParameter("search");
                String status = req.getParameter("status");
                int page = pageStr != null ? Integer.parseInt(pageStr) : 1;
                int pageSize = pageSizeStr != null ? Integer.parseInt(pageSizeStr) : 10;
                List<Activity> activities = activityService.getActivities(page, pageSize, search, status);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "获取活动列表成功", activities)));
            } else if (relativeURI.startsWith("/api/activities/user/")) {
                // 获取用户发布的活动
                String[] parts = relativeURI.split("/");
                if (parts.length < 5) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "无效的用户ID")));
                    return;
                }
                int userId = Integer.parseInt(parts[4]);
                String pageStr = req.getParameter("page");
                String pageSizeStr = req.getParameter("pageSize");
                String search = req.getParameter("search");
                String status = req.getParameter("status");
                int page = pageStr != null ? Integer.parseInt(pageStr) : 1;
                int pageSize = pageSizeStr != null ? Integer.parseInt(pageSizeStr) : 10;
                List<Activity> activities = activityService.getUserActivities(userId, page, pageSize, search, status);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "获取用户发布的活动成功", activities)));
            } else if (relativeURI.startsWith("/api/activities/participants/")) {
                // 获取活动参与者列表
                String[] parts = relativeURI.split("/");
                if (parts.length < 5) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "无效的活动ID")));
                    return;
                }
                int activityId = Integer.parseInt(parts[4]);
                String pageStr = req.getParameter("page");
                String pageSizeStr = req.getParameter("pageSize");
                int page = pageStr != null ? Integer.parseInt(pageStr) : 1;
                int pageSize = pageSizeStr != null ? Integer.parseInt(pageSizeStr) : 10;
                List<ActivityParticipant> participants = activityService.getActivityParticipants(activityId, page, pageSize);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "获取活动参与者列表成功", participants)));
            } else if (relativeURI.startsWith("/api/activities/images/")) {
                // 获取活动图片列表
                String[] parts = relativeURI.split("/");
                if (parts.length < 5) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "无效的活动ID")));
                    return;
                }
                int activityId = Integer.parseInt(parts[4]);
                List<ActivityImage> images = activityService.getActivityImages(activityId);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "获取活动图片列表成功", images)));
            } else if (relativeURI.startsWith("/api/activities/status/")) {
                // 状态更新由doPut处理，这里不需要处理
                resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                out.write(gson.toJson(new Response("error", "不允许使用GET方法更新活动状态")));
                return;
            } else if (relativeURI.startsWith("/api/activities/review/")) {
                String[] parts = relativeURI.split("/");
                if (parts.length < 5) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "无效的活动ID")));
                    return;
                }
                int activityId = Integer.parseInt(parts[4]);
                ActivityReview review = reviewDAO.getReviewByActivityId(activityId);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "获取活动回顾成功", review)));
            } else if (relativeURI.startsWith("/api/activities/")) {
                // 获取活动详情
                String[] parts = relativeURI.split("/");
                if (parts.length < 4) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "无效的活动ID")));
                    return;
                }
                int activityId = Integer.parseInt(parts[3]);
                Activity activity = activityService.getActivityDetail(activityId);
                if (activity != null) {
                    // 检查当前用户是否已报名
                    boolean isRegistered = currentUser != null ? activityService.isRegistered(activityId, currentUser.getId()) : false;
                    
                    // 获取活动照片列表
                    List<ActivityImage> images = activityService.getActivityImages(activityId);
                    
                    // 获取活动参与者列表（用于评价）
                    List<ActivityParticipant> participants = activityService.getActivityParticipants(activityId, 1, Integer.MAX_VALUE);
                    
                    // 过滤出有评价的参与者
                    List<java.util.Map<String, Object>> evaluations = new java.util.ArrayList<>();
                    for (ActivityParticipant participant : participants) {
                        if (participant.getEvaluation() != null && !participant.getEvaluation().isEmpty()) {
                            java.util.Map<String, Object> evalMap = new java.util.HashMap<>();
                            evalMap.put("userName", "用户" + participant.getUserId()); // 实际项目中应该从用户表获取用户名
                            evalMap.put("rating", participant.getRating());
                            evalMap.put("evaluation", participant.getEvaluation());
                            evalMap.put("createdAt", participant.getUpdatedAt()); // 使用更新时间作为评价时间
                            evaluations.add(evalMap);
                        }
                    }
                    
                    // 创建一个包含活动信息、报名状态、照片和评价的响应对象
                    java.util.Map<String, Object> responseData = new java.util.HashMap<>();
                    responseData.put("activity", activity);
                    responseData.put("isRegistered", isRegistered);
                    responseData.put("images", images);
                    responseData.put("evaluations", evaluations);
                    ActivityReview review = reviewDAO.getReviewByActivityId(activityId);
                    responseData.put("review", review);
                    
                    resp.setStatus(HttpServletResponse.SC_OK);
                    out.write(gson.toJson(new Response("success", "获取活动详情成功", responseData)));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.write(gson.toJson(new Response("error", "活动不存在")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(new Response("error", "服务器内部错误: " + e.getMessage())));
        } finally {
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
            // 获取请求URI
            String requestURI = req.getRequestURI();
            String contextPath = req.getContextPath();
            String relativeURI = requestURI.substring(contextPath.length());

            // 从JWT过滤器获取用户信息（可能为null，因为activities路径允许匿名访问）
            String username = (String) req.getAttribute("username");
            User currentUser = username != null ? userService.getUserByUsername(username) : null;
            String role = (String) req.getAttribute("role");

            if (relativeURI.equals("/api/activities/review")) {
                if (!RoleConstants.PROPERTY_ADMIN.equals(role)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(new Response("error", "无权限")));
                    return;
                }
                ReviewRequest reviewRequest = gson.fromJson(req.getReader(), ReviewRequest.class);
                if (reviewRequest == null || reviewRequest.getActivityId() <= 0) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "参数不完整")));
                    return;
                }
                ActivityReview review = new ActivityReview();
                review.setActivityId(reviewRequest.getActivityId());
                review.setSummary(reviewRequest.getSummary());
                review.setImages(reviewRequest.getImages());
                review.setCreateTime(new Timestamp(System.currentTimeMillis()));
                int updated = reviewDAO.saveReview(review);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(gson.toJson(new Response("success", "回顾已保存", java.util.Map.of("updated", updated))));
                return;
            }

            if (relativeURI.equals("/api/activities")) {
                // 发布活动
                Activity activity = gson.fromJson(req.getReader(), Activity.class);
                activity.setUserId(currentUser.getId());
                activity.setStatus("pending");
                activity.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                activity.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
                Activity createdActivity = activityService.publishActivity(activity);
                resp.setStatus(HttpServletResponse.SC_CREATED);
                out.write(gson.toJson(new Response("success", "活动发布成功", createdActivity)));
            } else if (relativeURI.startsWith("/api/activities/register/")) {
                // 报名活动
                String[] parts = relativeURI.split("/");
                if (parts.length < 5) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "无效的活动ID")));
                    return;
                }
                int activityId = Integer.parseInt(parts[4]);
                boolean success = activityService.registerForActivity(activityId, currentUser.getId());
                if (success) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    out.write(gson.toJson(new Response("success", "报名成功")));
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "报名失败，可能活动已结束或人数已满")));
                }
            } else if (relativeURI.startsWith("/api/activities/checkin/")) {
                // 签到
                String[] parts = relativeURI.split("/");
                if (parts.length < 5) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "无效的活动ID")));
                    return;
                }
                int activityId = Integer.parseInt(parts[4]);
                boolean success = activityService.checkin(activityId, currentUser.getId());
                if (success) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    out.write(gson.toJson(new Response("success", "签到成功")));
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "签到失败，可能未报名或已签到")));
                }
            } else if (relativeURI.startsWith("/api/activities/evaluate/")) {
                // 评价活动
                String[] parts = relativeURI.split("/");
                if (parts.length < 5) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "无效的活动ID")));
                    return;
                }
                int activityId = Integer.parseInt(parts[4]);
                EvaluateRequest request = gson.fromJson(req.getReader(), EvaluateRequest.class);
                boolean success = activityService.evaluateActivity(activityId, currentUser.getId(), request.evaluation, request.rating);
                if (success) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    out.write(gson.toJson(new Response("success", "评价成功")));
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "评价失败，可能未签到或活动未结束")));
                }
            } else if (relativeURI.startsWith("/api/activities/images/")) {
                // 上传活动图片
                String[] parts = relativeURI.split("/");
                if (parts.length < 5) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "无效的活动ID")));
                    return;
                }
                int activityId = Integer.parseInt(parts[4]);
                ImageUploadRequest request = gson.fromJson(req.getReader(), ImageUploadRequest.class);
                boolean success = activityService.uploadActivityImage(activityId, currentUser.getId(), request.imageUrl);
                if (success) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    out.write(gson.toJson(new Response("success", "图片上传成功")));
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "图片上传失败，可能活动未结束")));
                }
            } else if (relativeURI.startsWith("/api/activities/qrcode/")) {
                // 生成签到二维码
                String[] parts = relativeURI.split("/");
                if (parts.length < 5) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "无效的活动ID")));
                    return;
                }
                int activityId = Integer.parseInt(parts[4]);
                Activity activity = activityService.getActivityDetail(activityId);
                if (activity != null && activity.getUserId() == currentUser.getId()) {
                    String qrCodeUrl = activityService.generateCheckinQrCode(activityId);
                    resp.setStatus(HttpServletResponse.SC_OK);
                    out.write(gson.toJson(new Response("success", "二维码生成成功", qrCodeUrl)));
                } else {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(new Response("error", "无权操作该活动")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(new Response("error", "服务器内部错误: " + e.getMessage())));
        } finally {
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
            // 获取请求URI
            String requestURI = req.getRequestURI();
            String contextPath = req.getContextPath();
            String relativeURI = requestURI.substring(contextPath.length());

            // 从JWT过滤器获取用户信息（可能为null，因为activities路径允许匿名访问）
            String username = (String) req.getAttribute("username");
            User currentUser = username != null ? userService.getUserByUsername(username) : null;
            String role = (String) req.getAttribute("role");

            if (relativeURI.startsWith("/api/activities/status/")) {
                // 更新活动状态
                String[] parts = relativeURI.split("/");
                if (parts.length < 5) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "无效的活动ID")));
                    return;
                }
                int activityId = Integer.parseInt(parts[4]);
                
                // 检查当前用户是否已登录
                if (currentUser == null) {
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    out.write(gson.toJson(new Response("error", "请先登录")));
                    return;
                }
                
                // 检查当前用户是否是活动的发布者
                Activity existingActivity = activityService.getActivityDetail(activityId);
                if (existingActivity == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.write(gson.toJson(new Response("error", "活动不存在")));
                    return;
                }
                
                if (existingActivity.getUserId() != currentUser.getId()) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(new Response("error", "无权操作该活动")));
                    return;
                }
                
                StatusUpdateRequest request = gson.fromJson(req.getReader(), StatusUpdateRequest.class);
                boolean success = activityService.updateActivityStatus(activityId, request.status, currentUser.getId());
                if (success) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    out.write(gson.toJson(new Response("success", "活动状态更新成功")));
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "活动状态更新失败")));
                }
            } else if (relativeURI.startsWith("/api/activities/")) {
                // 更新活动
                String[] parts = relativeURI.split("/");
                if (parts.length < 4) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "无效的活动ID")));
                    return;
                }
                int activityId = Integer.parseInt(parts[3]);
                
                // 检查当前用户是否已登录
                if (currentUser == null) {
                    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    out.write(gson.toJson(new Response("error", "请先登录")));
                    return;
                }
                
                // 检查当前用户是否是活动的发布者
                Activity existingActivity = activityService.getActivityDetail(activityId);
                if (existingActivity == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.write(gson.toJson(new Response("error", "活动不存在")));
                    return;
                }
                
                if (existingActivity.getUserId() != currentUser.getId()) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(new Response("error", "无权操作该活动")));
                    return;
                }
                
                Activity activity = gson.fromJson(req.getReader(), Activity.class);
                activity.setId(activityId);
                activity.setUserId(currentUser.getId()); // 确保userId正确
                activity.setUpdatedAt(new Timestamp(System.currentTimeMillis())); // 更新时间戳
                
                Activity updatedActivity = activityService.updateActivity(activity);
                if (updatedActivity != null) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    out.write(gson.toJson(new Response("success", "活动更新成功", updatedActivity)));
                } else {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(new Response("error", "无权操作该活动")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(new Response("error", "服务器内部错误: " + e.getMessage())));
        } finally {
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
            // 获取请求URI
            String requestURI = req.getRequestURI();
            String contextPath = req.getContextPath();
            String relativeURI = requestURI.substring(contextPath.length());

            // 从JWT过滤器获取用户信息（可能为null，因为activities路径允许匿名访问）
            String username = (String) req.getAttribute("username");
            User currentUser = username != null ? userService.getUserByUsername(username) : null;
            String role = (String) req.getAttribute("role");

            if (relativeURI.startsWith("/api/activities/cancel/")) {
                // 取消报名
                String[] parts = relativeURI.split("/");
                if (parts.length < 5) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "无效的活动ID")));
                    return;
                }
                int activityId = Integer.parseInt(parts[4]);
                boolean success = activityService.cancelRegistration(activityId, currentUser.getId());
                if (success) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    out.write(gson.toJson(new Response("success", "取消报名成功")));
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "取消报名失败")));
                }
            } else if (relativeURI.startsWith("/api/activities/")) {
                // 删除活动
                String[] parts = relativeURI.split("/");
                if (parts.length < 4) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write(gson.toJson(new Response("error", "无效的活动ID")));
                    return;
                }
                int activityId = Integer.parseInt(parts[3]);
                boolean success = activityService.deleteActivity(activityId, currentUser.getId());
                if (success) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    out.write(gson.toJson(new Response("success", "活动删除成功")));
                } else {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.write(gson.toJson(new Response("error", "无权删除该活动")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(gson.toJson(new Response("error", "服务器内部错误: " + e.getMessage())));
        } finally {
            out.close();
        }
    }

    // 内部类定义请求和响应格式
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

    private static class ReviewRequest {
        private int activityId;
        private String summary;
        private String images;

        public int getActivityId() {
            return activityId;
        }

        public String getSummary() {
            return summary;
        }

        public String getImages() {
            return images;
        }
    }

    private static class EvaluateRequest {
        private String evaluation;
        private int rating;

        // 访问器与设置器方法
        public String getEvaluation() {
            return evaluation;
        }

        public void setEvaluation(String evaluation) {
            this.evaluation = evaluation;
        }

        public int getRating() {
            return rating;
        }

        public void setRating(int rating) {
            this.rating = rating;
        }
    }

    private static class ImageUploadRequest {
        private String imageUrl;

        // 访问器与设置器方法
        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }

    private static class StatusUpdateRequest {
        private String status;

        // 访问器与设置器方法
        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}