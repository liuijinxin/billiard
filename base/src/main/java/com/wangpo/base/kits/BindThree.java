package com.wangpo.base.kits;

import lombok.Data;

@Data
public class BindThree<T> {
	private T obj1;
	private T obj2;
	private T obj3;
	public BindThree(T o1, T o2, T o3) {
		this.obj1 = o1;
		this.obj2 = o2;
		this.obj3 = o3;
	}

	public BindThree(){}
}
