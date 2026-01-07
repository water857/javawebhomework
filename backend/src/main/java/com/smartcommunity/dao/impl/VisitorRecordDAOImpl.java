package com.smartcommunity.dao.impl;

import com.smartcommunity.dao.VisitorRecordDAO;
import com.smartcommunity.entity.VisitorRecord;
import com.smartcommunity.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VisitorRecordDAOImpl implements VisitorRecordDAO {
    @Override
    public int createRecord(VisitorRecord record) {
        String sql = "INSERT INTO visitor_record (visitor_name, phone, visit_time, host_user_id, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, record.getVisitorName());
            stmt.setString(2, record.getPhone());
            stmt.setTimestamp(3, record.getVisitTime());
            stmt.setInt(4, record.getHostUserId());
            stmt.setString(5, record.getStatus());
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
    public List<VisitorRecord> getRecordsByHost(int hostUserId) {
        List<VisitorRecord> records = new ArrayList<>();
        String sql = "SELECT id, visitor_name, phone, visit_time, host_user_id, status FROM visitor_record WHERE host_user_id = ? ORDER BY visit_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, hostUserId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    records.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    @Override
    public List<VisitorRecord> getAllRecords() {
        List<VisitorRecord> records = new ArrayList<>();
        String sql = "SELECT id, visitor_name, phone, visit_time, host_user_id, status FROM visitor_record ORDER BY visit_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                records.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    private VisitorRecord mapRow(ResultSet rs) throws SQLException {
        VisitorRecord record = new VisitorRecord();
        record.setId(rs.getInt("id"));
        record.setVisitorName(rs.getString("visitor_name"));
        record.setPhone(rs.getString("phone"));
        record.setVisitTime(rs.getTimestamp("visit_time"));
        record.setHostUserId(rs.getInt("host_user_id"));
        record.setStatus(rs.getString("status"));
        return record;
    }
}
