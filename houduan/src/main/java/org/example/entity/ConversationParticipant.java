package org.example.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("conversation_participants")
public class ConversationParticipant implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("conversation_id")
    private String conversationId;

    @TableField("user_id")
    private Integer userId;

    @TableField("unread_count")
    private Integer unreadCount;

    @TableField("joined_at")
    private LocalDateTime joinedAt;
}
