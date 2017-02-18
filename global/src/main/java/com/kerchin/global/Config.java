package com.kerchin.global;

/**
 * Created by Kerchin on 2015/11/2 0002.
 */
public class Config {

    public static boolean isLeakCanary = false;
    public static boolean isDebugMode = true;
    /**
     * Settings
     */
    public static final String KEY_USER = "xiaohuangjUser";
    public static final String KEY_USERICON = "xiaohuangjUserIcon";
    public static final String KEY_PASS = "xiaohuangjPass";
    public static final String KEY_ISLOGIN = "xiaohuangjIsLogin";
    public static final String KEY_DEFAULT_FOLDER = "xiaohuangjDefaultFolder";
    public static final String KEY_CAN_OFFLINE = "xiaohuangjCanOffline";
    public static final String KEY_WHEN_CHECK_UPDATE = "xiaohuangjWhenCheckUpdate";
//    public static final String KEY_CACHE_User = "cacheUser";
//    public static final String KEY_CACHE_Pass = "cachePass";
//    public static final String KEY_CACHE_RePass = "cacheRePass";
//    public static final String KEY_CACHE_Prove = "cacheProve";
    /**
     * 延时类设置
     */
    public static final int timeout_prov = 5;//min 发送验证码的过期时间
    public static final int timeout_avod = 15000;//mill 轮询间隔
    public static final int timeout_runnable = 20000;
    public static final int period_runnable = 100;

    /**
     * highLight
     */
    public static final String HIGHLIGHT_NAV = "HIGHLIGHT_NAV";
}
