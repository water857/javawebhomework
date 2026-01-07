package com.smartcommunity.dao.impl;

import com.smartcommunity.dao.SkillShareDAO;
import com.smartcommunity.entity.SkillShare;
import com.smartcommunity.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SkillShareDAOImpl implements SkillShareDAO {
    @Override
    public int createSkill(SkillShare skill) {
        String sql = "INSERT INTO skill_share (user_id, skill_name, description, contact, create_time) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, skill.getUserId());
            stmt.setString(2, skill.getSkillName());
            stmt.setString(3, skill.getDescription());
            stmt.setString(4, skill.getContact());
            stmt.setTimestamp(5, skill.getCreateTime());
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
    public List<SkillShare> getSkills() {
        List<SkillShare> skills = new ArrayList<>();
        String sql = "SELECT id, user_id, skill_name, description, contact, create_time FROM skill_share ORDER BY create_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                SkillShare skill = new SkillShare();
                skill.setId(rs.getInt("id"));
                skill.setUserId(rs.getInt("user_id"));
                skill.setSkillName(rs.getString("skill_name"));
                skill.setDescription(rs.getString("description"));
                skill.setContact(rs.getString("contact"));
                skill.setCreateTime(rs.getTimestamp("create_time"));
                skills.add(skill);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return skills;
    }
}
