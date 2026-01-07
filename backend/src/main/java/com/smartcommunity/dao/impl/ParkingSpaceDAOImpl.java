package com.smartcommunity.dao.impl;

import com.smartcommunity.dao.ParkingSpaceDAO;
import com.smartcommunity.entity.ParkingSpace;
import com.smartcommunity.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ParkingSpaceDAOImpl implements ParkingSpaceDAO {
    @Override
    public List<ParkingSpace> getAllSpaces() {
        List<ParkingSpace> spaces = new ArrayList<>();
        String sql = "SELECT id, code, status, owner_id FROM parking_space ORDER BY id ASC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ParkingSpace space = new ParkingSpace();
                space.setId(rs.getInt("id"));
                space.setCode(rs.getString("code"));
                space.setStatus(rs.getString("status"));
                int owner = rs.getInt("owner_id");
                space.setOwnerId(rs.wasNull() ? null : owner);
                spaces.add(space);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return spaces;
    }

    @Override
    public ParkingSpace getSpaceById(int id) {
        String sql = "SELECT id, code, status, owner_id FROM parking_space WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ParkingSpace space = new ParkingSpace();
                    space.setId(rs.getInt("id"));
                    space.setCode(rs.getString("code"));
                    space.setStatus(rs.getString("status"));
                    int owner = rs.getInt("owner_id");
                    space.setOwnerId(rs.wasNull() ? null : owner);
                    return space;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int updateSpaceStatus(int id, String status, Integer ownerId) {
        String sql = "UPDATE parking_space SET status = ?, owner_id = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            if (ownerId == null) {
                stmt.setNull(2, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(2, ownerId);
            }
            stmt.setInt(3, id);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
