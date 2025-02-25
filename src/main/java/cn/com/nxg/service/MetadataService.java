package cn.com.nxg.service;

import cn.com.nxg.model.ColumnInfo;
import cn.com.nxg.model.DatabaseConnectionInfo;
import cn.com.nxg.model.TableInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetadataService {
    
    private final DatabaseConnectionManager connectionManager;
    
    /**
     * 获取所有表信息
     */
    public List<TableInfo> getAllTables(DatabaseConnectionInfo connectionInfo) throws SQLException {
        List<TableInfo> tables = new ArrayList<>();
        
        try (Connection conn = connectionManager.getConnection(connectionInfo)) {
            DatabaseMetaData metaData = conn.getMetaData();
            String catalog = null;
            String schemaPattern = null;
            
            // Oracle需要特殊处理schema
            if ("ORACLE".equalsIgnoreCase(connectionInfo.getDbType())) {
                schemaPattern = connectionInfo.getUsername().toUpperCase();
            } else if ("MYSQL".equalsIgnoreCase(connectionInfo.getDbType())) {
                catalog = connectionInfo.getDbName();
            }
            
            try (ResultSet rs = metaData.getTables(catalog, schemaPattern, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    TableInfo table = new TableInfo();
                    table.setTableName(rs.getString("TABLE_NAME"));
                    table.setTableType(rs.getString("TABLE_TYPE"));
                    table.setRemarks(rs.getString("REMARKS"));
                    
                    // 获取列信息
                    table.setColumns(getTableColumns(connectionInfo, table.getTableName()));
                    
                    tables.add(table);
                }
            }
        }
        
        return tables;
    }
    
    /**
     * 获取表的列信息
     */
    public List<ColumnInfo> getTableColumns(DatabaseConnectionInfo connectionInfo, String tableName) throws SQLException {
        List<ColumnInfo> columns = new ArrayList<>();
        Map<String, ColumnInfo> columnMap = new HashMap<>();
        
        try (Connection conn = connectionManager.getConnection(connectionInfo)) {
            DatabaseMetaData metaData = conn.getMetaData();
            String catalog = null;
            String schemaPattern = null;
            
            // Oracle需要特殊处理schema
            if ("ORACLE".equalsIgnoreCase(connectionInfo.getDbType())) {
                schemaPattern = connectionInfo.getUsername().toUpperCase();
            } else if ("MYSQL".equalsIgnoreCase(connectionInfo.getDbType())) {
                catalog = connectionInfo.getDbName();
            }
            
            // 获取列信息
            try (ResultSet rs = metaData.getColumns(catalog, schemaPattern, tableName, "%")) {
                while (rs.next()) {
                    ColumnInfo column = new ColumnInfo();
                    column.setColumnName(rs.getString("COLUMN_NAME"));
                    column.setDataType(String.valueOf(rs.getInt("DATA_TYPE")));
                    column.setTypeName(rs.getString("TYPE_NAME"));
                    column.setColumnSize(rs.getInt("COLUMN_SIZE"));
                    column.setNullable(rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                    column.setRemarks(rs.getString("REMARKS"));
                    
                    columnMap.put(column.getColumnName(), column);
                    columns.add(column);
                }
            }
            
            // 获取主键信息
            try (ResultSet rs = metaData.getPrimaryKeys(catalog, schemaPattern, tableName)) {
                while (rs.next()) {
                    String columnName = rs.getString("COLUMN_NAME");
                    ColumnInfo column = columnMap.get(columnName);
                    if (column != null) {
                        column.setPrimaryKey(true);
                    }
                }
            }
            
            // 获取外键信息
            try (ResultSet rs = metaData.getImportedKeys(catalog, schemaPattern, tableName)) {
                while (rs.next()) {
                    String columnName = rs.getString("FKCOLUMN_NAME");
                    ColumnInfo column = columnMap.get(columnName);
                    if (column != null) {
                        column.setForeignKey(true);
                        column.setReferencedTable(rs.getString("PKTABLE_NAME"));
                        column.setReferencedColumn(rs.getString("PKCOLUMN_NAME"));
                    }
                }
            }
        }
        
        return columns;
    }
} 