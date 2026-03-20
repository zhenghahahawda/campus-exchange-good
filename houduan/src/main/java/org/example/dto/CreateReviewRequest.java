package org.example.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateReviewRequest {
    private Integer orderId;
    private Integer targetUserId;
    private Integer rating;       // 1-5星
    private String content;
    private List<String> images;  // 最多3张图片URL
}
