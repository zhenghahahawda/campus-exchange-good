package org.example.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("goods_categories")
public class GoodsCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "category_id", type = IdType.AUTO)
    private Integer categoryId;

    @TableField("category_name")
    private String categoryName;

    @TableField("category_code")
    private String categoryCode;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("is_active")
    private Integer isActive; // 0=禁用, 1=启用

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
