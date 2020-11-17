package com.wangpo.platform.enums;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wangpo.base.bean.BilliardProto;
import com.wangpo.base.bean.S2C;
import com.wangpo.base.excel.ShopConfig;
import com.wangpo.platform.excel.BaseExcelMgr;
import com.wangpo.platform.service.Cmd;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public enum ExcelEnum {
	CUE(1) {
		public S2C config(int cid){
			return getS2C(cid, 1,BaseExcelMgr.CUE_MAP);
		}
	},
	CUE_TYPE(2) {
		public S2C config( int cid ){
			return getS2C(cid, 2,BaseExcelMgr.CUE_TYPE_MAP);
		}
	},
	DAY_TASK(3) {
		public S2C config(int cid ){
//			return getS2C(cid, 3,excelMgr.getDayTaskMap());
			return getS2C(cid, 3,BaseExcelMgr.DAY_TASK_MAP);
		}
	},
	WEEK_TASK(4) {
		public S2C config(int cid ){
			return getS2C(cid, 4,BaseExcelMgr.WEEK_TASK_MAP);
		}
	},
	GROW_TASK(5) {
		public S2C config(int cid ){
			return getS2C(cid, 5,BaseExcelMgr.GROWTH_TASK_MAP);
		}
	},
	DAY_ACTIVE(7) {
		public S2C config(int cid ){
			return getS2C(cid, 7,BaseExcelMgr.DAY_ACTIVE_MAP);
		}
	},
	WEEK_ACTIVE(8) {
		public S2C config( int cid){
//			return getS2C(cid, 8,excelMgr.getWeekActiveMap());
			return getS2C(cid, 8,BaseExcelMgr.WEEK_ACTIVE_MAP);
		}
	},
	FILE_CODE(9) {
		public S2C config( int cid){
//			return getS2C(cid, 9,excelMgr.getFileCodeMap());
			return getS2C(cid, 9,BaseExcelMgr.FILE_CODE_MAP);
		}
	},
	EXP(10) {
		public S2C config(int cid ){
//			return getS2C(cid,10, excelMgr.getDayActiveMap());
			return getS2C(cid, 10,BaseExcelMgr.DAY_ACTIVE_MAP);
		}
	},
	ROLE(11) {
		public S2C config(int cid ){
			return getS2C(cid,11, BaseExcelMgr.ROlE_MAP);
		}
	},
	ITEM(12) {
		public S2C config(int cid ){
			return getS2C(cid,12, BaseExcelMgr.ITEM_MAP);
		}
	},
	CHANG(13) {
		public S2C config(int cid ){
			return getS2C(cid,13, BaseExcelMgr.CHANG_CONFIG_MAP);
		}
	},
	VIP(14) {
		public S2C config(int cid ){
			return getS2C(cid,14, BaseExcelMgr.MEMBER_MAP);
		}
	},
	AWARD_POOL(15) {
		public S2C config(int cid ){
			return getS2C(cid,15, BaseExcelMgr.AWARD_POOL_MAP);
		}
	},
	LUCKY_CUE(16) {
		public S2C config(int cid ){
			return getS2C(cid,16, BaseExcelMgr.LUCKY_CUE_MAP);
		}
	},
	GOODS(17) {
		public S2C config(int cid ){
			return getS2C(cid,17, BaseExcelMgr.GOODS_MAP);
		}
	},
	SHOP(18) {
		public S2C config(int cid){
			return getS2C(cid,18, BaseExcelMgr.SHOP_CONFIG_MAP);
		}
	},
	;

	private static S2C getS2C(int cid, int type,Map map) {
		BilliardProto.S2C_GetConfig.Builder b = BilliardProto.S2C_GetConfig.newBuilder();
		b.setConfigType(type);
//		b.setBody(jsonObject.toString());
		b.setBody(((JSONObject)JSON.toJSON(map)).toJSONString());
		S2C s2c = new S2C();
		s2c.setCid(Cmd.REQ_CONFIG);
		s2c.setCid(cid);
		s2c.setBody(b.build().toByteArray());
		return s2c;
	}

	private int code;
	ExcelEnum(int code) {
		this.code = code;
	}

	public abstract S2C config(int cid);

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
}
