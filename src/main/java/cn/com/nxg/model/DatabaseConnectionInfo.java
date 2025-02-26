package cn.com.nxg.model;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@Data
public class DatabaseConnectionInfo {
    
    @NotBlank(message = "数据库IP不能为空")
    private String ip;
    
    @NotNull(message = "端口不能为空")
    @Min(value = 1, message = "端口必须大于0")
    @Max(value = 65535, message = "端口必须小于65536")
    private Integer port;
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    @NotBlank(message = "数据库名不能为空")
    private String dbName;
    
    @NotBlank(message = "数据库类型不能为空")
    private String dbType; // 使用 DatabaseType 中的值
    
    // 扩展数据库类型枚举
    public enum DatabaseType {
        MYSQL,
        ORACLE,
        POSTGRESQL,
        SQLSERVER,
        DB2,
        SQLITE,
        MARIADB,
        H2,
        HIVE,
        CLICKHOUSE
    }
    
    // 修改 getJdbcUrl 方法支持更多数据库
    public String getJdbcUrl() {
        switch (DatabaseType.valueOf(dbType.toUpperCase())) {
            case MYSQL:
                return String.format("jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC", 
                        ip, port, dbName);
            case ORACLE:
                return String.format("jdbc:oracle:thin:@%s:%d:%s", ip, port, dbName);
            case POSTGRESQL:
                return String.format("jdbc:postgresql://%s:%d/%s", ip, port, dbName);
            case SQLSERVER:
                return String.format("jdbc:sqlserver://%s:%d;databaseName=%s", ip, port, dbName);
            case DB2:
                return String.format("jdbc:db2://%s:%d/%s", ip, port, dbName);
            case SQLITE:
                return String.format("jdbc:sqlite:%s", dbName);
            case MARIADB:
                return String.format("jdbc:mariadb://%s:%d/%s", ip, port, dbName);
            case H2:
                return String.format("jdbc:h2:tcp://%s:%d/%s", ip, port, dbName);
            case HIVE:
                return String.format("jdbc:hive2://%s:%d/%s", ip, port, dbName);
            case CLICKHOUSE:
                return String.format("jdbc:clickhouse://%s:%d/%s", ip, port, dbName);
            default:
                throw new IllegalArgumentException("不支持的数据库类型: " + dbType);
        }
    }
} 