package com.campus.exchange.service;

import com.campus.exchange.dto.PageRequest;
import com.campus.exchange.dto.PageResponse;
import com.campus.exchange.entity.ViolationReport;

import java.util.List;

/**
 * 违规举报服务接口
 */
public interface ViolationReportService {

    /**
     * 创建举报
     */
    ViolationReport createReport(ViolationReport report);

    /**
     * 根据ID获取举报详情
     */
    ViolationReport getReportById(Integer reportId);

    /**
     * 根据举报编号获取举报
     */
    ViolationReport getReportByNumber(String reportNumber);

    /**
     * 分页查询所有举报
     */
    PageResponse<ViolationReport> pageReports(PageRequest pageRequest);

    /**
     * 根据状态分页查询举报
     */
    PageResponse<ViolationReport> pageReportsByStatus(String status, PageRequest pageRequest);

    /**
     * 根据举报人ID分页查询举报
     */
    PageResponse<ViolationReport> pageReportsByReporterId(Integer reporterId, PageRequest pageRequest);

    /**
     * 根据目标类型和ID获取举报列表
     */
    List<ViolationReport> getReportsByTarget(String targetType, Integer targetId);

    /**
     * 根据处理人ID分页查询举报
     */
    PageResponse<ViolationReport> pageReportsByHandlerId(Integer handlerId, PageRequest pageRequest);

    /**
     * 更新举报
     */
    ViolationReport updateReport(Integer reportId, ViolationReport report);

    /**
     * 删除举报（软删除）
     */
    void deleteReport(Integer reportId);

    /**
     * 批量删除举报（软删除）
     */
    void batchDeleteReports(List<Integer> reportIds);

    /**
     * 物理删除举报
     */
    void permanentDeleteReport(Integer reportId);

    /**
     * 根据用户ID删除所有举报（级联删除用户时使用）
     */
    void deleteReportsByUserId(Integer userId);

    /**
     * 处理举报 - 警告
     */
    ViolationReport handleWarning(Integer reportId, Integer handlerId, String handleResult);

    /**
     * 处理举报 - 临时封禁
     */
    ViolationReport handleTempBan(Integer reportId, Integer handlerId, Integer banDuration,
                                   Integer creditDeduction, String handleResult);

    /**
     * 处理举报 - 永久封禁
     */
    ViolationReport handlePermBan(Integer reportId, Integer handlerId, Integer creditDeduction,
                                   String handleResult);

    /**
     * 处理举报 - 移除商品
     */
    ViolationReport handleRemoveProduct(Integer reportId, Integer handlerId, String handleResult);

    /**
     * 驳回举报
     */
    ViolationReport rejectReport(Integer reportId, Integer handlerId, String rejectReason);

    /**
     * 获取待处理举报数量
     */
    Long getPendingReportCount();

    /**
     * 获取处理中举报数量
     */
    Long getProcessingReportCount();
}
