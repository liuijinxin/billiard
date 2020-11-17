package com.wangpo.base.kits;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;


/**
 * 格式化工具类
 */
public class FormatKit {
    //台球坐标格式化，保留小数点后三位
    public static final DecimalFormat pos =  new DecimalFormat("0.000");
    //日期，不要使用SimpleDateFormat，线程不安全
    public static final  DateTimeFormatter dateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final  DateTimeFormatter dayTime = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    //返回今天的10位日期格式
    public static String today10(){
        return LocalDateTime.now().format(FormatKit.dayTime);
    }


    public static String today10(Date date){
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).format(FormatKit.dayTime);
    }

    //返回今天的19位日期格式
    public static String today19(){
        return LocalDateTime.now().format(FormatKit.dateTime);
    }

    //上次周一的日期
    public static String lastMonday(){
        LocalDateTime dt = LocalDateTime.now();
        //System.out.println("today:"+dt.getDayOfWeek().getValue());
        int days = dt.getDayOfWeek().getValue() - 1;
        LocalDateTime dt2 = dt.minusDays(days);
        return dt2.format(FormatKit.dayTime);
    }

    public static int hour(){
        return LocalDateTime.now().getHour();
    }

    /**
     * 返回多少年后的时间
     * @return
     */
    public static Date nextYears(int year){
        LocalDateTime dt = LocalDateTime.now();
        LocalDateTime next  = dt.plusYears(year);
        return Date.from(next.atZone(ZoneId.systemDefault()).toInstant());

//        String day = "2030-12-01 23:59:59";
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//        try {
//            return simpleDateFormat.parse(day);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
    }



    public static void main(String[] args) {
        System.out.println(nextYears(10).getTime());
//        double a = 1.23451;
//        System.out.println(FormatKit.pos.format(a));
//        System.out.println(LocalDateTime.now().format(FormatKit.dateTime));
//        System.out.println(LocalDateTime.now().format(FormatKit.dayTime));
    }
}
