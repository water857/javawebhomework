package com.smartcommunity.dao;

import com.smartcommunity.entity.Activity;

import java.util.List;

public interface ActivityDAO {
    // 创建活动
    int createActivity(Activity activity);
    
    // 获取活动详情
    Activity getActivityById(int id);
    
    // 获取活动列表（带分页、搜索和状态筛选）
    List<Activity> getActivities(int page, int pageSize, String search, String status);
    
    // 获取用户发布的活动列表（带分页、搜索和状态筛选）
    List<Activity> getActivitiesByUserId(int userId, int page, int pageSize, String search, String status);
    
    // 更新活动
    int updateActivity(Activity activity);
    
    // 删除活动
    int deleteActivity(int id);
    
    // 更新活动状态
    int updateActivityStatus(int id, String status);
    
    // 更新活动二维码
    int updateQrCode(int id, String qrCodeUrl);
    
    // 增加当前参与人数
    int incrementParticipants(int id);
    
    // 减少当前参与人数
    int decrementParticipants(int id);
}