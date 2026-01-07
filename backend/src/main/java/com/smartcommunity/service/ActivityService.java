package com.smartcommunity.service;

import com.smartcommunity.entity.Activity;
import com.smartcommunity.entity.ActivityImage;
import com.smartcommunity.entity.ActivityParticipant;

import java.util.List;

public interface ActivityService {
    // 发布活动
    Activity publishActivity(Activity activity);
    
    // 获取活动详情
    Activity getActivityDetail(int activityId);
    
    // 获取活动列表
    List<Activity> getActivities(int page, int pageSize, String search, String status);
    
    // 获取用户发布的活动
    List<Activity> getUserActivities(int userId, int page, int pageSize, String search, String status);
    
    // 更新活动
    Activity updateActivity(Activity activity);
    
    // 删除活动
    boolean deleteActivity(int activityId, int userId);
    
    // 更新活动状态
    boolean updateActivityStatus(int activityId, String status, int userId);
    
    // 生成签到二维码
    String generateCheckinQrCode(int activityId);
    
    // 报名活动
    boolean registerForActivity(int activityId, int userId);
    
    // 取消报名
    boolean cancelRegistration(int activityId, int userId);
    
    // 签到
    boolean checkin(int activityId, int userId);
    
    // 评价活动
    boolean evaluateActivity(int activityId, int userId, String evaluation, int rating);
    
    // 获取活动参与者列表
    List<ActivityParticipant> getActivityParticipants(int activityId, int page, int pageSize);
    
    // 获取用户参与的活动
    List<ActivityParticipant> getUserParticipants(int userId, int page, int pageSize);
    
    // 上传活动图片
    boolean uploadActivityImage(int activityId, int userId, String imageUrl);
    
    // 获取活动图片列表
    List<ActivityImage> getActivityImages(int activityId);
    
    // 检查用户是否已报名活动
    boolean isRegistered(int activityId, int userId);
    
    // 检查用户是否已签到活动
    boolean isCheckedIn(int activityId, int userId);
}