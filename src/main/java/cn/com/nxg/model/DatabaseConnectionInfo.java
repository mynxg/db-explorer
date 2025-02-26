package cn.com.nxg.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


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
    private String dbType; // MYSQL 或 ORACLE
    
    // 生成连接URL
    public String getJdbcUrl() {
        if ("MYSQL".equalsIgnoreCase(dbType)) {
            return String.format("jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC", 
                    ip, port, dbName);
        } else if ("ORACLE".equalsIgnoreCase(dbType)) {
            return String.format("jdbc:oracle:thin:@%s:%d:%s", ip, port, dbName);
        }
        throw new IllegalArgumentException("不支持的数据库类型: " + dbType);
    }
} 