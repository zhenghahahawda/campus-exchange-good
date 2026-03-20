package com.campus.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.exchange.dto.PageRequest;
import com.campus.exchange.dto.PageResponse;
import com.campus.exchange.entity.Goods;
import com.campus.exchange.entity.User;
import com.campus.exchange.entity.ViolationReport;
import com.campus.exchange.entity.ViolationReportLog;
import com.campus.exchange.mapper.GoodsMapper;
import com.campus.exchange.mapper.UserMapper;
import com.campus.exchange.mapper.ViolationReportLogMapper;
import com.campus.exchange.mapper.ViolationReportMapper;
import com.campus.exchange.service.ViolationReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

/**
 * 违规举报服务实现类
 */
@Service
public class ViolationReportServiceImpl implements ViolationReportService {

    @Autowired
    private ViolationReportMapper reportMapper;

    @Autowired
    private ViolationReportLogMapper reportLogMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    public ViolationReport createReport(ViolationReport report) {
        if (report.getReportNumber() == null || report.getReportNumber().isEmpty()) {
            report.setReportNumber(generateReportNumber());
        }
        if (report.getStatus() == null || report.getStatus().isEmpty()) {
            report.setStatus("pending");
        }
        if (report.getCreatedAt() == null) {
            report.setCreatedAt(LocalDateTime.now());
        }
        report.setUpdatedAt(LocalDateTime.now());
        reportMapper.insert(report);
        return report;
    }

    @Override
    public ViolationReport getReportById(Integer reportId) {
        return reportMapper.selectById(reportId);
    }

    @Override
    public ViolationReport getReportByNumber(String reportNumber) {
        QueryWrapper<ViolationReport> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("report_number", reportNumber);
        return reportMapper.selectOne(queryWrapper);
    }

    @Override
    public PageResponse<ViolationReport> pageReports(PageRequest pageRequest) {
        Page<ViolationReport> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        QueryWrapper<ViolationReport> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNull("deleted_at");

        String safeOrderBy = pageRequest.getSafeOrderBy();
        if (safeOrderBy != null) {
            queryWrapper.orderBy(true, pageRequest.getAsc(), safeOrderBy);
        } else {
            queryWrapper.orderByDesc("created_at");
        }

        IPage<ViolationReport> result = reportMapper.selectPage(page, queryWrapper);
        fillReporterNames(result.getRecords());
        return new PageResponse<>(
                result.getCurrent(),
                result.getSize(),
                result.getTotal(),
                result.getPages(),
                result.getRecords()
        );
    }

    @Override
    public PageResponse<ViolationReport> pageReportsByStatus(String status, PageRequest pageRequest) {
        Page<ViolationReport> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        QueryWrapper<ViolationReport> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", status);
        queryWrapper.isNull("deleted_at");

        String safeOrderBy = pageRequest.getSafeOrderBy();
        if (safeOrderBy != null) {
            queryWrapper.orderBy(true, pageRequest.getAsc(), safeOrderBy);
        } else {
            queryWrapper.orderByDesc("created_at");
        }

        IPage<ViolationReport> result = reportMapper.selectPage(page, queryWrapper);
        fillReporterNames(result.getRecords());
        return new PageResponse<>(
                result.getCurrent(),
                result.getSize(),
                result.getTotal(),
                result.getPages(),
                result.getRecords()
        );
    }

    @Override
    public PageResponse<ViolationReport> pageReportsByReporterId(Integer reporterId, PageRequest pageRequest) {
        Page<ViolationReport> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        QueryWrapper<ViolationReport> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("reporter_id", reporterId);
        queryWrapper.isNull("deleted_at");

        String safeOrderBy = pageRequest.getSafeOrderBy();
        if (safeOrderBy != null) {
            queryWrapper.orderBy(true, pageRequest.getAsc(), safeOrderBy);
        } else {
            queryWrapper.orderByDesc("created_at");
        }

        IPage<ViolationReport> result = reportMapper.selectPage(page, queryWrapper);
        fillReporterNames(result.getRecords());
        return new PageResponse<>(
                result.getCurrent(),
                result.getSize(),
                result.getTotal(),
                result.getPages(),
                result.getRecords()
        );
    }

    @Override
    public List<ViolationReport> getReportsByTarget(String targetType, Integer targetId) {
        QueryWrapper<ViolationReport> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("target_type", targetType);
        queryWrapper.eq("target_id", targetId);
        queryWrapper.isNull("deleted_at");
        queryWrapper.orderByDesc("created_at");
        return reportMapper.selectList(queryWrapper);
    }

    @Override
    public PageResponse<ViolationReport> pageReportsByHandlerId(Integer handlerId, PageRequest pageRequest) {
        Page<ViolationReport> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        QueryWrapper<ViolationReport> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("handler_id", handlerId);
        queryWrapper.isNull("deleted_at");

        String safeOrderBy = pageRequest.getSafeOrderBy();
        if (safeOrderBy != null) {
            queryWrapper.orderBy(true, pageRequest.getAsc(), safeOrderBy);
        } else {
            queryWrapper.orderByDesc("handled_at");
        }

        IPage<ViolationReport> result = reportMapper.selectPage(page, queryWrapper);
        fillReporterNames(result.getRecords());
        return new PageResponse<>(
                result.getCurrent(),
                result.getSize(),
                result.getTotal(),
                result.getPages(),
                result.getRecords()
        );
    }

    @Override
    public ViolationReport updateReport(Integer reportId, ViolationReport report) {
        report.setReportId(reportId);
        report.setUpdatedAt(LocalDateTime.now());
        reportMapper.updateById(report);
        return reportMapper.selectById(reportId);
    }

    @Override
    public void deleteReport(Integer reportId) {
        ViolationReport report = new ViolationReport();
        report.setReportId(reportId);
        report.setDeletedAt(LocalDateTime.now());
        reportMapper.updateById(report);
    }

    @Override
    public void batchDeleteReports(List<Integer> reportIds) {
        if (reportIds != null && !reportIds.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            for (Integer reportId : reportIds) {
                ViolationReport report = new ViolationReport();
                report.setReportId(reportId);
                report.setDeletedAt(now);
                reportMapper.updateById(report);
            }
        }
    }

    @Override
    public void permanentDeleteReport(Integer reportId) {
        reportMapper.deleteById(reportId);
    }

    @Override
    @Transactional
    public void deleteReportsByUserId(Integer userId) {
        LambdaQueryWrapper<ViolationReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ViolationReport::getReporterId, userId);
        reportMapper.delete(wrapper);
    }

    @Override
    @Transactional
    public ViolationReport handleWarning(Integer reportId, Integer handlerId, String handleResult) {
        ViolationReport existing = reportMapper.selectById(reportId);
        String oldStatus = existing != null ? existing.getStatus() : null;

        // 扣除被举报用户的信誉分（默认5分）
        if (existing != null && existing.getTargetType() != null && existing.getTargetType().equals("user")) {
            Integer targetUserId = existing.getTargetId();
            User user = userMapper.selectById(targetUserId);
            if (user != null) {
                int newScore = Math.max(1, user.getCreditScore() - 5);
                user.setCreditScore(newScore);
                user.setUpdatedAt(LocalDateTime.now());
                userMapper.updateById(user);
            }
        }

        ViolationReport report = new ViolationReport();
        report.setReportId(reportId);
        report.setStatus("approved");
        report.setHandlerId(handlerId);
        report.setHandleAction("warning");
        report.setHandleResult(handleResult);
        report.setHandledAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        reportMapper.updateById(report);

        insertLog(reportId, handlerId, "approve", "警告处理：" + (handleResult != null ? handleResult : ""), oldStatus, "approved");

        return reportMapper.selectById(reportId);
    }

    @Override
    @Transactional
    public ViolationReport handleTempBan(Integer reportId, Integer handlerId, Integer banDuration,
                                         Integer creditDeduction, String handleResult) {
        ViolationReport existing = reportMapper.selectById(reportId);
        String oldStatus = existing != null ? existing.getStatus() : null;

        // 临时封禁用户 + 扣信誉分
        if (existing != null && existing.getTargetType() != null && existing.getTargetType().equals("user")) {
            Integer targetUserId = existing.getTargetId();
            User user = userMapper.selectById(targetUserId);
            if (user != null) {
                user.setIsBanned(1);
                user.setBannedUntil(LocalDateTime.now().plusDays(banDuration));
                user.setBanReason("违规举报临时封禁：" + (handleResult != null ? handleResult : ""));
                if (creditDeduction != null && creditDeduction > 0) {
                    int newScore = Math.max(1, user.getCreditScore() - creditDeduction);
                    user.setCreditScore(newScore);
                }
                user.setUpdatedAt(LocalDateTime.now());
                userMapper.updateById(user);
            }
        }

        ViolationReport report = new ViolationReport();
        report.setReportId(reportId);
        report.setStatus("approved");
        report.setHandlerId(handlerId);
        report.setHandleAction("tempBan");
        report.setHandleResult(handleResult);
        report.setBanDuration(banDuration);
        report.setBanUntil(LocalDateTime.now().plusDays(banDuration));
        report.setCreditDeduction(creditDeduction);
        report.setHandledAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        reportMapper.updateById(report);

        String logContent = String.format("临时封禁%d天，扣除信誉分%d分：%s", banDuration, creditDeduction, handleResult != null ? handleResult : "");
        insertLog(reportId, handlerId, "approve", logContent, oldStatus, "approved");

        return reportMapper.selectById(reportId);
    }

    @Override
    @Transactional
    public ViolationReport handlePermBan(Integer reportId, Integer handlerId, Integer creditDeduction,
                                         String handleResult) {
        ViolationReport existing = reportMapper.selectById(reportId);
        String oldStatus = existing != null ? existing.getStatus() : null;

        // 永久封禁用户 + 扣信誉分
        if (existing != null && existing.getTargetType() != null && existing.getTargetType().equals("user")) {
            Integer targetUserId = existing.getTargetId();
            User user = userMapper.selectById(targetUserId);
            if (user != null) {
                user.setIsBanned(1);
                user.setBannedUntil(LocalDateTime.of(2099, 12, 31, 23, 59, 59)); // 永久封禁设为2099年
                user.setBanReason("违规举报永久封禁：" + (handleResult != null ? handleResult : ""));
                if (creditDeduction != null && creditDeduction > 0) {
                    int newScore = Math.max(1, user.getCreditScore() - creditDeduction);
                    user.setCreditScore(newScore);
                }
                user.setUpdatedAt(LocalDateTime.now());
                userMapper.updateById(user);
            }
        }

        ViolationReport report = new ViolationReport();
        report.setReportId(reportId);
        report.setStatus("approved");
        report.setHandlerId(handlerId);
        report.setHandleAction("permBan");
        report.setHandleResult(handleResult);
        report.setCreditDeduction(creditDeduction);
        report.setHandledAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        reportMapper.updateById(report);

        String logContent = String.format("永久封禁，扣除信誉分%d分：%s", creditDeduction, handleResult != null ? handleResult : "");
        insertLog(reportId, handlerId, "approve", logContent, oldStatus, "approved");

        return reportMapper.selectById(reportId);
    }

    @Override
    @Transactional
    public ViolationReport handleRemoveProduct(Integer reportId, Integer handlerId, String handleResult) {
        ViolationReport existing = reportMapper.selectById(reportId);
        String oldStatus = existing != null ? existing.getStatus() : null;

        // 下架商品
        if (existing != null && existing.getTargetType() != null && existing.getTargetType().equals("product")) {
            Integer targetGoodId = existing.getTargetId();
            Goods goods = goodsMapper.selectById(targetGoodId);
            if (goods != null) {
                goods.setShelfStatus(0); // 0=已下架
                goods.setUpdateTime(LocalDateTime.now());
                goodsMapper.updateById(goods);
            }
        }

        ViolationReport report = new ViolationReport();
        report.setReportId(reportId);
        report.setStatus("approved");
        report.setHandlerId(handlerId);
        report.setHandleAction("removeProduct");
        report.setHandleResult(handleResult);
        report.setHandledAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        reportMapper.updateById(report);

        insertLog(reportId, handlerId, "approve", "下架商品：" + (handleResult != null ? handleResult : ""), oldStatus, "approved");

        return reportMapper.selectById(reportId);
    }

    @Override
    public ViolationReport rejectReport(Integer reportId, Integer handlerId, String rejectReason) {
        ViolationReport existing = reportMapper.selectById(reportId);
        String oldStatus = existing != null ? existing.getStatus() : null;

        ViolationReport report = new ViolationReport();
        report.setReportId(reportId);
        report.setStatus("rejected");
        report.setHandlerId(handlerId);
        report.setRejectReason(rejectReason);
        report.setHandledAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        reportMapper.updateById(report);

        insertLog(reportId, handlerId, "reject", "驳回原因：" + rejectReason, oldStatus, "rejected");

        return reportMapper.selectById(reportId);
    }

    @Override
    public Long getPendingReportCount() {
        QueryWrapper<ViolationReport> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "pending");
        queryWrapper.isNull("deleted_at");
        Long count = reportMapper.selectCount(queryWrapper);
        return count != null ? count.longValue() : 0L;
    }

    @Override
    public Long getProcessingReportCount() {
        QueryWrapper<ViolationReport> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "processing");
        queryWrapper.isNull("deleted_at");
        Long count = reportMapper.selectCount(queryWrapper);
        return count != null ? count.longValue() : 0L;
    }

    /**
     * 填充举报人用户名
     */
    private void fillReporterNames(List<ViolationReport> reports) {
        if (reports == null || reports.isEmpty()) return;
        for (ViolationReport report : reports) {
            if (report.getReporterId() != null) {
                User user = userMapper.selectById(report.getReporterId());
                if (user != null) {
                    report.setReporterName(user.getUsername());
                } else {
                    report.setReporterName("用户" + report.getReporterId());
                }
            }
        }
    }

    /**
     * 生成举报编号
     * 格式: VR-{时间戳}-{随机码}
     */
    private String generateReportNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomCode = String.format("%04d", new Random().nextInt(10000));
        return "VR-" + timestamp + "-" + randomCode;
    }

    /**
     * 写入处理日志
     */
    private void insertLog(Integer reportId, Integer operatorId, String actionType,
                           String actionContent, String oldStatus, String newStatus) {
        ViolationReportLog log = new ViolationReportLog();
        log.setReportId(reportId);
        log.setOperatorId(operatorId);
        log.setActionType(actionType);
        log.setActionContent(actionContent);
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setCreatedAt(LocalDateTime.now());
        reportLogMapper.insert(log);
    }
}
