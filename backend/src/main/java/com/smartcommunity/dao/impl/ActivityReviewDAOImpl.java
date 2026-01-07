package com.smartcommunity.dao.impl;

import com.smartcommunity.dao.ActivityReviewDAO;
import com.smartcommunity.entity.ActivityReview;
import com.smartcommunity.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ActivityReviewDAOImpl implements ActivityReviewDAO {
    @Override
    public ActivityReview getReviewByActivityId(int activityId) {
        String sql = "SELECT activity_id, summary, images, create_time FROM activity_review WHERE activity_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, activityId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ActivityReview review = new ActivityReview();
                    review.setActivityId(rs.getInt("activity_id"));
                    review.setSummary(rs.getString("summary"));
                    review.setImages(rs.getString("images"));
                    review.setCreateTime(rs.getTimestamp("create_time"));
                    return review;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int saveReview(ActivityReview review) {
        String sql = "INSERT INTO activity_review (activity_id, summary, images, create_time) VALUES (?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE summary = VALUES(summary), images = VALUES(images), create_time = VALUES(create_time)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, review.getActivityId());
            stmt.setString(2, review.getSummary());
            stmt.setString(3, review.getImages());
            stmt.setTimestamp(4, review.getCreateTime());
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
