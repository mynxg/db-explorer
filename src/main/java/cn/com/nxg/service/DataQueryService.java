package cn.com.nxg.service;

import cn.com.nxg.model.DatabaseConnectionInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataQueryService {
    
    private final DatabaseConnectionManager connectionManager;
    
    /**
     * 查询表数据
     */
    public Map<String, Object> queryTableData(DatabaseConnectionInfo connectionInfo, 
                                             String tableName, 
                                             int page, 
                                             int pageSize) throws SQLException {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> dataList = new ArrayList<>();
        List<Map<String, Object>> columns = new ArrayList<>();
        
        try (Connection conn = connectionManager.getConnection(connectionInfo)) {
            // 获取总记录数
            long total = getTableRowCount(conn, tableName);
            
            // 构建分页查询SQL
            String sql = buildPaginationSql(connectionInfo.getDbType(), tableName, page, pageSize);
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                // 获取列信息
                for (int i = 1; i <= columnCount; i++) {
                    Map<String, Object> column = new HashMap<>();
                    column.put("name", metaData.getColumnName(i));
                    column.put("type", metaData.getColumnTypeName(i));
                    columns.add(column);
                }
                
                // 获取数据
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = rs.getObject(i);
                        
                        // 处理特殊类型
                        if (value instanceof Blob) {
                            value = "[BLOB]";
                        } else if (value instanceof Clob) {
                            value = "[CLOB]";
                        }
                        
                        row.put(columnName, value);
                    }
                    dataList.add(row);
                }
            }
            
            result.put("columns", columns);
            result.put("data", dataList);
            result.put("total", total);
            result.put("page", page);
            result.put("pageSize", pageSize);
        }
        
        return result;
    }
    
    /**
     * 获取表的总记录数
     */
    private long getTableRowCount(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return 0;
    }
    
    /**
     * 构建分页查询SQL
     */
    private String buildPaginationSql(String dbType, String tableName, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        
        if ("MYSQL".equalsIgnoreCase(dbType)) {
            return String.format("SELECT * FROM %s LIMIT %d, %d", tableName, offset, pageSize);
        } else if ("ORACLE".equalsIgnoreCase(dbType)) {
            return String.format(
                "SELECT * FROM (SELECT t.*, ROWNUM rn FROM %s t WHERE ROWNUM <= %d) WHERE rn > %d",
                tableName, offset + pageSize, offset);
        }
        
        throw new IllegalArgumentException("不支持的数据库类型: " + dbType);
    }
    
    /**
     * 执行自定义SQL查询（只读模式）
     */
    public Map<String, Object> executeQuery(DatabaseConnectionInfo connectionInfo, String sql) throws SQLException {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> dataList = new ArrayList<>();
        List<Map<String, Object>> columns = new ArrayList<>();
        
        // 安全检查：只允许SELECT语句
        String trimmedSql = sql.trim().toUpperCase();
        if (!trimmedSql.startsWith("SELECT")) {
            throw new SQLException("只允许执行SELECT查询");
        }
        
        try (Connection conn = connectionManager.getConnection(connectionInfo);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            // 获取列信息
            for (int i = 1; i <= columnCount; i++) {
                Map<String, Object> column = new HashMap<>();
                column.put("name", metaData.getColumnName(i));
                column.put("type", metaData.getColumnTypeName(i));
                columns.add(column);
            }
            
            // 获取数据（限制最多返回1000条）
            int rowCount = 0;
            while (rs.next() && rowCount < 1000) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = rs.getObject(i);
                    
                    // 处理特殊类型
                    if (value instanceof Blob) {
                        value = "[BLOB]";
                    } else if (value instanceof Clob) {
                        value = "[CLOB]";
                    }
                    
                    row.put(columnName, value);
                }
                dataList.add(row);
                rowCount++;
            }
            
            result.put("columns", columns);
            result.put("data", dataList);
            result.put("hasMore", rowCount >= 1000);
        }
        
        return result;
    }
} 