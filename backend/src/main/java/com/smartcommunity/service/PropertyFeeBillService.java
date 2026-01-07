package com.smartcommunity.service;

import com.smartcommunity.entity.PropertyFeeBill;
import java.util.List;

public interface PropertyFeeBillService {
    // 添加单个账单
    int addBill(PropertyFeeBill bill);
    
    // 批量添加账单
    int batchAddBills(List<PropertyFeeBill> bills);
    
    // 根据ID获取账单详情
    PropertyFeeBill getBillById(int id);
    
    // 获取用户的所有账单
    List<PropertyFeeBill> getBillsByUserId(int userId);
    
    // 根据状态获取用户账单
    List<PropertyFeeBill> getBillsByUserIdAndStatus(int userId, String status);
    
    // 根据条件查询账单
    List<PropertyFeeBill> getBillsByCondition(int userId, String status, String startDate, String endDate);
    
    // 更新账单信息
    int updateBill(PropertyFeeBill bill);
    
    // 更新账单状态
    int updateBillStatus(int billId, String status);
    
    // 标记缴费提醒已发送
    int markReminderSent(int billId);
    
    // 管理员获取所有账单
    List<PropertyFeeBill> getAllBills(String status, String startDate, String endDate);
    
    // 获取账单统计数据
    Object[] getBillStatistics();
    
    // 获取逾期未缴费的账单
    List<PropertyFeeBill> getOverdueBills();
    
    // 自动更新账单状态（处理逾期）
    void autoUpdateBillStatus();
    
    // 生成月度账单
    void generateMonthlyBills(int year, int month);
    
    // 删除账单
    int deleteBill(int billId);
}
