package com.smartcommunity.dao.impl;

import com.smartcommunity.dao.AnnouncementDAO;
import com.smartcommunity.entity.Announcement;
import com.smartcommunity.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnnouncementDAOImpl implements AnnouncementDAO {
    @Override
    public int createAnnouncement(Announcement announcement) {
        String sql = "INSERT INTO announcement (title, content, author_id, author_name, status, created_at, updated_at, published_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW(), ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, announcement.getTitle());
            pstmt.setString(2, announcement.getContent());
            pstmt.setInt(3, announcement.getAuthorId());
            pstmt.setString(4, announcement.getAuthorName());
            pstmt.setInt(5, announcement.getStatus());
            pstmt.setTimestamp(6, announcement.getPublishedAt());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public Announcement getAnnouncementById(int id) {
        String sql = "SELECT * FROM announcement WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapAnnouncementResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Announcement> getAnnouncements(int page, int pageSize, String search, Integer status) {
        StringBuilder sql = new StringBuilder("SELECT * FROM announcement WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        // 添加搜索条件
        if (search != null && !search.isEmpty()) {
            sql.append(" AND (title LIKE ? OR content LIKE ?)");
            params.add("%" + search + "%");
            params.add("%" + search + "%");
        }
        
        // 添加状态筛选条件
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        
        // 添加分页和排序：优先按发布时间降序，发布时间为空的按创建时间降序
        sql.append(" ORDER BY published_at DESC, created_at DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);
        
        List<Announcement> announcements = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            // 设置参数
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    announcements.add(mapAnnouncementResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return announcements;
    }

    @Override
    public List<Announcement> getAnnouncementsByAuthorId(int authorId, int page, int pageSize, String search, Integer status) {
        StringBuilder sql = new StringBuilder("SELECT * FROM announcement WHERE author_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(authorId);
        
        // 添加搜索条件
        if (search != null && !search.isEmpty()) {
            sql.append(" AND (title LIKE ? OR content LIKE ?)");
            params.add("%" + search + "%");
            params.add("%" + search + "%");
        }
        
        // 添加状态筛选条件
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        
        // 添加分页和排序：优先按发布时间降序，发布时间为空的按创建时间降序
        sql.append(" ORDER BY published_at DESC, created_at DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);
        
        List<Announcement> announcements = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            // 设置参数
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    announcements.add(mapAnnouncementResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return announcements;
    }

    @Override
    public int updateAnnouncement(Announcement announcement) {
        String sql = "UPDATE announcement SET title = ?, content = ?, author_name = ?, status = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, announcement.getTitle());
            pstmt.setString(2, announcement.getContent());
            pstmt.setString(3, announcement.getAuthorName());
            pstmt.setInt(4, announcement.getStatus());
            pstmt.setInt(5, announcement.getId());
            
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int deleteAnnouncement(int id) {
        String sql = "DELETE FROM announcement WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int updateAnnouncementStatus(int id, int status) {
        String sql = "UPDATE announcement SET status = ?, updated_at = NOW()";
        if (status == 1) {
            sql += ", published_at = NOW()";
        }
        sql += " WHERE id = ?";
        
        System.out.println("执行SQL: " + sql);
        System.out.println("参数1-状态: " + status);
        System.out.println("参数2-ID: " + id);
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, status);
            pstmt.setInt(2, id);
            int rows = pstmt.executeUpdate();
            System.out.println("更新影响行数: " + rows);
            return rows;
        } catch (SQLException e) {
            System.out.println("SQL异常: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public List<Announcement> getLatestPublishedAnnouncements(int limit) {
        String sql = "SELECT * FROM announcement WHERE status = 1 ORDER BY published_at DESC LIMIT ?";
        List<Announcement> announcements = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    announcements.add(mapAnnouncementResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return announcements;
    }

    // 辅助方法：将ResultSet映射为Announcement对象
    private Announcement mapAnnouncementResultSet(ResultSet rs) throws SQLException {
        Announcement announcement = new Announcement();
        announcement.setId(rs.getInt("id"));
        announcement.setTitle(rs.getString("title"));
        announcement.setContent(rs.getString("content"));
        announcement.setAuthorId(rs.getInt("author_id"));
        announcement.setAuthorName(rs.getString("author_name"));
        announcement.setStatus(rs.getInt("status"));
        announcement.setCreatedAt(rs.getTimestamp("created_at"));
        announcement.setUpdatedAt(rs.getTimestamp("updated_at"));
        announcement.setPublishedAt(rs.getTimestamp("published_at"));
        return announcement;
    }
}