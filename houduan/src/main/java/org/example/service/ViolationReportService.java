package org.example.service;

import org.example.common.Result;
import org.example.dto.CreateViolationReportRequest;
import org.example.dto.HandleViolationReportRequest;
import org.example.entity.ViolationReport;

import java.util.List;
import java.util.Map;

public interface ViolationReportService {

    // 用户提交举报
    Result<String> createReport(Integer reporterId, CreateViolationReportRequest req);

    // 查询我的举报列表
    Result<List<ViolationReport>> getMyReports(Integer userId, Integer page, Integer limit);

    // 管理员查询所有举报（可按状态筛选）
    Result<Map<String, Object>> getAllReports(String status, String targetType, Integer page, Integer limit);

    // 查询举报详情
    Result<ViolationReport> getReportDetail(Integer reportId);

    // 管理员处理举报（审批/驳回）
    Result<String> handleReport(Integer reportId, Integer handlerId, HandleViolationReportRequest req);
}
