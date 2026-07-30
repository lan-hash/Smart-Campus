package com.campus.common.constant;

import org.springframework.stereotype.Component;

/**
 * Redis所有Key常量统一管理
 */
@Component
public class RedisConstant {

    //====================登录防爆破相关====================
    public static final int MAX_FAIL_COUNT = 5;
    public static final long LOCK_DURATION = 15 * 60 * 1000L;
    public static final String FAIL_PREFIX = "login:fail:";
    public static final String LOCK_PREFIX = "login:lock:";

    //====================JWT令牌相关====================
    public static final String REFRESH_TOKEN_PREFIX = "jwt:refresh:";
    public static final String JWT_BLACKLIST_PREFIX = "jwt:blacklist:";

    //后续所有redis key全部放这里统一维护
}