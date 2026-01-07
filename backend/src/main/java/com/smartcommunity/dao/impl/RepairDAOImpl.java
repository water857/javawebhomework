package com.smartcommunity.dao.impl;

import com.smartcommunity.dao.RepairDAO;
import com.smartcommunity.entity.Repair;
import com.smartcommunity.entity.RepairImage;
import com.smartcommunity.entity.RepairEvaluation;
import com.smartcommunity.entity.User;
import com.smartcommunity.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RepairDAOImpl implements RepairDAO {

    @Override
    public int addRepair(Repair repair) {
        String sql = "INSERT INTO repair (resident_id, title, content, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, repair.getResidentId());
            pstmt.setString(2, repair.getTitle());
            pstmt.setString(3, repair.getContent());
            pstmt.setString(4, repair.getStatus());
            // 设置创建时间和更新时间
            Timestamp now = new Timestamp(System.currentTimeMillis());
            pstmt.setTimestamp(5, now);
            pstmt.setTimestamp(6, now);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to add repair", e);
        }
    }

    @Override
    public List<Repair> getRepairsByResidentId(int residentId) {
        return getRepairsByResidentId(residentId, null, null);
    }

    @Override
    public List<Repair> getRepairsByResidentId(int residentId, String searchKeyword, String status) {
        String sql = "SELECT r.*, u1.real_name as resident_name, u1.phone as resident_phone, u1.email as resident_email, u1.address as resident_address, " +
                "u2.real_name as provider_name, u2.phone as provider_phone, " +
                "u3.real_name as admin_name " +
                "FROM repair r " +
                "LEFT JOIN user u1 ON r.resident_id = u1.id " +
                "LEFT JOIN user u2 ON r.assigned_to = u2.id " +
                "LEFT JOIN user u3 ON r.property_admin_id = u3.id " +
                "WHERE r.resident_id = ?";
        
        // 构建动态WHERE条件
        List<String> conditions = new ArrayList<>();
        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            conditions.add("(r.title LIKE ? OR r.content LIKE ?)");
        }
        if (status != null && !status.isEmpty()) {
            conditions.add("r.status = ?");
        }
        
        // 添加条件到SQL
        if (!conditions.isEmpty()) {
            sql += " AND " + String.join(" AND ", conditions);
        }
        
        sql += " ORDER BY r.created_at DESC";
        
        List<Repair> repairs = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置参数
            int paramIndex = 1;
            pstmt.setInt(paramIndex++, residentId);
            
            if (searchKeyword != null && !searchKeyword.isEmpty()) {
                String likePattern = "%" + searchKeyword + "%";
                pstmt.setString(paramIndex++, likePattern);
                pstmt.setString(paramIndex++, likePattern);
            }
            
            if (status != null && !status.isEmpty()) {
                pstmt.setString(paramIndex++, status);
            }
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Repair repair = mapRepair(rs);
                // 获取报修图片（添加异常处理）
                try {
                    repair.setImages(getRepairImages(repair.getId()));
                } catch (Exception e) {
                    e.printStackTrace();
                    // 即使获取图片失败，也继续执行
                    repair.setImages(new ArrayList<>());
                }
                // 获取报修评价（添加异常处理）
                try {
                    repair.setEvaluation(getRepairEvaluation(repair.getId()));
                } catch (Exception e) {
                    e.printStackTrace();
                    // 即使获取评价失败，也继续执行
                    repair.setEvaluation(null);
                }
                repairs.add(repair);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get repairs by resident ID", e);
        }
        return repairs;
    }

    @Override
    public List<Repair> getRepairsByServiceProviderId(int serviceProviderId) {
        return getRepairsByServiceProviderId(serviceProviderId, null, null);
    }

    @Override
    public List<Repair> getRepairsByServiceProviderId(int serviceProviderId, String searchKeyword, String status) {
        String sql = "SELECT r.*, u1.real_name as resident_name, u1.phone as resident_phone, u1.email as resident_email, u1.address as resident_address, " +
                "u2.real_name as provider_name, u2.phone as provider_phone, " +
                "u3.real_name as admin_name " +
                "FROM repair r " +
                "LEFT JOIN user u1 ON r.resident_id = u1.id " +
                "LEFT JOIN user u2 ON r.assigned_to = u2.id " +
                "LEFT JOIN user u3 ON r.property_admin_id = u3.id " +
                "WHERE r.assigned_to = ?";
        
        // 构建动态WHERE条件
        List<String> conditions = new ArrayList<>();
        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            conditions.add("(r.title LIKE ? OR r.content LIKE ?)");
        }
        if (status != null && !status.isEmpty()) {
            conditions.add("r.status = ?");
        }
        
        // 添加条件到SQL
        if (!conditions.isEmpty()) {
            sql += " AND " + String.join(" AND ", conditions);
        }
        
        sql += " ORDER BY r.created_at DESC";
        
        List<Repair> repairs = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置参数
            int paramIndex = 1;
            pstmt.setInt(paramIndex++, serviceProviderId);
            
            if (searchKeyword != null && !searchKeyword.isEmpty()) {
                String likePattern = "%" + searchKeyword + "%";
                pstmt.setString(paramIndex++, likePattern);
                pstmt.setString(paramIndex++, likePattern);
            }
            
            if (status != null && !status.isEmpty()) {
                pstmt.setString(paramIndex++, status);
            }
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Repair repair = mapRepair(rs);
                // 获取报修图片（添加异常处理）
                try {
                    repair.setImages(getRepairImages(repair.getId()));
                } catch (Exception e) {
                    e.printStackTrace();
                    // 即使获取图片失败，也继续执行
                    repair.setImages(new ArrayList<>());
                }
                // 获取报修评价（添加异常处理）
                try {
                    repair.setEvaluation(getRepairEvaluation(repair.getId()));
                } catch (Exception e) {
                    e.printStackTrace();
                    // 即使获取评价失败，也继续执行
                    repair.setEvaluation(null);
                }
                repairs.add(repair);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get repairs by service provider ID", e);
        }
        return repairs;
    }

    @Override
    public List<Repair> getAllRepairs() {
        return getAllRepairs(null, null);
    }

    @Override
    public List<Repair> getAllRepairs(String searchKeyword, String status) {
        return getAllRepairs(searchKeyword, status, null);
    }
    
    @Override
    public List<Repair> getAllRepairs(String searchKeyword, String status, Integer residentId) {
        String sql = "SELECT r.*, u1.real_name as resident_name, u1.phone as resident_phone, u1.email as resident_email, u1.address as resident_address, " +
                "u2.real_name as provider_name, u2.phone as provider_phone, " +
                "u3.real_name as admin_name " +
                "FROM repair r " +
                "LEFT JOIN user u1 ON r.resident_id = u1.id " +
                "LEFT JOIN user u2 ON r.assigned_to = u2.id " +
                "LEFT JOIN user u3 ON r.property_admin_id = u3.id " +
                "WHERE 1 = 1";
        
        // 构建动态WHERE条件
        List<String> conditions = new ArrayList<>();
        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            conditions.add("(r.title LIKE ? OR r.content LIKE ?)");
        }
        if (status != null && !status.isEmpty()) {
            conditions.add("r.status = ?");
        }
        if (residentId != null) {
            conditions.add("r.resident_id = ?");
        }
        
        // 添加条件到SQL
        if (!conditions.isEmpty()) {
            sql += " AND " + String.join(" AND ", conditions);
        }
        
        sql += " ORDER BY r.created_at DESC";
        
        List<Repair> repairs = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置参数
            int paramIndex = 1;
            
            if (searchKeyword != null && !searchKeyword.isEmpty()) {
                String likePattern = "%" + searchKeyword + "%";
                pstmt.setString(paramIndex++, likePattern);
                pstmt.setString(paramIndex++, likePattern);
            }
            
            if (status != null && !status.isEmpty()) {
                pstmt.setString(paramIndex++, status);
            }
            
            if (residentId != null) {
                pstmt.setInt(paramIndex++, residentId);
            }
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Repair repair = mapRepair(rs);
                // 获取报修图片（添加异常处理）
                try {
                    repair.setImages(getRepairImages(repair.getId()));
                } catch (Exception e) {
                    e.printStackTrace();
                    // 即使获取图片失败，也继续执行
                    repair.setImages(new ArrayList<>());
                }
                // 获取报修评价（添加异常处理）
                try {
                    repair.setEvaluation(getRepairEvaluation(repair.getId()));
                } catch (Exception e) {
                    e.printStackTrace();
                    // 即使获取评价失败，也继续执行
                    repair.setEvaluation(null);
                }
                repairs.add(repair);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get all repairs", e);
        }
        return repairs;
    }

    @Override
    public Repair getRepairById(int id) {
        String sql = "SELECT r.*, u1.real_name as resident_name, u1.phone as resident_phone, u1.email as resident_email, u1.address as resident_address, " +
                "u2.real_name as provider_name, u2.phone as provider_phone, " +
                "u3.real_name as admin_name " +
                "FROM repair r " +
                "LEFT JOIN user u1 ON r.resident_id = u1.id " +
                "LEFT JOIN user u2 ON r.assigned_to = u2.id " +
                "LEFT JOIN user u3 ON r.property_admin_id = u3.id " +
                "WHERE r.id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Repair repair = mapRepair(rs);
                // 获取报修图片（添加异常处理）
                try {
                    repair.setImages(getRepairImages(id));
                } catch (Exception e) {
                    e.printStackTrace();
                    // 即使获取图片失败，也继续执行
                    repair.setImages(new ArrayList<>());
                }
                // 获取报修评价（添加异常处理）
                try {
                    repair.setEvaluation(getRepairEvaluation(id));
                } catch (Exception e) {
                    e.printStackTrace();
                    // 即使获取评价失败，也继续执行
                    repair.setEvaluation(null);
                }
                return repair;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get repair by ID", e);
        }
    }

    @Override
    public int updateRepair(Repair repair) {
        String sql = "UPDATE repair SET title = ?, content = ?, status = ?, assigned_to = ?, " +
                "property_admin_id = ?, repair_time = ?, completed_time = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, repair.getTitle());
            pstmt.setString(2, repair.getContent());
            pstmt.setString(3, repair.getStatus());
            pstmt.setObject(4, repair.getAssignedTo());
            pstmt.setObject(5, repair.getPropertyAdminId());
            pstmt.setTimestamp(6, repair.getRepairTime() != null ? new Timestamp(repair.getRepairTime().getTime()) : null);
            pstmt.setTimestamp(7, repair.getCompletedTime() != null ? new Timestamp(repair.getCompletedTime().getTime()) : null);
            // 设置更新时间
            pstmt.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
            pstmt.setInt(9, repair.getId());
            
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update repair", e);
        }
    }

    @Override
    public int deleteRepair(int id) {
        try (Connection conn = DBUtil.getConnection()) {
            return deleteRepair(id, conn);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to delete repair", e);
        }
    }
    
    public int deleteRepair(int id, Connection conn) throws SQLException {
        String sql = "DELETE FROM repair WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to delete repair", e);
        }
    }

    @Override
    public int addRepairImage(RepairImage image) {
        String sql = "INSERT INTO repair_image (repair_id, image_url, created_at) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, image.getRepairId());
            pstmt.setString(2, image.getImageUrl());
            // 设置创建时间
            Timestamp now = new Timestamp(System.currentTimeMillis());
            pstmt.setTimestamp(3, now);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to add repair image", e);
        }
    }

    @Override
    public List<RepairImage> getRepairImages(int repairId) {
        String sql = "SELECT * FROM repair_image WHERE repair_id = ?";
        List<RepairImage> images = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, repairId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                RepairImage image = new RepairImage();
                image.setId(rs.getInt("id"));
                image.setRepairId(rs.getInt("repair_id"));
                // 修复图片URL格式，确保使用正确的uploads路径
                String imageUrl = rs.getString("image_url");
                if (imageUrl != null && imageUrl.contains("/backend/upload/")) {
                    imageUrl = imageUrl.replace("/backend/upload/", "/backend/uploads/");
                }
                image.setImageUrl(imageUrl);
                image.setCreatedAt(rs.getTimestamp("created_at"));
                images.add(image);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get repair images", e);
        }
        return images;
    }

    @Override
    public int deleteRepairImages(int repairId) {
        try (Connection conn = DBUtil.getConnection()) {
            return deleteRepairImages(repairId, conn);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to delete repair images", e);
        }
    }
    
    public int deleteRepairImages(int repairId, Connection conn) throws SQLException {
        String sql = "DELETE FROM repair_image WHERE repair_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, repairId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to delete repair images", e);
        }
    }

    @Override
    public int addRepairEvaluation(RepairEvaluation evaluation) {
        String sql = "INSERT INTO repair_evaluation (repair_id, resident_id, service_provider_id, rating, comment, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, evaluation.getRepairId());
            pstmt.setInt(2, evaluation.getResidentId());
            pstmt.setInt(3, evaluation.getServiceProviderId());
            pstmt.setInt(4, evaluation.getRating());
            pstmt.setString(5, evaluation.getComment());
            pstmt.setTimestamp(6, evaluation.getCreatedAt() != null ? new Timestamp(evaluation.getCreatedAt().getTime()) : new Timestamp(System.currentTimeMillis()));
            
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to add repair evaluation", e);
        }
    }

    @Override
    public RepairEvaluation getRepairEvaluation(int repairId) {
        String sql = "SELECT re.*, u1.real_name as resident_name, u2.real_name as provider_name " +
                "FROM repair_evaluation re " +
                "LEFT JOIN user u1 ON re.resident_id = u1.id " +
                "LEFT JOIN user u2 ON re.service_provider_id = u2.id " +
                "WHERE re.repair_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, repairId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                RepairEvaluation evaluation = new RepairEvaluation();
                evaluation.setId(rs.getInt("id"));
                evaluation.setRepairId(rs.getInt("repair_id"));
                evaluation.setResidentId(rs.getInt("resident_id"));
                evaluation.setServiceProviderId(rs.getInt("service_provider_id"));
                evaluation.setRating(rs.getInt("rating"));
                evaluation.setComment(rs.getString("comment"));
                evaluation.setCreatedAt(rs.getTimestamp("created_at"));
                
                // 设置居民信息
                User resident = new User();
                resident.setRealName(rs.getString("resident_name"));
                evaluation.setResident(resident);
                
                // 设置服务商信息
                User provider = new User();
                provider.setRealName(rs.getString("provider_name"));
                evaluation.setServiceProvider(provider);
                
                return evaluation;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get repair evaluation", e);
        }
    }

    @Override
    public List<RepairEvaluation> getEvaluationsByServiceProviderId(int serviceProviderId) {
        String sql = "SELECT re.*, u1.real_name as resident_name, r.title as repair_title " +
                "FROM repair_evaluation re " +
                "LEFT JOIN user u1 ON re.resident_id = u1.id " +
                "LEFT JOIN repair r ON re.repair_id = r.id " +
                "WHERE re.service_provider_id = ? ORDER BY re.created_at DESC";
        List<RepairEvaluation> evaluations = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, serviceProviderId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                RepairEvaluation evaluation = new RepairEvaluation();
                evaluation.setId(rs.getInt("id"));
                evaluation.setRepairId(rs.getInt("repair_id"));
                evaluation.setResidentId(rs.getInt("resident_id"));
                evaluation.setServiceProviderId(rs.getInt("service_provider_id"));
                evaluation.setRating(rs.getInt("rating"));
                evaluation.setComment(rs.getString("comment"));
                evaluation.setCreatedAt(rs.getTimestamp("created_at"));
                
                // 设置居民信息
                User resident = new User();
                resident.setRealName(rs.getString("resident_name"));
                evaluation.setResident(resident);
                
                evaluations.add(evaluation);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get evaluations by service provider ID", e);
        }
        return evaluations;
    }

    @Override
    public int deleteRepairEvaluation(int repairId) {
        try (Connection conn = DBUtil.getConnection()) {
            return deleteRepairEvaluation(repairId, conn);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to delete repair evaluation", e);
        }
    }
    
    public int deleteRepairEvaluation(int repairId, Connection conn) throws SQLException {
        String sql = "DELETE FROM repair_evaluation WHERE repair_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, repairId);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to delete repair evaluation", e);
        }
    }

    // 辅助方法：将ResultSet映射为Repair对象
    private Repair mapRepair(ResultSet rs) throws SQLException {
        Repair repair = new Repair();
        repair.setId(rs.getInt("id"));
        repair.setResidentId(rs.getInt("resident_id"));
        repair.setTitle(rs.getString("title"));
        repair.setContent(rs.getString("content"));
        repair.setStatus(rs.getString("status"));
        // 处理可能为null的assigned_to字段
        repair.setAssignedTo(rs.getObject("assigned_to") != null ? rs.getInt("assigned_to") : null);
        // 处理可能为null的property_admin_id字段
        repair.setPropertyAdminId(rs.getObject("property_admin_id") != null ? rs.getInt("property_admin_id") : null);
        repair.setRepairTime(rs.getTimestamp("repair_time"));
        repair.setCompletedTime(rs.getTimestamp("completed_time"));
        repair.setCreatedAt(rs.getTimestamp("created_at"));
        repair.setUpdatedAt(rs.getTimestamp("updated_at"));
        
        // 设置居民信息
        User resident = new User();
        resident.setId(repair.getResidentId());
        resident.setRealName(rs.getString("resident_name"));
        resident.setPhone(rs.getString("resident_phone"));
        resident.setEmail(rs.getString("resident_email")); // 可能为null，某些查询中不包含此字段
        resident.setAddress(rs.getString("resident_address"));
        repair.setResident(resident);
        
        // 设置服务商信息
        if (repair.getAssignedTo() != null) {
            User provider = new User();
            provider.setId(repair.getAssignedTo());
            provider.setRealName(rs.getString("provider_name"));
            provider.setPhone(rs.getString("provider_phone"));
            repair.setServiceProvider(provider);
        }
        
        // 设置物业管理员信息
        if (repair.getPropertyAdminId() != null) {
            User admin = new User();
            admin.setId(repair.getPropertyAdminId());
            admin.setRealName(rs.getString("admin_name"));
            repair.setPropertyAdmin(admin);
        }
        
        return repair;
    }
}