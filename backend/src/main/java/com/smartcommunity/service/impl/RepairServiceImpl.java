package com.smartcommunity.service.impl;

import com.smartcommunity.dao.RepairDAO;
import com.smartcommunity.dao.impl.RepairDAOImpl;
import com.smartcommunity.entity.Repair;
import com.smartcommunity.entity.RepairImage;
import com.smartcommunity.entity.RepairEvaluation;
import com.smartcommunity.service.RepairService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.smartcommunity.util.DBUtil;

public class RepairServiceImpl implements RepairService {
    private RepairDAO repairDAO = new RepairDAOImpl();

    @Override
    public Repair submitRepair(Repair repair, List<String> imageUrls) {
        // 设置初始状态为待受理
        repair.setStatus("pending");
        
        // 保存报修信息
        int repairId = repairDAO.addRepair(repair);
        repair.setId(repairId);
        
        // 保存报修图片
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String imageUrl : imageUrls) {
                RepairImage image = new RepairImage();
                image.setRepairId(repairId);
                image.setImageUrl(imageUrl);
                repairDAO.addRepairImage(image);
            }
        }
        
        // 返回完整的报修信息
        return repairDAO.getRepairById(repairId);
    }

    @Override
    public List<Repair> getRepairsByResidentId(int residentId) {
        return repairDAO.getRepairsByResidentId(residentId);
    }

    @Override
    public List<Repair> getRepairsByResidentId(int residentId, String searchKeyword, String status) {
        return repairDAO.getRepairsByResidentId(residentId, searchKeyword, status);
    }

    @Override
    public List<Repair> getRepairsByServiceProviderId(int serviceProviderId) {
        return repairDAO.getRepairsByServiceProviderId(serviceProviderId);
    }

    @Override
    public List<Repair> getRepairsByServiceProviderId(int serviceProviderId, String searchKeyword, String status) {
        return repairDAO.getRepairsByServiceProviderId(serviceProviderId, searchKeyword, status);
    }

    @Override
    public List<Repair> getAllRepairs() {
        return repairDAO.getAllRepairs();
    }

    @Override
    public List<Repair> getAllRepairs(String searchKeyword, String status) {
        return getAllRepairs(searchKeyword, status, null);
    }

    @Override
    public List<Repair> getAllRepairs(String searchKeyword, String status, Integer residentId) {
        return repairDAO.getAllRepairs(searchKeyword, status, residentId);
    }

    @Override
    public Repair getRepairById(int id) {
        return repairDAO.getRepairById(id);
    }

    @Override
    public Repair updateRepairStatus(int id, String status, int userId, String role) {
        // 获取现有报修信息
        Repair repair = repairDAO.getRepairById(id);
        if (repair == null) {
            throw new RuntimeException("Repair not found");
        }
        
        // 检查是否已完成
        if ("completed".equals(repair.getStatus())) {
            throw new RuntimeException("Cannot update a completed repair");
        }
        
        // 权限检查：服务商只能更新分配给自己的报修单
        if ("service_provider".equals(role)) {
            if (repair.getAssignedTo() == null || repair.getAssignedTo() != userId) {
                throw new RuntimeException("You can only update repairs assigned to you");
            }
        }
        
        // 只有服务商角色才能将状态更新为已完成
        if ("completed".equals(status) && !"service_provider".equals(role)) {
            throw new RuntimeException("Only service providers can mark repairs as completed");
        }
        
        // 更新状态
        repair.setStatus(status);
        
        // 根据角色设置处理人（仅管理员可以分配）
        if ("property_admin".equals(role)) {
            repair.setPropertyAdminId(userId);
        }
        
        // 根据状态设置时间
        if ("processing".equals(status)) {
            repair.setRepairTime(new Date());
        } else if ("completed".equals(status)) {
            repair.setCompletedTime(new Date());
        }
        
        // 更新数据库
        repairDAO.updateRepair(repair);
        
        // 返回更新后的报修信息
        return repairDAO.getRepairById(id);
    }

    @Override
    public Repair assignRepair(int id, int providerId, int adminId) {
        // 获取现有报修信息
        Repair repair = repairDAO.getRepairById(id);
        if (repair == null) {
            throw new RuntimeException("Repair not found");
        }
        
        // 检查是否已完成
        if ("completed".equals(repair.getStatus())) {
            throw new RuntimeException("Cannot assign a completed repair");
        }
        
        // 分配报修任务
        repair.setAssignedTo(providerId);
        repair.setPropertyAdminId(adminId);
        repair.setStatus("processing");
        repair.setRepairTime(new Date());
        
        // 更新数据库
        repairDAO.updateRepair(repair);
        
        // 返回更新后的报修信息
        return repairDAO.getRepairById(id);
    }

    @Override
    public RepairEvaluation addRepairEvaluation(int repairId, int residentId, int rating, String comment) {
        // 获取报修信息，验证权限
        Repair repair = repairDAO.getRepairById(repairId);
        if (repair == null) {
            throw new RuntimeException("Repair not found");
        }
        if (repair.getResidentId() != residentId) {
            throw new RuntimeException("No permission to evaluate this repair");
        }
        if (!"completed".equals(repair.getStatus())) {
            throw new RuntimeException("Repair is not completed yet");
        }
        if (repairDAO.getRepairEvaluation(repairId) != null) {
            throw new RuntimeException("Repair already evaluated");
        }
        
        // 创建评价对象
        RepairEvaluation evaluation = new RepairEvaluation();
        evaluation.setRepairId(repairId);
        evaluation.setResidentId(residentId);
        evaluation.setServiceProviderId(repair.getAssignedTo());
        evaluation.setRating(rating);
        evaluation.setComment(comment);
        
        // 保存评价
        repairDAO.addRepairEvaluation(evaluation);
        
        // 返回完整的评价信息
        return repairDAO.getRepairEvaluation(repairId);
    }

    @Override
    public List<RepairEvaluation> getEvaluationsByServiceProviderId(int serviceProviderId) {
        return repairDAO.getEvaluationsByServiceProviderId(serviceProviderId);
    }

    @Override
    public void deleteRepair(int id, int residentId) {
        // 获取报修信息
        Repair repair = repairDAO.getRepairById(id);
        if (repair == null) {
            throw new RuntimeException("Repair not found");
        }
        
        // 检查是否已完成
        if ("completed".equals(repair.getStatus())) {
            throw new RuntimeException("Cannot delete a completed repair");
        }
        
        // 验证权限
        if (repair.getResidentId() != residentId) {
            throw new RuntimeException("No permission to delete this repair");
        }
        
        // 使用事务进行删除操作
        try (Connection conn = DBUtil.getConnection()) {
            // 开启事务
            conn.setAutoCommit(false);
            
            try {
                // 删除相关图片
                repairDAO.deleteRepairImages(id, conn);
                
                // 删除评价（如果有）
                repairDAO.deleteRepairEvaluation(id, conn);
                
                // 删除报修记录
                repairDAO.deleteRepair(id, conn);
                
                // 提交事务
                conn.commit();
            } catch (SQLException e) {
                // 回滚事务
                conn.rollback();
                e.printStackTrace();
                throw new RuntimeException("Failed to delete repair: " + e.getMessage(), e);
            } finally {
                // 恢复自动提交
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get database connection", e);
        }
    }
}