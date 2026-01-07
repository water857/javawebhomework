package com.smartcommunity.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * Gson工具类，提供统一的Gson配置
 */
public class GsonUtil {
    
    // 统一的时间格式
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    // 线程安全的Gson实例
    private static final Gson gson;
    
    static {
        // 创建GsonBuilder并配置自定义序列化器
        GsonBuilder gsonBuilder = new GsonBuilder();
        
        // 注册自定义Timestamp序列化器
        gsonBuilder.registerTypeAdapter(Timestamp.class, new TimestampSerializer());
        
        // 注册自定义java.util.Date序列化器
        gsonBuilder.registerTypeAdapter(java.util.Date.class, new DateSerializer());
        
        // 禁用HTML转义，确保特殊字符正确处理
        gsonBuilder.disableHtmlEscaping();
        
        // 创建Gson实例
        gson = gsonBuilder.create();
    }
    
    /**
     * 获取Gson实例
     * @return Gson实例
     */
    public static Gson getGson() {
        return gson;
    }
    
    /**
     * 自定义Timestamp序列化器
     */
    private static class TimestampSerializer implements JsonSerializer<Timestamp> {
        private final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        
        @Override
        public JsonElement serialize(Timestamp src, Type typeOfSrc, JsonSerializationContext context) {
            synchronized (sdf) {
                return new JsonPrimitive(sdf.format(src));
            }
        }
    }
    
    /**
     * 自定义java.util.Date序列化器
     */
    private static class DateSerializer implements JsonSerializer<java.util.Date> {
        private final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        
        @Override
        public JsonElement serialize(java.util.Date src, Type typeOfSrc, JsonSerializationContext context) {
            synchronized (sdf) {
                return new JsonPrimitive(sdf.format(src));
            }
        }
    }
}