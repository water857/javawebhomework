package com.smartcommunity.service.impl;

import com.smartcommunity.dao.ActivityDAO;
import com.smartcommunity.dao.ActivityImageDAO;
import com.smartcommunity.dao.ActivityParticipantDAO;
import com.smartcommunity.dao.impl.ActivityDAOImpl;
import com.smartcommunity.dao.impl.ActivityImageDAOImpl;
import com.smartcommunity.dao.impl.ActivityParticipantDAOImpl;
import com.smartcommunity.entity.Activity;
import com.smartcommunity.entity.ActivityImage;
import com.smartcommunity.entity.ActivityParticipant;
import com.smartcommunity.service.ActivityService;

import java.util.List;
import java.util.UUID;

public class ActivityServiceImpl implements ActivityService {
    private ActivityDAO activityDAO = new ActivityDAOImpl();
    private ActivityParticipantDAO participantDAO = new ActivityParticipantDAOImpl();
    private ActivityImageDAO imageDAO = new ActivityImageDAOImpl();

    @Override
    public Activity publishActivity(Activity activity) {
        int activityId = activityDAO.createActivity(activity);
        return activityDAO.getActivityById(activityId);
    }

    @Override
    public Activity getActivityDetail(int activityId) {
        return activityDAO.getActivityById(activityId);
    }

    @Override
    public List<Activity> getActivities(int page, int pageSize, String search, String status) {
        return activityDAO.getActivities(page, pageSize, search, status);
    }

    @Override
    public List<Activity> getUserActivities(int userId, int page, int pageSize, String search, String status) {
        return activityDAO.getActivitiesByUserId(userId, page, pageSize, search, status);
    }

    @Override
    public Activity updateActivity(Activity activity) {
        // 检查活动是否存在
        Activity existingActivity = activityDAO.getActivityById(activity.getId());
        if (existingActivity == null) {
            return null;
        }
        
        // 检查当前用户是否是活动的发布者
        if (existingActivity.getUserId() != activity.getUserId()) {
            return null;
        }
        
        // 更新活动
        activityDAO.updateActivity(activity);
        return activityDAO.getActivityById(activity.getId());
    }

    @Override
    public boolean deleteActivity(int activityId, int userId) {
        Activity activity = activityDAO.getActivityById(activityId);
        if (activity != null && activity.getUserId() == userId) {
            // 删除活动图片
            imageDAO.deleteImagesByActivityId(activityId);
            // 删除活动
            return activityDAO.deleteActivity(activityId) > 0;
        }
        return false;
    }

    @Override
    public boolean updateActivityStatus(int activityId, String status, int userId) {
        // 检查活动是否存在
        Activity activity = activityDAO.getActivityById(activityId);
        if (activity == null) {
            return false;
        }
        
        // 检查当前用户是否是活动的发布者
        if (activity.getUserId() != userId) {
            return false;
        }
        
        return activityDAO.updateActivityStatus(activityId, status) > 0;
    }

    @Override
    public String generateCheckinQrCode(int activityId) {
        // 这个方法不需要在服务层检查权限，因为在Servlet层已经进行了检查
        try {
            // 二维码内容：包含活动ID的签到链接
            String content = "http://localhost:8080/backend/api/activities/checkin/" + activityId;
            
            // 二维码图片宽度和高度
            int width = 300;
            int height = 300;
            
            // 二维码格式参数
            com.google.zxing.EncodeHintType hintType = com.google.zxing.EncodeHintType.CHARACTER_SET;
            java.util.Map<com.google.zxing.EncodeHintType, Object> hints = new java.util.HashMap<>();
            hints.put(hintType, "UTF-8");
            
            // 生成二维码
            com.google.zxing.BarcodeFormat format = com.google.zxing.BarcodeFormat.QR_CODE;
            com.google.zxing.Writer writer = new com.google.zxing.qrcode.QRCodeWriter();
            com.google.zxing.common.BitMatrix bitMatrix = writer.encode(content, format, width, height, hints);
            
            // 确保二维码目录存在
            String qrCodeDir = "e:\\智能社区服务平台系统\\SmartCommunityServicePlatform\\backend\\src\\main\\webapp\\uploads\\qrcodes\\";
            java.io.File dir = new java.io.File(qrCodeDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 二维码图片路径
            String qrCodePath = qrCodeDir + "activity_" + activityId + ".png";
            String qrCodeUrl = "/uploads/qrcodes/activity_" + activityId + ".png";
            
            // 写入二维码图片
            com.google.zxing.client.j2se.MatrixToImageWriter.writeToPath(bitMatrix, "PNG", java.nio.file.Paths.get(qrCodePath));
            
            // 更新活动的二维码URL
            activityDAO.updateQrCode(activityId, qrCodeUrl);
            
            return qrCodeUrl;
        } catch (Exception e) {
            e.printStackTrace();
            // 如果生成失败，返回一个默认的URL
            return "/uploads/qrcodes/default.png";
        }
    }

    @Override
    public boolean registerForActivity(int activityId, int userId) {
        Activity activity = activityDAO.getActivityById(activityId);
        if (activity == null) {
            return false;
        }

        // 检查活动是否已过期或取消
        if ("cancelled".equals(activity.getStatus()) || "completed".equals(activity.getStatus())) {
            return false;
        }

        // 检查是否已达到最大参与人数
        if (activity.getMaxParticipants() > 0 && activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
            return false;
        }

        // 检查用户是否已报名
        if (participantDAO.getParticipant(activityId, userId) != null) {
            return false;
        }

        // 创建参与者记录
        ActivityParticipant participant = new ActivityParticipant();
        participant.setActivityId(activityId);
        participant.setUserId(userId);

        // 开始事务
        boolean success = participantDAO.registerForActivity(participant) > 0;
        if (success) {
            // 增加当前参与人数
            activityDAO.incrementParticipants(activityId);
        }
        return success;
    }

    @Override
    public boolean cancelRegistration(int activityId, int userId) {
        // 检查用户是否已报名
        ActivityParticipant participant = participantDAO.getParticipant(activityId, userId);
        if (participant == null) {
            return false;
        }

        // 检查活动是否已开始
        Activity activity = activityDAO.getActivityById(activityId);
        if (activity == null) {
            return false;
        }

        // 检查活动是否已开始（只能在活动开始前取消报名）
        long now = System.currentTimeMillis();
        long activityStartTime = activity.getStartTime().getTime();
        if (now >= activityStartTime) {
            return false;
        }

        // 开始事务
        boolean success = participantDAO.cancelRegistration(activityId, userId) > 0;
        if (success) {
            // 减少当前参与人数
            activityDAO.decrementParticipants(activityId);
        }
        return success;
    }

    @Override
    public boolean checkin(int activityId, int userId) {
        // 检查用户是否已报名
        ActivityParticipant participant = participantDAO.getParticipant(activityId, userId);
        if (participant == null) {
            return false;
        }

        // 检查用户是否已签到
        if ("attended".equals(participant.getStatus())) {
            return false;
        }

        // 签到
        return participantDAO.checkin(activityId, userId) > 0;
    }

    @Override
    public boolean evaluateActivity(int activityId, int userId, String evaluation, int rating) {
        // 检查用户是否已签到
        ActivityParticipant participant = participantDAO.getParticipant(activityId, userId);
        if (participant == null || !"attended".equals(participant.getStatus())) {
            return false;
        }

        // 评价活动
        return participantDAO.evaluateActivity(activityId, userId, evaluation, rating) > 0;
    }

    @Override
    public List<ActivityParticipant> getActivityParticipants(int activityId, int page, int pageSize) {
        return participantDAO.getParticipantsByActivityId(activityId, page, pageSize);
    }

    @Override
    public List<ActivityParticipant> getUserParticipants(int userId, int page, int pageSize) {
        return participantDAO.getParticipantsByUserId(userId, page, pageSize);
    }

    @Override
    public boolean uploadActivityImage(int activityId, int userId, String imageUrl) {
        Activity activity = activityDAO.getActivityById(activityId);
        if (activity == null) {
            return false;
        }

        // 检查当前用户是否是活动的发布者
        if (activity.getUserId() != userId) {
            return false;
        }

        // 检查活动是否已完成（只有已完成的活动才能上传回顾图片）
        if (!"completed".equals(activity.getStatus())) {
            return false;
        }

        // 创建图片记录
        ActivityImage image = new ActivityImage();
        image.setActivityId(activityId);
        image.setUserId(userId);
        image.setImageUrl(imageUrl);

        return imageDAO.uploadImage(image) > 0;
    }

    @Override
    public List<ActivityImage> getActivityImages(int activityId) {
        return imageDAO.getImagesByActivityId(activityId);
    }

    @Override
    public boolean isRegistered(int activityId, int userId) {
        ActivityParticipant participant = participantDAO.getParticipant(activityId, userId);
        return participant != null;
    }

    @Override
    public boolean isCheckedIn(int activityId, int userId) {
        ActivityParticipant participant = participantDAO.getParticipant(activityId, userId);
        return participant != null && "attended".equals(participant.getStatus());
    }
}