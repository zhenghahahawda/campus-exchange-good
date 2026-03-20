package org.example.controller;

import org.example.common.Result;
import org.example.dto.CreateConversationRequest;
import org.example.dto.SendMessageRequest;
import org.example.entity.Conversation;
import org.example.entity.Message;
import org.example.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/conversations")
    public Result<List<Map<String, Object>>> getConversations(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(40101, "Unauthorized");
        }
        return chatService.getUserConversations(Integer.valueOf(userId));
    }

    @GetMapping("/conversations/{id}/messages")
    public Result<Map<String, Object>> getMessages(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit,
            HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(40101, "Unauthorized");
        }
        return chatService.getConversationMessages(id, page, limit, Integer.valueOf(userId));
    }

    @PostMapping("/conversations")
    public Result<Conversation> createConversation(@RequestBody CreateConversationRequest req, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(40101, "Unauthorized");
        }
        return chatService.createOrGetConversation(Integer.valueOf(userId), req.getParticipantId(), req.getGoodsId());
    }

    @PostMapping("/messages")
    public Result<Message> sendMessage(@RequestBody SendMessageRequest req, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(40101, "Unauthorized");
        }
        return chatService.sendMessage(req.getConversationId(), Integer.valueOf(userId), req.getContent(), req.getType());
    }
}
