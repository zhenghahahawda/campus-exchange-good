/*
 Navicat Premium Data Transfer

 Source Server         : 腾讯云服务器
 Source Server Type    : MySQL
 Source Server Version : 80045
 Source Host           : 106.52.174.132:3306
 Source Schema         : exchange_system

 Target Server Type    : MySQL
 Target Server Version : 80045
 File Encoding         : 65001

 Date: 18/03/2026 08:59:34
*/

/*
================================================================================
                        校园交换系统数据库设计报告
                    Campus Exchange System Database Design
================================================================================

【系统概述】
校园交换系统是一个面向校园用户的物品交换平台，支持用户发布闲置物品、浏览商品、
发起交换请求、在线沟通、订单管理、违规举报等功能。

【设计原则】
1. 数据完整性：使用外键约束保证数据一致性
2. 性能优化：合理设置索引提升查询效率
3. 安全性：密码加密存储、会话管理、操作日志
4. 可扩展性：预留扩展字段、支持多种业务场景

================================================================================
                              核心业务模块
================================================================================

一、用户管理模块
--------------------------------------------------------------------------------
【users】用户表
- 功能：存储用户账号信息、认证数据、信用评分
- 核心字段：user_id(8位ID)、username、password_hash、email、phone、user_type(admin/user)
- 特性：支持用户封禁、信用评分系统、头像管理、学校信息
- 索引：username/email/phone唯一索引，user_type/status/credit_score查询索引

【user_id_sequence】用户ID序列表
- 功能：生成10000000-99999999范围的顺序用户ID
- 机制：使用AUTO_INCREMENT确保ID唯一性和连续性

【user_sessions】用户会话表
- 功能：管理用户登录会话、token认证
- 核心字段：session_id、token、refresh_token、expires_at、device_id
- 特性：支持多设备登录、会话过期管理、设备追踪

【user_login_logs】登录日志表
- 功能：记录用户登录历史、审计追踪
- 核心字段：login_time、login_ip、login_status、device_type、browser、os
- 用途：安全审计、异常登录检测

二、商品管理模块
--------------------------------------------------------------------------------
【goods】商品表
- 功能：存储用户发布的交换物品信息
- 核心字段：good_id、good_name、description、exchange_for、category_id、condition_level
- 状态管理：
  * status: pending(待审核)/approved(已通过)/rejected(已拒绝)
  * shelf_status: archived(已下架)/listed(已上架)/sold(已售出)
- 特性：支持多图片(JSON)、浏览/点赞/收藏计数、交换码、审核机制
- 索引：user_id、category_id、status、shelf_status、复合索引优化查询

【goods_categories】商品分类表
- 功能：定义商品分类体系
- 核心字段：category_id、category_name、category_code、sort_order
- 特性：支持分类启用/禁用、排序控制

三、交易订单模块
--------------------------------------------------------------------------------
【orders】订单表
- 功能：管理用户间的物品交换流程
- 核心字段：order_id、order_number、initiator_id、receiver_id、
           initiator_good_id、receiver_good_id、exchange_code
- 状态流转：pending(待确认) → completed(已完成) / cancelled(已取消)
- 确认机制：双方确认(initiator_confirmed、receiver_confirmed)
- 特性：支持双向评价、交换码验证、完成/取消时间记录
- 索引：order_number唯一索引、用户ID索引、状态索引、交换码索引

四、即时通讯模块
--------------------------------------------------------------------------------
【conversations】会话表
- 功能：管理私聊和群聊会话
- 核心字段：id(UUID)、type(private/group)、goods_id、last_message_id
- 特性：支持商品关联、会话头像、最后消息追踪

【conversation_participants】会话参与者表
- 功能：记录会话成员及未读消息数
- 核心字段：conversation_id、user_id、unread_count、joined_at
- 约束：用户在同一会话中唯一(uk_conversation_user)

【messages】消息表
- 功能：存储会话中的所有消息
- 核心字段：id(UUID)、conversation_id、sender_id、content、type
- 消息类型：text(文本)、image(图片)、file(文件)
- 索引：conversation_id、sender_id、created_at时间索引

五、互动功能模块
--------------------------------------------------------------------------------
【favorites】收藏表
- 功能：用户收藏商品
- 约束：每个用户对每个商品只能收藏一次(uk_user_goods)
- 索引：user_id、goods_id、created_at

【notifications】通知表
- 功能：系统消息推送
- 通知类型：new_goods(新商品)、exchange_request(交换请求)、
           exchange_accepted(交换接受)、system(系统通知)
- 核心字段：user_id、type、title、content、related_id、is_read
- 索引：user_id、is_read、type、created_at

六、违规管理模块
--------------------------------------------------------------------------------
【violation_reports】违规举报表
- 功能：用户举报违规内容
- 举报对象：target_type(product/user)
- 举报类型：inappropriate_content(不当内容)、fraud(欺诈)、spam(垃圾信息)等
- 状态流转：pending → processing → approved/rejected
- 处理措施：warning(警告)、tempBan(临时封禁)、permBan(永久封禁)、removeProduct(删除商品)
- 核心字段：report_number、reporter_id、handler_id、handle_action、
           credit_deduction、ban_duration
- 索引：report_number唯一、target复合索引、status索引

【violation_report_logs】举报处理日志表
- 功能：记录举报处理全流程
- 操作类型：assign(分配)、process(处理中)、approve(批准)、reject(拒绝)、comment(评论)
- 核心字段：report_id、operator_id、action_type、old_status、new_status
- 用途：审计追踪、流程回溯

================================================================================
                            数据库设计特点
================================================================================

【外键约束】
- 所有关联表使用外键约束(FOREIGN KEY)
- 级联删除(ON DELETE CASCADE)：删除主记录时自动删除关联记录
- 限制更新(ON UPDATE RESTRICT)：防止误操作

【索引策略】
- 主键索引：所有表使用AUTO_INCREMENT主键
- 唯一索引：username、email、phone、order_number等业务唯一字段
- 单列索引：高频查询字段(user_id、status、created_at等)
- 复合索引：多条件查询优化(idx_status_shelf、idx_target_type_id等)

【字符集编码】
- 统一使用utf8mb4字符集
- 排序规则：utf8mb4_unicode_ci
- 支持emoji和多语言字符

【时间戳管理】
- created_at：创建时间，默认CURRENT_TIMESTAMP
- updated_at：更新时间，自动更新ON UPDATE CURRENT_TIMESTAMP
- 业务时间：completed_at、cancelled_at、expires_at等

【数据类型选择】
- ID字段：INT UNSIGNED(用户/商品)、BIGINT UNSIGNED(日志/消息)、VARCHAR(50)(UUID)
- 状态字段：VARCHAR(20)枚举值
- 计数字段：INT UNSIGNED DEFAULT 0
- 文本字段：VARCHAR(长度)、TEXT(长内容)
- JSON字段：存储图片数组等复杂数据

【安全设计】
- 密码：password_hash存储加密后的密码
- 会话：token/refresh_token双token机制
- 审计：login_logs、violation_report_logs记录关键操作
- 权限：user_type区分管理员和普通用户

【性能优化】
- 分页查询：created_at索引支持时间排序
- 计数缓存：view_count、like_count、favorite_count避免实时统计
- 未读消息：unread_count字段避免COUNT查询
- 最后消息：last_message_id、last_message_time冗余提升查询效率

================================================================================
                              表关系图
================================================================================

users (核心表)
  ├─→ goods (发布商品)
  ├─→ orders (发起/接收交换)
  ├─→ favorites (收藏商品)
  ├─→ notifications (接收通知)
  ├─→ user_sessions (登录会话)
  ├─→ user_login_logs (登录日志)
  ├─→ conversation_participants (参与会话)
  ├─→ messages (发送消息)
  ├─→ violation_reports (举报/处理)
  └─→ violation_report_logs (操作日志)

goods
  ├─→ orders (交换商品)
  ├─→ favorites (被收藏)
  ├─→ conversations (关联会话)
  └─→ goods_categories (所属分类)

conversations
  ├─→ conversation_participants (参与者)
  └─→ messages (消息记录)

violation_reports
  └─→ violation_report_logs (处理日志)

================================================================================
                            业务流程说明
================================================================================

【用户注册登录流程】
1. 用户注册 → user_id_sequence生成ID → users表创建记录
2. 用户登录 → 验证密码 → user_sessions创建会话 → user_login_logs记录日志

【商品发布流程】
1. 用户发布商品 → goods表(status=pending)
2. 管理员审核 → 更新status(approved/rejected)、auditor_id
3. 审核通过 → 用户上架 → shelf_status=listed

【交换流程】
1. 用户A浏览商品 → 发起交换请求 → orders表(status=pending)
2. 用户B收到通知 → 确认交换 → receiver_confirmed=true
3. 双方确认 → status=completed → goods.shelf_status=sold
4. 双方评价 → initiator_review、receiver_review

【即时通讯流程】
1. 用户发起聊天 → conversations表创建会话
2. 添加参与者 → conversation_participants表
3. 发送消息 → messages表 → 更新unread_count
4. 读取消息 → unread_count清零

【违规举报流程】
1. 用户举报 → violation_reports表(status=pending)
2. 管理员分配 → handler_id → violation_report_logs记录
3. 管理员处理 → status=processing → 执行handle_action
4. 完成处理 → status=approved/rejected → 扣除信用分/封禁用户

================================================================================
                              维护建议
================================================================================

1. 定期清理过期会话(user_sessions.expires_at)
2. 归档历史订单和消息数据
3. 监控索引使用情况，优化慢查询
4. 定期备份数据库
5. 审查违规举报处理效率
6. 监控用户信用评分分布

================================================================================
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for conversation_participants
-- ----------------------------
DROP TABLE IF EXISTS `conversation_participants`;
CREATE TABLE `conversation_participants`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '参与者记录ID',
  `conversation_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID',
  `user_id` int UNSIGNED NOT NULL COMMENT '用户ID',
  `unread_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '未读消息数',
  `joined_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_conversation_user`(`conversation_id`, `user_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_conversation_id`(`conversation_id`) USING BTREE,
  CONSTRAINT `fk_participants_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_participants_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会话参与者表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of conversation_participants
-- ----------------------------
INSERT INTO `conversation_participants` VALUES (2, '64cf61fb4a1f661178bff19b3957422a', 10000001, 6, '2026-03-14 00:51:49');
INSERT INTO `conversation_participants` VALUES (3, '85aa7b12fcbacba39a1c554019a38192', 10000004, 0, '2026-03-14 01:53:33');
INSERT INTO `conversation_participants` VALUES (5, '79c93ed96ac419135f9cbf5f1d2aedb6', 10000004, 0, '2026-03-14 20:28:08');
INSERT INTO `conversation_participants` VALUES (6, '79c93ed96ac419135f9cbf5f1d2aedb6', 10000001, 1, '2026-03-14 20:28:08');
INSERT INTO `conversation_participants` VALUES (7, '71e32daa75134b0643335e881f56e47a', 10000002, 1, '2026-03-14 21:13:01');
INSERT INTO `conversation_participants` VALUES (8, '71e32daa75134b0643335e881f56e47a', 10000003, 1, '2026-03-14 21:13:01');
INSERT INTO `conversation_participants` VALUES (9, '405effbcb4b3cf6486b3240d1fe67473', 10000003, 0, '2026-03-16 11:46:31');
INSERT INTO `conversation_participants` VALUES (10, '405effbcb4b3cf6486b3240d1fe67473', 10000001, 1, '2026-03-16 11:46:31');
INSERT INTO `conversation_participants` VALUES (11, '1ee0841cc5543ec431bd17455afa2c75', 10000002, 0, '2026-03-16 14:53:33');
INSERT INTO `conversation_participants` VALUES (12, '1ee0841cc5543ec431bd17455afa2c75', 10000001, 1, '2026-03-16 14:53:33');
INSERT INTO `conversation_participants` VALUES (21, '96bdac004c3582ab81c06bb716ffe9be', 10000002, 3, '2026-03-16 15:59:46');
INSERT INTO `conversation_participants` VALUES (22, '96bdac004c3582ab81c06bb716ffe9be', 10000001, 4, '2026-03-16 15:59:46');
INSERT INTO `conversation_participants` VALUES (23, '96bdac004c3582ab81c06bb716ffe9be', 10000003, 1, '2026-03-16 16:02:07');
INSERT INTO `conversation_participants` VALUES (24, '96bdac004c3582ab81c06bb716ffe9be', 10000004, 1, '2026-03-16 16:43:46');

-- ----------------------------
-- Table structure for conversations
-- ----------------------------
DROP TABLE IF EXISTS `conversations`;
CREATE TABLE `conversations`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话唯一ID',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'private' COMMENT '会话类型：private=私聊，group=群聊',
  `goods_id` int UNSIGNED NULL DEFAULT NULL COMMENT '关联的商品ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '会话名称',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '会话头像URL',
  `last_message_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最后一条消息ID',
  `last_message_time` timestamp NULL DEFAULT NULL COMMENT '最后消息时间',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_last_message_time`(`last_message_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会话表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of conversations
-- ----------------------------
INSERT INTO `conversations` VALUES ('1ee0841cc5543ec431bd17455afa2c75', 'private', NULL, 'admin', 'https://2638224627-1355158021.cos.ap-guangzhou.myqcloud.com/avatar/10000001/3c08c049293348ff91df39a0657c98cc.jpg', '31f1892ea20e694245999b7997716a2a', '2026-03-16 14:53:56', '2026-03-16 14:53:33', '2026-03-16 14:53:33');
INSERT INTO `conversations` VALUES ('405effbcb4b3cf6486b3240d1fe67473', 'private', NULL, 'admin', 'https://2638224627-1355158021.cos.ap-guangzhou.myqcloud.com/avatar/10000001/3c08c049293348ff91df39a0657c98cc.jpg', '5a802791427e69138baafdc89b208cbe', '2026-03-16 14:53:15', '2026-03-16 11:46:31', '2026-03-16 11:46:31');
INSERT INTO `conversations` VALUES ('64cf61fb4a1f661178bff19b3957422a', 'private', NULL, 'admin', 'https://2638224627-1355158021.cos.ap-guangzhou.myqcloud.com/avatar/10000001/3c08c049293348ff91df39a0657c98cc.jpg', '7b5f9b5c37fb82fd9d7f5cf23d79f5f4', '2026-03-14 01:09:38', '2026-03-14 00:51:49', '2026-03-14 00:51:49');
INSERT INTO `conversations` VALUES ('71e32daa75134b0643335e881f56e47a', 'private', NULL, 'yangtianbo', 'https://2638224627-1355158021.cos.ap-guangzhou.myqcloud.com/avatar/10000003/046f3e77a42043b0b5c7ecb193cb994c.png', 'e685e7d5195bef278990e100e01ff88b', '2026-03-15 00:35:43', '2026-03-14 21:13:01', '2026-03-14 21:13:01');
INSERT INTO `conversations` VALUES ('79c93ed96ac419135f9cbf5f1d2aedb6', 'private', NULL, 'admin', 'https://2638224627-1355158021.cos.ap-guangzhou.myqcloud.com/avatar/10000001/3c08c049293348ff91df39a0657c98cc.jpg', 'befccefea551a85714ed43e3491b6f76', '2026-03-14 20:28:28', '2026-03-14 20:28:08', '2026-03-14 20:28:08');
INSERT INTO `conversations` VALUES ('85aa7b12fcbacba39a1c554019a38192', 'private', NULL, 'testuser2024', NULL, 'a95608504bef5983923eebecb5d374f5', '2026-03-14 01:53:37', '2026-03-14 01:53:32', '2026-03-14 01:53:32');
INSERT INTO `conversations` VALUES ('96bdac004c3582ab81c06bb716ffe9be', 'group', 98, '大山里面课件撒啊啊哇大大达到啊 a 交换群', NULL, 'df687fb142d8069139d83307818bc18b', '2026-03-17 19:29:44', '2026-03-16 15:59:46', '2026-03-16 15:59:46');

-- ----------------------------
-- Table structure for favorites
-- ----------------------------
DROP TABLE IF EXISTS `favorites`;
CREATE TABLE `favorites`  (
  `favorite_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '收藏记录唯一ID',
  `goods_id` int UNSIGNED NOT NULL COMMENT '商品ID',
  `user_id` int UNSIGNED NOT NULL COMMENT '收藏用户ID',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`favorite_id`) USING BTREE,
  UNIQUE INDEX `uk_user_goods`(`user_id`, `goods_id`) USING BTREE COMMENT '每个用户对每个商品只能收藏一次',
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_goods_id`(`goods_id`) USING BTREE,
  INDEX `idx_created_at`(`created_at`) USING BTREE,
  CONSTRAINT `fk_favorites_goods` FOREIGN KEY (`goods_id`) REFERENCES `goods` (`good_id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_favorites_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of favorites
-- ----------------------------
INSERT INTO `favorites` VALUES (9, 98, 10000003, '2026-03-17 14:28:48');

-- ----------------------------
-- Table structure for goods
-- ----------------------------
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods`  (
  `good_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商品唯一ID',
  `good_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '商品详细描述',
  `exchange_for` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '期望交换的物品描述',
  `category_id` tinyint UNSIGNED NOT NULL COMMENT '商品分类ID',
  `condition_level` tinyint UNSIGNED NOT NULL COMMENT '新旧程度：1=全新，2=几乎全新，3=良好，4=一般',
  `status` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '商品状态：0=待审核，1=已通过，2=已拒绝',
  `shelf_status` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '0=已下架，1=已上架，2=已售出',
  `user_id` int UNSIGNED NOT NULL COMMENT '卖家用户ID',
  `images` json NULL COMMENT '商品图片URL数组，最多5张，格式：[\"url1\", \"url2\"]',
  `exchange_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '交换凭证码，格式：EX-{学校代码}-{日期}-{随机码}',
  `view_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `like_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '点赞数',
  `favorite_count` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '收藏数',
  `reject_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '驳回原因',
  `auditor_id` int UNSIGNED NULL DEFAULT NULL COMMENT '审核员ID',
  `audited_at` timestamp NULL DEFAULT NULL COMMENT '审核时间',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `sold_at` timestamp NULL DEFAULT NULL COMMENT '售出时间',
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT '软删除时间',
  PRIMARY KEY (`good_id`) USING BTREE,
  UNIQUE INDEX `exchange_code`(`exchange_code`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE COMMENT '卖家ID索引',
  INDEX `idx_category_id`(`category_id`) USING BTREE COMMENT '分类ID索引',
  INDEX `idx_status`(`status`) USING BTREE COMMENT '商品状态索引',
  INDEX `idx_shelf_status`(`shelf_status`) USING BTREE COMMENT '上架状态索引',
  INDEX `idx_created_at`(`created_at`) USING BTREE COMMENT '创建时间索引',
  INDEX `idx_exchange_code`(`exchange_code`) USING BTREE COMMENT '交换凭证索引',
  INDEX `idx_deleted_at`(`deleted_at`) USING BTREE COMMENT '软删除索引',
  INDEX `idx_status_shelf`(`status`, `shelf_status`) USING BTREE COMMENT '状态和上架复合索引',
  INDEX `idx_category_status`(`category_id`, `status`) USING BTREE COMMENT '分类和状态复合索引',
  INDEX `fk_goods_auditor`(`auditor_id`) USING BTREE,
  CONSTRAINT `fk_goods_auditor` FOREIGN KEY (`auditor_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_goods_category` FOREIGN KEY (`category_id`) REFERENCES `goods_categories` (`category_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_goods_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 107 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of goods
-- ----------------------------
INSERT INTO `goods` VALUES (91, '巴纳夫卡拉斯来看你了吗vl安家费阿萨 阿萨的撒的撒', '321', NULL, 1, 3, 1, 1, 10000003, '[\"https://2638224627-1355158021.cos.ap-guangzhou.myqcloud.com/goods/1773493633213-屏幕截图 2025-01-02 210311.png\"]', 'EX-武汉-20260314-IRYO', 170, 0, 0, NULL, NULL, '2026-03-14 21:09:15', '2026-03-14 21:07:14', '2026-03-17 08:18:26', NULL, NULL);
INSERT INTO `goods` VALUES (92, '给老婆奥氮平的历史怕【上了股票【三大流派【舍得离开as暗杀阿萨啊收到是的卡上打开电视了阿斯顿法定', '312', NULL, 1, 3, 1, 1, 10000002, '[\"https://2638224627-1355158021.cos.ap-guangzhou.myqcloud.com/goods/1773498127167-2459460183.jpg\"]', 'EX-武汉-20260314-MRUZ', 239, 0, 0, NULL, NULL, '2026-03-14 22:23:26', '2026-03-14 22:22:08', '2026-03-17 08:18:26', NULL, NULL);
INSERT INTO `goods` VALUES (93, '312', '321', NULL, 1, 3, 1, 1, 10000003, '[\"https://2638224627-1355158021.cos.ap-guangzhou.myqcloud.com/goods/1b3cbfb35fb941e1a6d3acfcb2e06d17.jpg\"]', 'EX-武汉-20260316-YT0K', 11, 0, 0, NULL, NULL, '2026-03-16 11:44:02', '2026-03-16 11:43:51', '2026-03-17 08:18:26', NULL, NULL);
INSERT INTO `goods` VALUES (94, '3321312', '312', NULL, 1, 3, 1, 1, 10000002, '[\"https://2638224627-1355158021.cos.ap-guangzhou.myqcloud.com/goods/1773632650247-wallhaven-k88yd7.jpg\"]', 'EX-武汉-20260316-WQSE', 65, 0, 0, NULL, NULL, '2026-03-16 11:44:26', '2026-03-16 11:44:11', '2026-03-17 08:18:26', NULL, NULL);
INSERT INTO `goods` VALUES (95, '该发明了开了个收到固定更发达啊啊的啊大大发v从vccv是 ', '111', NULL, 1, 7, 1, 2, 10000003, '[\"https://2638224627-1355158021.cos.ap-guangzhou.myqcloud.com/goods/1773643864961-wallhaven-3qqqwd.jpg\"]', 'EX-武汉-20260316-W8W6', 14, 0, 0, NULL, 10000001, '2026-03-16 14:57:33', '2026-03-16 14:51:07', '2026-03-16 15:46:03', '2026-03-16 15:09:03', NULL);
INSERT INTO `goods` VALUES (96, '3asda 0328239 uiok mn329 ufewewew   ', '31231', NULL, 1, 7, 1, 2, 10000002, '[\"https://2638224627-1355158021.cos.ap-guangzhou.myqcloud.com/goods/1773643879677-wallhaven-dpxxy3.png\"]', 'EX-武汉-20260316-ZDQK', 55, 0, 0, NULL, 10000001, '2026-03-16 14:57:38', '2026-03-16 14:51:21', '2026-03-17 08:13:56', '2026-03-16 15:09:02', NULL);
INSERT INTO `goods` VALUES (98, '大山里面课件撒啊啊哇大大达到啊 a', '提高到三十\n', NULL, 3, 10, 1, 1, 10000001, '[\"https://2638224627-1355158021.cos.ap-guangzhou.myqcloud.com/goods/46fca3138279451e808514ba8b56a0fc.jpg\"]', 'EX-武汉-20260316-7ASC', 25, 1, 1, NULL, 10000001, '2026-03-16 15:58:24', '2026-03-16 15:58:20', '2026-03-16 15:58:20', NULL, NULL);
INSERT INTO `goods` VALUES (99, 'adkakdla', '312', '312', 1, 7, 1, 0, 10000002, '[\"https://2638224627-1355158021.cos.ap-guangzhou.myqcloud.com/goods/1773647952909-2459460183.jpg\"]', 'EX-武汉-20260316-0FNA', 16, 0, 0, NULL, 10000001, '2026-03-16 15:59:29', '2026-03-16 15:59:14', '2026-03-17 12:59:24', NULL, NULL);
INSERT INTO `goods` VALUES (100, '你哪来的这么多图片', '312', '312', 1, 7, 1, 1, 10000003, '[\"https://2638224627-1355158021.cos.ap-guangzhou.myqcloud.com/goods/1773647969300-wallhaven-gww6wl.png\"]', 'EX-武汉-20260316-LDZ9', 18, 0, 0, NULL, 10000001, '2026-03-16 16:01:58', '2026-03-16 15:59:30', '2026-03-16 16:03:42', NULL, NULL);
INSERT INTO `goods` VALUES (101, '312', '312', '321', 1, 1, 1, 1, 10000004, '[\"https://2638224627-1355158021.cos.ap-guangzhou.myqcloud.com/goods/1773650569084-wallhaven-rqqw2q.jpg\"]', 'EX-武汉-20260316-0N00', 61, 0, 0, NULL, 10000001, '2026-03-16 16:43:31', '2026-03-16 16:42:50', '2026-03-16 16:42:50', NULL, NULL);
INSERT INTO `goods` VALUES (103, '231', '312', '312', 1, 1, 1, 1, 10000003, '[\"https://2638224627-1355158021.cos.ap-guangzhou.myqcloud.com/goods/1773712104870-wallhaven-k88yd7.jpg\"]', 'EX-武汉-20260317-E2OE', 13, 0, 0, NULL, 10000001, '2026-03-17 10:19:53', '2026-03-17 09:48:26', '2026-03-17 09:48:26', NULL, NULL);
INSERT INTO `goods` VALUES (104, '而阿尔巴人', '1111111111', '无', 1, 1, 1, 1, 10000004, '[\"https://2638224627-1355158021.cos.ap-guangzhou.myqcloud.com/goods/1773736904778-屏幕截图 2026-03-17 151415.png\"]', 'EX-武汉-20260317-EAFR', 2, 0, 0, NULL, 10000001, '2026-03-17 16:42:24', '2026-03-17 16:41:46', '2026-03-17 16:41:46', NULL, NULL);
INSERT INTO `goods` VALUES (105, '1', '1', '11', 1, 1, 1, 1, 10000003, '[\"https://2638224627-1355158021.cos.ap-guangzhou.myqcloud.com/goods/1773746765513-pc-dongman-copy.png\"]', 'EX-武汉-20260317-B2LJ', 1, 0, 0, NULL, 10000001, '2026-03-17 19:26:48', '2026-03-17 19:26:06', '2026-03-17 19:26:06', NULL, NULL);
INSERT INTO `goods` VALUES (106, '13', '321', '321', 1, 1, 0, 0, 10000003, '[\"https://2638224627-1355158021.cos.ap-guangzhou.myqcloud.com/goods/1773746862865-屏幕截图 2026-03-17 185714.png\"]', NULL, 0, 0, 0, NULL, NULL, NULL, '2026-03-17 19:27:43', '2026-03-17 19:27:43', NULL, NULL);

-- ----------------------------
-- Table structure for goods_categories
-- ----------------------------
DROP TABLE IF EXISTS `goods_categories`;
CREATE TABLE `goods_categories`  (
  `category_id` tinyint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `category_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类代码',
  `sort_order` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '排序顺序',
  `is_active` tinyint UNSIGNED NOT NULL DEFAULT 1 COMMENT '是否启用：0=禁用，1=启用',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`category_id`) USING BTREE,
  UNIQUE INDEX `category_code`(`category_code`) USING BTREE,
  INDEX `idx_is_active`(`is_active`) USING BTREE COMMENT '启用状态索引',
  INDEX `idx_sort_order`(`sort_order`) USING BTREE COMMENT '排序索引'
) ENGINE = InnoDB AUTO_INCREMENT = 34 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of goods_categories
-- ----------------------------
INSERT INTO `goods_categories` VALUES (1, '数码产品', 'digital', 1, 1, '2026-03-12 13:39:55');
INSERT INTO `goods_categories` VALUES (2, '手机通讯', 'mobile', 2, 1, '2026-03-12 13:39:55');
INSERT INTO `goods_categories` VALUES (3, '电脑办公', 'computer', 3, 1, '2026-03-12 13:39:55');
INSERT INTO `goods_categories` VALUES (4, '摄影摄像', 'camera', 4, 1, '2026-03-12 13:39:55');
INSERT INTO `goods_categories` VALUES (5, '智能设备', 'smart_device', 5, 1, '2026-03-12 13:39:55');
INSERT INTO `goods_categories` VALUES (6, '图书文具', 'books', 10, 1, '2026-03-12 13:39:55');
INSERT INTO `goods_categories` VALUES (7, '教材教辅', 'textbook', 11, 1, '2026-03-12 13:39:55');
INSERT INTO `goods_categories` VALUES (8, '考研资料', 'exam_material', 12, 1, '2026-03-12 13:39:55');
INSERT INTO `goods_categories` VALUES (9, '文具用品', 'stationery', 13, 1, '2026-03-12 13:39:55');
INSERT INTO `goods_categories` VALUES (10, '乐器音像', 'music', 14, 1, '2026-03-12 13:39:55');
INSERT INTO `goods_categories` VALUES (11, '家居生活', 'home', 20, 1, '2026-03-12 13:39:55');
INSERT INTO `goods_categories` VALUES (12, '家具家电', 'furniture', 21, 1, '2026-03-12 13:39:55');
INSERT INTO `goods_categories` VALUES (13, '厨房用品', 'kitchen', 22, 1, '2026-03-12 13:39:55');
INSERT INTO `goods_categories` VALUES (14, '床上用品', 'bedding', 23, 1, '2026-03-12 13:39:55');
INSERT INTO `goods_categories` VALUES (15, '日用百货', 'daily', 24, 1, '2026-03-12 13:39:55');
INSERT INTO `goods_categories` VALUES (16, '服饰鞋包', 'fashion', 30, 1, '2026-03-12 13:39:55');
INSERT INTO `goods_categories` VALUES (17, '男装', 'mens_wear', 31, 1, '2026-03-12 13:39:55');
INSERT INTO `goods_categories` VALUES (18, '女装', 'womens_wear', 32, 1, '2026-03-12 13:39:55');
INSERT INTO `goods_categories` VALUES (19, '鞋靴', 'shoes', 33, 1, '2026-03-12 13:39:55');
INSERT INTO `goods_categories` VALUES (20, '箱包配饰', 'bags', 34, 1, '2026-03-12 13:39:55');
INSERT INTO `goods_categories` VALUES (21, '运动户外', 'sports', 40, 1, '2026-03-12 13:39:55');
INSERT INTO `goods_categories` VALUES (22, '运动器材', 'sports_equipment', 41, 1, '2026-03-12 13:39:55');
INSERT INTO `goods_categories` VALUES (23, '自行车', 'bicycle', 42, 1, '2026-03-12 13:39:55');
INSERT INTO `goods_categories` VALUES (24, '户外装备', 'outdoor', 43, 1, '2026-03-12 13:39:55');
INSERT INTO `goods_categories` VALUES (25, '运动服饰', 'sportswear', 44, 1, '2026-03-12 13:39:55');
INSERT INTO `goods_categories` VALUES (26, '美妆个护', 'beauty', 50, 1, '2026-03-12 13:39:55');
INSERT INTO `goods_categories` VALUES (27, '护肤品', 'skincare', 51, 1, '2026-03-12 13:39:55');
INSERT INTO `goods_categories` VALUES (28, '彩妆', 'makeup', 52, 1, '2026-03-12 13:39:55');
INSERT INTO `goods_categories` VALUES (29, '个人护理', 'personal_care', 53, 1, '2026-03-12 13:39:55');
INSERT INTO `goods_categories` VALUES (30, '玩具手办', 'toys', 60, 1, '2026-03-12 13:39:55');
INSERT INTO `goods_categories` VALUES (31, '宠物用品', 'pet', 61, 1, '2026-03-12 13:39:55');
INSERT INTO `goods_categories` VALUES (32, '其他闲置', 'other', 99, 1, '2026-03-12 13:39:55');

-- ----------------------------
-- Table structure for messages
-- ----------------------------
DROP TABLE IF EXISTS `messages`;
CREATE TABLE `messages`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息唯一ID',
  `conversation_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话ID',
  `sender_id` int UNSIGNED NOT NULL COMMENT '发送者用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'text' COMMENT '消息类型：text=文本，image=图片，file=文件',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_conversation_id`(`conversation_id`) USING BTREE,
  INDEX `idx_sender_id`(`sender_id`) USING BTREE,
  INDEX `idx_created_at`(`created_at`) USING BTREE,
  CONSTRAINT `fk_messages_conversation` FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_messages_sender` FOREIGN KEY (`sender_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of messages
-- ----------------------------
INSERT INTO `messages` VALUES ('0d8e18f647382273edb17c1b8f3c03fc', '96bdac004c3582ab81c06bb716ffe9be', 10000002, '交换订单 ORD-1773647985586-299 已创建，请在此群聊中协商交换事宜。', 'text', '2026-03-16 15:59:46');
INSERT INTO `messages` VALUES ('2d5c96ff9dc37121c712bb1037b76db8', '96bdac004c3582ab81c06bb716ffe9be', 10000002, '大', 'text', '2026-03-16 15:59:57');
INSERT INTO `messages` VALUES ('31f1892ea20e694245999b7997716a2a', '1ee0841cc5543ec431bd17455afa2c75', 10000002, '3213', 'text', '2026-03-16 14:53:56');
INSERT INTO `messages` VALUES ('3c31225e48e53984044b3cb3cb684792', '96bdac004c3582ab81c06bb716ffe9be', 10000003, '312', 'text', '2026-03-16 16:02:18');
INSERT INTO `messages` VALUES ('5a802791427e69138baafdc89b208cbe', '405effbcb4b3cf6486b3240d1fe67473', 10000003, '312', 'text', '2026-03-16 14:53:15');
INSERT INTO `messages` VALUES ('a95608504bef5983923eebecb5d374f5', '85aa7b12fcbacba39a1c554019a38192', 10000004, '412312', 'text', '2026-03-14 01:53:37');
INSERT INTO `messages` VALUES ('befccefea551a85714ed43e3491b6f76', '79c93ed96ac419135f9cbf5f1d2aedb6', 10000004, '312', 'text', '2026-03-14 20:28:28');
INSERT INTO `messages` VALUES ('c864582f4d66362ba035b5d963e59033', '96bdac004c3582ab81c06bb716ffe9be', 10000004, '312', 'text', '2026-03-16 16:43:56');
INSERT INTO `messages` VALUES ('df687fb142d8069139d83307818bc18b', '96bdac004c3582ab81c06bb716ffe9be', 10000003, '3123', 'text', '2026-03-17 19:29:44');
INSERT INTO `messages` VALUES ('e685e7d5195bef278990e100e01ff88b', '71e32daa75134b0643335e881f56e47a', 10000003, '大', 'text', '2026-03-15 00:35:43');
INSERT INTO `messages` VALUES ('f91f19dd89c28499b5582d1bda0334f1', '71e32daa75134b0643335e881f56e47a', 10000002, '2·1', 'text', '2026-03-14 22:22:34');

-- ----------------------------
-- Table structure for notifications
-- ----------------------------
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` int UNSIGNED NOT NULL COMMENT '接收通知的用户ID',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知类型：new_goods=新物品发布, exchange_request=交换申请, exchange_accepted=交换接受, exchange_rejected=交换拒绝, system=系统通知',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知内容',
  `related_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联ID（商品ID或交换ID）',
  `related_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联类型：goods, exchange',
  `is_read` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否已读：0=未读，1=已读',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_is_read`(`is_read`) USING BTREE,
  INDEX `idx_created_at`(`created_at`) USING BTREE,
  INDEX `idx_type`(`type`) USING BTREE,
  CONSTRAINT `fk_notifications_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notifications
-- ----------------------------

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `order_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单唯一ID',
  `order_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号，格式：ORD-{时间戳}-{随机码}',
  `conversation_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联的会话ID',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending' COMMENT '订单状态：pending=待交易，completed=已完成，to_process=待处理，processing=处理中，cancelled=已取消',
  `initiator_id` int UNSIGNED NOT NULL COMMENT '发起方用户ID',
  `initiator_good_id` int UNSIGNED NOT NULL COMMENT '发起方提供的商品ID',
  `receiver_id` int UNSIGNED NOT NULL COMMENT '接收方用户ID',
  `receiver_good_id` int UNSIGNED NOT NULL COMMENT '接收方提供的商品ID',
  `exchange_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '交换凭证码',
  `initiator_confirmed` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '发起方是否已提交凭证码 0=未确认 1=已确认',
  `receiver_confirmed` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '接收方是否已提交凭证码 0=未确认 1=已确认',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '订单备注（管理员可编辑）',
  `cancel_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '取消原因',
  `admin_note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '管理员处理备注',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订单创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '订单更新时间',
  `completed_at` timestamp NULL DEFAULT NULL COMMENT '订单完成时间',
  `cancelled_at` timestamp NULL DEFAULT NULL COMMENT '订单取消时间',
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT '软删除时间',
  `initiator_review` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发起方评价内容',
  `initiator_review_images` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发起方评价图片，逗号分隔，最多3张',
  `receiver_review` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收方评价内容',
  `receiver_review_images` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接收方评价图片，逗号分隔，最多3张',
  `initiator_reviewed_at` timestamp NULL DEFAULT NULL COMMENT '发起方评价时间',
  `receiver_reviewed_at` timestamp NULL DEFAULT NULL COMMENT '接收方评价时间',
  PRIMARY KEY (`order_id`) USING BTREE,
  UNIQUE INDEX `order_number`(`order_number`) USING BTREE,
  UNIQUE INDEX `uk_order_number`(`order_number`) USING BTREE COMMENT '订单号唯一索引',
  UNIQUE INDEX `exchange_code`(`exchange_code`) USING BTREE,
  INDEX `idx_initiator_id`(`initiator_id`) USING BTREE COMMENT '发起方ID索引',
  INDEX `idx_receiver_id`(`receiver_id`) USING BTREE COMMENT '接收方ID索引',
  INDEX `idx_status`(`status`) USING BTREE COMMENT '订单状态索引',
  INDEX `idx_created_at`(`created_at`) USING BTREE COMMENT '创建时间索引',
  INDEX `idx_exchange_code`(`exchange_code`) USING BTREE COMMENT '交换凭证索引',
  INDEX `idx_deleted_at`(`deleted_at`) USING BTREE COMMENT '软删除索引',
  INDEX `fk_orders_initiator_good`(`initiator_good_id`) USING BTREE,
  INDEX `fk_orders_receiver_good`(`receiver_good_id`) USING BTREE,
  INDEX `idx_conversation_id`(`conversation_id`) USING BTREE,
  CONSTRAINT `fk_orders_initiator` FOREIGN KEY (`initiator_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_orders_initiator_good` FOREIGN KEY (`initiator_good_id`) REFERENCES `goods` (`good_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_orders_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_orders_receiver_good` FOREIGN KEY (`receiver_good_id`) REFERENCES `goods` (`good_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 39 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (27, 'ORD-1773506567451-389', NULL, 'completed', 10000003, 91, 10000002, 92, NULL, 1, 1, '我想和你交换 13', NULL, NULL, '2026-03-15 00:42:47', '2026-03-17 11:18:27', '2026-03-17 11:18:27', NULL, NULL, '[5]321', NULL, NULL, NULL, '2026-03-17 11:16:25', NULL);
INSERT INTO `orders` VALUES (28, 'ORD-1773632671526-579', NULL, 'completed', 10000002, 94, 10000003, 93, NULL, 1, 1, '我想和你交换 312', NULL, NULL, '2026-03-16 11:44:32', '2026-03-16 14:46:15', '2026-03-16 14:46:14', NULL, NULL, '[5]3213', 'https://2638224627-1355158021.cos.ap-guangzhou.myqcloud.com/violations/10000002-1773747668526-pc-dongman-copy.png', '[5]3123', 'https://2638224627-1355158021.cos.ap-guangzhou.myqcloud.com/violations/10000003-1773747683221-屏幕截图 2025-01-02 191057.png', '2026-03-17 19:41:10', '2026-03-17 19:41:23');
INSERT INTO `orders` VALUES (35, 'ORD-1773644826956-496', NULL, 'completed', 10000002, 96, 10000003, 95, NULL, 1, 1, '我想和你交换 311', NULL, NULL, '2026-03-16 15:07:07', '2026-03-17 18:26:02', '2026-03-17 18:26:02', NULL, NULL, NULL, NULL, '[5]31', 'https://2638224627-1355158021.cos.ap-guangzhou.myqcloud.com/violations/10000003-1773714968873-wallhaven-k88yd7.jpg', NULL, '2026-03-17 10:36:14');
INSERT INTO `orders` VALUES (36, 'ORD-1773647985586-299', NULL, 'cancelled', 10000002, 99, 10000001, 98, NULL, 0, 0, '我想和你交换 大山里面课件撒啊啊哇大大达到啊 a', NULL, NULL, '2026-03-16 15:59:46', '2026-03-17 16:46:19', NULL, '2026-03-17 16:46:19', NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `orders` VALUES (37, 'ORD-1773648127268-657', NULL, 'cancelled', 10000003, 100, 10000001, 98, NULL, 0, 0, '我想和你交换 大山里面课件撒啊啊哇大大达到啊 a', NULL, NULL, '2026-03-16 16:02:07', '2026-03-16 16:02:07', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `orders` VALUES (38, 'ORD-1773650625691-167', NULL, 'pending', 10000004, 101, 10000001, 98, NULL, 0, 0, '我想和你交换 大山里面课件撒啊啊哇大大达到啊 a', NULL, NULL, '2026-03-16 16:43:46', '2026-03-16 16:43:46', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for user_id_sequence
-- ----------------------------
DROP TABLE IF EXISTS `user_id_sequence`;
CREATE TABLE `user_id_sequence`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '当前序列值',
  `stub` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '占位字符',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `stub`(`stub`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10000043 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户ID序列生成器' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_id_sequence
-- ----------------------------
INSERT INTO `user_id_sequence` VALUES (10000042, 'a');

-- ----------------------------
-- Table structure for user_login_logs
-- ----------------------------
DROP TABLE IF EXISTS `user_login_logs`;
CREATE TABLE `user_login_logs`  (
  `log_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志唯一ID',
  `user_id` int UNSIGNED NOT NULL COMMENT '用户ID',
  `login_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  `login_ip` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录IP地址（支持IPv4和IPv6）',
  `login_status` tinyint UNSIGNED NOT NULL COMMENT '登录状态：0=失败，1=成功',
  `fail_reason` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '登录失败原因',
  `user_agent` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '浏览器User-Agent字符串',
  `device_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备类型：PC/Mobile/Tablet',
  `browser` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '浏览器名称和版本',
  `os` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作系统名称和版本',
  PRIMARY KEY (`log_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE COMMENT '用户ID索引',
  INDEX `idx_login_time`(`login_time`) USING BTREE COMMENT '登录时间索引',
  INDEX `idx_login_status`(`login_status`) USING BTREE COMMENT '登录状态索引',
  INDEX `idx_user_time`(`user_id`, `login_time`) USING BTREE COMMENT '用户和时间复合索引',
  CONSTRAINT `fk_login_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 171 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '登录日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_login_logs
-- ----------------------------
INSERT INTO `user_login_logs` VALUES (73, 10000003, '2026-03-15 20:18:35', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (74, 10000003, '2026-03-15 20:35:05', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (75, 10000003, '2026-03-15 20:37:58', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (76, 10000003, '2026-03-15 20:44:53', '127.0.0.1', 0, 'Account is inactive', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (77, 10000003, '2026-03-15 20:45:02', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (78, 10000001, '2026-03-15 20:46:57', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0', 'Computer', 'Chrome 14', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (79, 10000003, '2026-03-15 20:51:31', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (80, 10000003, '2026-03-15 21:01:53', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (81, 10000003, '2026-03-15 21:03:03', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (82, 10000003, '2026-03-15 21:08:10', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (83, 10000003, '2026-03-15 21:08:21', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (84, 10000003, '2026-03-15 21:08:47', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (85, 10000003, '2026-03-15 21:09:39', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (86, 10000003, '2026-03-15 21:11:00', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (87, 10000003, '2026-03-15 21:11:41', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (88, 10000001, '2026-03-15 23:05:41', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0', 'Computer', 'Chrome 14', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (89, 10000003, '2026-03-15 23:11:59', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (90, 10000003, '2026-03-15 23:19:43', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (91, 10000001, '2026-03-16 08:52:32', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0', 'Computer', 'Chrome 14', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (92, 10000001, '2026-03-16 09:50:08', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0', 'Computer', 'Chrome 14', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (93, 10000003, '2026-03-16 11:00:36', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (94, 10000003, '2026-03-16 11:02:27', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (95, 10000003, '2026-03-16 11:02:36', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (96, 10000002, '2026-03-16 11:26:54', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0', 'PC', 'Edge', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (97, 10000002, '2026-03-16 11:26:57', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0', 'PC', 'Edge', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (98, 10000002, '2026-03-16 11:27:19', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0', 'PC', 'Edge', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (99, 10000001, '2026-03-16 11:51:01', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0', 'Computer', 'Chrome 14', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (100, 10000001, '2026-03-16 12:31:01', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0', 'Computer', 'Chrome 14', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (101, 10000002, '2026-03-16 14:13:47', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0', 'PC', 'Edge', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (102, 10000003, '2026-03-16 14:13:54', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (103, 10000003, '2026-03-16 14:14:05', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (104, 10000002, '2026-03-16 14:14:18', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0', 'PC', 'Edge', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (105, 10000003, '2026-03-16 14:14:25', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (106, 10000003, '2026-03-16 14:25:11', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (107, 10000001, '2026-03-16 14:39:11', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0', 'Computer', 'Chrome 14', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (108, 10000002, '2026-03-16 14:45:58', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0', 'PC', 'Edge', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (109, 10000004, '2026-03-16 14:52:31', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0', 'PC', 'Edge', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (110, 10000002, '2026-03-16 14:52:41', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0', 'PC', 'Edge', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (111, 10000004, '2026-03-16 16:26:40', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (112, 10000001, '2026-03-16 16:43:14', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36 Edg/145.0.0.0', 'Computer', 'Chrome 14', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (113, 10000001, '2026-03-16 20:21:18', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0', 'Computer', 'Chrome 14', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (114, 10000004, '2026-03-16 21:06:53', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (115, 10000001, '2026-03-16 22:35:09', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0', 'Computer', 'Chrome 14', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (116, 10000003, '2026-03-16 23:37:12', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (117, 10000001, '2026-03-17 09:02:31', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0', 'Computer', 'Chrome 14', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (118, 10000003, '2026-03-17 09:12:21', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (119, 10000001, '2026-03-17 11:13:37', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0', 'Computer', 'Chrome 14', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (120, 10000003, '2026-03-17 11:15:46', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (121, 10000003, '2026-03-17 11:41:56', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (122, 10000003, '2026-03-17 13:05:04', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (123, 10000001, '2026-03-17 13:56:31', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0', 'Computer', 'Chrome 14', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (124, 10000001, '2026-03-17 15:22:51', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'Computer', 'Chrome 13', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (125, 10000001, '2026-03-17 15:56:45', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0', 'Computer', 'Chrome 14', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (126, 10000004, '2026-03-17 16:34:34', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (127, 10000004, '2026-03-17 16:35:56', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (128, 10000004, '2026-03-17 16:37:01', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (129, 10000004, '2026-03-17 16:37:39', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (130, 10000004, '2026-03-17 16:38:45', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (131, 10000003, '2026-03-17 16:39:02', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (132, 10000003, '2026-03-17 16:45:29', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (133, 10000003, '2026-03-17 16:46:34', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (134, 10000003, '2026-03-17 16:50:37', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (135, 10000003, '2026-03-17 16:56:20', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (136, 10000003, '2026-03-17 17:25:06', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (137, 10000003, '2026-03-17 17:26:14', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (138, 10000001, '2026-03-17 17:27:47', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0', 'Computer', 'Chrome 14', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (139, 10000003, '2026-03-17 17:28:23', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (140, 10000003, '2026-03-17 17:32:29', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (141, 10000003, '2026-03-17 17:35:56', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (142, 10000003, '2026-03-17 17:36:11', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (143, 10000003, '2026-03-17 17:40:54', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (144, 10000003, '2026-03-17 17:46:05', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (145, 10000003, '2026-03-17 18:10:00', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (146, 10000003, '2026-03-17 18:10:13', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (147, 10000003, '2026-03-17 18:23:37', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (148, 10000003, '2026-03-17 18:32:54', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (149, 10000003, '2026-03-17 18:33:20', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (150, 10000003, '2026-03-17 18:35:56', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (151, 10000003, '2026-03-17 18:36:23', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (152, 10000003, '2026-03-17 18:38:55', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (153, 10000003, '2026-03-17 18:39:10', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (154, 10000003, '2026-03-17 18:41:13', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (155, 10000003, '2026-03-17 18:46:17', '0:0:0:0:0:0:0:1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (156, 10000003, '2026-03-17 18:50:01', '0:0:0:0:0:0:0:1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (157, 10000003, '2026-03-17 19:20:35', '0:0:0:0:0:0:0:1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (159, 10000001, '2026-03-17 19:28:12', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0', 'Computer', 'Chrome 14', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (160, 10000002, '2026-03-17 19:40:50', '0:0:0:0:0:0:0:1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0', 'PC', 'Edge', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (161, 10000004, '2026-03-17 20:05:01', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (162, 10000004, '2026-03-17 20:23:34', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (163, 10000004, '2026-03-17 20:23:57', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (164, 10000004, '2026-03-17 20:43:38', '0:0:0:0:0:0:0:1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (165, 10000004, '2026-03-17 21:13:31', '0:0:0:0:0:0:0:1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', 'PC', 'Chrome', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (166, 10000001, '2026-03-17 21:28:48', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0', 'Computer', 'Chrome 14', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (167, 10000001, '2026-03-17 23:57:08', '127.0.0.1', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0', 'Computer', 'Chrome 14', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (168, 10000001, '2026-03-17 16:34:14', '117.154.105.11', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0', 'Computer', 'Chrome 14', 'Windows 10');
INSERT INTO `user_login_logs` VALUES (169, 10000001, '2026-03-17 16:55:41', '223.104.122.125', 1, NULL, 'Mozilla/5.0 (Linux; Android 16; V2304A) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.200 Mobile Safari/537.36 VivoBrowser/28.2.0.0', 'Mobile', 'Chrome Mobile', 'Android 1.x');
INSERT INTO `user_login_logs` VALUES (170, 10000001, '2026-03-18 00:58:01', '219.140.86.90', 1, NULL, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0', 'Computer', 'Chrome 14', 'Windows 10');

-- ----------------------------
-- Table structure for user_sessions
-- ----------------------------
DROP TABLE IF EXISTS `user_sessions`;
CREATE TABLE `user_sessions`  (
  `session_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话唯一ID',
  `user_id` int UNSIGNED NOT NULL COMMENT '用户ID',
  `token` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `refresh_token` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '会话创建时间',
  `expires_at` timestamp NOT NULL COMMENT '会话过期时间',
  `last_activity_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后活动时间',
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录IP地址',
  `user_agent` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '用户代理字符串',
  `device_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '设备唯一标识',
  `is_active` tinyint UNSIGNED NOT NULL DEFAULT 1 COMMENT '是否活跃：0=已失效，1=活跃中',
  PRIMARY KEY (`session_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE COMMENT '用户ID索引',
  INDEX `idx_expires_at`(`expires_at`) USING BTREE COMMENT '过期时间索引',
  INDEX `idx_is_active`(`is_active`) USING BTREE COMMENT '活跃状态索引',
  INDEX `idx_user_active`(`user_id`, `is_active`) USING BTREE COMMENT '用户和活跃状态复合索引',
  CONSTRAINT `fk_session_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会话表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_sessions
-- ----------------------------
INSERT INTO `user_sessions` VALUES ('009e9fa33cd84df5a0d11c424ecf21fb', 10000004, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDQiLCJ1c2VybmFtZSI6InNoaXpoZXl1YW4iLCJpYXQiOjE3NzM3MzY3MjQsImV4cCI6MTc3Mzc0MzkyNH0.m3N5hosO6KUCq8WrjnFR5GN7KQ22AdjiyVS2L7yab6g', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDQiLCJ1c2VybmFtZSI6InNoaXpoZXl1YW4iLCJpYXQiOjE3NzM3MzY3MjQsImV4cCI6MTc3Mzc4NzEyNH0.uKTwI9VCI9F1X3Q2kKNXH8ewnOuhpbpieD0oFPVP1zg', '2026-03-17 16:38:45', '2026-03-17 18:38:45', '2026-03-17 16:44:13', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', NULL, 0);
INSERT INTO `user_sessions` VALUES ('025cbecf5d8c4ea498edbad37d7cb2f3', 10000003, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3Mzk1NzQsImV4cCI6MTc3Mzc0Njc3NH0.sqZG0pILCBYUGvu7vpbwfbvzCjLIRF70S_NwP9esGtU', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3Mzk1NzQsImV4cCI6MTc3Mzc4OTk3NH0.14Udq32RtOycjde-qUOOUdoWyadoR07Ymm-99P_6pRI', '2026-03-17 17:26:14', '2026-03-17 19:26:14', '2026-03-17 17:26:18', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', NULL, 0);
INSERT INTO `user_sessions` VALUES ('0778714fe5bd429bacedc9d5480edfc1', 10000003, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDMwMTcsImV4cCI6MTc3Mzc1MDIxN30.2jvxJInVht-9KZn2pnp1-Ete9DXWjfZWYYDcmqiMGa8', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDMwMTcsImV4cCI6MTc3Mzc5MzQxN30.6WKBAHG_L5POCYp1QHmt2YeWSGnFUuUsV-nxWuOGsWo', '2026-03-17 18:23:37', '2026-03-17 20:23:37', '2026-03-17 18:28:43', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', NULL, 0);
INSERT INTO `user_sessions` VALUES ('08808e50f40240e7b2fe9b1262cb0446', 10000003, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3Mzc0MzYsImV4cCI6MTc3Mzc0NDYzNn0.oCFNPQsW2QTKF0WqFDe4ADZuYXjeneyHKPlHq6aj_qU', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3Mzc0MzYsImV4cCI6MTc3Mzc4NzgzNn0.eMrKUcsphft0M2FDVo6WDwBRr5IAqAH2r_cBps5wkjg', '2026-03-17 16:50:37', '2026-03-17 18:50:37', '2026-03-17 16:56:15', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', NULL, 0);
INSERT INTO `user_sessions` VALUES ('1398dd85817046b58ca49198a04706d8', 10000004, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDQiLCJ1c2VybmFtZSI6InNoaXpoZXl1YW4iLCJpYXQiOjE3NzM3NDkxMDAsImV4cCI6MTc3Mzc1NjMwMH0.dZkoNCUQCgbEPa45DPeCkbF4hyF7sdi-HfClYCzz6zk', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDQiLCJ1c2VybmFtZSI6InNoaXpoZXl1YW4iLCJpYXQiOjE3NzM3NDkxMDAsImV4cCI6MTc3Mzc5OTUwMH0.qx419JcX87e8Incs-CRW_Xvg-_-BeOyCT-vQLt2hmJk', '2026-03-17 20:05:01', '2026-03-17 22:05:01', '2026-03-17 20:23:18', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', NULL, 0);
INSERT INTO `user_sessions` VALUES ('1ad0e083d4cc4158aee9cab196965730', 10000003, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDM5NDksImV4cCI6MTc3Mzc1MTE0OX0.RZHtIDZ1wAqNumLZ3ENkXF8RA18vhMP-3X9an-rjHlc', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDM5NDksImV4cCI6MTc3Mzc5NDM0OX0.4fgQ6lxG3R4clZTqkX8OJJSVFf7ggytcgeCUoRaxxhs', '2026-03-17 18:39:10', '2026-03-17 20:39:10', '2026-03-17 18:39:10', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', NULL, 0);
INSERT INTO `user_sessions` VALUES ('1c0fe74e02bf4263aee64dcc4d26d8f9', 10000003, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3Mzc3ODAsImV4cCI6MTc3Mzc0NDk4MH0.hhFH6tKePs69zpeRsDTPSvxN8MJwhFr6l4US8X5AHdE', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3Mzc3ODAsImV4cCI6MTc3Mzc4ODE4MH0.6D3bCaOCQCXykJs_ifeBFWbds067gWaQ0oJpvow6s4k', '2026-03-17 16:56:20', '2026-03-17 18:56:20', '2026-03-17 16:56:22', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', NULL, 0);
INSERT INTO `user_sessions` VALUES ('1fa43b2803024cdd85db0f91f6963a3b', 10000003, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3MjM5MDQsImV4cCI6MTc3MzczMTEwNH0.qi5Sq5JFcMXCwCQX0apbHPDYBCWTUIF0xGUj38sGW-U', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3MjM5MDQsImV4cCI6MTc3Mzc3NDMwNH0.cIRcRSbWMRZxndf_ScWkuP6lmqAC3ujE8aX8iFwGYI4', '2026-03-17 13:05:04', '2026-03-17 15:05:04', '2026-03-17 15:04:16', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', NULL, 0);
INSERT INTO `user_sessions` VALUES ('24d96160def94ed1acd239ba436bc645', 10000003, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDQwNzMsImV4cCI6MTc3Mzc1MTI3M30.D2FWWPFP6wdOQtNdMdKhTaO3071H9ZPxywhDlJqIRdE', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDQwNzMsImV4cCI6MTc3Mzc5NDQ3M30.XKFbgKcoDcYdrGDGOufpyq6mSTZ9reCPQmNBZ-ocAk8', '2026-03-17 18:41:13', '2026-03-17 20:41:13', '2026-03-17 18:41:13', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', NULL, 0);
INSERT INTO `user_sessions` VALUES ('29b25b1499c44f66be1b32e05138aa58', 10000003, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDAxNzAsImV4cCI6MTc3Mzc0NzM3MH0.dUEHUHaONO79d4aweaKxXLQbyP8PxyhBFHjGfQfU1iI', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDAxNzAsImV4cCI6MTc3Mzc5MDU3MH0.kS4TJDcTwfrHFnyfWOkSMy8SHzaJoH5Bx5bIkHLphks', '2026-03-17 17:36:11', '2026-03-17 19:36:11', '2026-03-17 17:36:11', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', NULL, 0);
INSERT INTO `user_sessions` VALUES ('2b3d09806f5d49de860daf1deb8db4c2', 10000003, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3MzcxOTMsImV4cCI6MTc3Mzc0NDM5M30.gFJepZMbptf70FDlcr8qOP72_rT7Q0YeV6jJSWjqHaU', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3MzcxOTMsImV4cCI6MTc3Mzc4NzU5M30.b4jHArIOUtG7yJYqHiNYRz-u3JW9-KmDRXCRyJbWreU', '2026-03-17 16:46:34', '2026-03-17 18:46:34', '2026-03-17 16:50:11', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', NULL, 0);
INSERT INTO `user_sessions` VALUES ('2c6e0087f15e4325af514e9a553af528', 10000003, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDM5MzUsImV4cCI6MTc3Mzc1MTEzNX0.gbnoh35vJWKYkZw6B7hdAfJ_ptDRnVumwb91xwb2JQw', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDM5MzUsImV4cCI6MTc3Mzc5NDMzNX0.xG-uZwhTmSKLn_fSjBLQQvYSgQg3EMSuA77DbsBtSyI', '2026-03-17 18:38:55', '2026-03-17 20:38:55', '2026-03-17 18:38:55', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', NULL, 0);
INSERT INTO `user_sessions` VALUES ('2cab165f74e94243ac26da8b695cd6e1', 10000001, 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJUeXBlIjoxLCJ0b2tlblR5cGUiOiJhY2Nlc3MiLCJleHAiOjE3NzM3MjQ0MTYsInVzZXJJZCI6MTAwMDAwMDEsImlhdCI6MTc3MzcxNzIxNiwidXNlcm5hbWUiOiJhZG1pbiJ9.2zM14jx_BVOzvB3HEPd4PPA31gNL0koYg9zTYjNyMMHJXwCwoddPsJP9jUOTbEsHC5xDOLuc9B-K414QrhjbhA', 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInRva2VuVHlwZSI6InJlZnJlc2giLCJleHAiOjE3NzM4MDM2MTYsInVzZXJJZCI6MTAwMDAwMDEsImlhdCI6MTc3MzcxNzIxNiwidXNlcm5hbWUiOiJhZG1pbiJ9.BX6EcwXfUlnkEH7bX59ArvUFZ0S8zyXDwDu6AXvfJM7MH2NLkvd8xqGimE-qPIA8omngAy4xqW-5pBondM3QPQ', '2026-03-17 11:13:37', '2026-03-17 13:13:37', '2026-03-17 11:13:37', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0', NULL, 0);
INSERT INTO `user_sessions` VALUES ('37ca486eb25747d7810e2ef6ac23e53c', 10000004, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDQiLCJ1c2VybmFtZSI6InNoaXpoZXl1YW4iLCJpYXQiOjE3NzM3MzY0NzQsImV4cCI6MTc3Mzc0MzY3NH0.bmaOgExYJAquSluuiP5Mi21Ry_W4nLMjdQE_0WlPt6I', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDQiLCJ1c2VybmFtZSI6InNoaXpoZXl1YW4iLCJpYXQiOjE3NzM3MzY0NzQsImV4cCI6MTc3Mzc4Njg3NH0.tqSuU6nxGTDhNfUf6n4gJAPkfavCl0C5RWm8RPMzmU4', '2026-03-17 16:34:35', '2026-03-17 18:34:35', '2026-03-17 16:35:43', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', NULL, 0);
INSERT INTO `user_sessions` VALUES ('3804c813e8374acd81601d524b7441ad', 10000004, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDQiLCJ1c2VybmFtZSI6InNoaXpoZXl1YW4iLCJpYXQiOjE3NzM3MzY2NTksImV4cCI6MTc3Mzc0Mzg1OX0.Ski1imveLxuuH92IFqOf9NxWDdY-rENytPk_4mvl3d4', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDQiLCJ1c2VybmFtZSI6InNoaXpoZXl1YW4iLCJpYXQiOjE3NzM3MzY2NTksImV4cCI6MTc3Mzc4NzA1OX0.vA_fuQiWC_dAZDEQR0JlopWHgz0n9Y7ShdimIIAyPEc', '2026-03-17 16:37:39', '2026-03-17 18:37:39', '2026-03-17 16:38:43', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', NULL, 0);
INSERT INTO `user_sessions` VALUES ('4173273cee174d0991ba53706ad490f8', 10000004, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDQiLCJ1c2VybmFtZSI6InNoaXpoZXl1YW4iLCJpYXQiOjE3NzM3NTAyMTQsImV4cCI6MTc3Mzc1NzQxNH0.5jfhG5upKiWKstnUVZrzUE-1-WmA50A_jhTQuVM90CQ', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDQiLCJ1c2VybmFtZSI6InNoaXpoZXl1YW4iLCJpYXQiOjE3NzM3NTAyMTQsImV4cCI6MTc3MzgwMDYxNH0.iFjzqCdEewKNLmJbqgIgB-OBf0ulcnIUa9NxbAN-B2k', '2026-03-17 20:23:34', '2026-03-17 22:23:34', '2026-03-17 20:23:48', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', NULL, 0);
INSERT INTO `user_sessions` VALUES ('41b57f6d7dff4e77ac6d1cb1076082ed', 10000001, 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJUeXBlIjoxLCJ0b2tlblR5cGUiOiJhY2Nlc3MiLCJleHAiOjE3NzM3MzQxOTAsInVzZXJJZCI6MTAwMDAwMDEsImlhdCI6MTc3MzcyNjk5MCwidXNlcm5hbWUiOiJhZG1pbiJ9.zOcvasrjDnIY2JfpgPoiDf4ta5IyN2oWCVDKKOkpojOk7rxAxey1RkW3jbuL0V_Bh5JUIXuW5IS9DBJnU0CjZA', 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInRva2VuVHlwZSI6InJlZnJlc2giLCJleHAiOjE3NzM4MTMzOTAsInVzZXJJZCI6MTAwMDAwMDEsImlhdCI6MTc3MzcyNjk5MCwidXNlcm5hbWUiOiJhZG1pbiJ9.O_MKh026JYFiXesvLosgMJJZ45ZVy_s7kP2CYrJJyzYwAwNSoLArV9LfEira11G0k_XSDBnwWWnpj_Dtcxzlqg', '2026-03-17 13:56:31', '2026-03-17 15:56:31', '2026-03-17 13:56:31', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0', NULL, 0);
INSERT INTO `user_sessions` VALUES ('425ccdffa33f4ede95374a7a267c5ba3', 10000003, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3MTg5MTUsImV4cCI6MTc3MzcyNjExNX0.vIz_HFPNgYhumsEp0EosjY7UsOGxGk-9_B1eOrJes1o', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3MTg5MTUsImV4cCI6MTc3Mzc2OTMxNX0._FGJFB2PewcddP7S9Pl2hRVloTb5wYeGyHdGZP7Q83o', '2026-03-17 11:41:56', '2026-03-17 13:41:56', '2026-03-17 12:51:42', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', NULL, 0);
INSERT INTO `user_sessions` VALUES ('4f9c5f9b469942598354d804af30a904', 10000003, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDQzNzYsImV4cCI6MTc3Mzc1MTU3Nn0.n5OY-oNiuGtrsbkxMOvA4pT6djzM6Ua9cqk-gFHd2Yg', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDQzNzYsImV4cCI6MTc3Mzc5NDc3Nn0._9wmThG6AtzIBdGp5nrqJQlp0HcriHTyu7XXeLN-dRw', '2026-03-17 18:46:17', '2026-03-17 20:46:17', '2026-03-17 18:46:17', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', NULL, 0);
INSERT INTO `user_sessions` VALUES ('60244514dc504194bb28f6b58021e868', 10000002, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDIiLCJ1c2VybmFtZSI6ImNoZW5waW5neWUiLCJpYXQiOjE3NzM3NDc2NTAsImV4cCI6MTc3Mzc1NDg1MH0.iAyB0u0g_p9EnCptJsN1fd35b07NveMC8vxOZtw3RaE', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDIiLCJ1c2VybmFtZSI6ImNoZW5waW5neWUiLCJpYXQiOjE3NzM3NDc2NTAsImV4cCI6MTc3Mzc5ODA1MH0.mtfu3nOtK4xrztqWvy2LL6odTmnXs4e24E55cxtBbpc', '2026-03-17 19:40:51', '2026-03-17 21:40:51', '2026-03-17 19:46:15', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0', NULL, 0);
INSERT INTO `user_sessions` VALUES ('6238ba9e539347b9b9f06914a13b378a', 10000001, 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJUeXBlIjoxLCJ0b2tlblR5cGUiOiJhY2Nlc3MiLCJleHAiOjE3NzM3NjEzMjgsInVzZXJJZCI6MTAwMDAwMDEsImlhdCI6MTc3Mzc1NDEyOCwidXNlcm5hbWUiOiJhZG1pbiJ9.PHQIDU95CRWkzjEqbWpXxrLUh4coQnWdp4z235zi7ZtZrLgNXiyyIa5Vvu8PhGqc0bkT61I1EX86ftXBLrpESg', 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInRva2VuVHlwZSI6InJlZnJlc2giLCJleHAiOjE3NzM4NDA1MjgsInVzZXJJZCI6MTAwMDAwMDEsImlhdCI6MTc3Mzc1NDEyOCwidXNlcm5hbWUiOiJhZG1pbiJ9.wdF0_FEiBkuvKi86vXtLIltkAEJl5NKrKYzVb4g2vqWH3sXTKMK-gvFMvdFqQb9lrBN0iOB_znTf4AToOA-pzg', '2026-03-17 21:28:48', '2026-03-17 23:28:48', '2026-03-17 21:28:48', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0', NULL, 1);
INSERT INTO `user_sessions` VALUES ('6681527485534295af8013d7ccbc8a27', 10000003, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDA0NTMsImV4cCI6MTc3Mzc0NzY1M30.E_7qsGjuMVHQyTTznzsv5z-bMjCuggE9ucBXMA8-r6Y', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDA0NTMsImV4cCI6MTc3Mzc5MDg1M30.8g2twYLSCR9Fg8vj1Nif_YSqPAIsmfJk1skcYjJriEM', '2026-03-17 17:40:54', '2026-03-17 19:40:54', '2026-03-17 17:45:56', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', NULL, 0);
INSERT INTO `user_sessions` VALUES ('6db9e696a5b244848c0d67ba420ebdcb', 10000003, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDY0MzQsImV4cCI6MTc3Mzc1MzYzNH0.LJBMrUl-6YxkBojLHrWVFXMVnaVBUD2Te7gThlXKq6E', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDY0MzQsImV4cCI6MTc3Mzc5NjgzNH0.CcXvjZgQBO3_fM4jjcy0VbILIpK16_p1uoN_dwGTzL0', '2026-03-17 19:20:35', '2026-03-17 21:20:35', '2026-03-17 20:24:34', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', NULL, 0);
INSERT INTO `user_sessions` VALUES ('6ebebb0efcaf4139af609ab2e57697e1', 10000004, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDQiLCJ1c2VybmFtZSI6InNoaXpoZXl1YW4iLCJpYXQiOjE3NzM3MzY1NTcsImV4cCI6MTc3Mzc0Mzc1N30.6LqQpuBz6SNKl7cZCm75l-mzQKhk954SQR4fPco8eRk', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDQiLCJ1c2VybmFtZSI6InNoaXpoZXl1YW4iLCJpYXQiOjE3NzM3MzY1NTcsImV4cCI6MTc3Mzc4Njk1N30.Jj7GiIrEjOCNWOR6PFgFWJlsDcRGJ67y0aGagPyieIY', '2026-03-17 16:35:57', '2026-03-17 18:35:57', '2026-03-17 16:36:51', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', NULL, 0);
INSERT INTO `user_sessions` VALUES ('735e574f9916475ead0023848f93f6fd', 10000003, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDIyMDAsImV4cCI6MTc3Mzc0OTQwMH0.6apDZlClOtAvcveUc1jhAw3tQNbYp0Spa_BVvF4mj6Q', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDIyMDAsImV4cCI6MTc3Mzc5MjYwMH0.59yg_pOj-0MpFkKYW4dR9_gOqYZtDe1JoNzebxC5jY0', '2026-03-17 18:10:00', '2026-03-17 20:10:00', '2026-03-17 18:10:00', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', NULL, 0);
INSERT INTO `user_sessions` VALUES ('7d6e8564c8b34a3ebf32bf474fe982da', 10000001, 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJUeXBlIjoxLCJ0b2tlblR5cGUiOiJhY2Nlc3MiLCJleHAiOjE3NzM4MDI2ODEsInVzZXJJZCI6MTAwMDAwMDEsImlhdCI6MTc3Mzc5NTQ4MSwidXNlcm5hbWUiOiJhZG1pbiJ9.WiAaP5Iz8NPXOMvdakbHS8grU3HjA3Qhl4NeZSCAFivupt_1PCEg-D8knWOBFTqjvL5M5SXfq6CJEr4qfOiayg', 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInRva2VuVHlwZSI6InJlZnJlc2giLCJleHAiOjE3NzM4ODE4ODEsInVzZXJJZCI6MTAwMDAwMDEsImlhdCI6MTc3Mzc5NTQ4MSwidXNlcm5hbWUiOiJhZG1pbiJ9.djeM0WagEVWQTjtmnKFkW_3rDPanpX98xYpLpSuwl8sV3JaJ_YLCGZjzq5T55CxMD-Kcf-VEBwdOKeV_ZjIMOA', '2026-03-18 00:58:01', '2026-03-18 02:58:01', '2026-03-18 00:58:01', '219.140.86.90', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0', NULL, 1);
INSERT INTO `user_sessions` VALUES ('842158b8c6f64403a0bf0d7ac564e846', 10000003, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDA3NjQsImV4cCI6MTc3Mzc0Nzk2NH0.LI5w39fvDOkaZbKzhi7kY4n6B-TPtbfg876v4vMmnzE', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDA3NjQsImV4cCI6MTc3Mzc5MTE2NH0.o4BFTixI_Zei8swS_GYs5AKcNdqktToK4mVTKXLsfzU', '2026-03-17 17:46:05', '2026-03-17 19:46:05', '2026-03-17 18:06:16', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', NULL, 0);
INSERT INTO `user_sessions` VALUES ('8730d3d26e534b3dbbf7a5191a062c7f', 10000003, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDM3ODIsImV4cCI6MTc3Mzc1MDk4Mn0.KIuJMpON8RARYF3OskjmHkG7pPKjX406KI1RggJldwg', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDM3ODIsImV4cCI6MTc3Mzc5NDE4Mn0.Di2dMBJYXENr13JTnhVg6pf_BmtN-yLAlvUVr6M6tVM', '2026-03-17 18:36:23', '2026-03-17 20:36:23', '2026-03-17 18:36:23', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', NULL, 0);
INSERT INTO `user_sessions` VALUES ('87aa5ce190d64b6aa5cc2e532d713f14', 10000001, 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJUeXBlIjoxLCJ0b2tlblR5cGUiOiJhY2Nlc3MiLCJleHAiOjE3NzM3MzkzNzEsInVzZXJJZCI6MTAwMDAwMDEsImlhdCI6MTc3MzczMjE3MSwidXNlcm5hbWUiOiJhZG1pbiJ9.v3JTtRDqkh0JMBS0Lm-ZZQYJkeutAY1gaeMLUVxDY4pjGJJ322vYxhB53W2dys7Dld7J1ok4L5Zx58OVDSbrKw', 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInRva2VuVHlwZSI6InJlZnJlc2giLCJleHAiOjE3NzM4MTg1NzEsInVzZXJJZCI6MTAwMDAwMDEsImlhdCI6MTc3MzczMjE3MSwidXNlcm5hbWUiOiJhZG1pbiJ9.xJOQqXeCfUPXxeojrtfeP3yMcB1H5E12akjuYCK8LZck2WpVWI8E5Wjyt-qZxIk6DJRgaf6uz2Lu0OEasQyUPg', '2026-03-17 15:22:52', '2026-03-17 17:22:52', '2026-03-17 15:22:52', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', NULL, 0);
INSERT INTO `user_sessions` VALUES ('8c412275a0b348bd8cb386451a35a5b2', 10000004, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDQiLCJ1c2VybmFtZSI6InNoaXpoZXl1YW4iLCJpYXQiOjE3NzM3MzY2MjEsImV4cCI6MTc3Mzc0MzgyMX0.DImWXJ9IyvWljeOMQhCOCHN2ZNSaftSFEEQnLYEyZ7s', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDQiLCJ1c2VybmFtZSI6InNoaXpoZXl1YW4iLCJpYXQiOjE3NzM3MzY2MjEsImV4cCI6MTc3Mzc4NzAyMX0.d2cIOgpIssvMoa6iG9bFPayjyNtCaJSAzVjT1g7DoeM', '2026-03-17 16:37:02', '2026-03-17 18:37:02', '2026-03-17 16:37:31', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', NULL, 0);
INSERT INTO `user_sessions` VALUES ('8fdef09d4ea4490f80b2e31a5a7240ba', 10000003, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3Mzk1MDUsImV4cCI6MTc3Mzc0NjcwNX0.fSqhUzWa_s5va5NlLXgOBLBTXv85Xfiv3Ut7maMqHys', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3Mzk1MDUsImV4cCI6MTc3Mzc4OTkwNX0.i1XrUStkpkGYDGyOM0nZaoIDOulLv9cS3FxpTEB3xIs', '2026-03-17 17:25:06', '2026-03-17 19:25:06', '2026-03-17 17:25:54', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', NULL, 0);
INSERT INTO `user_sessions` VALUES ('92db555060ec440b9bdb5db2f83cbb95', 10000003, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3MTczNDUsImV4cCI6MTc3MzcyNDU0NX0.Fg_lxmO0UVOx1uiadXTkYspmb9I31YqNZOWy7-EHQz4', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3MTczNDUsImV4cCI6MTc3Mzc2Nzc0NX0.XSxf3JrGYHPbMqTQzUF_Q41VuEmrkuhisVh6Zxfzujc', '2026-03-17 11:15:46', '2026-03-17 13:15:46', '2026-03-17 11:41:43', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', NULL, 0);
INSERT INTO `user_sessions` VALUES ('9b0667421eb34c10865cb6ec6981215b', 10000001, 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJUeXBlIjoxLCJ0b2tlblR5cGUiOiJhY2Nlc3MiLCJleHAiOjE3NzM3NzI0NTQsInVzZXJJZCI6MTAwMDAwMDEsImlhdCI6MTc3Mzc2NTI1NCwidXNlcm5hbWUiOiJhZG1pbiJ9.zv2F1J4Z-5Mbi1KOJMe-SP0PC2bURr-G0zkdEPdCiXhwhL7N40R3bLkOTKPNEfS-q-S3kJq7XpIm0W3j9DoKtQ', 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInRva2VuVHlwZSI6InJlZnJlc2giLCJleHAiOjE3NzM4NTE2NTQsInVzZXJJZCI6MTAwMDAwMDEsImlhdCI6MTc3Mzc2NTI1NCwidXNlcm5hbWUiOiJhZG1pbiJ9.yokNwjyYtki-CpLZyV7Nm8ONBUovJkvsp3MvqjUavpJLsKMW0kPoLLVl3Hb6RCcNxL0S-WZaDid8s9CAtbEiNg', '2026-03-17 16:34:15', '2026-03-17 18:34:15', '2026-03-17 16:34:15', '117.154.105.11', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0', NULL, 1);
INSERT INTO `user_sessions` VALUES ('9e5f8d6ace024c4cb857ce8001dbc66f', 10000003, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDM2MDAsImV4cCI6MTc3Mzc1MDgwMH0.Gtg5j7-xBWD6u4GqjuijEXm5U1MC7CV2tv5K_iJND-4', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDM2MDAsImV4cCI6MTc3Mzc5NDAwMH0.j9txHv7kECysI3JcJtxDHAnxcQ_D4ZbHUbUJcjWVkyk', '2026-03-17 18:33:20', '2026-03-17 20:33:20', '2026-03-17 18:33:20', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', NULL, 0);
INSERT INTO `user_sessions` VALUES ('a238ff90526540c5bae8bd7c729f4140', 10000003, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3MzY3NDIsImV4cCI6MTc3Mzc0Mzk0Mn0.849NKRJWizkkkC1IKmF9FH1IpTxZQwtwRKbiCvFWJeE', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3MzY3NDIsImV4cCI6MTc3Mzc4NzE0Mn0.KvPqYK8wQgFqF-1InJk5bqV4HMqthWc8t3A53qjY4tY', '2026-03-17 16:39:02', '2026-03-17 18:39:02', '2026-03-17 16:45:18', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', NULL, 0);
INSERT INTO `user_sessions` VALUES ('a81f0926dc56483b85ffeee30c76e991', 10000003, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3MzcxMjgsImV4cCI6MTc3Mzc0NDMyOH0.YNto-2Hic-q0dRzfLURlhkaQAwqnC4GtAxvDdvG8hJw', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3MzcxMjgsImV4cCI6MTc3Mzc4NzUyOH0.vypa34X6Svm5Ya9mz0zFZEgkK2Ep2yia5YzxKgp4g04', '2026-03-17 16:45:29', '2026-03-17 18:45:29', '2026-03-17 16:46:28', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', NULL, 0);
INSERT INTO `user_sessions` VALUES ('add5d82908644ade865e274c74cce8ee', 10000004, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDQiLCJ1c2VybmFtZSI6InNoaXpoZXl1YW4iLCJpYXQiOjE3NzM3NTMyMTEsImV4cCI6MTc3Mzc2MDQxMX0.e1fp3G1B0Ee9vRb3B4KP74orajL_oApwuhzM5yDcEX4', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDQiLCJ1c2VybmFtZSI6InNoaXpoZXl1YW4iLCJpYXQiOjE3NzM3NTMyMTEsImV4cCI6MTc3MzgwMzYxMX0.hp9s3Sz-BYzWj5k44nrU7khN_gY1p_3UQqLZCFP-eyM', '2026-03-17 21:13:31', '2026-03-17 23:13:31', '2026-03-17 21:13:34', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', NULL, 0);
INSERT INTO `user_sessions` VALUES ('bc3ebd66b1c1430582bb2ad016e380e0', 10000001, 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJUeXBlIjoxLCJ0b2tlblR5cGUiOiJhY2Nlc3MiLCJleHAiOjE3NzM3NTQwOTEsInVzZXJJZCI6MTAwMDAwMDEsImlhdCI6MTc3Mzc0Njg5MSwidXNlcm5hbWUiOiJhZG1pbiJ9.jQvV5oFMtR2h6Uu4Af3-FMo4NLTsEBZyVH8GJQD7TOklBSJA9rudFQmaDK2buVExwKf0gCufLahAIfT2hziDyg', 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInRva2VuVHlwZSI6InJlZnJlc2giLCJleHAiOjE3NzM4MzMyOTEsInVzZXJJZCI6MTAwMDAwMDEsImlhdCI6MTc3Mzc0Njg5MSwidXNlcm5hbWUiOiJhZG1pbiJ9.IfMD-ZJSyHFpyadAFIste2PKWh05zdyvYXNO9iuCzGDNW7uS9YEsyH25VLRUj6m5av2F9nveWO_KQwv-vGLKPA', '2026-03-17 19:28:12', '2026-03-17 21:28:12', '2026-03-17 19:28:12', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0', NULL, 0);
INSERT INTO `user_sessions` VALUES ('beb81e6d700a46a185136033e82243d0', 10000003, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3Mzk5NDksImV4cCI6MTc3Mzc0NzE0OX0.rID6f_ADV_8kgcRM9dtjzLUFwkq7YGQ27a5smfSb1v4', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3Mzk5NDksImV4cCI6MTc3Mzc5MDM0OX0._Mf24IT7By_oO7lKA2iNs7TrTMWi7HDKXl9aWuhg98I', '2026-03-17 17:32:30', '2026-03-17 19:32:30', '2026-03-17 17:32:30', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', NULL, 0);
INSERT INTO `user_sessions` VALUES ('c056e4a9862140908fab3d3125248ff7', 10000004, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDQiLCJ1c2VybmFtZSI6InNoaXpoZXl1YW4iLCJpYXQiOjE3NzM3NTE0MTcsImV4cCI6MTc3Mzc1ODYxN30.dLlf9Jhi8NQIw90TjJBWxRUC2gDYtZGzfPbeCDljR-g', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDQiLCJ1c2VybmFtZSI6InNoaXpoZXl1YW4iLCJpYXQiOjE3NzM3NTE0MTcsImV4cCI6MTc3MzgwMTgxN30.0VP_q2UHA8iP1KqF2ItcAPC9PiwxZgojSUqUsci9dRA', '2026-03-17 20:43:38', '2026-03-17 22:43:38', '2026-03-17 21:13:25', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', NULL, 0);
INSERT INTO `user_sessions` VALUES ('c1948b400f33470eb6d661b35657f569', 10000003, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDIyMTIsImV4cCI6MTc3Mzc0OTQxMn0.HQ5d6JSfhRkRlQ7V01BWcYtGy4I3ZPNI1XO33FazvMA', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDIyMTIsImV4cCI6MTc3Mzc5MjYxMn0.fEDaXDZHH4sktfhVgdOkAgXGasaEiyyq24pnRnIvZg0', '2026-03-17 18:10:13', '2026-03-17 20:10:13', '2026-03-17 18:20:13', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', NULL, 0);
INSERT INTO `user_sessions` VALUES ('c518e6a1e6a6436b9dac7cd78545ad23', 10000004, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDQiLCJ1c2VybmFtZSI6InNoaXpoZXl1YW4iLCJpYXQiOjE3NzM3NTAyMzYsImV4cCI6MTc3Mzc1NzQzNn0.qUqyRcVI0e_qrRqxAUqMLHvaX5l28QC7a9LMWJPMG74', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDQiLCJ1c2VybmFtZSI6InNoaXpoZXl1YW4iLCJpYXQiOjE3NzM3NTAyMzYsImV4cCI6MTc3MzgwMDYzNn0.AWeR6BxrwNm5KVoGMY2AHn8Lo9kw6agDre-3FLi9NhY', '2026-03-17 20:23:57', '2026-03-17 22:23:57', '2026-03-17 20:28:58', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36', NULL, 0);
INSERT INTO `user_sessions` VALUES ('c649b66f2c3d42669d01c378485dc150', 10000003, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDM1NzQsImV4cCI6MTc3Mzc1MDc3NH0.oc4MkeZcXpjUeH57s5CD81qK5f9lHtAR7jdi5YLG8-Y', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDM1NzQsImV4cCI6MTc3Mzc5Mzk3NH0.VDzbhZ9VBWQHvn3MJ2k_82qn9ng4FIW7ktSv5BR7KMM', '2026-03-17 18:32:54', '2026-03-17 20:32:54', '2026-03-17 18:32:54', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', NULL, 0);
INSERT INTO `user_sessions` VALUES ('e31695c69e954c17bb85c6bae517011f', 10000003, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDQ2MDAsImV4cCI6MTc3Mzc1MTgwMH0.aUVE4csOMB1Zv9AUKgcfw2xiuuOqJk-hkfSGJ-T7RJQ', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDQ2MDAsImV4cCI6MTc3Mzc5NTAwMH0.hQ7VuN3iUvycGBL2zdFuL_XiBm6sEcQ6BF3f3zm2gis', '2026-03-17 18:50:01', '2026-03-17 20:50:01', '2026-03-17 19:15:31', '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', NULL, 0);
INSERT INTO `user_sessions` VALUES ('e375af160d73481bac2bb91cc2223b72', 10000001, 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJUeXBlIjoxLCJ0b2tlblR5cGUiOiJhY2Nlc3MiLCJleHAiOjE3NzM3NDE0MDUsInVzZXJJZCI6MTAwMDAwMDEsImlhdCI6MTc3MzczNDIwNSwidXNlcm5hbWUiOiJhZG1pbiJ9.kAcc4NazIimUhSvMrGXIpYLqaNJas5adIVpNDGK3hBgffii22L6ft1JEo0ya8JCOyt0p1KU6Aly9s7kj4xhSHA', 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInRva2VuVHlwZSI6InJlZnJlc2giLCJleHAiOjE3NzM4MjA2MDUsInVzZXJJZCI6MTAwMDAwMDEsImlhdCI6MTc3MzczNDIwNSwidXNlcm5hbWUiOiJhZG1pbiJ9.q2uaHvWMg7d0UXJxS4u30uTXtWlagVDqEnzfNibPSp74NO9XhoTiarnxmIRzNP4znaBmM2u5szCRiHTMUBxmiw', '2026-03-17 15:56:46', '2026-03-17 17:56:46', '2026-03-17 15:56:46', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0', NULL, 0);
INSERT INTO `user_sessions` VALUES ('e7be080922684643b9ae9b809f41d88f', 10000003, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDM3NTUsImV4cCI6MTc3Mzc1MDk1NX0.dZ0Uc4kpT-ciwmHQnAPrTHGspQ_7BmLHn4JG6msiAUw', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDM3NTUsImV4cCI6MTc3Mzc5NDE1NX0.5nDjEnXsAqv6dpuAe6Art4YEQ0z9oR5AbcCFxSmLQ_Y', '2026-03-17 18:35:56', '2026-03-17 20:35:56', '2026-03-17 18:35:56', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', NULL, 0);
INSERT INTO `user_sessions` VALUES ('ea4a7c2bbd6d4721b64f234148697e50', 10000001, 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJUeXBlIjoxLCJ0b2tlblR5cGUiOiJhY2Nlc3MiLCJleHAiOjE3NzM3NzM3NDEsInVzZXJJZCI6MTAwMDAwMDEsImlhdCI6MTc3Mzc2NjU0MSwidXNlcm5hbWUiOiJhZG1pbiJ9.T4nDPOkb-iPWICvaG0u4G45l1-zaVrbROymkuKA18utOWxC3Io52dzbWSNIODhBbmqvZY6zSse5bI7SP3GC3Ag', 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInRva2VuVHlwZSI6InJlZnJlc2giLCJleHAiOjE3NzM4NTI5NDEsInVzZXJJZCI6MTAwMDAwMDEsImlhdCI6MTc3Mzc2NjU0MSwidXNlcm5hbWUiOiJhZG1pbiJ9.ThpXmjaAibS0vTiDfRUBPDcvlbHVNDmpH9gaj9I6iPS_cSr1J7rmJ29DSuExTyw3A2TbKYlteaPrMnt5kZIwpw', '2026-03-17 16:55:42', '2026-03-17 18:55:42', '2026-03-17 16:55:42', '210.71.196.232', 'Mozilla/5.0 (Linux; Android 16; V2304A) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.200 Mobile Safari/537.36 VivoBrowser/28.2.0.0', NULL, 1);
INSERT INTO `user_sessions` VALUES ('eacc06a40dfe46c19c02f9e987712b06', 10000001, 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJUeXBlIjoxLCJ0b2tlblR5cGUiOiJhY2Nlc3MiLCJleHAiOjE3NzM3NDY4NjcsInVzZXJJZCI6MTAwMDAwMDEsImlhdCI6MTc3MzczOTY2NywidXNlcm5hbWUiOiJhZG1pbiJ9.JXZAU-YMEqMw-Ma1Tjpjs9Mkb5f0EpplZIDwLedE5mQyG4BdpsEjFIA9qNBfzLrWaIBy0mN-B2soNt0IxTJv8g', 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInRva2VuVHlwZSI6InJlZnJlc2giLCJleHAiOjE3NzM4MjYwNjcsInVzZXJJZCI6MTAwMDAwMDEsImlhdCI6MTc3MzczOTY2NywidXNlcm5hbWUiOiJhZG1pbiJ9.c4AmzDsGtSmcfQ_ElaVzoIs4Jt7ME5peIw2khdmlI9IjJ7ANxpA3p1kHzrIHXr-IZMqJElTZofx9ptXFnEETeQ', '2026-03-17 17:27:47', '2026-03-17 19:27:47', '2026-03-17 17:27:47', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0', NULL, 0);
INSERT INTO `user_sessions` VALUES ('ed1ad3eec3a44ee08730d9d71a356d89', 10000001, 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJUeXBlIjoxLCJ0b2tlblR5cGUiOiJhY2Nlc3MiLCJleHAiOjE3NzM3NzAyMjgsInVzZXJJZCI6MTAwMDAwMDEsImlhdCI6MTc3Mzc2MzAyOCwidXNlcm5hbWUiOiJhZG1pbiJ9.zCCDmewRKKsOF233ncq_1L8cKB09ZarkH_x_SLLh9YSMZHieRVRna73hHXONXOoPoK6x3UjH51ZQZMb6K0M_GA', 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInRva2VuVHlwZSI6InJlZnJlc2giLCJleHAiOjE3NzM4NDk0MjgsInVzZXJJZCI6MTAwMDAwMDEsImlhdCI6MTc3Mzc2MzAyOCwidXNlcm5hbWUiOiJhZG1pbiJ9.eIPhrHgkV4J7CXHDnc3Eyt6E-6W34yCXL24Mir0IKtWiwIWWYySyeksB2zXCkIyDzxPgZ0UiZ54nVpZt9q7Tng', '2026-03-17 23:57:08', '2026-03-18 01:57:08', '2026-03-17 23:57:08', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/146.0.0.0 Safari/537.36 Edg/146.0.0.0', NULL, 1);
INSERT INTO `user_sessions` VALUES ('ee5f5f5e7a3f4c08b0983e85991fb71b', 10000003, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDAxNTYsImV4cCI6MTc3Mzc0NzM1Nn0.BCS6vcT2PvSVf-8e6ydDQ26iDnJBUTZOkjj1YuZAizg', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3NDAxNTYsImV4cCI6MTc3Mzc5MDU1Nn0.vkS-bxWuW5WOsRd8s3o81Y-2zxIMZecZsDRyCcO7w64', '2026-03-17 17:35:56', '2026-03-17 19:35:56', '2026-03-17 17:35:56', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', NULL, 0);
INSERT INTO `user_sessions` VALUES ('faa2c166a1de45ae9ef51b2c195b4a7d', 10000003, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6MiwidG9rZW5UeXBlIjoiYWNjZXNzIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3Mzk3MDIsImV4cCI6MTc3Mzc0NjkwMn0.Q5fxbi-mACKB3-MWfvsOm0WC1A5rtodBZLBaKHaoIls', 'eyJhbGciOiJIUzI1NiJ9.eyJ0b2tlblR5cGUiOiJyZWZyZXNoIiwidXNlcklkIjoiMTAwMDAwMDMiLCJ1c2VybmFtZSI6Inlhbmd0aWFuYm8iLCJpYXQiOjE3NzM3Mzk3MDIsImV4cCI6MTc3Mzc5MDEwMn0.y-DScLECvbtT6qYPSHv6YJDkokB6VFxgskU1cHlPrPY', '2026-03-17 17:28:23', '2026-03-17 19:28:23', '2026-03-17 17:32:08', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/141.0.0.0 Safari/537.36 SLBrowser/9.0.7.12231 SLBChan/111 SLBVPV/64-bit', NULL, 0);

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `user_id` int UNSIGNED NOT NULL COMMENT '用户唯一ID，8位数字（10000000-99999999）',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名，用于登录，3-50字符',
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码哈希值（bcrypt加密）',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '邮箱地址',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号码',
  `user_type` tinyint UNSIGNED NOT NULL DEFAULT 2 COMMENT '用户类型：1=管理员，2=普通用户',
  `status` tinyint UNSIGNED NOT NULL DEFAULT 1 COMMENT '账户状态：0=禁用，1=正常，2=锁定',
  `credit_score` tinyint UNSIGNED NOT NULL DEFAULT 80 COMMENT '用户信誉值（1-100，默认80）',
  `is_banned` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '是否被封禁：0=正常，1=已封禁',
  `banned_until` timestamp NULL DEFAULT NULL COMMENT '封禁截止时间',
  `ban_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封禁原因',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `avatar_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL地址（OSS存储）',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户地址（详细地址信息）',
  `school` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '学校名称（用于校园交易场景）',
  `gender` tinyint UNSIGNED NULL DEFAULT NULL COMMENT '性别：0=未知，1=男，2=女',
  `birthday` date NULL DEFAULT NULL COMMENT '出生日期',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '账户创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `last_login_at` timestamp NULL DEFAULT NULL COMMENT '最后登录时间',
  `login_attempts` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
  `locked_until` timestamp NULL DEFAULT NULL COMMENT '账户锁定截止时间',
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT '软删除时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE COMMENT '用户名唯一索引',
  UNIQUE INDEX `uk_email`(`email`) USING BTREE COMMENT '邮箱唯一索引',
  UNIQUE INDEX `uk_phone`(`phone`) USING BTREE COMMENT '手机号唯一索引',
  INDEX `idx_user_type`(`user_type`) USING BTREE COMMENT '用户类型索引',
  INDEX `idx_status`(`status`) USING BTREE COMMENT '账户状态索引',
  INDEX `idx_school`(`school`) USING BTREE COMMENT '学校索引',
  INDEX `idx_created_at`(`created_at`) USING BTREE COMMENT '创建时间索引',
  INDEX `idx_deleted_at`(`deleted_at`) USING BTREE COMMENT '软删除索引',
  INDEX `idx_credit_score`(`credit_score`) USING BTREE COMMENT '信誉值索引',
  INDEX `idx_is_banned`(`is_banned`) USING BTREE COMMENT '封禁状态索引',
  INDEX `idx_type_status`(`user_type`, `status`) USING BTREE COMMENT '类型和状态复合索引',
  INDEX `idx_status_deleted`(`status`, `deleted_at`) USING BTREE COMMENT '状态和删除时间复合索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (10000001, 'admin', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'admin@campus-exchange.com', NULL, 1, 1, 80, 0, NULL, NULL, '系统管理员', 'https://2638224627-1355158021.cos.ap-guangzhou.myqcloud.com/avatar/10000001/3c08c049293348ff91df39a0657c98cc.jpg', '湖北省', '武汉工程大学', 1, NULL, '2026-03-12 13:39:56', '2026-03-18 00:58:01', '2026-03-18 00:58:01', 0, NULL, NULL);
INSERT INTO `users` VALUES (10000002, 'chenpingye', '$2a$10$kSMNynSZleakyYHudFF/qO96Eewrl0moEUJh.0mQtFFnSb4dlNLtO', 'abc402qua@winvvv.com', '15605060708', 2, 1, 80, 0, NULL, NULL, '陈品烨', 'https://2638224627-1355158021.cos.ap-guangzhou.myqcloud.com/avatar/10000002/99e542e993384de69aea42a7464c4200.jpg', '湖北省', '武汉工程大学', NULL, NULL, '2026-03-12 21:52:14', '2026-03-17 06:22:56', '2026-03-17 19:40:50', 0, NULL, NULL);
INSERT INTO `users` VALUES (10000003, 'yangtianbo', '$2a$10$4WKJ1XiqQgjdliV4RD9GX./xRWHzbJn0WsiYmLMY7072dFQ007Qva', 'abc40321a@winvvv.com', '17205060708', 2, 2, 80, 0, NULL, NULL, '杨天波', 'https://2638224627-1355158021.cos.ap-guangzhou.myqcloud.com/avatars/10000003-1773587975037-2459460183.jpg', '湖北省', '武汉工程大学', NULL, NULL, '2026-03-12 21:56:48', '2026-03-17 15:17:13', '2026-03-17 19:20:35', 0, NULL, NULL);
INSERT INTO `users` VALUES (10000004, 'shizheyuan', '$2a$10$z5y1Viipm43LDJ0XHbWEiuC4JwSsfTzIz5AF6fLum49R./Uvtj8eW', 'abc42qua@winvvv.com', '17615060708', 2, 2, 80, 0, NULL, NULL, NULL, 'https://2638224627-1355158021.cos.ap-guangzhou.myqcloud.com/avatar/10000004/f8a9beb2ca2f4ac0b1374241fce7e7e7.jpg', '湖北省', '武汉工程大学', NULL, NULL, '2026-03-12 21:57:49', '2026-03-15 16:35:14', '2026-03-17 21:13:31', 0, NULL, NULL);
INSERT INTO `users` VALUES (10000037, '范吉奥', '$2a$10$ejgwIprEg.5/tgnfSJxL2uknKhYnc5ZyZJqmR9gTiVEwqHVeEQzcm', 'a2402qua@winvvv.com', '14323242432', 2, 1, 80, 0, NULL, NULL, NULL, NULL, '北京市', '北京大学', NULL, NULL, '2026-03-17 10:40:52', '2026-03-17 10:40:52', NULL, 0, NULL, NULL);
INSERT INTO `users` VALUES (10000038, '熊大', '$2a$10$OMvQSXxlZ6tqAoL8nsE2aOrGZE63u1zBQGVKTjWbZDgpxOivMbf8e', 'xiongda@qq.com', '17837833243', 2, 1, 80, 0, NULL, NULL, NULL, NULL, '狗熊林', '狗熊林大学', NULL, NULL, '2026-03-17 10:42:05', '2026-03-17 10:42:05', NULL, 0, NULL, NULL);
INSERT INTO `users` VALUES (10000039, '熊二', '$2a$10$hfPqvi84LUnn92XdrqLJw.Id8JbhLcQgc0whiVQV5iI2A20L.flfC', 'a32bc402qua@winvvv.com', '15805060708', 2, 1, 80, 0, NULL, NULL, NULL, NULL, '狗熊林', '狗熊林大学', NULL, NULL, '2026-03-17 15:57:50', '2026-03-17 15:57:50', NULL, 0, NULL, NULL);
INSERT INTO `users` VALUES (10000040, '小明', '$2a$10$GNRN5PqjDSeJEXMN1dC9IO78Hrk0LRN6CMdFAKdfilZLjAI0eCvDO', '21321321@qq.com', '17605260708', 2, 2, 80, 0, NULL, NULL, NULL, NULL, '加里沌', '加里沌大学', NULL, NULL, '2026-03-17 15:59:12', '2026-03-17 15:59:12', NULL, 0, NULL, NULL);
INSERT INTO `users` VALUES (10000041, '李四', '$2a$10$292LWLtWO88YA8qMBPSIi.JDElApMXpOEymq8SvdBrV00UfYeeSt.', 'c4dada02ua@winvvv.com', '14832983938', 2, 1, 80, 0, NULL, NULL, NULL, NULL, '北京', '清华大学', NULL, NULL, '2026-03-17 16:00:50', '2026-03-17 16:00:50', NULL, 0, NULL, NULL);
INSERT INTO `users` VALUES (10000042, 'select * from user', '$2a$10$U6YB73GjK1A7iiCHcQlzuOqZ/zmWn3rpgCFnvGj60o6KHvJ9ngcyu', 'fac402qua@winvvv.com', '17605960708', 2, 1, 80, 0, NULL, NULL, NULL, NULL, '湖北省', '清华大学', NULL, NULL, '2026-03-17 16:02:01', '2026-03-17 16:02:01', NULL, 0, NULL, NULL);

-- ----------------------------
-- Table structure for violation_report_logs
-- ----------------------------
DROP TABLE IF EXISTS `violation_report_logs`;
CREATE TABLE `violation_report_logs`  (
  `log_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志唯一ID',
  `report_id` int UNSIGNED NOT NULL COMMENT '关联的举报记录ID',
  `operator_id` int UNSIGNED NOT NULL COMMENT '操作人ID（管理员）',
  `action_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型：assign=分配处理，process=开始处理，approve=批准处理，reject=驳回，comment=添加备注',
  `action_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '操作内容/备注',
  `old_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '变更前状态',
  `new_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '变更后状态',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`log_id`) USING BTREE,
  INDEX `idx_report_id`(`report_id`) USING BTREE COMMENT '举报记录ID索引',
  INDEX `idx_operator_id`(`operator_id`) USING BTREE COMMENT '操作人ID索引',
  INDEX `idx_action_type`(`action_type`) USING BTREE COMMENT '操作类型索引',
  INDEX `idx_created_at`(`created_at`) USING BTREE COMMENT '操作时间索引',
  INDEX `idx_report_created`(`report_id`, `created_at`) USING BTREE COMMENT '举报记录和时间复合索引',
  CONSTRAINT `fk_log_operator` FOREIGN KEY (`operator_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_log_report` FOREIGN KEY (`report_id`) REFERENCES `violation_reports` (`report_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '违规举报处理日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of violation_report_logs
-- ----------------------------
INSERT INTO `violation_report_logs` VALUES (1, 3, 10000001, 'approve', '下架商品：', 'pending', 'approved', '2026-03-17 10:19:27');
INSERT INTO `violation_report_logs` VALUES (2, 4, 10000001, 'approve', '下架商品：', 'pending', 'approved', '2026-03-17 10:30:34');

-- ----------------------------
-- Table structure for violation_reports
-- ----------------------------
DROP TABLE IF EXISTS `violation_reports`;
CREATE TABLE `violation_reports`  (
  `report_id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '举报记录唯一ID',
  `report_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '举报编号，格式：VR-{时间戳}-{随机码}',
  `target_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '举报对象类型：product=商品举报，user=用户举报',
  `target_id` int UNSIGNED NOT NULL COMMENT '被举报对象ID（商品ID或用户ID）',
  `target_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '被举报对象名称（冗余字段，便于查询）',
  `reporter_id` int UNSIGNED NOT NULL COMMENT '举报人用户ID',
  `report_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '举报类型：product-fake/product-prohibited/user-harassment等',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '举报标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '举报描述（简要说明）',
  `detail_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '举报详细内容（完整描述）',
  `evidence_images` json NULL COMMENT '证据图片URL数组（OSS存储）',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending' COMMENT '处理状态：pending=待处理，processing=处理中，approved=已处理，rejected=已驳回',
  `handler_id` int UNSIGNED NULL DEFAULT NULL COMMENT '处理人ID（管理员）',
  `handle_action` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '处理行动：warning=警告，tempBan=临时封禁，permBan=永久封禁，removeProduct=下架商品',
  `handle_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '处理结果说明',
  `reject_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '驳回原因',
  `credit_deduction` tinyint UNSIGNED NULL DEFAULT NULL COMMENT '扣除信誉值（1-100）',
  `ban_duration` tinyint UNSIGNED NULL DEFAULT NULL COMMENT '封禁时长（天数）',
  `ban_until` timestamp NULL DEFAULT NULL COMMENT '封禁截止时间',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '举报创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `handled_at` timestamp NULL DEFAULT NULL COMMENT '处理完成时间',
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT '软删除时间',
  PRIMARY KEY (`report_id`) USING BTREE,
  UNIQUE INDEX `report_number`(`report_number`) USING BTREE,
  UNIQUE INDEX `uk_report_number`(`report_number`) USING BTREE COMMENT '举报编号唯一索引',
  INDEX `idx_reporter_id`(`reporter_id`) USING BTREE COMMENT '举报人ID索引',
  INDEX `idx_target_type`(`target_type`) USING BTREE COMMENT '举报对象类型索引',
  INDEX `idx_target_id`(`target_id`) USING BTREE COMMENT '被举报对象ID索引',
  INDEX `idx_report_type`(`report_type`) USING BTREE COMMENT '举报类型索引',
  INDEX `idx_status`(`status`) USING BTREE COMMENT '处理状态索引',
  INDEX `idx_handler_id`(`handler_id`) USING BTREE COMMENT '处理人ID索引',
  INDEX `idx_created_at`(`created_at`) USING BTREE COMMENT '创建时间索引',
  INDEX `idx_deleted_at`(`deleted_at`) USING BTREE COMMENT '软删除索引',
  INDEX `idx_target_type_id`(`target_type`, `target_id`) USING BTREE COMMENT '举报对象复合索引',
  INDEX `idx_status_created`(`status`, `created_at`) USING BTREE COMMENT '状态和创建时间复合索引',
  INDEX `idx_reporter_created`(`reporter_id`, `created_at`) USING BTREE COMMENT '举报人和创建时间复合索引',
  CONSTRAINT `fk_violation_handler` FOREIGN KEY (`handler_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_violation_reporter` FOREIGN KEY (`reporter_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '违规举报表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of violation_reports
-- ----------------------------
INSERT INTO `violation_reports` VALUES (1, 'VR-1773710776149-1780', 'product', 101, '312', 10000003, 'product-fake', '321', '321', '312', NULL, 'approved', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-17 09:26:16', '2026-03-17 10:00:26', NULL, NULL);
INSERT INTO `violation_reports` VALUES (2, 'VR-1773711793272-6107', 'product', 101, '312', 10000003, 'product-fake', '31', '321', '312', '[\"https://2638224627-1355158021.cos.ap-guangzhou.myqcloud.com/violations/10000003-1773711791076-pc-dongman-copy.png\"]', 'rejected', 10000001, NULL, NULL, '不会说话', NULL, NULL, NULL, '2026-03-17 09:43:13', '2026-03-17 09:52:10', '2026-03-17 09:52:10', NULL);
INSERT INTO `violation_reports` VALUES (3, 'VR-1773713942167-1431', 'product', 101, '312', 10000003, 'product-fake', '432', '423423', '4234', '[\"https://2638224627-1355158021.cos.ap-guangzhou.myqcloud.com/violations/10000003-1773713939113-wallhaven-rqqw2q.jpg\"]', 'approved', 10000001, 'removeProduct', '', NULL, NULL, NULL, NULL, '2026-03-17 10:19:02', '2026-03-17 10:19:27', '2026-03-17 10:19:27', NULL);
INSERT INTO `violation_reports` VALUES (4, 'VR-1773714090357-5315', 'product', 101, '312', 10000003, 'product-fake', '321', '321', '321', NULL, 'approved', 10000001, 'removeProduct', '', NULL, NULL, NULL, NULL, '2026-03-17 10:21:30', '2026-03-17 10:30:34', '2026-03-17 10:30:34', NULL);

-- ----------------------------
-- Procedure structure for generate_user_id
-- ----------------------------
DROP PROCEDURE IF EXISTS `generate_user_id`;
delimiter ;;
CREATE PROCEDURE `generate_user_id`(OUT new_user_id INT)
BEGIN
    -- 更新序列表，获取新ID
    UPDATE user_id_sequence SET id = LAST_INSERT_ID(id + 1) WHERE stub = 'a';

    -- 获取生成的ID
    SET new_user_id = LAST_INSERT_ID();

    -- 检查是否超过最大值
    IF new_user_id > 99999999 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = '用户ID已达到最大值99999999，无法继续生成';
    END IF;
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- Table structure for exchange_proofs
-- ----------------------------
DROP TABLE IF EXISTS `exchange_proofs`;
CREATE TABLE `exchange_proofs` (
  `proof_id` int NOT NULL AUTO_INCREMENT COMMENT '凭证ID',
  `order_id` int UNSIGNED NOT NULL COMMENT '订单ID',
  `user_id` int NOT NULL COMMENT '上传用户ID',
  `user_role` varchar(20) NOT NULL COMMENT '用户角色(initiator/receiver)',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态(0=待审核,1=已上传,2=已验证)',
  `images` text COMMENT '凭证图片(JSON)',
  `comment` varchar(500) DEFAULT NULL COMMENT '备注',
  `verified_by` int DEFAULT NULL COMMENT '审核人ID',
  `verified_at` datetime DEFAULT NULL COMMENT '审核时间',
  `uploaded_at` datetime DEFAULT NULL COMMENT '上传时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`proof_id`),
  INDEX `idx_order_id` (`order_id`)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '交换凭证表';
