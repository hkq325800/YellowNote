package com.kerchin.global;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by hkq325800 on 2017/3/7.
 */

public class ShareUtil {

    private static String pkgName;
    private static String className;

    private void getThings(Activity activity, ImageView imageView) {
        Intent intent = new Intent(Intent.ACTION_SEND, null);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setType("image/*");
        PackageManager packageManager = activity.getPackageManager();
        List<ResolveInfo> mApps = packageManager.queryIntentActivities(intent, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);

        for (ResolveInfo info : mApps) {
            if ("com.tencent.mm.ui.tools.ShareToTimeLineUI".equals(info.activityInfo.name)) {
                imageView.setImageDrawable(info.loadIcon(packageManager));
                pkgName = info.activityInfo.packageName;
                className = info.activityInfo.name;
//                Log.e("log", System.currentTimeMillis()+"");
                break;
            }
        }
    }

    public void share(Activity activity, String pkgName, String classname, Uri uri) {
        if (TextUtils.isEmpty(pkgName) || TextUtils.isEmpty(className))
            return;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setComponent(new ComponentName(pkgName, classname));
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "hhh");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        activity.startActivity(shareIntent);
    }
}
