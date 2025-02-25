package cn.com.nxg.model;

import lombok.Data;

@Data
public class ColumnInfo {
    private String columnName;
    private String dataType;
    private String typeName;
    private int columnSize;
    private boolean nullable;
    private String remarks;
    private boolean isPrimaryKey;
    private boolean isForeignKey;
    private String referencedTable;
    private String referencedColumn;
} 