package com.smartcommunity.dao.impl;

import com.smartcommunity.dao.LostAndFoundDAO;
import com.smartcommunity.entity.LostAndFound;
import com.smartcommunity.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LostAndFoundDAOImpl implements LostAndFoundDAO {
    @Override
    public int createRecord(LostAndFound record) {
        String sql = "INSERT INTO lost_and_found (type, title, description, contact, create_time) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, record.getType());
            stmt.setString(2, record.getTitle());
            stmt.setString(3, record.getDescription());
            stmt.setString(4, record.getContact());
            stmt.setTimestamp(5, record.getCreateTime());
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
    public List<LostAndFound> getRecords(String type) {
        List<LostAndFound> records = new ArrayList<>();
        String baseSql = "SELECT id, type, title, description, contact, create_time FROM lost_and_found";
        String sql = type == null ? baseSql + " ORDER BY create_time DESC" : baseSql + " WHERE type = ? ORDER BY create_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (type != null) {
                stmt.setString(1, type);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LostAndFound record = new LostAndFound();
                    record.setId(rs.getInt("id"));
                    record.setType(rs.getString("type"));
                    record.setTitle(rs.getString("title"));
                    record.setDescription(rs.getString("description"));
                    record.setContact(rs.getString("contact"));
                    record.setCreateTime(rs.getTimestamp("create_time"));
                    records.add(record);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }
}
