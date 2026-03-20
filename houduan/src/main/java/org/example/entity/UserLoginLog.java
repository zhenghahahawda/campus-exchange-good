package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("user_login_logs")
public class UserLoginLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    private Integer userId;

    private LocalDateTime loginTime;

    private String loginIp;

    private Integer loginStatus; // 0=失败, 1=成功

    private String failReason;

    private String userAgent;

    private String deviceType; // PC/Mobile/Tablet

    private String browser;

    private String os;
}
