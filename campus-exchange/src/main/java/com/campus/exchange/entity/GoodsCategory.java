package com.campus.exchange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品分类实体类
 */
@Data
@TableName("goods_categories")
public class GoodsCategory {

    @TableId(value = "category_id", type = IdType.AUTO)
    private Integer categoryId;

    private String categoryName;

    private String categoryCode;

    private Integer sortOrder;

    private Integer isActive;

    private LocalDateTime createdAt;
}
