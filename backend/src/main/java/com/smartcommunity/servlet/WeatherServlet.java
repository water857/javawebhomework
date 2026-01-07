package com.smartcommunity.servlet;

import com.smartcommunity.service.WeatherService;
import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/weather")
public class WeatherServlet extends HttpServlet {
    
    private WeatherService weatherService;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        super.init();
        weatherService = new WeatherService();
        gson = new Gson();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            // 获取请求参数
            String city = request.getParameter("city");
            if (city == null || city.isEmpty()) {
                city = "110000"; // 默认北京
            }
            
            // 获取天气信息
            String weatherTips = weatherService.getWeatherTips(city);
            
            // 构建响应数据
            Response result = new Response();
            result.setSuccess(true);
            result.setMessage("获取天气信息成功");
            result.setData(weatherTips);
            
            // 返回JSON响应
            out.write(gson.toJson(result));
        } catch (Exception e) {
            Response result = new Response();
            result.setSuccess(false);
            result.setMessage("获取天气信息失败：" + e.getMessage());
            out.write(gson.toJson(result));
            e.printStackTrace();
        } finally {
            out.close();
        }
    }
    
    // 内部类，用于构建响应数据
    private static class Response {
        private boolean success;
        private String message;
        private Object data;
        
        // getter和setter方法
        public boolean isSuccess() {
            return success;
        }
        public void setSuccess(boolean success) {
            this.success = success;
        }
        public String getMessage() {
            return message;
        }
        public void setMessage(String message) {
            this.message = message;
        }
        public Object getData() {
            return data;
        }
        public void setData(Object data) {
            this.data = data;
        }
    }
}