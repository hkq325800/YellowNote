package zj.remote.baselibrary.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;

/**
 * flags是用来标识在 Span 范围内的文本前后输入新的字符时是否把它们也应用这个效果。
 * 分别有 Spanned.SPAN_EXCLUSIVE_EXCLUSIVE(前后都不包括)、
 * Spanned.SPAN_INCLUSIVE_EXCLUSIVE(前面包括，后面不包括)、
 * Spanned.SPAN_EXCLUSIVE_INCLUSIVE(前面不包括，后面包括)、
 * Spanned.SPAN_INCLUSIVE_INCLUSIVE(前后都包括)
 * Created by hkq325800 on 2016/12/2.
 */

public class SpannableUtil {

    private SpannableUtil() {
    }

    /**
     * 设置文本背景色
     *
     * @param context
     * @param str
     * @param colorRes
     * @return
     */
    public static Spannable setBackGroundColor(Context context, String str, @ColorRes int colorRes, int start, int end) {
        Spannable spannable = new SpannableString(str);
        return setBackGroundColor(context, spannable, colorRes, start, end);
    }

    /**
     * 设置文本背景色
     *
     * @param context
     * @param colorRes
     * @return
     */
    public static Spannable setBackGroundColor(Context context, Spannable old, @ColorRes int colorRes, int start, int end) {
        old.setSpan(new BackgroundColorSpan(context.getResources().getColor(colorRes))
                , start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return old;
    }

    /**
     * 设置文本url
     * 电话"tel:4155551212"
     * 邮件"mailto:webmaster@google.com"
     * 网络"http://www.baidu.com"
     * 短信"sms:4155551212"
     * 地图"geo:38.899533,-77.036476"
     * 需要注意对TextView设置.setMovementMethod(LinkMovementMethod.getInstance());
     *
     * @param str
     * @param url
     * @return
     */
    public static Spannable setUrlSpan(String str, String url, int start, int end) {
        Spannable spannable = new SpannableString(str);
        return setUrlSpan(spannable, url, start, end);
    }

    /**
     * 设置文本url
     * 电话"tel:4155551212"
     * 邮件"mailto:webmaster@google.com"
     * 网络"http://www.baidu.com"
     * 短信"sms:4155551212"
     * 地图"geo:38.899533,-77.036476"
     * 需要注意对TextView设置.setMovementMethod(LinkMovementMethod.getInstance());
     *
     * @param url
     * @return
     */
    public static Spannable setUrlSpan(Spannable spannable, String url, int start, int end) {
        spannable.setSpan(new URLSpan(url), start, end
                , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    /**
     * 设置文本的style
     * NORMAL BOLD ITALIC BOLD_ITALIC
     *
     * @param str
     * @param typeface {@link android.graphics.Typeface}
     * @return
     */
    public static Spannable setStyle(String str, int typeface, int start, int end) {
        Spannable spannable = new SpannableString(str);
        return setStyle(spannable, typeface, start, end);
    }

    /**
     * 设置文本的style
     * NORMAL BOLD ITALIC BOLD_ITALIC
     *
     * @param typeface {@link android.graphics.Typeface}
     * @return
     */
    public static Spannable setStyle(Spannable spannable, int typeface, int start, int end) {
        spannable.setSpan(new StyleSpan(typeface), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    /**
     * 设置文本字体大小
     *
     * @param str
     * @param textSize
     * @param isDp
     * @return
     */
    public static Spannable setSize(String str, int textSize, boolean isDp, int start, int end) {
        Spannable spannable = new SpannableString(str);
        return setSize(spannable, textSize, isDp, start, end);
    }

    /**
     * 设置文本字体大小
     *
     * @param textSize
     * @param isDp
     * @return
     */
    public static Spannable setSize(Spannable spannable, int textSize, boolean isDp, int start, int end) {
        spannable.setSpan(new AbsoluteSizeSpan(textSize, isDp), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    /**
     * 设置文本下划线
     *
     * @param str
     * @return
     */
    public static Spannable setUnderline(String str, int start, int end) {
        Spannable spannable = new SpannableString(str);
        return setUnderline(spannable, start, end);
    }

    /**
     * 设置文本下划线
     *
     * @param spannable
     * @return
     */
    public static Spannable setUnderline(Spannable spannable, int start, int end) {
        spannable.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    /**
     * 设置文本下划线
     *
     * @param str
     * @return
     */
    public static Spannable setDeleteLine(String str, int start, int end) {
        Spannable spannable = new SpannableString(str);
        return setDeleteLine(spannable, start, end);
    }

    /**
     * 设置文本下划线
     *
     * @param spannable
     * @return
     */
    public static Spannable setDeleteLine(Spannable spannable, int start, int end) {
        spannable.setSpan(new StrikethroughSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    /**
     * 为Spannable设置图片（使用图片默认的大小）
     *
     * @param context
     * @param spannable
     * @param imageRes
     * @param start
     * @param end
     * @return
     */
    public static Spannable setDrawable(Context context, Spannable spannable, @DrawableRes int imageRes,
                                        int start, int end) {
        Drawable drawable = context.getResources().getDrawable(imageRes);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        spannable.setSpan(new ImageSpan(drawable), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    /**
     * 为Spannable设置图片（可设置图片大小）
     *
     * @param context
     * @param spannable
     * @param imageRes
     * @param start
     * @param end
     * @return
     */
    public static Spannable setDrawable(Context context, Spannable spannable, @DrawableRes int imageRes,
                                        int start, int end, int drawableLeft, int drawableTop, int drawableRight, int drawableBottom) {
        Drawable drawable = context.getResources().getDrawable(imageRes);
        drawable.setBounds(drawableLeft, drawableTop, drawableRight, drawableBottom);
        spannable.setSpan(new ImageSpan(drawable), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }
}
