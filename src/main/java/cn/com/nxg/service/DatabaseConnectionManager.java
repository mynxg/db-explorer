package cn.com.nxg.service;

import cn.com.nxg.model.DatabaseConnectionInfo;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class DatabaseConnectionManager {
    
    // 使用ConcurrentHashMap存储连接池，key为连接信息的唯一标识
    private final Map<String, HikariDataSource> dataSources = new ConcurrentHashMap<>();
    
    /**
     * 获取数据库连接
     */
    public Connection getConnection(DatabaseConnectionInfo connectionInfo) throws SQLException {
        String key = generateKey(connectionInfo);
        HikariDataSource dataSource = dataSources.computeIfAbsent(key, k -> createDataSource(connectionInfo));
        
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            log.error("获取数据库连接失败: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * 测试数据库连接
     */
    public boolean testConnection(DatabaseConnectionInfo connectionInfo) {
        try (Connection conn = createDataSource(connectionInfo).getConnection()) {
            return conn.isValid(5); // 5秒超时
        } catch (Exception e) {
            log.error("测试数据库连接失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 关闭指定的数据源
     */
    public void closeDataSource(DatabaseConnectionInfo connectionInfo) {
        String key = generateKey(connectionInfo);
        HikariDataSource dataSource = dataSources.remove(key);
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
    
    /**
     * 关闭所有数据源
     */
    public void closeAllDataSources() {
        dataSources.forEach((key, dataSource) -> {
            if (!dataSource.isClosed()) {
                dataSource.close();
            }
        });
        dataSources.clear();
    }
    
    /**
     * 创建数据源
     */
    private HikariDataSource createDataSource(DatabaseConnectionInfo connectionInfo) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(connectionInfo.getJdbcUrl());
        config.setUsername(connectionInfo.getUsername());
        config.setPassword(connectionInfo.getPassword());
        
        // 基本配置
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(5);
        config.setIdleTimeout(TimeUnit.MINUTES.toMillis(10));
        config.setMaxLifetime(TimeUnit.MINUTES.toMillis(30));
        config.setConnectionTimeout(TimeUnit.SECONDS.toMillis(5));
        config.setConnectionTestQuery("SELECT 1");
        
        // 根据数据库类型设置驱动类
        if ("MYSQL".equalsIgnoreCase(connectionInfo.getDbType())) {
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        } else if ("ORACLE".equalsIgnoreCase(connectionInfo.getDbType())) {
            config.setDriverClassName("oracle.jdbc.OracleDriver");
        }
        
        return new HikariDataSource(config);
    }
    
    /**
     * 生成连接池的唯一标识
     */
    private String generateKey(DatabaseConnectionInfo connectionInfo) {
        return String.format("%s_%s_%d_%s_%s", 
                connectionInfo.getDbType(),
                connectionInfo.getIp(), 
                connectionInfo.getPort(),
                connectionInfo.getUsername(),
                connectionInfo.getDbName());
    }
} 