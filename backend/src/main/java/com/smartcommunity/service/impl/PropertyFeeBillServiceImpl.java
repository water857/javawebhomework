package com.smartcommunity.service.impl;

import com.smartcommunity.dao.PropertyFeeBillDAO;
import com.smartcommunity.dao.impl.PropertyFeeBillDAOImpl;
import com.smartcommunity.entity.PropertyFeeBill;
import com.smartcommunity.service.PropertyFeeBillService;
import com.smartcommunity.service.PropertyFeePaymentService;
import com.smartcommunity.service.impl.PropertyFeePaymentServiceImpl;

import java.util.Date;
import java.util.List;

public class PropertyFeeBillServiceImpl implements PropertyFeeBillService {
    private PropertyFeeBillDAO billDAO = new PropertyFeeBillDAOImpl();
    private PropertyFeePaymentService paymentService = new PropertyFeePaymentServiceImpl();

    @Override
    public int addBill(PropertyFeeBill bill) {
        // 验证账单数据
        validateBill(bill);
        return billDAO.addBill(bill);
    }

    @Override
    public int batchAddBills(List<PropertyFeeBill> bills) {
        // 批量验证账单数据
        for (PropertyFeeBill bill : bills) {
            validateBill(bill);
        }
        return billDAO.batchAddBills(bills);
    }

    @Override
    public PropertyFeeBill getBillById(int id) {
        return billDAO.getBillById(id);
    }

    @Override
    public List<PropertyFeeBill> getBillsByUserId(int userId) {
        return billDAO.getBillsByUserId(userId);
    }

    @Override
    public List<PropertyFeeBill> getBillsByUserIdAndStatus(int userId, String status) {
        return billDAO.getBillsByUserIdAndStatus(userId, status);
    }

    @Override
    public List<PropertyFeeBill> getBillsByCondition(int userId, String status, String startDate, String endDate) {
        return billDAO.getBillsByCondition(userId, status, startDate, endDate);
    }

    @Override
    public int updateBill(PropertyFeeBill bill) {
        // 验证账单数据
        validateBill(bill);
        return billDAO.updateBill(bill);
    }

    @Override
    public int updateBillStatus(int billId, String status) {
        // 验证状态值
        validateStatus(status);
        return billDAO.updateBillStatus(billId, status);
    }

    @Override
    public int markReminderSent(int billId) {
        return billDAO.markReminderSent(billId);
    }

    @Override
    public List<PropertyFeeBill> getAllBills(String status, String startDate, String endDate) {
        return billDAO.getAllBills(status, startDate, endDate);
    }

    @Override
    public Object[] getBillStatistics() {
        return billDAO.getBillStatistics();
    }

    @Override
    public List<PropertyFeeBill> getOverdueBills() {
        return billDAO.getOverdueBills();
    }

    @Override
    public void autoUpdateBillStatus() {
        // 获取所有未支付的账单
        List<PropertyFeeBill> unpaidBills = billDAO.getAllBills("unpaid", null, null);
        Date today = new Date();
        
        for (PropertyFeeBill bill : unpaidBills) {
            // 检查是否逾期
            if (bill.getDueDate().before(today)) {
                // 更新为逾期状态
                billDAO.updateBillStatus(bill.getId(), "overdue");
            }
        }
    }

    @Override
    public void generateMonthlyBills(int year, int month) {
        // 这里可以实现生成月度账单的逻辑
        // 例如：获取所有居民用户，为每个用户生成当月的物业费账单
        // 目前留空，后续可以根据实际需求实现
    }

    @Override
    public int deleteBill(int billId) {
        return billDAO.deleteBill(billId);
    }

    private void validateBill(PropertyFeeBill bill) {
        if (bill == null) {
            throw new RuntimeException("Bill cannot be null");
        }
        if (bill.getUserId() <= 0) {
            throw new RuntimeException("Invalid user ID");
        }
        if (bill.getPeriodStart() == null || bill.getPeriodEnd() == null) {
            throw new RuntimeException("Period start and end dates are required");
        }
        if (bill.getPeriodStart().after(bill.getPeriodEnd())) {
            throw new RuntimeException("Period start date cannot be after end date");
        }
        if (bill.getAmount() <= 0) {
            throw new RuntimeException("Bill amount must be greater than zero");
        }
        if (bill.getDueDate() == null) {
            throw new RuntimeException("Due date is required");
        }
        validateStatus(bill.getStatus());
    }

    private void validateStatus(String status) {
        if (status == null || status.isEmpty()) {
            throw new RuntimeException("Status cannot be empty");
        }
        // 验证状态值是否合法
        String[] validStatuses = {"unpaid", "paid", "overdue", "partially_paid"};
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
}
