package cn.com.nxg.controller;

import cn.com.nxg.model.DatabaseConnectionInfo;
import cn.com.nxg.model.TableInfo;
import cn.com.nxg.service.DatabaseConnectionManager;
import cn.com.nxg.service.DataQueryService;
import cn.com.nxg.service.MetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/database")
@RequiredArgsConstructor
public class DatabaseController {
    
    private final DatabaseConnectionManager connectionManager;
    private final MetadataService metadataService;
    private final DataQueryService dataQueryService;
    
    /**
     * 测试数据库连接
     */
    @PostMapping("/test-connection")
    public ResponseEntity<Map<String, Object>> testConnection(@Valid @RequestBody DatabaseConnectionInfo connectionInfo) {
        Map<String, Object> result = new HashMap<>();
        boolean success = connectionManager.testConnection(connectionInfo);
        
        result.put("success", success);
        if (!success) {
            result.put("message", "连接失败，请检查连接信息");
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取所有表信息
     */
    @PostMapping("/tables")
    public ResponseEntity<List<TableInfo>> getAllTables(@Valid @RequestBody DatabaseConnectionInfo connectionInfo) {
        try {
            List<TableInfo> tables = metadataService.getAllTables(connectionInfo);
            return ResponseEntity.ok(tables);
        } catch (SQLException e) {
            log.error("获取表信息失败", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 查询表数据
     */
    @PostMapping("/query")
    public ResponseEntity<Map<String, Object>> queryTableData(
            @Valid @RequestBody DatabaseConnectionInfo connectionInfo,
            @RequestParam String tableName,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize) {
        
        try {
            Map<String, Object> data = dataQueryService.queryTableData(connectionInfo, tableName, page, pageSize);
            return ResponseEntity.ok(data);
        } catch (SQLException e) {
            log.error("查询表数据失败", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 执行自定义SQL查询
     */
    @PostMapping("/execute")
    public ResponseEntity<Map<String, Object>> executeQuery(
            @Valid @RequestBody DatabaseConnectionInfo connectionInfo,
            @RequestParam String sql) {
        
        try {
            Map<String, Object> data = dataQueryService.executeQuery(connectionInfo, sql);
            return ResponseEntity.ok(data);
        } catch (SQLException e) {
            log.error("执行SQL查询失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
} 