package org.example.service;

import org.example.common.Result;
import org.example.entity.Conversation;
import org.example.entity.Message;

import java.util.List;
import java.util.Map;

public interface ChatService {

    Result<List<Map<String, Object>>> getUserConversations(Integer userId);

    Result<Map<String, Object>> getConversationMessages(String conversationId, Integer page, Integer limit, Integer userId);

    Result<Conversation> createOrGetConversation(Integer userId, Integer participantId, Integer goodsId);

    Result<Conversation> createGroupConversation(Integer initiatorId, Integer receiverId, String orderId, String orderNumber, Integer goodsId);

    Result<Message> sendMessage(String conversationId, Integer senderId, String content, String type);
}
