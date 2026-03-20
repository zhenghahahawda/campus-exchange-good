package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.common.Result;
import org.example.dto.CreateViolationReportRequest;
import org.example.dto.HandleViolationReportRequest;
import org.example.entity.Good;
import org.example.entity.User;
import org.example.entity.ViolationReport;
import org.example.entity.ViolationReportLog;
import org.example.mapper.GoodMapper;
import org.example.mapper.UserMapper;
import org.example.mapper.ViolationReportLogMapper;
import org.example.mapper.ViolationReportMapper;
import org.example.service.NotificationService;
import org.example.service.UserSessionService;
import org.example.service.ViolationReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ViolationReportServiceImpl implements ViolationReportService {

    @Autowired
    private ViolationReportMapper reportMapper;

    @Autowired
    private ViolationReportLogMapper logMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private GoodMapper goodMapper;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private NotificationService notificationService;

    @Override
    public Result<String> createReport(Integer reporterId, CreateViolationReportRequest req) {
        // 生成举报编号
        String reportNumber = "VR-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);

        // 自动填充被举报对象名称
        String targetName = null;
        if ("product".equals(req.getTargetType())) {
            Good good = goodMapper.selectById(req.getTargetId());
            if (good == null) return Result.error(40401, "商品不存在");
            targetName = good.getGoodName();
        } else if ("user".equals(req.getTargetType())) {
            User user = userMapper.selectById(req.getTargetId());
            if (user == null) return Result.error(40401, "用户不存在");
            targetName = user.getUsername();
        } else {
            return Result.error(40001, "举报类型无效");
        }

        ViolationReport report = new ViolationReport();
        report.setReportNumber(reportNumber);
        report.setTargetType(req.getTargetType());
        report.setTargetId(req.getTargetId());
        report.setTargetName(targetName);
        report.setReporterId(reporterId);
        report.setReportType(req.getReportType());
        report.setTitle(req.getTitle());
        report.setDescription(req.getDescription());
        report.setDetailContent(req.getDetailContent());
        report.setEvidenceImages(req.getEvidenceImages());
        report.setStatus("pending");
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());

        reportMapper.insert(report);
        return Result.success("举报提交成功", reportNumber);
    }

    @Override
    public Result<List<ViolationReport>> getMyReports(Integer userId, Integer page, Integer limit) {
        LambdaQueryWrapper<ViolationReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ViolationReport::getReporterId, userId)
               .isNull(ViolationReport::getDeletedAt)
               .orderByDesc(ViolationReport::getCreatedAt);
        Page<ViolationReport> pageParam = new Page<>(page, limit);
        IPage<ViolationReport> result = reportMapper.selectPage(pageParam, wrapper);
        return Result.success(result.getRecords());
    }

    @Override
    public Result<Map<String, Object>> getAllReports(String status, String targetType, Integer page, Integer limit) {
        LambdaQueryWrapper<ViolationReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(ViolationReport::getDeletedAt);
        if (StringUtils.hasText(status)) {
            wrapper.eq(ViolationReport::getStatus, status);
        }
        if (StringUtils.hasText(targetType)) {
            wrapper.eq(ViolationReport::getTargetType, targetType);
        }
        wrapper.orderByDesc(ViolationReport::getCreatedAt);

        Page<ViolationReport> pageParam = new Page<>(page, limit);
        IPage<ViolationReport> result = reportMapper.selectPage(pageParam, wrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", page);
        data.put("limit", limit);
        return Result.success(data);
    }

    @Override
    public Result<ViolationReport> getReportDetail(Integer reportId) {
        ViolationReport report = reportMapper.selectById(reportId);
        if (report == null || report.getDeletedAt() != null) {
            return Result.error(40401, "举报记录不存在");
        }
        return Result.success(report);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> handleReport(Integer reportId, Integer handlerId, HandleViolationReportRequest req) {
        ViolationReport report = reportMapper.selectById(reportId);
        if (report == null) return Result.error(40401, "举报记录不存在");
        if ("approved".equals(report.getStatus()) || "rejected".equals(report.getStatus())) {
            return Result.error(40001, "该举报已处理");
        }

        String oldStatus = report.getStatus();
        String newStatus = "approve".equals(req.getAction()) ? "approved" : "rejected";

        report.setHandlerId(handlerId);
        report.setStatus(newStatus);
        report.setHandleAction(req.getHandleAction());
        report.setHandleResult(req.getHandleResult());
        report.setRejectReason(req.getRejectReason());
        report.setCreditDeduction(req.getCreditDeduction());
        report.setBanDuration(req.getBanDuration());
        report.setHandledAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());

        // 审批通过时执行处罚
        if ("approved".equals(newStatus) && StringUtils.hasText(req.getHandleAction())) {
            applyPunishment(report, req);
        }

        reportMapper.updateById(report);

        // 写操作日志
        ViolationReportLog log = new ViolationReportLog();
        log.setReportId(reportId);
        log.setOperatorId(handlerId);
        log.setActionType(req.getAction());
        log.setActionContent(req.getHandleResult());
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setCreatedAt(LocalDateTime.now());
        logMapper.insert(log);

        // 推送站内通知给被举报对象的用户
        if ("approved".equals(newStatus)) {
            Integer notifyUserId = null;
            if ("user".equals(report.getTargetType())) {
                notifyUserId = report.getTargetId();
            } else if ("product".equals(report.getTargetType())) {
                Good good = goodMapper.selectById(report.getTargetId());
                if (good != null) notifyUserId = good.getUserId();
            }
            if (notifyUserId != null) {
                String actionDesc = getActionDesc(req.getHandleAction(), req.getBanDuration());
                notificationService.createNotification(
                        notifyUserId,
                        "violation",
                        "违规处理通知",
                        "您的" + ("user".equals(report.getTargetType()) ? "账号" : "商品「" + report.getTargetName() + "」")
                                + "因违规举报已被处理：" + actionDesc
                                + (StringUtils.hasText(req.getHandleResult()) ? "。处理说明：" + req.getHandleResult() : ""),
                        String.valueOf(reportId),
                        "violation_report"
                );
            }
        }

        return Result.success("处理成功", null);
    }

    private String getActionDesc(String action, Integer banDays) {
        if (action == null) return "违规警告";
        switch (action) {
            case "removeProduct": return "商品已被下架";
            case "warning": return "信誉分已扣除";
            case "tempBan": return "账号已被限时封禁" + (banDays != null ? "（" + banDays + "天）" : "");
            case "permBan": return "账号已被永久封禁";
            default: return "违规处理";
        }
    }

    private void applyPunishment(ViolationReport report, HandleViolationReportRequest req) {
        User target = userMapper.selectById(report.getTargetId());

        switch (req.getHandleAction()) {
            case "removeProduct":
                // 下架商品
                Good good = goodMapper.selectById(report.getTargetId());
                if (good != null) {
                    good.setShelfStatus(0);
                    goodMapper.updateById(good);
                }
                break;
            case "warning":
                // 扣信誉分
                if (target != null && req.getCreditDeduction() != null) {
                    int newScore = Math.max(0, target.getCreditScore() - req.getCreditDeduction());
                    target.setCreditScore(newScore);
                    userMapper.updateById(target);
                }
                break;
            case "tempBan":
                // 限时封禁
                if (target != null) {
                    target.setIsBanned(1);
                    target.setStatus(0);
                    target.setBanReason("违规举报处罚");
                    if (req.getBanDuration() != null) {
                        target.setBannedUntil(LocalDateTime.now().plusDays(req.getBanDuration()));
                        report.setBanUntil(LocalDateTime.now().plusDays(req.getBanDuration()));
                    }
                    userMapper.updateById(target);
                    userSessionService.invalidateAllUserSessions(target.getUserId());
                }
                break;
            case "permBan":
                // 永久封禁
                if (target != null) {
                    target.setIsBanned(1);
                    target.setStatus(0);
                    target.setBanReason("违规举报永久封禁");
                    target.setBannedUntil(null);
                    userMapper.updateById(target);
                    userSessionService.invalidateAllUserSessions(target.getUserId());
                }
                break;
        }
    }
}
