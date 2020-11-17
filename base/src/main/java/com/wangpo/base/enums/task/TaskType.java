package com.wangpo.base.enums.task;

public enum TaskType {
	DAY(1),
	WEEK(2),
	GROWING(3),
	;
	public int code;

	TaskType(int code) {
		this.code = code;
	}
}
