package com.smartcommunity.service;

import com.smartcommunity.entity.PropertyFeePayment;
import java.util.List;

public interface PropertyFeePaymentService {
    // 创建支付记录
    int createPayment(PropertyFeePayment payment);
    
    // 根据ID获取支付记录
    PropertyFeePayment getPaymentById(int id);
    
    // 根据账单ID获取支付记录
    List<PropertyFeePayment> getPaymentsByBillId(int billId);
    
    // 获取用户的所有支付记录
    List<PropertyFeePayment> getPaymentsByUserId(int userId);
    
    // 根据条件查询支付记录
    List<PropertyFeePayment> getPaymentsByCondition(int userId, String paymentMethod, String startDate, String endDate, Double minAmount, Double maxAmount);
    
    // 根据交易ID获取支付记录
    PropertyFeePayment getPaymentByTransactionId(String transactionId);
    
    // 更新支付记录
    int updatePayment(PropertyFeePayment payment);
    
    // 更新支付状态
    int updatePaymentStatus(int paymentId, String status);
    
    // 管理员获取所有支付记录
    List<PropertyFeePayment> getAllPayments(String paymentMethod, String status, String startDate, String endDate);
    
    // 获取支付统计数据
    Object[] getPaymentStatistics();
    
    // 获取异常交易记录
    List<PropertyFeePayment> getAbnormalPayments();
    
    // 处理支付回调
    boolean handlePaymentCallback(String transactionId, String status, String paymentMethod, double amount);
    
    // 执行支付（集成支付渠道）
    String executePayment(int billId, int userId, String paymentMethod, double amount);
    
    // 自动对账
    void autoReconcile();
    
    // 设置自动扣费
    boolean setAutoDeduction(int userId, boolean enabled);
    
    // 获取对账历史
    List<Object> getReconciliationHistory();
    
    // 解决异常交易
    boolean resolveException(String transactionId);
}
