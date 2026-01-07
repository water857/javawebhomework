package com.smartcommunity.dao.impl;

import com.smartcommunity.dao.SecondHandItemDAO;
import com.smartcommunity.entity.SecondHandItem;
import com.smartcommunity.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SecondHandItemDAOImpl implements SecondHandItemDAO {
    @Override
    public int createItem(SecondHandItem item) {
        String sql = "INSERT INTO second_hand_item (user_id, title, description, price, status, create_time) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, item.getUserId());
            stmt.setString(2, item.getTitle());
            stmt.setString(3, item.getDescription());
            stmt.setBigDecimal(4, item.getPrice());
            stmt.setString(5, item.getStatus());
            stmt.setTimestamp(6, item.getCreateTime());
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
    public List<SecondHandItem> getItems(String status) {
        List<SecondHandItem> items = new ArrayList<>();
        String baseSql = "SELECT id, user_id, title, description, price, status, create_time FROM second_hand_item";
        String sql = status == null ? baseSql + " ORDER BY create_time DESC" : baseSql + " WHERE status = ? ORDER BY create_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (status != null) {
                stmt.setString(1, status);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    @Override
    public SecondHandItem getItemById(int id) {
        String sql = "SELECT id, user_id, title, description, price, status, create_time FROM second_hand_item WHERE id = ?";
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
    public int updateStatus(int id, String status) {
        String sql = "UPDATE second_hand_item SET status = ? WHERE id = ?";
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

    private SecondHandItem mapRow(ResultSet rs) throws SQLException {
        SecondHandItem item = new SecondHandItem();
        item.setId(rs.getInt("id"));
        item.setUserId(rs.getInt("user_id"));
        item.setTitle(rs.getString("title"));
        item.setDescription(rs.getString("description"));
        item.setPrice(rs.getBigDecimal("price"));
        item.setStatus(rs.getString("status"));
        item.setCreateTime(rs.getTimestamp("create_time"));
        return item;
    }
}
