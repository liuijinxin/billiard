package com.wangpo.billiard.logic.room;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LinkMgr {
	private static final Map<String, ChannelHandlerContext> map = new ConcurrentHashMap<>();

	public void putLink(String url, ChannelHandlerContext ctx) {
		map.put(url, ctx);
	}

	public ChannelHandlerContext getLink(String url) {
		return map.get(url);
	}
}
