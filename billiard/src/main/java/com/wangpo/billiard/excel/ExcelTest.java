package com.wangpo.billiard.excel;

import com.alibaba.excel.EasyExcel;
import com.wangpo.base.excel.MemberConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ExcelTest {

  public static void main(String[] args) throws Exception {
    //存放数据的Excel表名，映射的类
    Map<String,Object> map = new HashMap<>();
    map.put("MemberConfig", MemberConfig.class);
    ExcelMgr excelMgr = new ExcelMgr();
    String path3 = System.getProperty("user.dir");
    String prefix = path3 + File.separator+"excel"+File.separator;
//    for (Map.Entry<String, Object> entry : map.entrySet()) {
//      InputStream inputStream = new FileInputStream(new File(prefix + entry.getKey() + ".xlsx"));
//      DataListener listener = new DataListener();
//      EasyExcel.read(inputStream, (Class) entry.getValue(), listener).sheet().doRead();
//      System.out.println(listener.list().size());
//      if ("MemberConfig".equals(entry.getKey())) {
//        excelMgr.storeMember(listener.list());
//      }
//    }
//    Map<Integer, MemberConfig> MemberConfigMap = excelMgr.getMemberMap();
//    for (Map.Entry<Integer, MemberConfig> entry : MemberConfigMap.entrySet()) {
//      System.out.println(entry.getKey() + ":" +  entry.getValue());
//    }
//    int point = 10050;
//    int vipLevel = 0;
//    for (MemberConfig value : MemberConfigMap.values()) {
//      if (point > value.getPoint()) {
//        vipLevel = value.getId();
//      } else if (point < value.getPoint()) {
//        break;
//      }
//    }
//    System.out.println("vip等级为：" + vipLevel);

//
//    Map<Integer, CueData> cueMap = excelMgr.getCueMap();
//    for (CueData cueData : cueMap.values()) {
//      System.out.println(cueData);
//    }
//    Map<Integer, CueTypeData> cueTypeMap = excelMgr.getCueTypeMap();
//    for (CueTypeData cueTypeData : cueTypeMap.values()) {
//      System.out.println(cueTypeData);
//    }

  }
}
