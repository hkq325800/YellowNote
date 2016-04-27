package com.kerchin.yellownote.global;

/**
 * Created by Kerchin on 2015/11/2 0002.
 */
public class Config {

    public static boolean isDebugMode = false;
    /**
     * LeanCloud所需配置
     */
    public static String APP_ID = "yourLeanCloud APP_ID";
    public static String APP_KEY = "yourLeanCloud APP_KEY";
    /**
     * Settings
     */
    public static final String KEY_USER = "xiaohuangjUser";
    public static final String KEY_PASS = "xiaohuangjPass";
    public static final String KEY_ISLOGIN = "xiaohuangjIsLogin";
    public static final String KEY_DEFAULT_FOLDER = "xiaohuangjDefaultFolder";
//    public static final String KEY_CACHE_User = "cacheUser";
//    public static final String KEY_CACHE_Pass = "cachePass";
//    public static final String KEY_CACHE_RePass = "cacheRePass";
//    public static final String KEY_CACHE_Prove = "cacheProve";
    /**
     * 延时类设置
     */
    public static final int timeout_prov = 5;//min 发送验证码的过期时间
    public static final int timeout_avod = 5000;//mill 轮询间隔
}
