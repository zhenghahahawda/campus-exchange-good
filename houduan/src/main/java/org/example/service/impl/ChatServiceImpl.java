package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.common.Result;
import org.example.entity.Conversation;
import org.example.entity.ConversationParticipant;
import org.example.entity.Message;
import org.example.entity.User;
import org.example.entity.Good;
import org.example.mapper.ConversationMapper;
import org.example.mapper.ConversationParticipantMapper;
import org.example.mapper.GoodMapper;
import org.example.mapper.MessageMapper;
import org.example.mapper.UserMapper;
import org.example.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ConversationMapper conversationMapper;

    @Autowired
    private ConversationParticipantMapper participantMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private GoodMapper goodMapper;

    @Override
    public Result<List<Map<String, Object>>> getUserConversations(Integer userId) {
        try {
            System.out.println("=== 获取用户会话列表 userId=" + userId + " ===");
            
            LambdaQueryWrapper<ConversationParticipant> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ConversationParticipant::getUserId, userId);
            List<ConversationParticipant> participants = participantMapper.selectList(wrapper);
            
            System.out.println("查询到 " + participants.size() + " 个会话参与记录");

            List<Map<String, Object>> result = new ArrayList<>();
            for (ConversationParticipant participant : participants) {
                Conversation conversation = conversationMapper.selectById(participant.getConversationId());
                if (conversation != null) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", conversation.getId());
                    item.put("lastTime", conversation.getLastMessageTime());
                    item.put("unread", participant.getUnreadCount());
                    
                    // 动态获取对方用户信息
                    LambdaQueryWrapper<ConversationParticipant> otherWrapper = new LambdaQueryWrapper<>();
                    otherWrapper.eq(ConversationParticipant::getConversationId, conversation.getId())
                                .ne(ConversationParticipant::getUserId, userId);
                    List<ConversationParticipant> otherParticipants = participantMapper.selectList(otherWrapper);

                    if ("group".equals(conversation.getType())) {
                        // 群聊：用商品名动态生成名称
                        String displayName = conversation.getName();
                        if (conversation.getGoodsId() != null) {
                            Good good = goodMapper.selectById(conversation.getGoodsId());
                            if (good != null) {
                                displayName = good.getGoodName() + " 交换群";
                            }
                        }
                        item.put("name", displayName);
                        item.put("avatar", conversation.getAvatar());
                        item.put("isGroup", true);
                        item.put("goodsId", conversation.getGoodsId());
                        List<String> memberNames = new ArrayList<>();
                        for (ConversationParticipant op : otherParticipants) {
                            User u = userMapper.selectById(op.getUserId());
                            if (u != null) memberNames.add(u.getUsername());
                        }
                        item.put("members", memberNames);
                    } else {
                        // 私聊：显示对方信息
                        item.put("isGroup", false);
                        if (!otherParticipants.isEmpty()) {
                            User otherUser = userMapper.selectById(otherParticipants.get(0).getUserId());
                            if (otherUser != null) {
                                item.put("name", otherUser.getUsername());
                                item.put("avatar", otherUser.getAvatar());
                                item.put("otherUserId", otherUser.getUserId());
                            }
                        } else {
                            item.put("name", conversation.getName());
                            item.put("avatar", conversation.getAvatar());
                        }
                    }
                    
                    // Get last message
                    if (conversation.getLastMessageId() != null) {
                        Message lastMsg = messageMapper.selectById(conversation.getLastMessageId());
                        if (lastMsg != null) {
                            String content = lastMsg.getContent();
                            // 群聊显示发送者名字前缀
                            if ("group".equals(conversation.getType())) {
                                User sender = userMapper.selectById(lastMsg.getSenderId());
                                if (sender != null) {
                                    content = sender.getUsername() + ": " + content;
                                }
                            }
                            item.put("lastMessage", content);
                        }
                    }
                    
                    result.add(item);
                }
            }

            result.sort((a, b) -> {
                LocalDateTime timeA = (LocalDateTime) a.get("lastTime");
                LocalDateTime timeB = (LocalDateTime) b.get("lastTime");
                if (timeA == null) return 1;
                if (timeB == null) return -1;
                return timeB.compareTo(timeA);
            });
            
            System.out.println("返回 " + result.size() + " 个会话");
            return Result.success(result);
            
        } catch (Exception e) {
            System.err.println("获取会话列表失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error(50001, "获取会话列表失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Map<String, Object>> getConversationMessages(String conversationId, Integer page, Integer limit, Integer userId) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getConversationId, conversationId)
                .orderByDesc(Message::getCreatedAt);

        Page<Message> pageParam = new Page<>(page, limit);
        IPage<Message> resultPage = messageMapper.selectPage(pageParam, wrapper);

        List<Message> messages = resultPage.getRecords();
        for (Message message : messages) {
            User sender = userMapper.selectById(message.getSenderId());
            if (sender != null) {
                sender.setPasswordHash(null);
                message.setSender(sender);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("messages", messages);
        result.put("hasMore", resultPage.getCurrent() < resultPage.getPages());

        return Result.success(result);
    }

    @Override
    public Result<Conversation> createGroupConversation(Integer initiatorId, Integer receiverId, String orderId, String orderNumber, Integer goodsId) {
        try {
            // 先查是否已有关于这个商品的群聊
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Conversation> convWrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            convWrapper.eq("type", "group").eq("goods_id", goodsId);
            Conversation existing = conversationMapper.selectOne(convWrapper);

            if (existing != null) {
                // 已有群聊，把新申请人加进去（如果不在里面）
                LambdaQueryWrapper<ConversationParticipant> checkWrapper = new LambdaQueryWrapper<>();
                checkWrapper.eq(ConversationParticipant::getConversationId, existing.getId())
                        .eq(ConversationParticipant::getUserId, initiatorId);
                if (participantMapper.selectCount(checkWrapper) == 0) {
                    ConversationParticipant p = new ConversationParticipant();
                    p.setConversationId(existing.getId());
                    p.setUserId(initiatorId);
                    p.setUnreadCount(0);
                    p.setJoinedAt(LocalDateTime.now());
                    participantMapper.insert(p);
                }
                return Result.success(existing);
            }

            // 不存在则新建群聊
            User initiator = userMapper.selectById(initiatorId);
            User receiver = userMapper.selectById(receiverId);
            
            // 用商品名称作为群聊名，更直观
            Good good = goodMapper.selectById(goodsId);
            String groupName = (good != null ? good.getGoodName() : "商品") + " 交换群";

            Conversation conversation = new Conversation();
            conversation.setType("group");
            conversation.setName(groupName);
            conversation.setGoodsId(goodsId);
            conversationMapper.insert(conversation);

            // 添加发起方
            ConversationParticipant p1 = new ConversationParticipant();
            p1.setConversationId(conversation.getId());
            p1.setUserId(initiatorId);
            p1.setUnreadCount(0);
            p1.setJoinedAt(LocalDateTime.now());
            participantMapper.insert(p1);

            // 添加接收方（商品卖家）
            ConversationParticipant p2 = new ConversationParticipant();
            p2.setConversationId(conversation.getId());
            p2.setUserId(receiverId);
            p2.setUnreadCount(0);
            p2.setJoinedAt(LocalDateTime.now());
            participantMapper.insert(p2);

            // 发一条系统消息
            Message sysMsg = new Message();
            sysMsg.setConversationId(conversation.getId());
            sysMsg.setSenderId(initiatorId);
            sysMsg.setContent("交换订单 " + orderNumber + " 已创建，请在此群聊中协商交换事宜。");
            sysMsg.setType("text");
            messageMapper.insert(sysMsg);

            conversation.setLastMessageId(sysMsg.getId());
            conversation.setLastMessageTime(sysMsg.getCreatedAt());
            conversationMapper.updateById(conversation);

            return Result.success(conversation);
        } catch (Exception e) {
            System.err.println("创建群聊失败: " + e.getMessage());
            e.printStackTrace();
            return Result.error(50001, "创建群聊失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Conversation> createOrGetConversation(Integer userId, Integer participantId, Integer goodsId) {
        // Check if conversation already exists between these two users
        LambdaQueryWrapper<ConversationParticipant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConversationParticipant::getUserId, userId);
        List<ConversationParticipant> userParticipants = participantMapper.selectList(wrapper);

        for (ConversationParticipant up : userParticipants) {
            LambdaQueryWrapper<ConversationParticipant> checkWrapper = new LambdaQueryWrapper<>();
            checkWrapper.eq(ConversationParticipant::getConversationId, up.getConversationId())
                    .eq(ConversationParticipant::getUserId, participantId);
            if (participantMapper.selectCount(checkWrapper) > 0) {
                Conversation existing = conversationMapper.selectById(up.getConversationId());
                return Result.success(existing);
            }
        }

        // Create new conversation
        Conversation conversation = new Conversation();
        conversation.setType("private");
        
        User otherUser = userMapper.selectById(participantId);
        if (otherUser != null) {
            conversation.setName(otherUser.getUsername());
            conversation.setAvatar(otherUser.getAvatar());
        }
        
        conversationMapper.insert(conversation);

        // Add participants
        ConversationParticipant p1 = new ConversationParticipant();
        p1.setConversationId(conversation.getId());
        p1.setUserId(userId);
        p1.setUnreadCount(0);
        p1.setJoinedAt(LocalDateTime.now());
        participantMapper.insert(p1);

        ConversationParticipant p2 = new ConversationParticipant();
        p2.setConversationId(conversation.getId());
        p2.setUserId(participantId);
        p2.setUnreadCount(0);
        p2.setJoinedAt(LocalDateTime.now());
        participantMapper.insert(p2);

        return Result.success(conversation);
    }

    @Override
    public Result<Message> sendMessage(String conversationId, Integer senderId, String content, String type) {
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setContent(content);
        message.setType(type != null ? type : "text");
        messageMapper.insert(message);

        // Update conversation
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation != null) {
            conversation.setLastMessageId(message.getId());
            conversation.setLastMessageTime(message.getCreatedAt());
            conversationMapper.updateById(conversation);
        }

        // Update unread count for other participants
        LambdaQueryWrapper<ConversationParticipant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConversationParticipant::getConversationId, conversationId)
                .ne(ConversationParticipant::getUserId, senderId);
        List<ConversationParticipant> others = participantMapper.selectList(wrapper);
        for (ConversationParticipant p : others) {
            p.setUnreadCount(p.getUnreadCount() + 1);
            participantMapper.updateById(p);
        }

        return Result.success(message);
    }
}
