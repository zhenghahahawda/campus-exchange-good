package org.example.controller;

import org.example.common.Result;
import org.example.dto.CreateViolationReportRequest;
import org.example.dto.HandleViolationReportRequest;
import org.example.entity.ViolationReport;
import org.example.service.CosService;
import org.example.service.ViolationReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/violations")
public class ViolationReportController {

    @Autowired
    private ViolationReportService violationReportService;

    @Autowired
    private CosService cosService;

    // 上传举报证据图片（最多3张）
    @PostMapping("/upload-evidence")
    public Result<String> uploadEvidence(@RequestParam("file") MultipartFile file,
                                         HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) return Result.error(40101, "Unauthorized");
        if (file.isEmpty()) return Result.error(40001, "文件不能为空");
        try {
            File tempFile = File.createTempFile("evidence-", file.getOriginalFilename());
            file.transferTo(tempFile);
            String key = "violations/" + userId + "-" + System.currentTimeMillis() + "-" + file.getOriginalFilename();
            cosService.uploadFile(tempFile.getAbsolutePath(), key);
            if (tempFile.exists()) tempFile.delete();
            String url = "https://2638224627-1355158021.cos.ap-guangzhou.myqcloud.com/" + key;
            return Result.success("上传成功", url);
        } catch (Exception e) {
            return Result.error(50001, "上传失败: " + e.getMessage());
        }
    }

    // 提交举报
    @PostMapping
    public Result<String> createReport(@RequestBody CreateViolationReportRequest req,
                                       HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) return Result.error(40101, "Unauthorized");
        if (req.getEvidenceImages() != null && req.getEvidenceImages().size() > 3) {
            return Result.error(40001, "证据图片最多3张");
        }
        return violationReportService.createReport(Integer.valueOf(userId), req);
    }

    // 查看我的举报
    @GetMapping("/my")
    public Result<List<ViolationReport>> getMyReports(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit,
            HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) return Result.error(40101, "Unauthorized");
        return violationReportService.getMyReports(Integer.valueOf(userId), page, limit);
    }

    // 查看举报详情
    @GetMapping("/{id}")
    public Result<ViolationReport> getDetail(@PathVariable Integer id,
                                             HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) return Result.error(40101, "Unauthorized");
        return violationReportService.getReportDetail(id);
    }

    // 管理员查看所有举报
    @GetMapping("/admin/list")
    public Result<Map<String, Object>> getAllReports(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String targetType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit,
            HttpServletRequest request) {
        Integer userType = (Integer) request.getAttribute("userType");
        if (userType == null || userType != 1) return Result.error(40301, "需要管理员权限");
        return violationReportService.getAllReports(status, targetType, page, limit);
    }

    // 管理员处理举报
    @PostMapping("/admin/{id}/handle")
    public Result<String> handleReport(@PathVariable Integer id,
                                       @RequestBody HandleViolationReportRequest req,
                                       HttpServletRequest request) {
        Integer userType = (Integer) request.getAttribute("userType");
        String userId = (String) request.getAttribute("userId");
        if (userType == null || userType != 1) return Result.error(40301, "需要管理员权限");
        return violationReportService.handleReport(id, Integer.valueOf(userId), req);
    }
}
