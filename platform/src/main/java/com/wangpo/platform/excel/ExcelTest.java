package com.wangpo.platform.excel;

import com.alibaba.excel.EasyExcel;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ExcelTest {

  public static void main(String[] args) throws Exception {
    int day1 = 1;
    int day2 = 2;
    int day3 = 3;
    int status1 = 1 << day1;
    int status2 = 1 << day2;
    int status3 = 1 << day3;
    System.out.println(status1 >> day1 & 1);
    System.out.println(status2 & 2);
    System.out.println(status3 & 2);
    System.out.println((status1 + status3)  >> day3 & 1);

    //存放数据的Excel表名，映射的类
//    Map<String,Object> map = new HashMap<>();
//    BaseExcelMgr excelMgr = new BaseExcelMgr();
//    String path3 = System.getProperty("user.dir");
//    String prefix = path3 + File.separator+"excel"+File.separator;
//    for (Map.Entry<String, Object> entry : map.entrySet()) {
//      InputStream inputStream = new FileInputStream(new File(prefix + entry.getKey() + ".xlsx"));
//      DataListener listener = new DataListener();
//      EasyExcel.read(inputStream, (Class) entry.getValue(), listener).sheet().doRead();
//      System.out.println(listener.list().size());
//    }


  }
}
