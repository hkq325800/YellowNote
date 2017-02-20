package com.kerchin.global;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by hkq325800 on 2017/2/20.
 */

public class DateUtil {

    /**
     * 获取匹配字符串格式的日期字符串
     *
     * @param matchStr 匹配字符串
     * @return String
     */
    public static String getDateStr(Date date, String matchStr) {
        SimpleDateFormat myFmt = new SimpleDateFormat(matchStr, Locale.CHINA);
        return myFmt.format(date);
    }

    /**
     * 返回Note中的日期字符串
     *
     * @param date Date类型
     * @return String
     */
    public static String getDateString(Date date) {
        Calendar now = Calendar.getInstance();
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        SimpleDateFormat formatter = new SimpleDateFormat(" HH:mm:ss", Locale.CHINA);
        if (c.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
            int d1 = c.get(Calendar.DAY_OF_YEAR);
            int d2 = now.get(Calendar.DAY_OF_YEAR);
            if (d1 == d2) {
                return "今天" + formatter.format(date);
            } else if (d2 - d1 == 1) {
                return "昨天" + formatter.format(date);
            } else if (d2 - d1 == 2) {
                return "前天" + formatter.format(date);
            }
        }
        formatter = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);/* HH:mm:ss*/
        return formatter.format(date);
    }
}
