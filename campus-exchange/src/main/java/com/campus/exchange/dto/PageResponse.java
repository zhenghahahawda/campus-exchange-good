package com.campus.exchange.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    /** 当前页码 */
    private Long current;

    /** 每页大小 */
    private Long size;

    /** 总记录数 */
    private Long total;

    /** 总页数 */
    private Long pages;

    /** 数据列表 */
    private List<T> records;
}
