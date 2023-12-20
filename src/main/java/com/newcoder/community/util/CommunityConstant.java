package com.newcoder.community.util;

public interface CommunityConstant {

    /**
     * 激活成功
     */
    final int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    final int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    final int ACTIVATION_FAILURE = 2;

    /**
     * 默认状态的登陆凭证超时时间
     */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;    // 12 hours

    /**
     * 记住状态的登陆凭证超时时间
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 10;    // 30 days
}
