package zj.remote.baselibrary.util;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.IdRes;

/**
 * Created by hkq325800 on 2017/3/8.
 */

public class UriUtil {
    public static Uri getLocalResUri(Context context, @IdRes int id){
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + context.getResources().getResourcePackageName(id) + "/"
                + context.getResources().getResourceTypeName(id) + "/"
                + context.getResources().getResourceEntryName(id));
    }
}
