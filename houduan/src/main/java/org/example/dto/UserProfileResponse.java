package org.example.dto;

import lombok.Data;
import org.example.entity.Good;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserProfileResponse {
    private Integer userId;
    private String username;
    private String avatar;
    private String school;
    private Integer creditScore;
    private Integer userType;
    private LocalDateTime createdAt;
    private Integer exchangeCount;
    private List<Good> goods; // 该用户已上架的商品
}
