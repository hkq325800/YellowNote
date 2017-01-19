/*
 * Tencent is pleased to support the open source community by making Tinker available.
 *
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kerchin.yellownote.global;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.util.DisplayMetrics;

import com.avos.avoscloud.AVOSCloud;
import com.kerchin.global.R;
import com.kerchin.yellownote.global.CrashHandler.CrashExceptionHandler;
import com.kerchin.yellownote.global.CrashHandler.SimpleCrashReporter;
import com.kerchin.yellownote.global.tinker.MyLogImp;
import com.kerchin.yellownote.global.tinker.TinkerManager;
import com.tencent.tinker.anno.DefaultLifeCycle;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.tencent.tinker.loader.app.ApplicationLifeCycle;
import com.tencent.tinker.loader.app.DefaultApplicationLike;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.uuzuche.lib_zxing.DisplayUtil;


/**
 * because you can not use any other class in your application, we need to
 * move your implement of Application to {@link ApplicationLifeCycle}
 * As Application, all its direct reference class should be in the main dex.
 * <p>
 * We use tinker-android-anno to make sure all your classes can be patched.
 * <p>
 * application: if it is start with '.', we will add SampleApplicationLifeCycle's package name
 * <p>
 * flags:
 * TINKER_ENABLE_ALL: support dex, lib and resource
 * TINKER_DEX_MASK: just support dex
 * TINKER_NATIVE_LIBRARY_MASK: just support lib
 * TINKER_RESOURCE_MASK: just support resource
 * <p>
 * loaderClass: define the tinker loader class, we can just use the default TinkerLoader
 * <p>
 * loadVerifyFlag: whether check files' md5 on the load time, defualt it is false.
 * <p>
 * Created by zhangshaowen on 16/3/17.
 */
@SuppressWarnings("unused")
@DefaultLifeCycle(application = "com.kerchin.yellownote.global.MyApplication",
        flags = ShareConstants.TINKER_ENABLE_ALL,
        loadVerifyFlag = false)
public class SampleApplicationLike extends DefaultApplicationLike {
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
//    public static String userDefaultFolderId = "";
//    public static String userIcon;//永远是最新的

    public SampleApplicationLike(Application application, int tinkerFlags, boolean tinkerLoadVerifyFlag,
                                 long applicationStartElapsedTime, long applicationStartMillisTime, Intent tinkerResultIntent,
                                 Resources[] resources, ClassLoader[] classLoader, AssetManager[] assetManager) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime, applicationStartMillisTime, tinkerResultIntent, resources, classLoader, assetManager);
    }

    /**
     * install multiDex before install tinker
     * so we don't need to put the tinker lib classes in the main dex
     *
     * @param base
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onBaseContextAttached(Context base) {
        super.onBaseContextAttached(base);
        //you must install multiDex whatever tinker is installed!
        MultiDex.install(base);

        context = getApplication();
        zj.remote.baselibrary.Config.isDebugMode = Config.isDebugMode;

        TinkerManager.setTinkerApplicationLike(this);
        //阻碍了crash信息收集
//        TinkerManager.initFastCrashProtect();
        //should set before tinker is installed
        TinkerManager.setUpgradeRetryEnable(true);
        //optional set logIml, or you can use default debug log
        TinkerInstaller.setLogIml(new MyLogImp());
        //installTinker after load multiDex
        //or you can put com.tencent.tinker.** to main dex
        TinkerManager.installTinker(this);

//        Realm realm = Realm.getInstance(context);
        AVOSCloud.initialize(getApplication()
                , getApplication().getResources().getString(R.string.APP_ID)
                , getApplication().getResources().getString(R.string.APP_KEY));
//        if (Config.isLeakCanary)
//            LeakCanary.install(this);
        configCollectCrashInfo();
//        shared = new SecurePreferences(getApplication());
        //来自手势密码
        initDisplayOpinion();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void registerActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks callback) {
        getApplication().registerActivityLifecycleCallbacks(callback);
    }

    private void initDisplayOpinion() {
        DisplayMetrics dm = getApplication().getResources().getDisplayMetrics();
        DisplayUtil.density = dm.density;
        DisplayUtil.densityDPI = dm.densityDpi;
        DisplayUtil.screenWidthPx = dm.widthPixels;
        DisplayUtil.screenhightPx = dm.heightPixels;
        DisplayUtil.screenWidthDip = DisplayUtil.px2dip(getApplication(), dm.widthPixels);
        DisplayUtil.screenHightDip = DisplayUtil.px2dip(getApplication(), dm.heightPixels);
    }

    /**
     * 配置崩溃信息的搜集
     */
    private void configCollectCrashInfo() {
        CrashExceptionHandler crashExceptionHandler = new CrashExceptionHandler(getApplication(), APP_MAIN_FOLDER_NAME, CRASH_FOLDER_NAME);
        CrashExceptionHandler.CrashExceptionRemoteReport remoteReport = new SimpleCrashReporter();
        crashExceptionHandler.configRemoteReport(remoteReport); //设置友盟统计报错日志回传到远程服务器上
        Thread.setDefaultUncaughtExceptionHandler(crashExceptionHandler);
    }

//    public static SharedPreferences getDefaultShared() {//可在应用间共享数据
//        return shared;
//    }

    public static String Secret(String val) {
        return NormalUtils.md5(val + SaltKey);
    }
}
