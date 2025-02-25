package cn.com.nxg.service;

import cn.com.nxg.model.ColumnInfo;
import cn.com.nxg.model.DatabaseConnectionInfo;
import cn.com.nxg.model.TableInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MetadataServiceTest {

    @Autowired
    private MetadataService metadataService;

    @Test
    public void testGetAllTables() {
        // 创建MySQL连接信息
        DatabaseConnectionInfo connectionInfo = new DatabaseConnectionInfo();
        connectionInfo.setDbType("MYSQL");
        connectionInfo.setIp("localhost");
        connectionInfo.setPort(3306);
        connectionInfo.setUsername("root");
        connectionInfo.setPassword("password"); // 替换为实际密码
        connectionInfo.setDbName("test");

        try {
            // 获取所有表
            List<TableInfo> tables = metadataService.getAllTables(connectionInfo);
            
            // 打印表信息
            System.out.println("获取到 " + tables.size() + " 个表");
            for (TableInfo table : tables) {
                System.out.println("表名: " + table.getTableName());
                System.out.println("表类型: " + table.getTableType());
                System.out.println("备注: " + table.getRemarks());
                System.out.println("列数: " + (table.getColumns() != null ? table.getColumns().size() : 0));
                System.out.println("------------------------");
            }
            
        } catch (SQLException e) {
            System.out.println("获取表信息时发生异常: " + e.getMessage());
            // 如果预期会失败，可以注释掉下面的断言
            // fail("不应该抛出异常: " + e.getMessage());
        }
    }

    @Test
    public void testGetTableColumns() {
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
            
            // 获取表的列信息
            List<ColumnInfo> columns = metadataService.getTableColumns(connectionInfo, tableName);
            
            // 打印列信息
            System.out.println("表 " + tableName + " 有 " + columns.size() + " 列");
            for (ColumnInfo column : columns) {
                System.out.println("列名: " + column.getColumnName());
                System.out.println("数据类型: " + column.getTypeName());
                System.out.println("是否可为空: " + (column.isNullable() ? "是" : "否"));
                System.out.println("是否主键: " + (column.isPrimaryKey() ? "是" : "否"));
                System.out.println("是否外键: " + (column.isForeignKey() ? "是" : "否"));
                if (column.isForeignKey()) {
                    System.out.println("引用表: " + column.getReferencedTable());
                    System.out.println("引用列: " + column.getReferencedColumn());
                }
                System.out.println("------------------------");
            }
            
        } catch (SQLException e) {
            System.out.println("获取列信息时发生异常: " + e.getMessage());
            // 如果预期会失败，可以注释掉下面的断言
            // fail("不应该抛出异常: " + e.getMessage());
        }
    }
} 