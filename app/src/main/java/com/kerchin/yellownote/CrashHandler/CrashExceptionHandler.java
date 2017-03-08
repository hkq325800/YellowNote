package com.kerchin.yellownote.CrashHandler;

import android.content.Context;
import android.os.Environment;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import com.kerchin.global.Config;
import com.kerchin.yellownote.global.MyApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

import zj.remote.baselibrary.util.NormalUtils;
import zj.remote.baselibrary.util.PreferenceUtils;
import zj.remote.baselibrary.util.SystemUtils;

/**
 * app奔溃异常处理器
 * <p/>
 * 使用此类需要在AndroidManifest.xml配置以下权限
 * <p/>
 * <bold>android.permission.READ_EXTERNAL_STORAGE</bold>
 * <p/>
 * <bold>android.permission.WRITE_EXTERNAL_STORAGE</bold>
 * <p/>
 * <bold>android.permission.READ_PHONE_STATE</bold>
 * <p/>
 * Created by Clock on 2016/1/24.
 */
public class CrashExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmm");
    public static final int MAX_CRASH_COUNT = 3;
    /**
     * 默认存放闪退信息的文件夹名称
     */
    private static final String DEFAULT_CRASH_FOLDER_NAME = "Log";
    /**
     * appSD卡默认目录
     */
    private static final String DEFAULT_APP_FOLDER_NAME = "DefaultCrash";


    private Context mApplicationContext;
    /**
     * app在SD卡上的主目录
     */
    private File mAppMainFolder;
    /**
     * 保存闪退日志的文件目录
     */
    private File mCrashInfoFolder;
    /**
     * 向远程服务器发送错误信息
     */
    private CrashExceptionRemoteReport mCrashExceptionRemoteReport;
    private static final int killAfterMills = 2000;
    String timeStampString;
    Date date;

    /**
     * @param context
     * @param appMainFolderName   app程序主目录名，配置后位于SD卡一级目录下
     * @param crashInfoFolderName 闪退日志保存目录名，配置后位于 appMainFolderName 配置的一级目录下
     */
    public CrashExceptionHandler(Context context, String appMainFolderName, String crashInfoFolderName) {
        this.mApplicationContext = context;
        if (!TextUtils.isEmpty(appMainFolderName)) {
            this.mAppMainFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), appMainFolderName);
        } else {
            this.mAppMainFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), DEFAULT_APP_FOLDER_NAME);
        }
        if (!TextUtils.isEmpty(crashInfoFolderName)) {
            this.mCrashInfoFolder = new File(mAppMainFolder, crashInfoFolderName);
        } else {
            this.mCrashInfoFolder = new File(mAppMainFolder, DEFAULT_CRASH_FOLDER_NAME);
        }
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        date = new Date();
        timeStampString = DATE_FORMAT.format(date);//当先的时间格式化
        ex.printStackTrace();
        handleException(ex);
        try {
            Thread.sleep(killAfterMills);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //杀死进程
        NormalUtils.rebot(mApplicationContext);
        NormalUtils.killSelf();
    }

    /**
     * 配置远程传回log到服务器的设置
     *
     * @param crashExceptionRemoteReport
     */
    public void configRemoteReport(CrashExceptionRemoteReport crashExceptionRemoteReport) {
        this.mCrashExceptionRemoteReport = crashExceptionRemoteReport;
    }

    /**
     * 处理异常
     *
     * @param ex
     */
    private void handleException(Throwable ex) {
        if (ex == null) {
            return;
        } else {
            File file = saveCrashInfoToFile(ex);
//            saveCatchInfo2File(ex);
            sendCrashInfoToServer(file);

            //使用Toast来显示异常信息
            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    try {
                        Toast.makeText(mApplicationContext, "程序出现异常,即将退出....", Toast.LENGTH_LONG).show();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    Looper.loop();
                }
            }.start();
        }
    }

    /**
     * 保存闪退信息到本地文件中
     *
     * @param ex
     */
    private File saveCrashInfoToFile(Throwable ex) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try {
                long start = System.currentTimeMillis();
                if (!mAppMainFolder.exists()) {//app目录不存在则先创建目录
                    mAppMainFolder.mkdirs();
                }
                if (!mCrashInfoFolder.exists()) {//闪退日志目录不存在则先创建闪退日志目录
                    mCrashInfoFolder.mkdirs();
                }
                String crashLogFileName = PreferenceUtils.getString(Config.KEY_USER, "", MyApplication.context)
                        + "_" + timeStampString + ".txt";
                File crashLogFile = new File(mCrashInfoFolder, crashLogFileName);
                crashLogFile.createNewFile();

                //记录闪退环境的信息
                RandomAccessFile randomAccessFile = new RandomAccessFile(crashLogFile, "rw");
//                randomAccessFile.writeUTF("------------Crash Environment Info------------" + "\n");
//                randomAccessFile.writeUTF("------------Manufacture: " + SystemUtils.getDeviceManufacture() + "------------" + "\n");
//                randomAccessFile.writeUTF("------------DeviceName: " + SystemUtils.getDeviceName() + "------------" + "\n");
//                randomAccessFile.writeUTF("------------SystemVersion: " + SystemUtils.getSystemVersion() + "------------" + "\n");
//                randomAccessFile.writeUTF("------------DeviceIMEI: " + SystemUtils.getDeviceIMEI(mApplicationContext) + "------------" + "\n");
//                randomAccessFile.writeUTF("------------AppVersion: " + SystemUtils.getAppVersion(mApplicationContext) + "------------" + "\n");
//                randomAccessFile.writeUTF("------------Crash Environment Info------------" + "\n");
//                randomAccessFile.writeUTF("\n");
                String str = ("------------Crash Environment Info------------" + "\n") +
                        "------------Manufacture: " +
                        SystemUtils.getDeviceManufacture() +
                        "------------" + "\n" +
                        "------------DeviceName: " +
                        SystemUtils.getDeviceName() +
                        "------------" + "\n" +
                        "------------SystemVersion: " +
                        SystemUtils.getSystemVersion() +
                        "------------" + "\n" +
                        "------------DeviceIMEI: " +
                        SystemUtils.getDeviceIMEI(mApplicationContext) +
                        "------------" + "\n" +
                        "------------AppVersion: " +
                        SystemUtils.getAppVersion(mApplicationContext) +
                        "------------" + "\n" +
                        "------------Crash Environment Info------------" + "\n\n";
                randomAccessFile.write(str.getBytes(Charset.forName("UTF-8")));
                randomAccessFile.close();


                PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(crashLogFile.getAbsolutePath(), true)), true);
//                String time = "[" + NormalUtils.getNowDate("yyyy-MM-dd-HH-mm-ss") + "]";
                ex.printStackTrace(pw);//写入崩溃的日志信息
                long end = System.currentTimeMillis();
                String strS = "logStartAt:" + date + "\n";
                pw.append(strS);
                String strD = "logCreateFor:" + (end - start) + "ms";
                pw.append(strD);
                pw.close();
                return crashLogFile;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 发送发送闪退信息到远程服务器
     *
     * @param file
     */
    private void sendCrashInfoToServer(File file) {
        if (mCrashExceptionRemoteReport != null) {
            mCrashExceptionRemoteReport.onCrash(file);
        }
    }

    /**
     * 闪退日志远程奔溃接口，主要考虑不同app下，把log回传给服务器的方式不一样，所以此处留一个对外开放的接口
     */
    public static interface CrashExceptionRemoteReport {
        /**
         * 当闪退发生时，回调此接口函数
         *
         * @param file
         */
        public void onCrash(File file);
    }


//    /**
//     * Application配置
//     * 配置崩溃信息的搜集
//     */
//    private void configCollectCrashInfo() {
//        CrashExceptionHandler crashExceptionHandler = new CrashExceptionHandler(this, APP_MAIN_FOLDER_NAME, CRASH_FOLDER_NAME);
//        CrashExceptionHandler.CrashExceptionRemoteReport remoteReport = new SimpleCrashReporter();
//        crashExceptionHandler.configRemoteReport(remoteReport); //设置友盟统计报错日志回传到远程服务器上
//        Thread.setDefaultUncaughtExceptionHandler(crashExceptionHandler);
//    }
}
