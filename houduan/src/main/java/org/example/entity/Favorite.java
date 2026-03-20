package org.example.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("favorites")
public class Favorite implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "favorite_id", type = IdType.AUTO)
    private Long favoriteId;

    @TableField("user_id")
    private Integer userId;

    @TableField("goods_id")
    private Integer goodsId;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
