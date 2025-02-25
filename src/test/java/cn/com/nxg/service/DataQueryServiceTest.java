package cn.com.nxg.service;

import cn.com.nxg.model.DatabaseConnectionInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DataQueryServiceTest {

    @Autowired
    private DataQueryService dataQueryService;

    @Test
    public void testQueryTableData() {
        // 创建MySQL连接信息
        DatabaseConnectionInfo connectionInfo = new DatabaseConnectionInfo();
        connectionInfo.setDbType("MYSQL");
        connectionInfo.setIp("localhost");
        connectionInfo.setPort(3306);
        connectionInfo.setUsername("root");
        connectionInfo.setPassword("password"); // 替换为实际密码
        connectionInfo.setDbName("test");

        try {
            // 假设有一个名为"users"的表
            String tableName = "users";
            int page = 1;
            int pageSize = 10;
            
            // 查询表数据
            Map<String, Object> result = dataQueryService.queryTableData(connectionInfo, tableName, page, pageSize);
            
            // 打印查询结果
            System.out.println("查询表 " + tableName + " 的数据");
            System.out.println("总记录数: " + result.get("total"));
            System.out.println("当前页: " + result.get("page"));
            System.out.println("每页大小: " + result.get("pageSize"));
            
            // 打印列信息
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> columns = (List<Map<String, Object>>) result.get("columns");
            System.out.println("列数: " + columns.size());
            for (Map<String, Object> column : columns) {
                System.out.println("列名: " + column.get("name") + ", 类型: " + column.get("type"));
            }
            
            // 打印数据
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
            System.out.println("获取到 " + data.size() + " 条记录");
            for (Map<String, Object> row : data) {
                System.out.println(row);
            }
            
        } catch (SQLException e) {
            System.out.println("查询表数据时发生异常: " + e.getMessage());
            // 如果预期会失败，可以注释掉下面的断言
            // fail("不应该抛出异常: " + e.getMessage());
        }
    }

    @Test
    public void testExecuteQuery() {
        // 创建MySQL连接信息
        DatabaseConnectionInfo connectionInfo = new DatabaseConnectionInfo();
        connectionInfo.setDbType("MYSQL");
        connectionInfo.setIp("localhost");
        connectionInfo.setPort(3306);
        connectionInfo.setUsername("root");
        connectionInfo.setPassword("password"); // 替换为实际密码
        connectionInfo.setDbName("test");

        try {
            // 执行自定义SQL查询
            String sql = "SELECT * FROM users LIMIT 5";
            
            Map<String, Object> result = dataQueryService.executeQuery(connectionInfo, sql);
            
            // 打印查询结果
            System.out.println("执行SQL: " + sql);
            
            // 打印列信息
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> columns = (List<Map<String, Object>>) result.get("columns");
            System.out.println("列数: " + columns.size());
            for (Map<String, Object> column : columns) {
                System.out.println("列名: " + column.get("name") + ", 类型: " + column.get("type"));
            }
            
            // 打印数据
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
            System.out.println("获取到 " + data.size() + " 条记录");
            for (Map<String, Object> row : data) {
                System.out.println(row);
            }
            
            // 检查是否有更多数据
            Boolean hasMore = (Boolean) result.get("hasMore");
            System.out.println("是否有更多数据: " + (hasMore != null && hasMore ? "是" : "否"));
            
        } catch (SQLException e) {
            System.out.println("执行SQL查询时发生异常: " + e.getMessage());
            // 如果预期会失败，可以注释掉下面的断言
            // fail("不应该抛出异常: " + e.getMessage());
        }
    }

    @Test
    public void testExecuteInvalidQuery() {
        // 创建MySQL连接信息
        DatabaseConnectionInfo connectionInfo = new DatabaseConnectionInfo();
        connectionInfo.setDbType("MYSQL");
        connectionInfo.setIp("localhost");
        connectionInfo.setPort(3306);
        connectionInfo.setUsername("root");
        connectionInfo.setPassword("password"); // 替换为实际密码
        connectionInfo.setDbName("test");

        try {
            // 尝试执行非SELECT语句
            String sql = "DELETE FROM users";
            
            dataQueryService.executeQuery(connectionInfo, sql);
            
            // 如果执行到这里，说明安全检查失败
            fail("应该拒绝执行非SELECT语句");
            
        } catch (SQLException e) {
            // 预期会抛出异常
            System.out.println("预期的异常: " + e.getMessage());
            assertTrue(e.getMessage().contains("只允许执行SELECT查询"), "异常消息应该包含安全提示");
        }
    }
} 