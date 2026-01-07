package com.smartcommunity.dao;

import com.smartcommunity.entity.Announcement;

import java.util.List;

public interface AnnouncementDAO {
    // 创建公告
    int createAnnouncement(Announcement announcement);
    
    // 获取公告详情
    Announcement getAnnouncementById(int id);
    
    // 获取公告列表（带分页、搜索和状态筛选）
    List<Announcement> getAnnouncements(int page, int pageSize, String search, Integer status);
    
    // 获取发布者的公告列表（带分页、搜索和状态筛选）
    List<Announcement> getAnnouncementsByAuthorId(int authorId, int page, int pageSize, String search, Integer status);
    
    // 更新公告
    int updateAnnouncement(Announcement announcement);
    
    // 删除公告
    int deleteAnnouncement(int id);
    
    // 更新公告状态
    int updateAnnouncementStatus(int id, int status);
    
    // 获取最新发布的公告列表（带数量限制）
    List<Announcement> getLatestPublishedAnnouncements(int limit);
}