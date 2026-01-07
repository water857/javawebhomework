package com.smartcommunity.dao;

import com.smartcommunity.entity.ActivityParticipant;

import java.util.List;

public interface ActivityParticipantDAO {
    // 报名活动
    int registerForActivity(ActivityParticipant participant);
    
    // 获取参与者信息
    ActivityParticipant getParticipant(int activityId, int userId);
    
    // 获取活动参与者列表
    List<ActivityParticipant> getParticipantsByActivityId(int activityId, int page, int pageSize);
    
    // 获取用户参与的活动列表
    List<ActivityParticipant> getParticipantsByUserId(int userId, int page, int pageSize);
    
    // 签到
    int checkin(int activityId, int userId);
    
    // 更新参与状态
    int updateParticipantStatus(int activityId, int userId, String status);
    
    // 评价活动
    int evaluateActivity(int activityId, int userId, String evaluation, int rating);
    
    // 取消报名
    int cancelRegistration(int activityId, int userId);
    
    // 获取活动参与人数
    int getParticipantCount(int activityId);
}