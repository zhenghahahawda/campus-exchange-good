/**
 * 违规举报模拟数据
 * @author Kiro
 * @version 1.0
 */

import { REPORT_TYPES, REPORT_STATUS, REPORT_TARGET_TYPES } from '@/utils/illegalConstants'

/**
 * 生成模拟举报数据
 * @returns {Array} 举报数据列表
 */
export function generateMockReports() {
  return Object.freeze([
    // 商品举报案例
    {
      id: 1,
      title: '商品信息虚假',
      description: '该商品标注为全新iPhone 14，实际收到的是翻新机，存在明显使用痕迹',
      detailContent: '我在2024年3月5日购买了这款iPhone 14 Pro Max，商家明确标注为"全新未拆封"，但收到货后发现：\n\n1. 包装盒已有明显拆封痕迹\n2. 手机屏幕有轻微划痕\n3. 充电接口有使用痕迹\n4. 系统显示已激活30天\n\n这明显是翻新机冒充全新机销售，严重欺骗消费者。',
      evidenceImages: [
        '/images/evidence/phone-scratch.jpg',
        '/images/evidence/package-opened.jpg',
        '/images/evidence/system-info.jpg'
      ],
      type: REPORT_TYPES.PRODUCT_FAKE,
      status: REPORT_STATUS.PENDING,
      reporterName: '张三',
      targetName: 'P202403080001',
      targetType: REPORT_TARGET_TYPES.PRODUCT,
      productName: 'iPhone 14 Pro Max 256GB',
      createdAt: new Date('2024-03-08 14:30:00')
    },
    {
      id: 2,
      title: '销售违禁物品',
      description: '该用户在平台销售管制刀具，违反平台规定',
      detailContent: '发现该商家在平台销售管制刀具，具体情况如下：\n\n1. 商品名称虽然写的是"户外工具"，但实际是管制刀具\n2. 刀身长度超过规定标准\n3. 商品详情页面有"防身"等违规宣传\n4. 已有多名用户反映此问题\n\n此类商品属于国家管制物品，不应在平台销售。',
      evidenceImages: [
        '/images/evidence/knife-product.jpg',
        '/images/evidence/knife-description.jpg'
      ],
      type: REPORT_TYPES.PRODUCT_PROHIBITED,
      status: REPORT_STATUS.APPROVED,
      reporterName: '王五',
      targetName: 'P202403070002',
      targetType: REPORT_TARGET_TYPES.PRODUCT,
      productName: '户外多功能刀具',
      createdAt: new Date('2024-03-07 16:45:00')
    },
    {
      id: 3,
      title: '商品图片侵权',
      description: '该商品使用了我的原创设计图片，未经授权盗用',
      detailContent: '该商家盗用了我的原创设计作品，具体侵权情况：\n\n1. 商品主图完全复制我的设计\n2. 未经任何授权或许可\n3. 我拥有该设计的完整版权\n4. 已在多个平台发现类似侵权行为\n\n附上我的原创作品证明和商家盗用的对比图片。',
      evidenceImages: [
        '/images/evidence/original-design.jpg',
        '/images/evidence/copied-design.jpg',
        '/images/evidence/copyright-certificate.jpg'
      ],
      type: REPORT_TYPES.PRODUCT_COPYRIGHT,
      status: REPORT_STATUS.PROCESSING,
      reporterName: '孙七',
      targetName: 'P202403060003',
      targetType: REPORT_TARGET_TYPES.PRODUCT,
      productName: '创意手机壳设计',
      createdAt: new Date('2024-03-06 10:20:00')
    },
    {
      id: 4,
      title: '价格欺诈行为',
      description: '商品标价999元，下单后要求补差价至1999元',
      type: REPORT_TYPES.PRODUCT_PRICE,
      status: REPORT_STATUS.REJECTED,
      reporterName: '吴九',
      targetName: 'P202403050004',
      targetType: REPORT_TARGET_TYPES.PRODUCT,
      productName: '蓝牙耳机',
      createdAt: new Date('2024-03-05 09:15:00')
    },
    {
      id: 5,
      title: '商品描述不符',
      description: '商品描述为全新未拆封，实际收到已拆封使用过的商品',
      type: REPORT_TYPES.PRODUCT_DESCRIPTION,
      status: REPORT_STATUS.APPROVED,
      reporterName: '陈十一',
      targetName: 'P202403040005',
      targetType: REPORT_TARGET_TYPES.PRODUCT,
      productName: '游戏手柄',
      createdAt: new Date('2024-03-04 11:30:00')
    },
    
    // 用户举报案例
    {
      id: 6,
      title: '恶意骚扰买家',
      description: '该用户在交易过程中多次辱骂买家，态度恶劣',
      detailContent: '该用户在交易过程中表现极其恶劣：\n\n1. 在聊天中多次使用侮辱性词汇\n2. 威胁要泄露买家个人信息\n3. 在商品评论区恶意攻击买家\n4. 拒绝正常的售后服务要求\n\n此行为严重影响平台交易环境，请严肃处理。',
      evidenceImages: [
        '/images/evidence/chat-harassment1.jpg',
        '/images/evidence/chat-harassment2.jpg',
        '/images/evidence/comment-attack.jpg'
      ],
      type: REPORT_TYPES.USER_HARASSMENT,
      status: REPORT_STATUS.APPROVED,
      reporterName: '黄十三',
      targetName: '刘十四',
      targetType: REPORT_TARGET_TYPES.USER,
      createdAt: new Date('2024-03-03 15:20:00')
    },
    {
      id: 7,
      title: '诈骗买家钱财',
      description: '该用户收款后拒不发货，联系方式失效，疑似诈骗',
      detailContent: '该用户涉嫌诈骗，具体情况如下：\n\n1. 3月1日付款购买商品，金额1299元\n2. 付款后卖家承诺3天内发货\n3. 一周后仍未发货，联系卖家无回应\n4. 卖家微信和电话均已失效\n5. 商品页面已被删除\n\n怀疑是有预谋的诈骗行为，请尽快处理。',
      evidenceImages: [
        '/images/evidence/payment-record.jpg',
        '/images/evidence/chat-promise.jpg',
        '/images/evidence/contact-failed.jpg'
      ],
      type: REPORT_TYPES.USER_FRAUD,
      status: REPORT_STATUS.PENDING,
      reporterName: '杨十五',
      targetName: '何十六',
      targetType: REPORT_TARGET_TYPES.USER,
      createdAt: new Date('2024-03-02 08:45:00')
    },
    {
      id: 8,
      title: '发送垃圾信息',
      description: '该用户频繁发送广告信息，干扰正常交易',
      detailContent: '该用户大量发送垃圾广告信息：\n\n1. 每天发送超过50条广告信息\n2. 内容涉及违规产品推广\n3. 在多个商品评论区刷屏\n4. 私信骚扰其他用户\n5. 已有多名用户投诉\n\n严重影响平台正常秩序，建议封禁处理。',
      evidenceImages: [
        '/images/evidence/spam-messages.jpg',
        '/images/evidence/comment-spam.jpg'
      ],
      type: REPORT_TYPES.USER_SPAM,
      status: REPORT_STATUS.PROCESSING,
      reporterName: '罗十七',
      targetName: '郭十八',
      targetType: REPORT_TARGET_TYPES.USER,
      createdAt: new Date('2024-03-01 13:10:00')
    },
    {
      id: 9,
      title: '冒充官方客服',
      description: '该用户冒充平台客服，试图获取用户个人信息',
      type: REPORT_TYPES.USER_IMPERSONATION,
      status: REPORT_STATUS.APPROVED,
      reporterName: '钱十九',
      targetName: '孙二十',
      targetType: REPORT_TARGET_TYPES.USER,
      createdAt: new Date('2024-02-29 17:25:00')
    },
    {
      id: 10,
      title: '恶意刷单行为',
      description: '该用户存在明显的刷单行为，虚假提升信誉',
      type: REPORT_TYPES.USER_INAPPROPRIATE,
      status: REPORT_STATUS.PENDING,
      reporterName: '李二一',
      targetName: '赵二二',
      targetType: REPORT_TARGET_TYPES.USER,
      createdAt: new Date('2024-02-28 09:40:00')
    },
    {
      id: 11,
      title: '交易纠纷处理',
      description: '该用户拒绝按约定时间发货，且拒绝沟通解决',
      type: REPORT_TYPES.USER_TRANSACTION,
      status: REPORT_STATUS.REJECTED,
      reporterName: '王二三',
      targetName: '张二四',
      targetType: REPORT_TARGET_TYPES.USER,
      createdAt: new Date('2024-02-27 14:15:00')
    },
    {
      id: 12,
      title: '商品质量严重问题',
      description: '收到的电子产品存在安全隐患，可能对用户造成伤害',
      type: REPORT_TYPES.PRODUCT_QUALITY,
      status: REPORT_STATUS.APPROVED,
      reporterName: '刘二五',
      targetName: 'P202402260012',
      targetType: REPORT_TARGET_TYPES.PRODUCT,
      productName: '充电宝',
      createdAt: new Date('2024-02-26 11:50:00')
    }
  ])
}