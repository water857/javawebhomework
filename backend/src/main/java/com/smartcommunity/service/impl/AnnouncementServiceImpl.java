package com.smartcommunity.service.impl;

import com.smartcommunity.dao.AnnouncementDAO;
import com.smartcommunity.dao.impl.AnnouncementDAOImpl;
import com.smartcommunity.entity.Announcement;
import com.smartcommunity.service.AnnouncementService;

import java.sql.Timestamp;
import java.util.List;

public class AnnouncementServiceImpl implements AnnouncementService {
    private AnnouncementDAO announcementDAO = new AnnouncementDAOImpl();

    @Override
    public Announcement publishAnnouncement(Announcement announcement) {
        // 设置公告状态为发布（1）
        announcement.setStatus(1);
        // 设置发布时间为当前时间
        announcement.setPublishedAt(new Timestamp(System.currentTimeMillis()));
        
        int announcementId = announcementDAO.createAnnouncement(announcement);
        return announcementDAO.getAnnouncementById(announcementId);
    }

    @Override
    public Announcement saveDraft(Announcement announcement) {
        // 设置公告状态为草稿（0）
        announcement.setStatus(0);
        // 草稿状态下发布时间为空
        announcement.setPublishedAt(null);
        
        int announcementId = announcementDAO.createAnnouncement(announcement);
        return announcementDAO.getAnnouncementById(announcementId);
    }

    @Override
    public Announcement getAnnouncementDetail(int announcementId) {
        return announcementDAO.getAnnouncementById(announcementId);
    }

    @Override
    public List<Announcement> getAnnouncements(int page, int pageSize, String search, Integer status) {
        return announcementDAO.getAnnouncements(page, pageSize, search, status);
    }

    @Override
    public List<Announcement> getAuthorAnnouncements(int authorId, int page, int pageSize, String search, Integer status) {
        return announcementDAO.getAnnouncementsByAuthorId(authorId, page, pageSize, search, status);
    }

    @Override
    public Announcement updateAnnouncement(Announcement announcement) {
        // 检查公告是否存在
        Announcement existingAnnouncement = announcementDAO.getAnnouncementById(announcement.getId());
        if (existingAnnouncement == null) {
            return null;
        }
        
        // 更新公告
        announcementDAO.updateAnnouncement(announcement);
        return announcementDAO.getAnnouncementById(announcement.getId());
    }

    @Override
    public boolean deleteAnnouncement(int announcementId, int authorId) {
        // 检查公告是否存在
        Announcement announcement = announcementDAO.getAnnouncementById(announcementId);
        if (announcement == null) {
            return false;
        }
        
        // 删除公告
        return announcementDAO.deleteAnnouncement(announcementId) > 0;
    }

    @Override
    public boolean updateAnnouncementStatus(int announcementId, int status, int authorId) {
        // 检查公告是否存在
        Announcement announcement = announcementDAO.getAnnouncementById(announcementId);
        if (announcement == null) {
            return false;
        }
        
        // 更新公告状态
        return announcementDAO.updateAnnouncementStatus(announcementId, status) > 0;
    }

    @Override
    public List<Announcement> getLatestPublishedAnnouncements(int limit) {
        return announcementDAO.getLatestPublishedAnnouncements(limit);
    }

    @Override
    public boolean existsById(int announcementId) {
        return announcementDAO.getAnnouncementById(announcementId) != null;
    }

    @Override
    public boolean isAuthor(int announcementId, int authorId) {
        Announcement announcement = announcementDAO.getAnnouncementById(announcementId);
        return announcement != null && announcement.getAuthorId() == authorId;
    }
}