package zj.baselibrary.util;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;

/**
 * Created by ucmed on 2016/9/26.
 */

public class DialogUtils {
    public static MaterialDialog.Builder showIndeterminateProgressDialog(Context context, boolean horizontal, String title, String content) {
        return new MaterialDialog.Builder(context)
                .title(title)
                .content(content)
                .progress(true, 0)
                .progressIndeterminateStyle(horizontal);
    }
}
