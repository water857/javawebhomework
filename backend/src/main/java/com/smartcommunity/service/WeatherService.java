package com.smartcommunity.service;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;

public class WeatherService {
    
    // 这里使用高德地图天气API作为示例，实际使用时需要替换为有效的API密钥
    private static final String WEATHER_API_KEY = "your_weather_api_key";
    private static final String WEATHER_API_URL = "https://restapi.amap.com/v3/weather/weatherInfo?key=%s&city=%s&extensions=all";
    
    private Gson gson = new Gson();
    
    /**
     * 获取指定城市的天气信息
     */
    public String getWeatherInfo(String city) {
        // 如果API密钥未配置，返回模拟数据
        if (WEATHER_API_KEY.equals("your_weather_api_key")) {
            return getMockWeatherData();
        }
        
        String url = String.format(WEATHER_API_URL, WEATHER_API_KEY, city);
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(new HttpGet(url))) {
            
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity, "UTF-8");
                return result;
            }
        } catch (IOException e) {
            e.printStackTrace();
            // API调用失败时返回模拟数据
            return getMockWeatherData();
        }
        return getMockWeatherData();
    }
    
    /**
     * 获取模拟天气数据
     */
    private String getMockWeatherData() {
        // 模拟高德地图天气API返回格式
        return "{\"status\": \"1\",\"count\": \"1\",\"info\": \"OK\",\"infocode\": \"10000\",\"lives\": [{\"province\": \"北京\",\"city\": \"北京市\",\"adcode\": \"110000\",\"weather\": \"晴\",\"temperature\": \"22\",\"winddirection\": \"东南\",\"windpower\": \"2\",\"humidity\": \"45\",\"reporttime\": \"2026-01-02 14:00:00\"}]}";
    }
    
    /**
     * 生成个性化温馨提示
     */
    public String generateWeatherTips(String weatherJson) {
        if (weatherJson == null) {
            return "当前天气信息获取失败，建议关注天气预报。";
        }
        
        try {
            Map<String, Object> weatherData = gson.fromJson(weatherJson, Map.class);
            
            // 检查weatherData是否包含"lives"字段，以及其值是否为有效的List
            if (!weatherData.containsKey("lives") || !(weatherData.get("lives") instanceof java.util.List)) {
                return "当前天气信息格式异常，建议关注天气预报。";
            }
            
            java.util.List<Object> livesList = (java.util.List<Object>) weatherData.get("lives");
            if (livesList.isEmpty()) {
                return "当前天气信息为空，建议关注天气预报。";
            }
            
            Object livesObj = livesList.get(0);
            if (!(livesObj instanceof Map)) {
                return "当前天气信息格式异常，建议关注天气预报。";
            }
            
            Map<String, Object> lives = (Map<String, Object>) livesObj;
            
            // 检查lives map是否包含必要的字段
            if (!lives.containsKey("weather") || !lives.containsKey("temperature") ||
                !lives.containsKey("winddirection") || !lives.containsKey("windpower") ||
                !lives.containsKey("humidity")) {
                return "当前天气信息不完整，建议关注天气预报。";
            }
            
            String weather = (String) lives.get("weather");
            String temperature = (String) lives.get("temperature");
            String winddirection = (String) lives.get("winddirection");
            String windpower = (String) lives.get("windpower");
            String humidity = (String) lives.get("humidity");
            
            // 检查字段值是否为空
            if (weather == null || temperature == null || winddirection == null ||
                windpower == null || humidity == null) {
                return "当前天气信息不完整，建议关注天气预报。";
            }
            
            // 根据天气状况生成温馨提示
            StringBuilder tips = new StringBuilder();
            tips.append("今日天气：").append(weather).append("，温度：").append(temperature).append("℃\n");
            tips.append("风向：").append(winddirection).append("，风力：").append(windpower).append("级，湿度：").append(humidity).append("%\n");
            
            // 根据不同天气添加温馨提示
            if (weather.contains("雨")) {
                tips.append("温馨提示：今天有雨，出门请携带雨具，注意路滑。");
            } else if (weather.contains("晴")) {
                try {
                    int temp = Integer.parseInt(temperature);
                    if (temp > 30) {
                        tips.append("温馨提示：今天天气炎热，请注意防暑降温，多喝水。");
                    } else if (temp < 10) {
                        tips.append("温馨提示：今天天气寒冷，请注意保暖，添加衣物。");
                    } else {
                        tips.append("温馨提示：今天天气宜人，适合户外活动。");
                    }
                } catch (NumberFormatException e) {
                    tips.append("温馨提示：请注意关注天气变化，合理安排出行。");
                }
            } else if (weather.contains("雪")) {
                tips.append("温馨提示：今天有雪，出门请注意交通安全，保暖防滑。");
            } else if (weather.contains("雾")) {
                tips.append("温馨提示：今天有雾，能见度较低，出门请减速慢行，注意交通安全。");
            } else if (weather.contains("霾")) {
                tips.append("温馨提示：今天空气质量较差，出门请佩戴口罩，减少户外活动。");
            } else {
                tips.append("温馨提示：请注意关注天气变化，合理安排出行。");
            }
            
            return tips.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "天气信息解析失败，建议关注天气预报。";
        }
    }
    
    /**
     * 获取天气温馨提示
     */
    public String getWeatherTips(String city) {
        String weatherJson = getWeatherInfo(city);
        return generateWeatherTips(weatherJson);
    }
}