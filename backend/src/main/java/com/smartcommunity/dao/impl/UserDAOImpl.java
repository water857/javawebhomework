package com.smartcommunity.dao.impl;

import com.smartcommunity.dao.UserDAO;
import com.smartcommunity.entity.User;
import com.smartcommunity.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserDAOImpl implements UserDAO {
    @Override
    public int addUser(User user) {
        String sql = "INSERT INTO user (username, password, real_name, phone, email, id_card, address, role, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRealName());
            pstmt.setString(4, user.getPhone());
            pstmt.setString(5, user.getEmail());
            pstmt.setString(6, user.getIdCard());
            pstmt.setString(7, user.getAddress());
            pstmt.setString(8, user.getRole());
            pstmt.setInt(9, user.getStatus());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to add user", e);
        } finally {
            DBUtil.closeConnection(conn);
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM user WHERE username = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get user by username", e);
        } finally {
            DBUtil.closeConnection(conn);
            closeResources(pstmt, rs);
        }
    }

    @Override
    public User getUserById(int id) {
        String sql = "SELECT * FROM user WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get user by id", e);
        } finally {
            DBUtil.closeConnection(conn);
            closeResources(pstmt, rs);
        }
    }

    @Override
    public int updateUser(User user) {
        String sql = "UPDATE user SET real_name = ?, phone = ?, email = ?, id_card = ?, address = ?, password = ?, updated_at = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, user.getRealName());
            pstmt.setString(2, user.getPhone());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getIdCard());
            pstmt.setString(5, user.getAddress());
            pstmt.setString(6, user.getPassword());
            pstmt.setTimestamp(7, new Timestamp(new Date().getTime()));
            pstmt.setInt(8, user.getId());
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update user", e);
        } finally {
            DBUtil.closeConnection(conn);
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM user WHERE username = ?";
        return checkExists(sql, username);
    }

    @Override
    public boolean existsByPhone(String phone) {
        String sql = "SELECT COUNT(*) FROM user WHERE phone = ?";
        return checkExists(sql, phone);
    }

    @Override
    public boolean existsByPhone(String phone, int excludeUserId) {
        String sql = "SELECT COUNT(*) FROM user WHERE phone = ? AND id != ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, phone);
            pstmt.setInt(2, excludeUserId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to check phone existence: " + e.getMessage(), e);
        } finally {
            DBUtil.closeConnection(conn);
            closeResources(pstmt, rs);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM user WHERE email = ?";
        return checkExists(sql, email);
    }

    @Override
    public boolean existsByEmail(String email, int excludeUserId) {
        String sql = "SELECT COUNT(*) FROM user WHERE email = ? AND id != ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            pstmt.setInt(2, excludeUserId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to check email existence: " + e.getMessage(), e);
        } finally {
            DBUtil.closeConnection(conn);
            closeResources(pstmt, rs);
        }
    }
    
    @Override
    public List<User> getResidents() {
        return getResidents(null, null);
    }
    
    @Override
    public List<User> getResidents(String searchKeyword, Integer status) {
        StringBuilder sql = new StringBuilder("SELECT * FROM user WHERE role = 'resident'");
        List<Object> params = new ArrayList<>();
        
        // 添加搜索关键字条件
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            sql.append(" AND (username LIKE ? OR real_name LIKE ? OR phone LIKE ? OR email LIKE ?)");
            String keyword = "%" + searchKeyword.trim() + "%";
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
        }
        
        // 添加状态条件
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<User> residents = new ArrayList<>();
        
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            
            // 设置参数
            for (int i = 0; i < params.size(); i++) {
                if (params.get(i) instanceof String) {
                    pstmt.setString(i + 1, (String) params.get(i));
                } else if (params.get(i) instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) params.get(i));
                }
            }
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                residents.add(mapResultSetToUser(rs));
            }
            
            return residents;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get residents", e);
        } finally {
            DBUtil.closeConnection(conn);
            closeResources(pstmt, rs);
        }
    }
    
    @Override
    public int updateUserStatus(int userId, int status) {
        String sql = "UPDATE user SET status = ?, updated_at = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, status);
            pstmt.setTimestamp(2, new Timestamp(new Date().getTime()));
            pstmt.setInt(3, userId);
            
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update user status", e);
        } finally {
            DBUtil.closeConnection(conn);
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    @Override
    public List<User> getProviders() {
        return getProviders(null, null);
    }
    
    @Override
    public List<User> getProviders(String searchKeyword, Integer status) {
        StringBuilder sql = new StringBuilder("SELECT * FROM user WHERE role = 'service_provider'");
        List<Object> params = new ArrayList<>();
        
        // 添加搜索关键字条件
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            sql.append(" AND (username LIKE ? OR real_name LIKE ? OR phone LIKE ? OR email LIKE ?)");
            String keyword = "%" + searchKeyword.trim() + "%";
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
            params.add(keyword);
        }
        
        // 添加状态条件
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<User> providers = new ArrayList<>();
        
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            
            // 设置参数
            for (int i = 0; i < params.size(); i++) {
                if (params.get(i) instanceof String) {
                    pstmt.setString(i + 1, (String) params.get(i));
                } else if (params.get(i) instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) params.get(i));
                }
            }
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                providers.add(mapResultSetToUser(rs));
            }
            
            return providers;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get providers", e);
        } finally {
            DBUtil.closeConnection(conn);
            closeResources(pstmt, rs);
        }
    }

    private boolean checkExists(String sql, String value) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, value);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to check existence: " + e.getMessage(), e);
        } finally {
            DBUtil.closeConnection(conn);
            closeResources(pstmt, rs);
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRealName(rs.getString("real_name"));
        user.setPhone(rs.getString("phone"));
        user.setEmail(rs.getString("email"));
        user.setIdCard(rs.getString("id_card"));
        user.setAddress(rs.getString("address"));
        user.setRole(rs.getString("role"));
        user.setStatus(rs.getInt("status"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));
        return user;
    }

    private void closeResources(PreparedStatement pstmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
