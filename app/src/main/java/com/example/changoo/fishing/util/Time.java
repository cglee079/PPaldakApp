package com.example.changoo.fishing.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by changoo on 2017-03-05.
 */

public class Time {
    public static long capture() {
        return System.currentTimeMillis();
    }

    public static String getDate() {
        long time = System.currentTimeMillis();
        return Formatter.toDate(time);
    }

    public static String getTime() {
        long time = System.currentTimeMillis();
        return Formatter.toTime(time);

    }

    public static String getDateTime() {
        long time = System.currentTimeMillis();
        return Formatter.toDateTime(time);
    }

    public static String getCurMonth() {
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        SimpleDateFormat CurMonthFormat = new SimpleDateFormat("MM");
        String strCurMonth = CurMonthFormat.format(date);
        return strCurMonth;
    }

    public static String getCurYear() {
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        SimpleDateFormat CurYearFormat = new SimpleDateFormat("yyyy");
        String strCurYear = CurYearFormat.format(date);
        return strCurYear;

    }

    public static String getCurDay() {
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        SimpleDateFormat CurDayFormat = new SimpleDateFormat("dd");
        String strCurDay = CurDayFormat.format(date);
        return strCurDay;
    }


}
