package com.wangpo.billiard.logic.excel;

import com.wangpo.billiard.enums.ExcelEnum;
import com.wangpo.base.bean.BilliardProto;
import com.wangpo.base.bean.C2S;
import com.wangpo.base.bean.S2C;
import com.wangpo.billiard.excel.ExcelMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class ExcelHandler {
	@Resource
	ExcelMgr excelMgr;

	public S2C reqConfig(C2S c2s) throws Exception{
		BilliardProto.C2S_GetConfig proto = BilliardProto.C2S_GetConfig.parseFrom(c2s.getBody());
		int type = proto.getConfigType();

		for(ExcelEnum e:ExcelEnum.values()) {
			if( type == e.getCode()) {
				return e.config(c2s.getCid(),excelMgr);
			}
		}
		return null;
	}
}
