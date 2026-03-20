package org.example.dto;

import lombok.Data;

import java.util.List;

@Data
public class PublishGoodRequest {
    private String goodName;
    private String condition;       // 兼容旧格式：全新/九成新/八成新/七成及以下
    private Integer conditionLevel; // 前端直接传数字 1/2/3/4
    private String category;        // 兼容旧格式：electronics等
    private Integer categoryId;     // 前端直接传分类ID
    private String location;
    private String school;
    private String paymentMethod;
    private String description;
    private List<String> images;
    private String exchangeFor;
}
