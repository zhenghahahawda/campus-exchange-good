package org.example.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("conversations")
public class Conversation implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    private String name;

    private String type; // private, group

    @TableField("goods_id")
    private Integer goodsId; // 关联的商品ID（群聊时使用）

    private String avatar;

    @TableField("last_message_id")
    private String lastMessageId;

    @TableField("last_message_time")
    private LocalDateTime lastMessageTime;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
