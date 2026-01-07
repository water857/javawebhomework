package com.smartcommunity.service;

import com.smartcommunity.entity.Repair;
import com.smartcommunity.entity.RepairImage;
import com.smartcommunity.entity.RepairEvaluation;
import java.util.List;

public interface RepairService {
    // 报修相关方法
    Repair submitRepair(Repair repair, List<String> imageUrls);
    List<Repair> getRepairsByResidentId(int residentId);
    List<Repair> getRepairsByResidentId(int residentId, String searchKeyword, String status);
    List<Repair> getRepairsByServiceProviderId(int serviceProviderId);
    List<Repair> getRepairsByServiceProviderId(int serviceProviderId, String searchKeyword, String status);
    List<Repair> getAllRepairs();
    List<Repair> getAllRepairs(String searchKeyword, String status);
    List<Repair> getAllRepairs(String searchKeyword, String status, Integer residentId);
    Repair getRepairById(int id);
    Repair updateRepairStatus(int id, String status, int userId, String role);
    Repair assignRepair(int id, int providerId, int adminId);
    void deleteRepair(int id, int residentId);
    
    // 报修评价相关方法
    RepairEvaluation addRepairEvaluation(int repairId, int residentId, int rating, String comment);
    List<RepairEvaluation> getEvaluationsByServiceProviderId(int serviceProviderId);
}