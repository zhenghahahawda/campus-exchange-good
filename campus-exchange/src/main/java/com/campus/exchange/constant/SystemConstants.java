package com.campus.exchange.constant;

/**
 * 系统常量
 */
public class SystemConstants {

    /**
     * 用户相关常量
     */
    public static class User {
        /** 用户ID起始值 */
        public static final int USER_ID_START = 10000001;
        /** 用户ID最大值 */
        public static final int USER_ID_MAX = 99999999;
        /** 登录失败锁定次数 */
        public static final int LOGIN_FAIL_LOCK_COUNT = 5;
        /** 账户锁定时长（分钟） */
        public static final int ACCOUNT_LOCK_MINUTES = 30;
    }

    /**
     * 商品相关常量
     */
    public static class Goods {
        /** 交换凭证码前缀 */
        public static final String EXCHANGE_CODE_PREFIX = "EX";
        /** 随机码长度 */
        public static final int RANDOM_CODE_LENGTH = 4;
    }

    /**
     * 分页相关常量
     */
    public static class Page {
        /** 默认页码 */
        public static final int DEFAULT_PAGE_NUM = 1;
        /** 默认每页大小 */
        public static final int DEFAULT_PAGE_SIZE = 10;
        /** 最大每页大小 */
        public static final int MAX_PAGE_SIZE = 100;
    }
}
