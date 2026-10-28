package com.adminbase.common;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Ranges {

    private static void shift(String range, Calendar c) {
        if ("7d".equals(range)) c.add(Calendar.DAY_OF_MONTH, -7);
        else if ("30d".equals(range)) c.add(Calendar.DAY_OF_MONTH, -30);
        else c.add(Calendar.HOUR_OF_DAY, -24);
    }

    public static String from(String range) {
        Calendar c = Calendar.getInstance();
        shift(range, c);
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(c.getTime());
    }

    public static String prevFrom(String range) {
        Calendar c = Calendar.getInstance();
        shift(range, c);
        shift(range, c);
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(c.getTime());
    }

    public static String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
