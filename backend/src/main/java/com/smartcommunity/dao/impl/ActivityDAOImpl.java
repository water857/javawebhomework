package com.smartcommunity.dao.impl;

import com.smartcommunity.dao.ActivityDAO;
import com.smartcommunity.entity.Activity;
import com.smartcommunity.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityDAOImpl implements ActivityDAO {
    @Override
    public int createActivity(Activity activity) {
        String sql = "INSERT INTO activities (user_id, title, content, start_time, end_time, location, max_participants, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, activity.getUserId());
            pstmt.setString(2, activity.getTitle());
            pstmt.setString(3, activity.getContent());
            pstmt.setTimestamp(4, activity.getStartTime());
            pstmt.setTimestamp(5, activity.getEndTime());
            pstmt.setString(6, activity.getLocation());
            pstmt.setInt(7, activity.getMaxParticipants() != null ? activity.getMaxParticipants() : 0);
            pstmt.setString(8, activity.getStatus() != null ? activity.getStatus() : "pending");
            
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
    public Activity getActivityById(int id) {
        String sql = "SELECT * FROM activities WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapActivityResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Activity> getActivities(int page, int pageSize, String search, String status) {
        StringBuilder sql = new StringBuilder("SELECT * FROM activities WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        // 添加搜索条件
        if (search != null && !search.isEmpty()) {
            sql.append(" AND (title LIKE ? OR content LIKE ?)");
            params.add("%" + search + "%");
            params.add("%" + search + "%");
        }
        
        // 添加状态筛选条件
        if (status != null && !status.isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        
        // 添加分页和排序
        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);
        
        List<Activity> activities = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            // 设置参数
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    activities.add(mapActivityResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return activities;
    }

    @Override
    public List<Activity> getActivitiesByUserId(int userId, int page, int pageSize, String search, String status) {
        StringBuilder sql = new StringBuilder("SELECT * FROM activities WHERE user_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);
        
        // 添加搜索条件
        if (search != null && !search.isEmpty()) {
            sql.append(" AND (title LIKE ? OR content LIKE ?)");
            params.add("%" + search + "%");
            params.add("%" + search + "%");
        }
        
        // 添加状态筛选条件
        if (status != null && !status.isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        
        // 添加分页和排序
        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);
        
        List<Activity> activities = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            // 设置参数
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    activities.add(mapActivityResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return activities;
    }

    @Override
    public int updateActivity(Activity activity) {
        String sql = "UPDATE activities SET title = ?, content = ?, start_time = ?, end_time = ?, location = ?, max_participants = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, activity.getTitle());
            pstmt.setString(2, activity.getContent());
            pstmt.setTimestamp(3, activity.getStartTime());
            pstmt.setTimestamp(4, activity.getEndTime());
            pstmt.setString(5, activity.getLocation());
            pstmt.setInt(6, activity.getMaxParticipants() != null ? activity.getMaxParticipants() : 0);
            pstmt.setInt(7, activity.getId());
            
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int deleteActivity(int id) {
        String sql = "DELETE FROM activities WHERE id = ?";
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
    public int updateActivityStatus(int id, String status) {
        String sql = "UPDATE activities SET status = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int updateQrCode(int id, String qrCodeUrl) {
        String sql = "UPDATE activities SET qr_code_url = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, qrCodeUrl);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int incrementParticipants(int id) {
        String sql = "UPDATE activities SET current_participants = current_participants + 1, updated_at = NOW() WHERE id = ?";
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
    public int decrementParticipants(int id) {
        String sql = "UPDATE activities SET current_participants = GREATEST(current_participants - 1, 0), updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 映射ResultSet到Activity对象
    private Activity mapActivityResultSet(ResultSet rs) throws SQLException {
        Activity activity = new Activity();
        activity.setId(rs.getInt("id"));
        activity.setUserId(rs.getInt("user_id"));
        activity.setTitle(rs.getString("title"));
        activity.setContent(rs.getString("content"));
        activity.setStartTime(rs.getTimestamp("start_time"));
        activity.setEndTime(rs.getTimestamp("end_time"));
        activity.setLocation(rs.getString("location"));
        activity.setMaxParticipants(rs.getInt("max_participants"));
        activity.setCurrentParticipants(rs.getInt("current_participants"));
        activity.setStatus(rs.getString("status"));
        activity.setQrCodeUrl(rs.getString("qr_code_url"));
        activity.setCreatedAt(rs.getTimestamp("created_at"));
        activity.setUpdatedAt(rs.getTimestamp("updated_at"));
        return activity;
    }
}