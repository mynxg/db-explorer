package cn.com.nxg.model;

import lombok.Data;
import java.util.List;

@Data
public class TableInfo {
    private String tableName;
    private String tableType;
    private String remarks;
    private List<ColumnInfo> columns;
} 