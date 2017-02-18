package com.kerchin.yellownote.global;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.DisplayMetrics;

import com.avos.avoscloud.AVOSCloud;
import com.kerchin.global.Config;
import com.kerchin.yellownote.CrashHandler.CrashExceptionHandler;
import com.kerchin.yellownote.CrashHandler.SimpleCrashReporter;
import com.kerchin.global.NormalUtils;
import com.tencent.tinker.loader.app.ApplicationLike;
import com.tinkerpatch.sdk.TinkerPatch;
import com.tinkerpatch.sdk.loader.TinkerPatchApplicationLike;
import com.uuzuche.lib_zxing.DisplayUtil;

/**
 * Created by hkq325800 on 2017/2/18.
 */

public class MyApplication extends Application {
    private static final String TAG = "global.SampleApplicationLike";
    /**
     * app在sd卡的主目录
     */
    public final static String APP_MAIN_FOLDER_NAME = "YellowNote";
    /**
     * 本地存放闪退日志的目录
     */
    public final static String CRASH_FOLDER_NAME = "crash";
    public static Context context;
    private static final String SaltKey = "xiaohuangj";
    private ApplicationLike tinkerApplicationLike;
//    public static String userDefaultFolderId = "";
//    public static String userIcon;//永远是最新的

    @Override
    public void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //you must install multiDex whatever tinker is installed!
        MultiDex.install(base);
    }

    /**
     * 由于在onCreate替换真正的Application,
     * 我们建议在onCreate初始化TinkerPatch,而不是attachBaseContext
     */
    @Override
    public void onCreate() {
        super.onCreate();
        // 我们可以从这里获得Tinker加载过程的信息
//        if (BuildConfig.TINKER_ENABLE) {
            tinkerApplicationLike = TinkerPatchApplicationLike.getTinkerPatchApplicationLike();

            // 初始化TinkerPatch SDK
            TinkerPatch.init(tinkerApplicationLike)
                    .reflectPatchLibrary()
                    .setPatchRollbackOnScreenOff(true)
                    .setPatchRestartOnSrceenOff(true);

            // 每隔3个小时去访问后台时候有更新,通过handler实现轮训的效果
            new FetchPatchHandler().fetchPatchWithInterval(3);
//        }
        context = this;
        zj.remote.baselibrary.Config.isDebugMode = Config.isDebugMode;

//        Realm realm = Realm.getInstance(context);
        AVOSCloud.initialize(this
                , getResources().getString(com.kerchin.global.R.string.APP_ID)
                , getResources().getString(com.kerchin.global.R.string.APP_KEY));
//        if (Config.isLeakCanary)
//            LeakCanary.install(this);
        configCollectCrashInfo();
//        shared = new SecurePreferences(getApplication());
        //来自手势密码
        initDisplayOpinion();
    }


    private void initDisplayOpinion() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        DisplayUtil.density = dm.density;
        DisplayUtil.densityDPI = dm.densityDpi;
        DisplayUtil.screenWidthPx = dm.widthPixels;
        DisplayUtil.screenhightPx = dm.heightPixels;
        DisplayUtil.screenWidthDip = DisplayUtil.px2dip(context, dm.widthPixels);
        DisplayUtil.screenHightDip = DisplayUtil.px2dip(context, dm.heightPixels);
    }

    /**
     * 配置崩溃信息的搜集
     */
    private void configCollectCrashInfo() {
        CrashExceptionHandler crashExceptionHandler = new CrashExceptionHandler(context, APP_MAIN_FOLDER_NAME, CRASH_FOLDER_NAME);
        CrashExceptionHandler.CrashExceptionRemoteReport remoteReport = new SimpleCrashReporter();
        crashExceptionHandler.configRemoteReport(remoteReport); //设置友盟统计报错日志回传到远程服务器上
        Thread.setDefaultUncaughtExceptionHandler(crashExceptionHandler);
    }

    public static String Secret(String val) {
        return NormalUtils.md5(val + SaltKey);
    }
}
