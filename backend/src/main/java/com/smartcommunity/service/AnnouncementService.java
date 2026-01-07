package com.smartcommunity.service;

import com.smartcommunity.entity.Announcement;

import java.util.List;

public interface AnnouncementService {
    // 发布公告
    Announcement publishAnnouncement(Announcement announcement);
    
    // 保存草稿
    Announcement saveDraft(Announcement announcement);
    
    // 获取公告详情
    Announcement getAnnouncementDetail(int announcementId);
    
    // 获取公告列表（带分页、搜索和状态筛选）
    List<Announcement> getAnnouncements(int page, int pageSize, String search, Integer status);
    
    // 获取发布者的公告列表（带分页、搜索和状态筛选）
    List<Announcement> getAuthorAnnouncements(int authorId, int page, int pageSize, String search, Integer status);
    
    // 更新公告
    Announcement updateAnnouncement(Announcement announcement);
    
    // 删除公告
    boolean deleteAnnouncement(int announcementId, int authorId);
    
    // 更新公告状态（发布/草稿）
    boolean updateAnnouncementStatus(int announcementId, int status, int authorId);
    
    // 获取最新发布的公告列表（带数量限制）
    List<Announcement> getLatestPublishedAnnouncements(int limit);
    
    // 检查公告是否存在
    boolean existsById(int announcementId);
    
    // 检查当前用户是否是公告的发布者
    boolean isAuthor(int announcementId, int authorId);
}