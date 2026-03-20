package org.example.controller;

import org.example.common.Result;
import org.example.dto.CreateReviewRequest;
import org.example.entity.Exchange;
import org.example.mapper.ExchangeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ExchangeMapper exchangeMapper;

    @PostMapping
    public Result<String> submitReview(@RequestBody CreateReviewRequest req,
                                       HttpServletRequest request) {
        String userIdStr = (String) request.getAttribute("userId");
        if (userIdStr == null) return Result.error(40101, "Unauthorized");
        Integer userId = Integer.valueOf(userIdStr);

        // 校验
        if (req.getOrderId() == null || req.getContent() == null || req.getRating() == null) {
            return Result.error(40001, "参数不完整");
        }
        if (req.getRating() < 1 || req.getRating() > 5) {
            return Result.error(40001, "评分须在1-5之间");
        }
        if (req.getImages() != null && req.getImages().size() > 3) {
            return Result.error(40001, "图片最多3张");
        }

        Exchange exchange = exchangeMapper.selectById(req.getOrderId());
        if (exchange == null) return Result.error(40401, "订单不存在");
        if (!"completed".equals(exchange.getStatus())) {
            return Result.error(40001, "只有已完成的订单才能评价");
        }

        String images = req.getImages() != null ? String.join(",", req.getImages()) : null;
        // rating暂存在content前缀，格式：[rating]content
        String reviewContent = "[" + req.getRating() + "]" + req.getContent();

        boolean isInitiator = exchange.getInitiatorId().equals(userId);
        boolean isReceiver = exchange.getReceiverId().equals(userId);

        if (!isInitiator && !isReceiver) {
            return Result.error(40301, "无权评价此订单");
        }

        if (isInitiator) {
            if (exchange.getInitiatorReviewedAt() != null) {
                return Result.error(40001, "已评价过该订单");
            }
            exchange.setInitiatorReview(reviewContent);
            exchange.setInitiatorReviewImages(images);
            exchange.setInitiatorReviewedAt(LocalDateTime.now());
        } else {
            if (exchange.getReceiverReviewedAt() != null) {
                return Result.error(40001, "已评价过该订单");
            }
            exchange.setReceiverReview(reviewContent);
            exchange.setReceiverReviewImages(images);
            exchange.setReceiverReviewedAt(LocalDateTime.now());
        }

        exchangeMapper.updateById(exchange);
        return Result.success("评价成功", null);
    }
}
