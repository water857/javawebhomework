package com.smartcommunity.listener;

import com.smartcommunity.scheduler.PropertyFeeReminderScheduler;
import com.smartcommunity.scheduler.WeatherReminderScheduler;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class SchedulerListener implements ServletContextListener {
    private PropertyFeeReminderScheduler propertyFeeScheduler;
    private WeatherReminderScheduler weatherScheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // 应用启动时初始化并启动定时任务
        propertyFeeScheduler = new PropertyFeeReminderScheduler();
        propertyFeeScheduler.start();
        System.out.println("SchedulerListener: Property fee reminder scheduler started");
        
        // 启动天气提醒调度器，默认使用北京作为城市，实际项目中可以从配置文件读取
        weatherScheduler = new WeatherReminderScheduler("110000"); // 北京的城市编码
        weatherScheduler.start();
        System.out.println("SchedulerListener: Weather reminder scheduler started");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // 应用关闭时停止定时任务
        if (propertyFeeScheduler != null) {
            propertyFeeScheduler.stop();
            System.out.println("SchedulerListener: Property fee reminder scheduler stopped");
        }
        
        if (weatherScheduler != null) {
            weatherScheduler.stop();
            System.out.println("SchedulerListener: Weather reminder scheduler stopped");
        }
    }
}