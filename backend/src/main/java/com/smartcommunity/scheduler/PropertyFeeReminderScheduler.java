package com.smartcommunity.scheduler;

import com.smartcommunity.dao.PropertyFeeBillDAO;
import com.smartcommunity.dao.impl.PropertyFeeBillDAOImpl;
import com.smartcommunity.entity.PropertyFeeBill;
import com.smartcommunity.service.PropertyFeeBillService;
import com.smartcommunity.service.impl.PropertyFeeBillServiceImpl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class PropertyFeeReminderScheduler {

    private final PropertyFeeBillService billService;
    private final PropertyFeeBillDAO billDAO;
    private Timer timer;

    public PropertyFeeReminderScheduler() {
        this.billDAO = new PropertyFeeBillDAOImpl();
        this.billService = new PropertyFeeBillServiceImpl();
    }

    public void start() {
        if (timer != null) {
            timer.cancel();
        }
        
        timer = new Timer("PropertyFeeReminderScheduler");
        
        // 每天凌晨1点执行一次提醒检查
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        
        // 如果当前时间已经过了1点，则设置为明天凌晨1点
        Date firstExecutionTime = calendar.getTime();
        if (firstExecutionTime.before(new Date())) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            firstExecutionTime = calendar.getTime();
        }
        
        // 每天执行一次
        long period = TimeUnit.DAYS.toMillis(1);
        
        timer.scheduleAtFixedRate(new ReminderTask(), firstExecutionTime, period);
        
        System.out.println("Property fee reminder scheduler started. First execution: " + firstExecutionTime);
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
            System.out.println("Property fee reminder scheduler stopped.");
        }
    }

    private class ReminderTask extends TimerTask {
        @Override
        public void run() {
            System.out.println("Starting property fee reminder check at: " + new Date());
            
            try {
                // 自动更新账单状态（未支付→逾期）
                billService.autoUpdateBillStatus();
                
                // 发送缴费提醒
                sendPaymentReminders();
                
                // 发送逾期提醒
                sendOverdueReminders();
                
            } catch (Exception e) {
                System.err.println("Error during reminder check: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void sendPaymentReminders() {
        // 获取所有未支付的账单
        List<PropertyFeeBill> unpaidBills = billService.getAllBills("unpaid", null, null);
        Date today = new Date();
        
        for (PropertyFeeBill bill : unpaidBills) {
            // 计算距离截止日期还有几天
            long diffInMillies = bill.getDueDate().getTime() - today.getTime();
            long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            
            // 检查是否需要发送提醒（提前7天、3天、1天）
            if (diffInDays == 7 || diffInDays == 3 || diffInDays == 1) {
                // 这里可以添加具体的提醒发送逻辑（邮件、短信、APP推送等）
                System.out.println("Sending payment reminder for bill ID " + bill.getId() + ", user ID " + bill.getUserId() + ", due in " + diffInDays + " days");
                
                // 标记提醒为已发送
                billService.markReminderSent(bill.getId());
            }
        }
    }

    private void sendOverdueReminders() {
        // 获取所有逾期账单
        List<PropertyFeeBill> overdueBills = billService.getOverdueBills();
        Date today = new Date();
        
        for (PropertyFeeBill bill : overdueBills) {
            // 计算逾期天数
            long diffInMillies = today.getTime() - bill.getDueDate().getTime();
            long overdueDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            
            // 检查是否需要发送逾期提醒（逾期1天、7天、15天）
            if (overdueDays == 1 || overdueDays == 7 || overdueDays == 15) {
                // 这里可以添加具体的提醒发送逻辑（邮件、短信、APP推送等）
                System.out.println("Sending overdue reminder for bill ID " + bill.getId() + ", user ID " + bill.getUserId() + ", overdue by " + overdueDays + " days");
            }
        }
    }

    // 主方法，用于测试
    public static void main(String[] args) {
        PropertyFeeReminderScheduler scheduler = new PropertyFeeReminderScheduler();
        scheduler.start();
        
        // 运行10秒后停止
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        scheduler.stop();
    }
}