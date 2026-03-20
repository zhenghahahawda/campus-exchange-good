package org.example.controller;

import org.example.common.Result;
import org.example.dto.CreateExchangeRequest;
import org.example.dto.UpdateStatusRequest;
import org.example.entity.Exchange;
import org.example.service.ExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/exchanges")
public class ExchangeController {

    @Autowired
    private ExchangeService exchangeService;

    @GetMapping
    public Result<List<Exchange>> getExchanges(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(40101, "Unauthorized");
        }
        return exchangeService.getUserExchanges(Integer.valueOf(userId), status, type);
    }

    @PostMapping
    public Result<String> createExchange(@RequestBody CreateExchangeRequest req, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(40101, "Unauthorized");
        }
        return exchangeService.createExchange(Integer.valueOf(userId), req.getGoodsId(), req.getMessage(), req.getOfferItems());
    }

    @PutMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable String id, @RequestBody UpdateStatusRequest req, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(40101, "Unauthorized");
        }
        return exchangeService.updateExchangeStatus(id, req.getStatus(), Integer.valueOf(userId));
    }

    @GetMapping("/conversation/{conversationId}")
    public Result<Exchange> getExchangeByConversation(@PathVariable String conversationId, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(40101, "Unauthorized");
        }
        return exchangeService.getExchangeByConversationId(conversationId);
    }

    // 提交凭证码确认收货
    @PostMapping("/{id}/confirm")
    public Result<String> confirmExchange(@PathVariable String id,
                                          @RequestParam String exchangeCode,
                                          HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(40101, "Unauthorized");
        }
        return exchangeService.submitExchangeCode(id, Integer.valueOf(userId), exchangeCode);
    }
}
