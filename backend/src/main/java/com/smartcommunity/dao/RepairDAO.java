package com.smartcommunity.dao;

import com.smartcommunity.entity.Repair;
import com.smartcommunity.entity.RepairImage;
import com.smartcommunity.entity.RepairEvaluation;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface RepairDAO {
    // 报修相关方法
    int addRepair(Repair repair);
    List<Repair> getRepairsByResidentId(int residentId);
    List<Repair> getRepairsByResidentId(int residentId, String searchKeyword, String status);
    List<Repair> getRepairsByServiceProviderId(int serviceProviderId);
    List<Repair> getRepairsByServiceProviderId(int serviceProviderId, String searchKeyword, String status);
    List<Repair> getAllRepairs();
    List<Repair> getAllRepairs(String searchKeyword, String status);
    List<Repair> getAllRepairs(String searchKeyword, String status, Integer residentId);
    Repair getRepairById(int id);
    int updateRepair(Repair repair);
    int deleteRepair(int id);
    int deleteRepair(int id, Connection conn) throws SQLException;
    
    // 报修图片相关方法
    int addRepairImage(RepairImage image);
    List<RepairImage> getRepairImages(int repairId);
    int deleteRepairImages(int repairId);
    int deleteRepairImages(int repairId, Connection conn) throws SQLException;
    
    // 报修评价相关方法
    int addRepairEvaluation(RepairEvaluation evaluation);
    RepairEvaluation getRepairEvaluation(int repairId);
    int deleteRepairEvaluation(int repairId);
    int deleteRepairEvaluation(int repairId, Connection conn) throws SQLException;
    List<RepairEvaluation> getEvaluationsByServiceProviderId(int serviceProviderId);
}