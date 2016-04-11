package com.kerchin.yellownote.global;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.avos.avoscloud.AVOSCloud;
import com.kerchin.yellownote.bean.PrimaryData;
import com.kerchin.yellownote.utilities.CrashExceptionHandler;
import com.kerchin.yellownote.utilities.NormalUtils;
import com.kerchin.yellownote.utilities.SimpleCrashReporter;
import com.securepreferences.SecurePreferences;

public class MyApplication extends Application {
    /**
     * app在sd卡的主目录
     */
    private final static String APP_MAIN_FOLDER_NAME = "YellowNote";
    /**
     * 本地存放闪退日志的目录
     */
    private final static String CRASH_FOLDER_NAME = "crash";
    private static Context context;
    private static SharedPreferences shared;
    private static final String SaltKey = "xiaohuangj";
    private static boolean isLogin = false;
    public static String user;
    public static String userDefaultFolderId = "";
    public static String view = "note";//TODO
    //public static final int pageLimit = 5;
    public static int thisPosition = 0;
    public static boolean isItemsReadyToGo = false;//TODO

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        context = getApplicationContext();
        AVOSCloud.initialize(context,
                Config.APP_ID, Config.APP_KEY);
//        CrashHandler crashHandler = CrashHandler.getInstance();
//        crashHandler.init(context);
        configCollectCrashInfo();
        shared = new SecurePreferences(context);
        user = shared.getString(Config.KEY_User, "");
        isLogin = shared.getBoolean(Config.KEY_ISLOGIN, false);
        super.onCreate();
    }

    /**
     * 配置崩溃信息的搜集
     */
    private void configCollectCrashInfo() {
        CrashExceptionHandler crashExceptionHandler = new CrashExceptionHandler(this, APP_MAIN_FOLDER_NAME, CRASH_FOLDER_NAME);
        CrashExceptionHandler.CrashExceptionRemoteReport remoteReport = new SimpleCrashReporter();
        crashExceptionHandler.configRemoteReport(remoteReport); //设置友盟统计报错日志回传到远程服务器上
        Thread.setDefaultUncaughtExceptionHandler(crashExceptionHandler);
    }

    public static SharedPreferences getDefaultShared() {//可在应用间共享数据
        return shared;
    }

    public static String Secret(String val) {
        return NormalUtils.md5(val + SaltKey);
    }

    public static boolean isLogin() {
        return isLogin;
    }

    public static void setUser(String u) {
        user = u;
        isLogin = true;
    }

    public static void logout() {
        isLogin = false;
        PrimaryData.clearData();
        //清除密码缓存
        SecurePreferences.Editor editor = (SecurePreferences.Editor) shared.edit();
        editor.putBoolean(Config.KEY_ISLOGIN, isLogin);
        editor.putString(Config.KEY_PASS, "");
        editor.apply();
    }
}
