package com.kerchin.yellownote.global;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.avos.avoscloud.AVOSCloud;
import com.kerchin.yellownote.bean.SimpleFolder;
import com.kerchin.yellownote.bean.SimpleNote;
import com.kerchin.yellownote.model.Folder;
import com.kerchin.yellownote.model.Note;
import com.kerchin.yellownote.utilities.CrashHandler;
import com.kerchin.yellownote.utilities.NormalUtils;
import com.securepreferences.SecurePreferences;

import java.util.ArrayList;
import java.util.List;

public class MyApplication extends Application {
    private static Context context;
    private static SharedPreferences shared;
    private static final String SaltKey = "xiaohuangj";
    private static boolean isLogin = false;
    public static ArrayList<Folder> listFolder;
    public static ArrayList<Note> listNote;
    public static List<SimpleNote> mItems;
    public static String user;
    public static String userDefaultFolderId = "";
    public static String view = "note";
    //public static final int pageLimit = 5;
    public static int thisPosition = 0;
    public static boolean isItemsReady = false;
    /*public static final String KEY_PROV = "xiaohuangjProv";
    public static final String KEY_ISFLOAT = "isFloat";
    public static final String KEY_ISFLOAT = "isFloat";
    public static final String KEY_ISFLOAT = "isFloat";*/

    public static Context getContext() {
        return context;
    }

    public static void getItemsReady() {
        if (MyApplication.listNote != null)
            MyApplication.mItems = new ArrayList<SimpleNote>();
            for (int i = 0; i < MyApplication.listNote.size(); i++) {
                MyApplication.mItems.add(new SimpleNote(i
                        , MyApplication.listNote.get(i).getTitle()
                        , MyApplication.listNote.get(i).getFolderId()));
            }
    }

    @Override
    public void onCreate() {
        context = getApplicationContext();
        AVOSCloud.initialize(context,
                Config.APP_ID, Config.APP_KEY);
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(context);
        shared = new SecurePreferences(context);
        user = shared.getString(Config.KEY_User, "");
        if (!user.equals(""))
            isLogin = true;
        super.onCreate();
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
        //清除密码缓存
        SecurePreferences.Editor editor = (SecurePreferences.Editor) shared.edit();
//        editor.putString(Config.KEY_User, "");
        editor.putString(Config.KEY_PASS, "");
        editor.apply();
    }

    public static Folder getFolder(String folderId) {
        for (int i = 0; i < listFolder.size(); i++) {
            if (listFolder.get(i).getObjectId().equals(folderId))
                return listFolder.get(i);
        }
        return null;
    }

    public static Note getNote(String objectId) {
        for (int i = 0; i < listNote.size(); i++) {
            if (listNote.get(i).getObjectId().equals(objectId))
                return listNote.get(i);
        }
        return null;
    }

    public static String getUserDefaultFolderId() {
        return "";
    }
}
