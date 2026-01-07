package com.smartcommunity.scheduler;

import com.smartcommunity.dao.PropertyFeeBillDAO;
import com.smartcommunity.dao.impl.PropertyFeeBillDAOImpl;
import com.smartcommunity.entity.PropertyFeeBill;
import com.smartcommunity.service.PropertyFeeBillService;
import com.smartcommunity.service.impl.PropertyFeeBillServiceImpl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 物业费提醒功能测试类
 */
public class PropertyFeeReminderTest {

    public static void main(String[] args) {
        // 测试提醒功能
        testReminderFunctionality();
    }

    private static void testReminderFunctionality() {
        System.out.println("开始测试物业费提醒功能...");
        
        // 1. 初始化依赖
        PropertyFeeBillDAO billDAO = new PropertyFeeBillDAOImpl();
        PropertyFeeBillService billService = new PropertyFeeBillServiceImpl();
        
        // 2. 获取所有未支付账单
        List<PropertyFeeBill> unpaidBills = billService.getAllBills("unpaid", null, null);
        System.out.println("当前未支付账单数量: " + unpaidBills.size());
        
        // 3. 打印即将到期的账单
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DAY_OF_MONTH, 7); // 7天后
        Date sevenDaysLater = calendar.getTime();
        
        System.out.println("\n7天内到期的账单:");
        for (PropertyFeeBill bill : unpaidBills) {
            if (bill.getDueDate().after(today) && bill.getDueDate().before(sevenDaysLater)) {
                long diffDays = getDaysBetween(today, bill.getDueDate());
                System.out.println("账单ID: " + bill.getId() + ", 用户ID: " + bill.getUserId() + ", 金额: " + bill.getAmount() + ", 截止日期: " + bill.getDueDate() + ", 距离截止日期还有: " + diffDays + "天, 已发送提醒: " + bill.isPaymentDeadlineReminderSent());
            }
        }
        
        // 4. 获取所有逾期账单
        List<PropertyFeeBill> overdueBills = billService.getOverdueBills();
        System.out.println("\n当前逾期账单数量: " + overdueBills.size());
        
        // 5. 打印逾期账单详情
        System.out.println("\n逾期账单详情:");
        for (PropertyFeeBill bill : overdueBills) {
            long overdueDays = getDaysBetween(bill.getDueDate(), today);
            System.out.println("账单ID: " + bill.getId() + ", 用户ID: " + bill.getUserId() + ", 金额: " + bill.getAmount() + ", 截止日期: " + bill.getDueDate() + ", 逾期天数: " + overdueDays + "天");
        }
        
        // 6. 手动调用自动更新状态功能
        System.out.println("\n正在更新账单状态...");
        billService.autoUpdateBillStatus();
        System.out.println("账单状态更新完成");
        
        // 7. 打印测试结果
        System.out.println("\n物业费提醒功能测试完成！");
        System.out.println("总结:");
        System.out.println("- 未支付账单: " + unpaidBills.size());
        System.out.println("- 逾期账单: " + overdueBills.size());
        System.out.println("- 定时任务已配置，每天凌晨1点执行");
        System.out.println("- 提醒规则: 提前7天、3天、1天发送缴费提醒，逾期1天、7天、15天发送逾期提醒");
    }
    
    /**
     * 计算两个日期之间的天数差
     */
    private static long getDaysBetween(Date startDate, Date endDate) {
        long diffInMillies = endDate.getTime() - startDate.getTime();
        return diffInMillies / (1000 * 60 * 60 * 24);
    }
}