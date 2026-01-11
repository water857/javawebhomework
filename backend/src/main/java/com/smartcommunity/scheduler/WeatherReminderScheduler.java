package com.smartcommunity.scheduler;

import com.smartcommunity.service.WeatherService;
import com.smartcommunity.websocket.MessageWebSocket;
import com.google.gson.Gson;

import java.util.Date;
import java.util.TimerTask;
import java.util.Timer;

public class WeatherReminderScheduler {
    
    private Timer timer;
    private WeatherService weatherService;
    private String city;
    
    public WeatherReminderScheduler(String city) {
        this.weatherService = new WeatherService();
        this.city = city;
        this.timer = new Timer();
    }
    
    public void start() {
        // 每天早上8点执行一次天气提醒
        // 这里使用简单的定时任务，实际项目中可以使用更复杂的调度策略
        timer.schedule(new WeatherReminderTask(), 0, 24 * 60 * 60 * 1000);
        System.out.println("天气提醒调度器已启动，每天早上8点推送天气信息");
    }
    
    public void stop() {
        timer.cancel();
        System.out.println("天气提醒调度器已停止");
    }
    
    private class WeatherReminderTask extends TimerTask {
        @Override
        public void run() {
            try {
                System.out.println("开始执行天气提醒任务：" + new Date());
                
                // 获取天气信息
                String weatherTips = weatherService.getWeatherTips(city);
                
                // 构建消息格式
                Gson gson = new Gson();
                WeatherMessage message = new WeatherMessage();
                message.setType("weather_tips");
                message.setContent(weatherTips);
                message.setTimestamp(new Date().getTime());
                
                // 通过WebSocket发送给所有用户
                String jsonMessage = gson.toJson(message);
                MessageWebSocket.sendMessageToAll(jsonMessage);
                
                System.out.println("天气提醒任务执行完成：" + new Date());
            } catch (Exception e) {
                System.out.println("天气提醒任务执行失败：" + new Date());
                e.printStackTrace();
            }
        }
    }
    
    // 天气消息实体类
    private class WeatherMessage {
        private String type;
        private String content;
        private long timestamp;
        
        // 访问器与设置器方法
        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }
        public String getContent() {
            return content;
        }
        public void setContent(String content) {
            this.content = content;
        }
        public long getTimestamp() {
            return timestamp;
        }
        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
