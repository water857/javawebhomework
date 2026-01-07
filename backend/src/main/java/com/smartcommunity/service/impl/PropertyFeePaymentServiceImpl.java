package com.smartcommunity.service.impl;

import com.smartcommunity.dao.PropertyFeeBillDAO;
import com.smartcommunity.dao.PropertyFeePaymentDAO;
import com.smartcommunity.dao.impl.PropertyFeeBillDAOImpl;
import com.smartcommunity.dao.impl.PropertyFeePaymentDAOImpl;
import com.smartcommunity.entity.PropertyFeeBill;
import com.smartcommunity.entity.PropertyFeePayment;
import com.smartcommunity.service.PropertyFeePaymentService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class PropertyFeePaymentServiceImpl implements PropertyFeePaymentService {
    private PropertyFeePaymentDAO paymentDAO = new PropertyFeePaymentDAOImpl();
    private PropertyFeeBillDAO billDAO = new PropertyFeeBillDAOImpl();

    @Override
    public int createPayment(PropertyFeePayment payment) {
        // 验证支付数据
        validatePayment(payment);
        return paymentDAO.addPayment(payment);
    }

    @Override
    public PropertyFeePayment getPaymentById(int id) {
        return paymentDAO.getPaymentById(id);
    }

    @Override
    public List<PropertyFeePayment> getPaymentsByBillId(int billId) {
        return paymentDAO.getPaymentsByBillId(billId);
    }

    @Override
    public List<PropertyFeePayment> getPaymentsByUserId(int userId) {
        return paymentDAO.getPaymentsByUserId(userId);
    }

    @Override
    public List<PropertyFeePayment> getPaymentsByCondition(int userId, String paymentMethod, String startDate, String endDate, Double minAmount, Double maxAmount) {
        return paymentDAO.getPaymentsByCondition(userId, paymentMethod, startDate, endDate, minAmount, maxAmount);
    }

    @Override
    public PropertyFeePayment getPaymentByTransactionId(String transactionId) {
        return paymentDAO.getPaymentByTransactionId(transactionId);
    }

    @Override
    public int updatePayment(PropertyFeePayment payment) {
        // 验证支付数据
        validatePayment(payment);
        return paymentDAO.updatePayment(payment);
    }

    @Override
    public int updatePaymentStatus(int paymentId, String status) {
        // 验证状态值
        validateStatus(status);
        return paymentDAO.updatePaymentStatus(paymentId, status);
    }

    @Override
    public List<PropertyFeePayment> getAllPayments(String paymentMethod, String status, String startDate, String endDate) {
        return paymentDAO.getAllPayments(paymentMethod, status, startDate, endDate);
    }

    @Override
    public Object[] getPaymentStatistics() {
        return paymentDAO.getPaymentStatistics();
    }

    @Override
    public List<PropertyFeePayment> getAbnormalPayments() {
        return paymentDAO.getAbnormalPayments();
    }

    @Override
    public boolean handlePaymentCallback(String transactionId, String status, String paymentMethod, double amount) {
        // 根据交易ID获取支付记录
        PropertyFeePayment payment = paymentDAO.getPaymentByTransactionId(transactionId);
        if (payment == null) {
            return false;
        }

        // 验证支付金额
        if (Math.abs(payment.getAmount() - amount) > 0.01) {
            return false;
        }

        // 更新支付状态
        paymentDAO.updatePaymentStatus(payment.getId(), status);

        // 如果支付成功，更新对应账单状态
        if ("success".equals(status)) {
            // 获取账单信息
            PropertyFeeBill bill = billDAO.getBillById(payment.getBillId());
            if (bill != null) {
                // 检查是否全额支付
                double totalAmount = bill.getAmount();
                List<PropertyFeePayment> payments = paymentDAO.getPaymentsByBillId(bill.getId());
                double paidAmount = 0;
                for (PropertyFeePayment p : payments) {
                    if ("success".equals(p.getStatus())) {
                        paidAmount += p.getAmount();
                    }
                }

                // 更新账单状态
                if (Math.abs(paidAmount - totalAmount) < 0.01) {
                    billDAO.updateBillStatus(bill.getId(), "paid");
                } else if (paidAmount > 0) {
                    billDAO.updateBillStatus(bill.getId(), "partially_paid");
                }
            }
        }

        return true;
    }

    @Override
    public String executePayment(int billId, int userId, String paymentMethod, double amount) {
        // 验证参数
        if (billId <= 0 || userId <= 0 || amount <= 0) {
            throw new RuntimeException("Invalid payment parameters");
        }

        // 验证账单存在
        PropertyFeeBill bill = billDAO.getBillById(billId);
        if (bill == null) {
            throw new RuntimeException("Bill not found");
        }

        // 验证账单属于该用户
        if (bill.getUserId() != userId) {
            throw new RuntimeException("Bill does not belong to this user");
        }

        // 生成唯一交易ID
        String transactionId = generateTransactionId();

        // 创建支付记录
        PropertyFeePayment payment = new PropertyFeePayment();
        payment.setBillId(billId);
        payment.setUserId(userId);
        payment.setAmount(amount);
        payment.setPaymentMethod(paymentMethod);
        payment.setTransactionId(transactionId);
        payment.setStatus("pending");
        payment.setPaymentTime(new Date());
        payment.setAutoDeduction(false);

        // 保存支付记录
        paymentDAO.addPayment(payment);

        // 模拟支付处理（实际环境中需要调用支付渠道API）
        // 这里直接返回成功，实际环境中需要根据支付渠道返回结果处理
        simulatePaymentProcessing(payment);

        return transactionId;
    }

    @Override
    public void autoReconcile() {
        // 自动对账逻辑
        // 1. 获取所有支付记录
        // 2. 调用支付渠道API获取交易记录
        // 3. 匹配交易记录
        // 4. 标记异常交易
        // 由于是模拟环境，这里只打印日志
        System.out.println("Auto reconciliation executed at: " + new Date());
    }

    @Override
    public boolean setAutoDeduction(int userId, boolean enabled) {
        // 这里可以实现设置自动扣费的逻辑
        // 例如：将用户ID和自动扣费状态保存到数据库
        // 目前留空，后续可以根据实际需求实现
        return true;
    }

    @Override
    public List<Object> getReconciliationHistory() {
        // 实现获取对账历史，返回符合前端期望格式的数据
        System.out.println("getReconciliationHistory executed");
        
        List<Object> historyList = new ArrayList<>();
        
        // 创建模拟数据，实际环境中需要从数据库查询
        java.util.Map<String, Object> history1 = new java.util.HashMap<>();
        history1.put("id", 1);
        history1.put("reconciliationDate", "2026-01-01");
        history1.put("channel", "wechat");
        history1.put("matchedCount", 98);
        history1.put("unmatchedCount", 2);
        history1.put("amountDifference", -1.50);
        history1.put("status", "partial");
        history1.put("createdAt", "2026-01-01 15:00:00");
        historyList.add(history1);
        
        java.util.Map<String, Object> history2 = new java.util.HashMap<>();
        history2.put("id", 2);
        history2.put("reconciliationDate", "2025-12-31");
        history2.put("channel", "alipay");
        history2.put("matchedCount", 150);
        history2.put("unmatchedCount", 0);
        history2.put("amountDifference", 0);
        history2.put("status", "success");
        history2.put("createdAt", "2025-12-31 16:00:00");
        historyList.add(history2);
        
        java.util.Map<String, Object> history3 = new java.util.HashMap<>();
        history3.put("id", 3);
        history3.put("reconciliationDate", "2025-12-30");
        history3.put("channel", "unionpay");
        history3.put("matchedCount", 85);
        history3.put("unmatchedCount", 5);
        history3.put("amountDifference", 3.20);
        history3.put("status", "partial");
        history3.put("createdAt", "2025-12-30 14:30:00");
        historyList.add(history3);
        
        return historyList;
    }

    @Override
    public boolean resolveException(String transactionId) {
        // 模拟实现解决异常交易
        // 实际环境中需要更新数据库中的交易状态
        System.out.println("resolveException executed for transaction ID: " + transactionId);
        return true;
    }

    private void validatePayment(PropertyFeePayment payment) {
        if (payment == null) {
            throw new RuntimeException("Payment cannot be null");
        }
        if (payment.getBillId() <= 0) {
            throw new RuntimeException("Invalid bill ID");
        }
        if (payment.getUserId() <= 0) {
            throw new RuntimeException("Invalid user ID");
        }
        if (payment.getAmount() <= 0) {
            throw new RuntimeException("Payment amount must be greater than zero");
        }
        if (payment.getPaymentMethod() == null || payment.getPaymentMethod().isEmpty()) {
            throw new RuntimeException("Payment method is required");
        }
        if (payment.getTransactionId() == null || payment.getTransactionId().isEmpty()) {
            throw new RuntimeException("Transaction ID is required");
        }
        validateStatus(payment.getStatus());
    }

    private void validateStatus(String status) {
        if (status == null || status.isEmpty()) {
            throw new RuntimeException("Status cannot be empty");
        }
        // 验证状态值是否合法
        String[] validStatuses = {"pending", "success", "failed", "refunded"};
        boolean isValid = false;
        for (String validStatus : validStatuses) {
            if (validStatus.equals(status)) {
                isValid = true;
                break;
            }
        }
        if (!isValid) {
            throw new RuntimeException("Invalid status value");
        }
    }

    private String generateTransactionId() {
        // 生成唯一交易ID
        return UUID.randomUUID().toString().replace("-", "");
    }

    private void simulatePaymentProcessing(PropertyFeePayment payment) {
        // 模拟支付处理过程
        try {
            // 模拟网络延迟
            Thread.sleep(1000);
            // 更新支付状态为成功
            paymentDAO.updatePaymentStatus(payment.getId(), "success");
            // 处理支付回调
            handlePaymentCallback(payment.getTransactionId(), "success", payment.getPaymentMethod(), payment.getAmount());
        } catch (InterruptedException e) {
            e.printStackTrace();
            // 更新支付状态为失败
            paymentDAO.updatePaymentStatus(payment.getId(), "failed");
        }
    }
}
