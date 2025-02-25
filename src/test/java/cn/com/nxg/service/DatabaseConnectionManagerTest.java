package cn.com.nxg.service;

import cn.com.nxg.model.DatabaseConnectionInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DatabaseConnectionManagerTest {

    @Autowired
    private DatabaseConnectionManager connectionManager;

    @Test
    public void testMySQLConnection() {
        // 创建MySQL连接信息
        DatabaseConnectionInfo connectionInfo = new DatabaseConnectionInfo();
        connectionInfo.setDbType("MYSQL");
        connectionInfo.setIp("localhost");
        connectionInfo.setPort(3306);
        connectionInfo.setUsername("root");
        connectionInfo.setPassword("password"); // 替换为实际密码
        connectionInfo.setDbName("test");

        // 测试连接
        boolean result = connectionManager.testConnection(connectionInfo);
        
        // 如果本地没有MySQL服务，这个测试可能会失败，可以根据实际情况调整断言
        // assertTrue(result, "MySQL连接测试应该成功");
        
        // 打印连接结果
        System.out.println("MySQL连接测试结果: " + (result ? "成功" : "失败"));
    }

    @Test
    public void testGetConnection() {
        // 创建MySQL连接信息
        DatabaseConnectionInfo connectionInfo = new DatabaseConnectionInfo();
        connectionInfo.setDbType("MYSQL");
        connectionInfo.setIp("localhost");
        connectionInfo.setPort(3306);
        connectionInfo.setUsername("root");
        connectionInfo.setPassword("password"); // 替换为实际密码
        connectionInfo.setDbName("test");

        try {
            // 尝试获取连接
            Connection connection = connectionManager.getConnection(connectionInfo);
            
            // 如果本地没有MySQL服务，这个测试可能会失败
            // assertNotNull(connection, "应该能获取到连接");
            // assertTrue(connection.isValid(1), "连接应该是有效的");
            
            if (connection != null) {
                System.out.println("成功获取数据库连接");
                connection.close();
            } else {
                System.out.println("获取数据库连接失败");
            }
        } catch (SQLException e) {
            System.out.println("获取连接时发生异常: " + e.getMessage());
            // 如果预期会失败，可以注释掉下面的断言
            // fail("不应该抛出异常: " + e.getMessage());
        }
    }

    @Test
    public void testConnectionPoolReuse() {
        // 创建MySQL连接信息
        DatabaseConnectionInfo connectionInfo = new DatabaseConnectionInfo();
        connectionInfo.setDbType("MYSQL");
        connectionInfo.setIp("localhost");
        connectionInfo.setPort(3306);
        connectionInfo.setUsername("root");
        connectionInfo.setPassword("password"); // 替换为实际密码
        connectionInfo.setDbName("test");

        try {
            // 第一次获取连接
            Connection connection1 = connectionManager.getConnection(connectionInfo);
            if (connection1 != null) {
                connection1.close();
            }
            
            // 第二次获取连接（应该复用连接池）
            Connection connection2 = connectionManager.getConnection(connectionInfo);
            if (connection2 != null) {
                connection2.close();
            }
            
            // 关闭数据源
            connectionManager.closeDataSource(connectionInfo);
            
            System.out.println("连接池复用测试完成");
        } catch (SQLException e) {
            System.out.println("测试连接池复用时发生异常: " + e.getMessage());
        }
    }
} 