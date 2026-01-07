package com.smartcommunity.dao;

import com.smartcommunity.entity.PropertyFeeBill;
import java.util.List;

public interface PropertyFeeBillDAO {
    // 添加账单
    int addBill(PropertyFeeBill bill);
    
    // 批量添加账单
    int batchAddBills(List<PropertyFeeBill> bills);
    
    // 根据ID查询账单
    PropertyFeeBill getBillById(int id);
    
    // 根据用户ID查询账单列表
    List<PropertyFeeBill> getBillsByUserId(int userId);
    
    // 根据用户ID和状态查询账单列表
    List<PropertyFeeBill> getBillsByUserIdAndStatus(int userId, String status);
    
    // 根据条件查询账单列表（支持时间范围筛选）
    List<PropertyFeeBill> getBillsByCondition(int userId, String status, String startDate, String endDate);
    
    // 更新账单
    int updateBill(PropertyFeeBill bill);
    
    // 更新账单状态
    int updateBillStatus(int billId, String status);
    
    // 标记缴费提醒已发送
    int markReminderSent(int billId);
    
    // 获取所有账单（管理员使用）
    List<PropertyFeeBill> getAllBills(String status, String startDate, String endDate);
    
    // 统计账单数据（用于收费监控）
    Object[] getBillStatistics();
    
    // 获取逾期未缴费的账单
    List<PropertyFeeBill> getOverdueBills();
    
    // 删除账单
    int deleteBill(int billId);
}
