package com.smartcommunity.dao.impl;

import com.smartcommunity.dao.ActivityParticipantDAO;
import com.smartcommunity.entity.ActivityParticipant;
import com.smartcommunity.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityParticipantDAOImpl implements ActivityParticipantDAO {
    @Override
    public int registerForActivity(ActivityParticipant participant) {
        String sql = "INSERT INTO activity_participants (activity_id, user_id, status, registration_time, created_at, updated_at) VALUES (?, ?, 'registered', NOW(), NOW(), NOW())";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, participant.getActivityId());
            pstmt.setInt(2, participant.getUserId());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public ActivityParticipant getParticipant(int activityId, int userId) {
        String sql = "SELECT * FROM activity_participants WHERE activity_id = ? AND user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, activityId);
            pstmt.setInt(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapParticipantResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ActivityParticipant> getParticipantsByActivityId(int activityId, int page, int pageSize) {
        String sql = "SELECT * FROM activity_participants WHERE activity_id = ? ORDER BY registration_time ASC LIMIT ? OFFSET ?";
        List<ActivityParticipant> participants = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, activityId);
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, (page - 1) * pageSize);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    participants.add(mapParticipantResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return participants;
    }

    @Override
    public List<ActivityParticipant> getParticipantsByUserId(int userId, int page, int pageSize) {
        String sql = "SELECT * FROM activity_participants WHERE user_id = ? ORDER BY registration_time DESC LIMIT ? OFFSET ?";
        List<ActivityParticipant> participants = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, (page - 1) * pageSize);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    participants.add(mapParticipantResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return participants;
    }

    @Override
    public int checkin(int activityId, int userId) {
        String sql = "UPDATE activity_participants SET status = 'attended', checkin_time = NOW(), updated_at = NOW() WHERE activity_id = ? AND user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, activityId);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int updateParticipantStatus(int activityId, int userId, String status) {
        String sql = "UPDATE activity_participants SET status = ?, updated_at = NOW() WHERE activity_id = ? AND user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, activityId);
            pstmt.setInt(3, userId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int evaluateActivity(int activityId, int userId, String evaluation, int rating) {
        String sql = "UPDATE activity_participants SET evaluation = ?, rating = ?, updated_at = NOW() WHERE activity_id = ? AND user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, evaluation);
            pstmt.setInt(2, rating);
            pstmt.setInt(3, activityId);
            pstmt.setInt(4, userId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int cancelRegistration(int activityId, int userId) {
        String sql = "DELETE FROM activity_participants WHERE activity_id = ? AND user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, activityId);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int getParticipantCount(int activityId) {
        String sql = "SELECT COUNT(*) FROM activity_participants WHERE activity_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, activityId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 映射ResultSet到ActivityParticipant对象
    private ActivityParticipant mapParticipantResultSet(ResultSet rs) throws SQLException {
        ActivityParticipant participant = new ActivityParticipant();
        participant.setId(rs.getInt("id"));
        participant.setActivityId(rs.getInt("activity_id"));
        participant.setUserId(rs.getInt("user_id"));
        participant.setStatus(rs.getString("status"));
        participant.setRegistrationTime(rs.getTimestamp("registration_time"));
        participant.setCheckinTime(rs.getTimestamp("checkin_time"));
        participant.setEvaluation(rs.getString("evaluation"));
        participant.setRating(rs.getInt("rating"));
        participant.setCreatedAt(rs.getTimestamp("created_at"));
        participant.setUpdatedAt(rs.getTimestamp("updated_at"));
        return participant;
    }
}