package com.pro.switchlibrary;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    public static String dateToStamp() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String format = simpleDateFormat.format(date);
        return format;
    }

    public static String dateToStamp(long time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(time);
        String format = simpleDateFormat.format(date);
        return format;
    }

    public static String dateToStampDay(long time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(time);
        String format = simpleDateFormat.format(date);
        return format;
    }

    public static long getBeforeTen() {
        Calendar nowBefore = Calendar.getInstance();
        Calendar nowAfter = Calendar.getInstance();
        nowBefore.add(Calendar.MINUTE, 10);
        nowAfter.add(Calendar.MINUTE, -10);
        return nowBefore.getTimeInMillis();
    }

    public static long dateToStamp(String s) {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = simpleDateFormat.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long ts = date.getTime() / 1000;
        res = String.valueOf(ts);
        return ts;
    }

    /**
     * 提供了精確的減法運算
     *
     * @param
     */
    public static double sub(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2).doubleValue();
    }

    public static String matchNum(String a) {
        String regEx = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(a);

        return m.replaceAll("").trim();

    }

    /**
     * 提供精確的加法運算
     *
     * @param
     */
    public static double add(double v1, double v2) {
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2).doubleValue();
    }

    public static double div(double d1, double d2, int len) {// 进行除法运算
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.divide(b2, len, BigDecimal.ROUND_HALF_UP).doubleValue();
    }


    public static double divnoRound(double d1, double d2, int len) {// 进行除法运算
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.divide(b2, len, BigDecimal.ROUND_FLOOR).doubleValue();
    }

    public static double mul(double d1, double d2) {// 进行乘法运算
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.multiply(b2).doubleValue();
    }




    /**
     * Double类型转化为四舍五入6位小数
     *
     * @param
     * @return
     */
    public static double double1Point(double num) {

        BigDecimal b = new BigDecimal(String.valueOf(num));
        BigDecimal bg = b.setScale(1, BigDecimal.ROUND_HALF_UP);
        return bg.doubleValue();

    }

    public static String doublePoint(double d){
        if(Math.round(d)-d==0){
            return String.valueOf((long)d);
        }
        return String.valueOf(d);
    }

}
