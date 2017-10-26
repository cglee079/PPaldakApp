package com.example.changoo.fishing.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by changoo on 2017-03-12.
 */

public class Formatter {

    public static double setFormat(double d) {
        return Double.parseDouble(String.format("%.2f", d));
    }

    public static String toParameter(String key, String value){
        return "&"+key+"="+value;
    }
    public static String toFirstParameter(String key, String value){
        return "?"+key+"="+value;
    }

    public static String toDate(long time) {
        SimpleDateFormat dayTime = new SimpleDateFormat("yyyy /MM /dd");
        String str = dayTime.format(new Date(time));
        return str;
    }

    public static String toTime(long time) {
        SimpleDateFormat dayTime = new SimpleDateFormat("hh: mm: ss");
        String str = dayTime.format(new Date(time));
        return str;
    }
    public static Integer toTimeDouble(long time) {
        SimpleDateFormat dayTime = new SimpleDateFormat("s");
        String str = dayTime.format(new Date(time));
        return Integer.valueOf(str);
    }

    public static String toDateTime(long time) {
        SimpleDateFormat dayTime = new SimpleDateFormat("yyyyMMddhhmmss");
        String str = dayTime.format(new Date(time));
        return str;
    }
}
