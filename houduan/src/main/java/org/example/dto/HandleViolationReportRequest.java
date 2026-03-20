package org.example.dto;

import lombok.Data;

@Data
public class HandleViolationReportRequest {
    private String action;          // approve / reject
    private String handleAction;    // warning/tempBan/permBan/removeProduct
    private String handleResult;
    private String rejectReason;
    private Integer creditDeduction;
    private Integer banDuration;    // 天数，permBan时为null
}
