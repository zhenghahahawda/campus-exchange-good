package org.example.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "joinedAt", LocalDateTime.class, LocalDateTime.now());
        
        // 自动生成订单号 - 只对 orderNumber 字段处理
        if (metaObject.hasGetter("orderNumber") && metaObject.getValue("orderNumber") == null) {
            String orderNumber = "ORD-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
            this.strictInsertFill(metaObject, "orderNumber", String.class, orderNumber);
        }
        
        // 不要处理 orderId，让数据库自动生成
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}
