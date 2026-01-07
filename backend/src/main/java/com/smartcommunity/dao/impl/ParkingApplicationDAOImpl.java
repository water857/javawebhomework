package com.smartcommunity.dao.impl;

import com.smartcommunity.dao.ParkingApplicationDAO;
import com.smartcommunity.entity.ParkingApplication;
import com.smartcommunity.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ParkingApplicationDAOImpl implements ParkingApplicationDAO {
    @Override
    public int createApplication(ParkingApplication application) {
        String sql = "INSERT INTO parking_application (user_id, parking_id, status, create_time) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, application.getUserId());
            stmt.setInt(2, application.getParkingId());
            stmt.setString(3, application.getStatus());
            stmt.setTimestamp(4, application.getCreateTime());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public ParkingApplication getApplicationById(int id) {
        String sql = "SELECT id, user_id, parking_id, status, create_time FROM parking_application WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ParkingApplication> getApplicationsByUser(int userId) {
        List<ParkingApplication> applications = new ArrayList<>();
        String sql = "SELECT id, user_id, parking_id, status, create_time FROM parking_application WHERE user_id = ? ORDER BY create_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    applications.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return applications;
    }

    @Override
    public List<ParkingApplication> getAllApplications() {
        List<ParkingApplication> applications = new ArrayList<>();
        String sql = "SELECT id, user_id, parking_id, status, create_time FROM parking_application ORDER BY create_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                applications.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return applications;
    }

    @Override
    public int updateStatus(int id, String status) {
        String sql = "UPDATE parking_application SET status = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, id);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private ParkingApplication mapRow(ResultSet rs) throws SQLException {
        ParkingApplication application = new ParkingApplication();
        application.setId(rs.getInt("id"));
        application.setUserId(rs.getInt("user_id"));
        application.setParkingId(rs.getInt("parking_id"));
        application.setStatus(rs.getString("status"));
        application.setCreateTime(rs.getTimestamp("create_time"));
        return application;
    }
}
