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
        String sql = "INSERT INTO lost_and_found (user_id, type, title, description, contact, create_time) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, record.getUserId());
            stmt.setString(2, record.getType());
            stmt.setString(3, record.getTitle());
            stmt.setString(4, record.getDescription());
            stmt.setString(5, record.getContact());
            stmt.setTimestamp(6, record.getCreateTime());
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
        String baseSql = "SELECT lost.id, lost.user_id, lost.type, lost.title, lost.description, lost.contact, lost.create_time, " +
                "user.real_name AS publisher_name FROM lost_and_found lost LEFT JOIN user ON lost.user_id = user.id";
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
                    record.setUserId(rs.getInt("user_id"));
                    record.setType(rs.getString("type"));
                    record.setTitle(rs.getString("title"));
                    record.setDescription(rs.getString("description"));
                    record.setContact(rs.getString("contact"));
                    record.setCreateTime(rs.getTimestamp("create_time"));
                    record.setPublisherName(rs.getString("publisher_name"));
                    records.add(record);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }
}
