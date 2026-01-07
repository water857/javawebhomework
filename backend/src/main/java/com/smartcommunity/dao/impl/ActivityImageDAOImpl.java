package com.smartcommunity.dao.impl;

import com.smartcommunity.dao.ActivityImageDAO;
import com.smartcommunity.entity.ActivityImage;
import com.smartcommunity.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityImageDAOImpl implements ActivityImageDAO {
    @Override
    public int uploadImage(ActivityImage image) {
        String sql = "INSERT INTO activity_images (activity_id, user_id, image_url, created_at) VALUES (?, ?, ?, NOW())";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, image.getActivityId());
            pstmt.setInt(2, image.getUserId());
            pstmt.setString(3, image.getImageUrl());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public List<ActivityImage> getImagesByActivityId(int activityId) {
        String sql = "SELECT * FROM activity_images WHERE activity_id = ? ORDER BY created_at DESC";
        List<ActivityImage> images = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, activityId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    images.add(mapImageResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return images;
    }

    @Override
    public int deleteImage(int id) {
        String sql = "DELETE FROM activity_images WHERE id = ?";
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
    public int deleteImagesByActivityId(int activityId) {
        String sql = "DELETE FROM activity_images WHERE activity_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, activityId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 映射ResultSet到ActivityImage对象
    private ActivityImage mapImageResultSet(ResultSet rs) throws SQLException {
        ActivityImage image = new ActivityImage();
        image.setId(rs.getInt("id"));
        image.setActivityId(rs.getInt("activity_id"));
        image.setUserId(rs.getInt("user_id"));
        image.setImageUrl(rs.getString("image_url"));
        image.setCreatedAt(rs.getTimestamp("created_at"));
        return image;
    }
}