package com.smartcommunity.util;

import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtil {
    private static BasicDataSource dataSource;

    static {
        try {
            Properties props = PropertiesUtil.loadProperties("db.properties");
            dataSource = new BasicDataSource();
            dataSource.setDriverClassName(props.getProperty("jdbc.driver"));
            dataSource.setUrl(props.getProperty("jdbc.url"));
            dataSource.setUsername(props.getProperty("jdbc.username"));
            dataSource.setPassword(props.getProperty("jdbc.password"));
            
            // Connection pool configuration
            dataSource.setInitialSize(Integer.parseInt(props.getProperty("jdbc.initialSize")));
            dataSource.setMaxTotal(Integer.parseInt(props.getProperty("jdbc.maxActive")));
            dataSource.setMaxIdle(Integer.parseInt(props.getProperty("jdbc.maxIdle")));
            dataSource.setMinIdle(Integer.parseInt(props.getProperty("jdbc.minIdle")));
            dataSource.setMaxWaitMillis(Integer.parseInt(props.getProperty("jdbc.maxWait")));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize database connection pool", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}