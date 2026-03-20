package org.example.service;

import org.example.common.Result;
import org.example.dto.ExchangeDetailResponse;
import org.example.entity.Exchange;

import java.util.List;

public interface ExchangeService {

    Result<List<Exchange>> getUserExchanges(Integer userId, String status, String type);
    
    Result<List<ExchangeDetailResponse>> getUserExchangesWithDetails(Integer userId, String status, String type);

    Result<String> createExchange(Integer requesterId, Integer goodsId, String message, List<String> offerItems);

    Result<String> updateExchangeStatus(String exchangeId, String status, Integer userId);
    
    Result<String> acceptExchange(String exchangeId, Integer userId);
    
    Result<String> rejectExchange(String exchangeId, Integer userId, String reason);

    Result<String> submitExchangeCode(String exchangeId, Integer userId, String exchangeCode);

    Result<Exchange> getExchangeByConversationId(String conversationId);
}
