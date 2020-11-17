package com.wangpo.billiard.enums;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wangpo.base.bean.BilliardProto;
import com.wangpo.base.bean.S2C;
import com.wangpo.billiard.logic.Cmd;
import com.wangpo.billiard.excel.ExcelMgr;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public enum ExcelEnum {
	//球杆
	CUE(1) {
		@Override
		public S2C config(int cid, ExcelMgr excelMgr){
			return getS2C(cid, 1,excelMgr.getCueMap());
		}
	},
	//球杆类型
	CUE_TYPE(2) {
		@Override
		public S2C config(int cid, ExcelMgr excelMgr){
			return getS2C(cid, 2,excelMgr.getCueTypeMap());
		}
	},
	//日常任务
	DAY_TASK(3) {
		@Override
		public S2C config(int cid, ExcelMgr excelMgr ){
//			return getS2C(cid, 3,excelMgr.getDayTaskMap());
			return null;
		}
	},
	//周任务
	WEEK_TASK(4) {
		@Override
		public S2C config(int cid, ExcelMgr excelMgr ){
//			return getS2C(cid,4, excelMgr.getWeekTaskMap());
			return null;
		}
	},
	GROW_TASK(5) {
		@Override
		public S2C config(int cid, ExcelMgr excelMgr ){
//			return getS2C(cid, 5,excelMgr.getGrewUpTaskMap());
			return null;
		}
	},
	DAY_ACTIVE(7) {
		@Override
		public S2C config(int cid, ExcelMgr excelMgr ){
//			return getS2C(cid, 7,excelMgr.getDayActiveMap());
			return null;
		}
	},
	WEEK_ACTIVE(8) {
		@Override
		public S2C config(int cid, ExcelMgr excelMgr){
//			return getS2C(cid, 8,excelMgr.getWeekActiveMap());
			return null;
		}
	},
	FILE_CODE(9) {
		@Override
		public S2C config(int cid, ExcelMgr excelMgr){
//			return getS2C(cid, 9,excelMgr.getFileCodeMap());
			return null;
		}
	},
	EXP(10) {
		@Override
		public S2C config(int cid, ExcelMgr excelMgr ){
//			return getS2C(cid,10, excelMgr.getDayActiveMap());
			return null;
		}
	},
	ROLE(11) {
		@Override
		public S2C config(int cid, ExcelMgr excelMgr ){
			return getS2C(cid,11, excelMgr.getROlE_MAP());
		}
	},
	ITEM(12) {
		@Override
		public S2C config(int cid, ExcelMgr excelMgr ){
			return getS2C(cid,12, excelMgr.getItemMap());
		}
	},
	CHANG(13) {
		@Override
		public S2C config(int cid, ExcelMgr excelMgr ){
			return getS2C(cid,13, excelMgr.getChangConfigMap());
		}
	},
	VIP(14) {
		@Override
		public S2C config(int cid, ExcelMgr excelMgr ){
//			return getS2C(cid,14, excelMgr.getMemberMap());
			return null;
		}
	},
	AWARD_POOL(15) {
		@Override
		public S2C config(int cid, ExcelMgr excelMgr ){
			return getS2C(cid,15, excelMgr.getAwardPoolMap());
		}
	},
	LUCKY_CUE(16) {
		@Override
		public S2C config(int cid, ExcelMgr excelMgr ){
			return getS2C(cid,16, excelMgr.getLuckyCueMap());
		}
	},
	GOODS(17) {
		@Override
		public S2C config(int cid, ExcelMgr excelMgr ){
//			return getS2C(cid,17, excelMgr.getGoodsMap());
			return null;
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

	public abstract S2C config(int cid, ExcelMgr excelMgr);

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}
}
