package org.example.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateViolationReportRequest {
    private String targetType;      // product / user
    private Integer targetId;
    private String reportType;
    private String title;
    private String description;
    private String detailContent;
    private List<String> evidenceImages;
}
