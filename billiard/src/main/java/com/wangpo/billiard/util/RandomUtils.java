package com.wangpo.billiard.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomUtils {
	static Random random = new Random();
	public static List<Integer> randomTwo(List<Integer> list) {
		if( list==null ||list.size()<2) {
			return null;
		}
		List<Integer> r = new ArrayList<>();
		int index = random.nextInt(list.size());
		r.add(list.get(index));
		list.remove(index);

		if( list.size()==1 ) {
			r.add(list.get(0));
			return r;
		}
		int index2 = random.nextInt(list.size());
		r.add(list.get(index2));
		return r;
	}

	public static List<Integer> randomThree(List<Integer> list) {
		if( list==null ||list.size()<3) {
			return null;
		}
		List<Integer> r = new ArrayList<>();
		int index = random.nextInt(list.size());
		r.add(list.get(index));
		list.remove(index);

		int index2 = random.nextInt(list.size());
		r.add(list.get(index2));
		list.remove(index2);

		if( list.size()==3 ) {
			r.add(list.get(0));
			return r;
		}
		int index3 = random.nextInt(list.size());
		r.add(list.get(index3));
		return r;
	}

	public static void main(String[] args) {
		List<Integer> list = new ArrayList<>();
		for(int i=0;i<3;i++) {
			list.add(i);
		}
		List<Integer> a = randomThree(list);
		System.out.println(a);
	}
}
