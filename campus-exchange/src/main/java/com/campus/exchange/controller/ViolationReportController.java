package com.campus.exchange.controller;

import com.campus.exchange.annotation.RequireAdmin;
import com.campus.exchange.dto.PageRequest;
import com.campus.exchange.dto.PageResponse;
import com.campus.exchange.entity.ViolationReport;
import com.campus.exchange.service.ViolationReportService;
import com.campus.exchange.util.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 违规举报控制器
 */
@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "违规举报管理")
public class ViolationReportController {

    @Autowired
    private ViolationReportService reportService;

    /**
     * 创建举报
     */
    @PostMapping
    @Operation(summary = "创建举报", description = "用户提交违规举报")
    public Result<ViolationReport> createReport(@RequestBody ViolationReport report, HttpServletRequest request) {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("未登录或Token无效");
            }
            report.setReporterId(userId);
            ViolationReport created = reportService.createReport(report);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error("创建举报失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取举报详情
     */
    @GetMapping("/{reportId}")
    @Operation(summary = "获取举报详情", description = "根据举报ID获取详细信息")
    public Result<ViolationReport> getReportById(@PathVariable Integer reportId) {
        try {
            ViolationReport report = reportService.getReportById(reportId);
            if (report == null) {
                return Result.error("举报不存在");
            }
            return Result.success(report);
        } catch (Exception e) {
            return Result.error("获取举报详情失败: " + e.getMessage());
        }
    }

    /**
     * 根据举报编号获取举报
     */
    @GetMapping("/number/{reportNumber}")
    @Operation(summary = "根据编号获取举报", description = "通过举报编号查询举报信息")
    public Result<ViolationReport> getReportByNumber(@PathVariable String reportNumber) {
        try {
            ViolationReport report = reportService.getReportByNumber(reportNumber);
            if (report == null) {
                return Result.error("举报不存在");
            }
            return Result.success(report);
        } catch (Exception e) {
            return Result.error("获取举报失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询所有举报
     */
    @GetMapping("/page")
    @RequireAdmin
    @Operation(summary = "分页查询所有举报", description = "管理员分页查询所有举报")
    public Result<PageResponse<ViolationReport>> pageReports(PageRequest pageRequest) {
        try {
            PageResponse<ViolationReport> page = reportService.pageReports(pageRequest);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error("分页查询举报失败: " + e.getMessage());
        }
    }

    /**
     * 根据状态分页查询举报
     */
    @GetMapping("/status/{status}/page")
    @RequireAdmin
    @Operation(summary = "根据状态分页查询举报", description = "管理员根据状态分页查询举报")
    public Result<PageResponse<ViolationReport>> pageReportsByStatus(
            @PathVariable String status,
            PageRequest pageRequest) {
        try {
            PageResponse<ViolationReport> page = reportService.pageReportsByStatus(status, pageRequest);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error("分页查询举报失败: " + e.getMessage());
        }
    }

    /**
     * 获取我的举报
     */
    @GetMapping("/my/page")
    @Operation(summary = "获取我的举报", description = "用户查询自己提交的举报")
    public Result<PageResponse<ViolationReport>> getMyReports(PageRequest pageRequest, HttpServletRequest request) {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                return Result.error("未登录或Token无效");
            }
            PageResponse<ViolationReport> page = reportService.pageReportsByReporterId(userId, pageRequest);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error("获取我的举报失败: " + e.getMessage());
        }
    }

    /**
     * 根据举报人ID分页查询举报
     */
    @GetMapping("/reporter/{reporterId}/page")
    @RequireAdmin
    @Operation(summary = "根据举报人ID分页查询举报", description = "管理员查询指定用户提交的举报")
    public Result<PageResponse<ViolationReport>> pageReportsByReporterId(
            @PathVariable Integer reporterId,
            PageRequest pageRequest) {
        try {
            PageResponse<ViolationReport> page = reportService.pageReportsByReporterId(reporterId, pageRequest);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error("分页查询举报失败: " + e.getMessage());
        }
    }

    /**
     * 根据目标类型和ID获取举报列表
     */
    @GetMapping("/target/{targetType}/{targetId}")
    @RequireAdmin
    @Operation(summary = "根据目标获取举报列表", description = "管理员查询针对特定目标的所有举报")
    public Result<List<ViolationReport>> getReportsByTarget(
            @PathVariable String targetType,
            @PathVariable Integer targetId) {
        try {
            List<ViolationReport> reports = reportService.getReportsByTarget(targetType, targetId);
            return Result.success(reports);
        } catch (Exception e) {
            return Result.error("获取举报列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据处理人ID分页查询举报
     */
    @GetMapping("/handler/{handlerId}/page")
    @RequireAdmin
    @Operation(summary = "根据处理人ID分页查询举报", description = "管理员查询指定处理人处理的举报")
    public Result<PageResponse<ViolationReport>> pageReportsByHandlerId(
            @PathVariable Integer handlerId,
            PageRequest pageRequest) {
        try {
            PageResponse<ViolationReport> page = reportService.pageReportsByHandlerId(handlerId, pageRequest);
            return Result.success(page);
        } catch (Exception e) {
            return Result.error("分页查询举报失败: " + e.getMessage());
        }
    }

    /**
     * 更新举报
     */
    @PutMapping("/{reportId}")
    @RequireAdmin
    @Operation(summary = "更新举报", description = "管理员更新举报信息")
    public Result<ViolationReport> updateReport(
            @PathVariable Integer reportId,
            @RequestBody ViolationReport report) {
        try {
            ViolationReport updated = reportService.updateReport(reportId, report);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error("更新举报失败: " + e.getMessage());
        }
    }

    /**
     * 删除举报（软删除）
     */
    @DeleteMapping("/{reportId}")
    @RequireAdmin
    @Operation(summary = "删除举报", description = "管理员删除举报（软删除）")
    public Result<Void> deleteReport(@PathVariable Integer reportId) {
        try {
            reportService.deleteReport(reportId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("删除举报失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除举报（软删除）
     */
    @DeleteMapping("/batch")
    @RequireAdmin
    @Operation(summary = "批量删除举报", description = "管理员批量删除举报（软删除）")
    public Result<Void> batchDeleteReports(@RequestBody List<Integer> reportIds) {
        try {
            reportService.batchDeleteReports(reportIds);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("批量删除举报失败: " + e.getMessage());
        }
    }

    /**
     * 物理删除举报
     */
    @DeleteMapping("/{reportId}/permanent")
    @RequireAdmin
    @Operation(summary = "物理删除举报", description = "管理员物理删除举报")
    public Result<Void> permanentDeleteReport(@PathVariable Integer reportId) {
        try {
            reportService.permanentDeleteReport(reportId);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("物理删除举报失败: " + e.getMessage());
        }
    }

    /**
     * 处理举报 - 警告
     */
    @PostMapping("/{reportId}/handle/warning")
    @RequireAdmin
    @Operation(summary = "处理举报-警告", description = "管理员处理举报，给予警告")
    public Result<ViolationReport> handleWarning(
            @PathVariable Integer reportId,
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        try {
            Integer handlerId = (Integer) httpRequest.getAttribute("userId");
            if (handlerId == null) {
                return Result.error("未登录或Token无效");
            }
            String handleResult = request.get("handleResult");
            ViolationReport report = reportService.handleWarning(reportId, handlerId, handleResult);
            return Result.success(report);
        } catch (Exception e) {
            return Result.error("处理举报失败: " + e.getMessage());
        }
    }

    /**
     * 处理举报 - 临时封禁
     */
    @PostMapping("/{reportId}/handle/temp-ban")
    @RequireAdmin
    @Operation(summary = "处理举报-临时封禁", description = "管理员处理举报，临时封禁用户")
    public Result<ViolationReport> handleTempBan(
            @PathVariable Integer reportId,
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        try {
            Integer handlerId = (Integer) httpRequest.getAttribute("userId");
            if (handlerId == null) {
                return Result.error("未登录或Token无效");
            }

            // 安全地转换 banDuration
            Integer banDuration = null;
            Object banDurationObj = request.get("banDuration");
            if (banDurationObj instanceof Number) {
                banDuration = ((Number) banDurationObj).intValue();
            }

            // 安全地转换 creditDeduction
            Integer creditDeduction = null;
            Object creditDeductionObj = request.get("creditDeduction");
            if (creditDeductionObj instanceof Number) {
                creditDeduction = ((Number) creditDeductionObj).intValue();
            }

            String handleResult = (String) request.get("handleResult");

            if (banDuration == null || banDuration <= 0) {
                return Result.error("封禁时长必须大于0");
            }

            ViolationReport report = reportService.handleTempBan(reportId, handlerId, banDuration,
                    creditDeduction, handleResult);
            return Result.success(report);
        } catch (Exception e) {
            return Result.error("处理举报失败: " + e.getMessage());
        }
    }

    /**
     * 处理举报 - 永久封禁
     */
    @PostMapping("/{reportId}/handle/perm-ban")
    @RequireAdmin
    @Operation(summary = "处理举报-永久封禁", description = "管理员处理举报，永久封禁用户")
    public Result<ViolationReport> handlePermBan(
            @PathVariable Integer reportId,
            @RequestBody Map<String, Object> request,
            HttpServletRequest httpRequest) {
        try {
            Integer handlerId = (Integer) httpRequest.getAttribute("userId");
            if (handlerId == null) {
                return Result.error("未登录或Token无效");
            }

            // 安全地转换 creditDeduction
            Integer creditDeduction = null;
            Object creditDeductionObj = request.get("creditDeduction");
            if (creditDeductionObj instanceof Number) {
                creditDeduction = ((Number) creditDeductionObj).intValue();
            }

            String handleResult = (String) request.get("handleResult");

            ViolationReport report = reportService.handlePermBan(reportId, handlerId, creditDeduction, handleResult);
            return Result.success(report);
        } catch (Exception e) {
            return Result.error("处理举报失败: " + e.getMessage());
        }
    }

    /**
     * 处理举报 - 移除商品
     */
    @PostMapping("/{reportId}/handle/remove-product")
    @RequireAdmin
    @Operation(summary = "处理举报-移除商品", description = "管理员处理举报，移除违规商品")
    public Result<ViolationReport> handleRemoveProduct(
            @PathVariable Integer reportId,
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        try {
            Integer handlerId = (Integer) httpRequest.getAttribute("userId");
            if (handlerId == null) {
                return Result.error("未登录或Token无效");
            }
            String handleResult = request.get("handleResult");
            ViolationReport report = reportService.handleRemoveProduct(reportId, handlerId, handleResult);
            return Result.success(report);
        } catch (Exception e) {
            return Result.error("处理举报失败: " + e.getMessage());
        }
    }

    /**
     * 驳回举报
     */
    @PostMapping("/{reportId}/reject")
    @RequireAdmin
    @Operation(summary = "驳回举报", description = "管理员驳回举报")
    public Result<ViolationReport> rejectReport(
            @PathVariable Integer reportId,
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        try {
            Integer handlerId = (Integer) httpRequest.getAttribute("userId");
            if (handlerId == null) {
                return Result.error("未登录或Token无效");
            }
            String rejectReason = request.get("rejectReason");

            if (rejectReason == null || rejectReason.trim().isEmpty()) {
                return Result.error("驳回原因不能为空");
            }

            ViolationReport report = reportService.rejectReport(reportId, handlerId, rejectReason);
            return Result.success(report);
        } catch (Exception e) {
            return Result.error("驳回举报失败: " + e.getMessage());
        }
    }

    /**
     * 获取待处理举报数量
     */
    @GetMapping("/count/pending")
    @RequireAdmin
    @Operation(summary = "获取待处理举报数量", description = "管理员查询待处理举报数量")
    public Result<Long> getPendingReportCount() {
        try {
            Long count = reportService.getPendingReportCount();
            return Result.success(count);
        } catch (Exception e) {
            return Result.error("获取待处理举报数量失败: " + e.getMessage());
        }
    }

    /**
     * 获取处理中举报数量
     */
    @GetMapping("/count/processing")
    @RequireAdmin
    @Operation(summary = "获取处理中举报数量", description = "管理员查询处理中举报数量")
    public Result<Long> getProcessingReportCount() {
        try {
            Long count = reportService.getProcessingReportCount();
            return Result.success(count);
        } catch (Exception e) {
            return Result.error("获取处理中举报数量失败: " + e.getMessage());
        }
    }
}
