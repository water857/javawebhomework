package com.smartcommunity.dao;

import com.smartcommunity.entity.PropertyFeePayment;
import java.util.List;

public interface PropertyFeePaymentDAO {
    // 添加支付记录
    int addPayment(PropertyFeePayment payment);
    
    // 根据ID查询支付记录
    PropertyFeePayment getPaymentById(int id);
    
    // 根据账单ID查询支付记录
    List<PropertyFeePayment> getPaymentsByBillId(int billId);
    
    // 根据用户ID查询支付记录
    List<PropertyFeePayment> getPaymentsByUserId(int userId);
    
    // 根据条件查询支付记录（支持时间、金额筛选）
    List<PropertyFeePayment> getPaymentsByCondition(int userId, String paymentMethod, String startDate, String endDate, Double minAmount, Double maxAmount);
    
    // 根据交易ID查询支付记录
    PropertyFeePayment getPaymentByTransactionId(String transactionId);
    
    // 更新支付记录
    int updatePayment(PropertyFeePayment payment);
    
    // 更新支付状态
    int updatePaymentStatus(int paymentId, String status);
    
    // 获取所有支付记录（管理员使用）
    List<PropertyFeePayment> getAllPayments(String paymentMethod, String status, String startDate, String endDate);
    
    // 统计支付数据（用于对账）
    Object[] getPaymentStatistics();
    
    // 获取异常交易记录（用于对账）
    List<PropertyFeePayment> getAbnormalPayments();
}
