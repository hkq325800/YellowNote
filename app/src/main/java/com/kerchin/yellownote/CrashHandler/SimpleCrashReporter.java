package com.kerchin.yellownote.CrashHandler;

import com.avos.avoscloud.AVFile;
import com.kerchin.global.Config;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * 自定义的回传闪退日志到远程服务器
 * <p/>
 * Created by Clock on 2016/1/27.
 */
public class SimpleCrashReporter implements CrashExceptionHandler.CrashExceptionRemoteReport {

    @Override
    public void onCrash(File file) {
        if (file != null && !Config.isDebugMode) {
            //接下来要在此处加入将闪退日志回传到服务器的功能
            try {
                AVFile avFile = AVFile.withAbsoluteLocalPath(file.getName(), file.getAbsolutePath());
                avFile.saveInBackground();
                // AVObject crash = new AVObject("Crash");
                // crash.put("user_tel", MyApplication.user);
                // crash.put("crash_log", avFile);
                // crash.saveInBackground();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}
