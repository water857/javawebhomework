package com.smartcommunity.util;

import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {
    public static Properties loadProperties(String fileName) {
        Properties props = new Properties();
        try (InputStream is = PropertiesUtil.class.getClassLoader().getResourceAsStream(fileName)) {
            if (is == null) {
                throw new RuntimeException("Properties file not found: " + fileName);
            }
            props.load(is);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load properties file: " + fileName, e);
        }
        return props;
    }
}