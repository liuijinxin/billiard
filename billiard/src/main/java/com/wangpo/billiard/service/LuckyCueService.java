package com.wangpo.billiard.service;


import com.wangpo.billiard.bean.LuckyCue;

public interface LuckyCueService {
	int insertLuckyCue(LuckyCue player);
	int updateLuckyCue(LuckyCue player);
	LuckyCue selectLuckyCueByID(Integer id);
}
