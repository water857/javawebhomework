package com.smartcommunity.dao.impl;

import com.smartcommunity.dao.PrivateMessageDAO;
import com.smartcommunity.entity.PrivateConversation;
import com.smartcommunity.entity.PrivateMessage;
import com.smartcommunity.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrivateMessageDAOImpl implements PrivateMessageDAO {
    @Override
    public int sendMessage(PrivateMessage message) {
        String sql = "INSERT INTO private_message (from_user_id, to_user_id, content, create_time, is_read) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, message.getFromUserId());
            stmt.setInt(2, message.getToUserId());
            stmt.setString(3, message.getContent());
            stmt.setTimestamp(4, message.getCreateTime());
            stmt.setBoolean(5, message.isRead());
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
    public List<PrivateMessage> getConversation(int userId, int otherUserId) {
        List<PrivateMessage> messages = new ArrayList<>();
        String sql = "SELECT id, from_user_id, to_user_id, content, create_time, is_read "
                + "FROM private_message "
                + "WHERE (from_user_id = ? AND to_user_id = ?) OR (from_user_id = ? AND to_user_id = ?) "
                + "ORDER BY create_time ASC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, otherUserId);
            stmt.setInt(3, otherUserId);
            stmt.setInt(4, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PrivateMessage message = new PrivateMessage();
                    message.setId(rs.getInt("id"));
                    message.setFromUserId(rs.getInt("from_user_id"));
                    message.setToUserId(rs.getInt("to_user_id"));
                    message.setContent(rs.getString("content"));
                    message.setCreateTime(rs.getTimestamp("create_time"));
                    message.setRead(rs.getBoolean("is_read"));
                    messages.add(message);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    @Override
    public List<PrivateConversation> getConversationList(int userId) {
        List<PrivateConversation> conversations = new ArrayList<>();
        Map<Integer, Integer> unreadCounts = new HashMap<>();
        String unreadSql = "SELECT from_user_id, COUNT(*) AS unread_count FROM private_message "
                + "WHERE to_user_id = ? AND is_read = 0 GROUP BY from_user_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(unreadSql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    unreadCounts.put(rs.getInt("from_user_id"), rs.getInt("unread_count"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String sql = "SELECT pm.id, pm.content, pm.create_time, pm.from_user_id, pm.to_user_id, "
                + "u.id AS other_user_id, u.username, u.real_name "
                + "FROM private_message pm "
                + "JOIN user u ON u.id = CASE WHEN pm.from_user_id = ? THEN pm.to_user_id ELSE pm.from_user_id END "
                + "JOIN (SELECT CASE WHEN from_user_id = ? THEN to_user_id ELSE from_user_id END AS other_user_id, "
                + "MAX(create_time) AS last_time "
                + "FROM private_message WHERE from_user_id = ? OR to_user_id = ? GROUP BY other_user_id) latest "
                + "ON latest.other_user_id = u.id AND pm.create_time = latest.last_time "
                + "WHERE pm.from_user_id = ? OR pm.to_user_id = ? "
                + "ORDER BY pm.create_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);
            stmt.setInt(4, userId);
            stmt.setInt(5, userId);
            stmt.setInt(6, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PrivateConversation conversation = new PrivateConversation();
                    int otherUserId = rs.getInt("other_user_id");
                    conversation.setOtherUserId(otherUserId);
                    conversation.setOtherUsername(rs.getString("username"));
                    conversation.setOtherRealName(rs.getString("real_name"));
                    conversation.setLastMessage(rs.getString("content"));
                    conversation.setLastTime(rs.getTimestamp("create_time"));
                    conversation.setUnreadCount(unreadCounts.getOrDefault(otherUserId, 0));
                    conversations.add(conversation);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conversations;
    }

    @Override
    public int markReadByMessageId(int messageId, int userId) {
        String sql = "UPDATE private_message SET is_read = 1 WHERE id = ? AND to_user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, messageId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int markReadByUser(int userId, int otherUserId) {
        String sql = "UPDATE private_message SET is_read = 1 WHERE to_user_id = ? AND from_user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, otherUserId);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
