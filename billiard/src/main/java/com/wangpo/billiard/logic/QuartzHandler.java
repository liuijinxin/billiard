package com.wangpo.billiard.logic;

import com.wangpo.billiard.excel.ExcelMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class QuartzHandler {

	@Resource
	ExcelMgr excelMgr;

	@Scheduled(cron = "0 * * * * ?")
	public void reloadExcel(){
		excelMgr.hotReload();
	}
}
