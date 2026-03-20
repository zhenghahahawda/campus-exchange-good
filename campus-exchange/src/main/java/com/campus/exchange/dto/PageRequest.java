package com.campus.exchange.dto;

import lombok.Data;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 分页请求DTO
 */
@Data
public class PageRequest {
    /** 当前页码 */
    private Integer pageNum = 1;

    /** 每页大小 */
    private Integer pageSize = 10;

    /** 排序字段 */
    private String orderBy;

    /** 是否升序 */
    private Boolean asc = false;

    /** 关键词搜索 */
    private String keyword;

    /** 状态筛选（登录日志: 0=失败 1=成功） */
    private Integer status;

    /** 是否活跃（会话管理: 0=失效 1=活跃） */
    private Integer isActive;

    /** 允许排序的字段白名单 */
    private static final Set<String> ALLOWED_ORDER_FIELDS = new HashSet<>(Arrays.asList(
            "id", "created_at", "updated_at", "create_time", "update_time",
            "login_time", "sort_order", "category_id", "price", "status",
            "report_number", "session_id", "expires_at", "order_no"
    ));

    /**
     * 获取安全的排序字段，防止SQL注入
     */
    public String getSafeOrderBy() {
        if (orderBy == null || orderBy.trim().isEmpty()) {
            return null;
        }
        String field = orderBy.trim().toLowerCase();
        if (ALLOWED_ORDER_FIELDS.contains(field)) {
            return field;
        }
        return null;
    }
}
