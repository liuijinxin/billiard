package com.wangpo.net;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Channel管理器，用于存储当前网关服的所有websocket链接
 */
@Component("channelService")
public class ChannelService {
	private static final Map<Integer, ChannelHandlerContext> CHNNEL_MAP = new ConcurrentHashMap<>();

	public void addChannel(Integer uid,ChannelHandlerContext ctx) {
		CHNNEL_MAP.put(uid,ctx);
	}

	public ChannelHandlerContext getChannel(Integer uid) {
		if(null == uid) {
			return null;
		}
		return CHNNEL_MAP.get(uid);
	}
}
