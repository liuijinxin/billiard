package com.wangpo.service;

import com.wangpo.base.service.BilliardService;
import com.wangpo.base.service.PlatformService;
import com.wangpo.base.bean.C2S;
import com.wangpo.base.bean.S2C;
import com.wangpo.base.bean.CommonUser;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

/**
 * HandlerService
 * 统一处理器，代理 dubbo rpc 服务方法
 */
@Service
public class HandlerService {
	@DubboReference
	private BilliardService service ;
	@DubboReference
	private PlatformService platformService;

	public S2C request(C2S c2s) {
		return service.request(c2s);
	}

	public void logout(int uid) {
		service.logout(uid);
		platformService.logout(uid);
	}

	public S2C requestPlatform(C2S c2s) {
		return platformService.request(c2s);
	}

	/**
	 * 查询平台用户信息
	 * @param c2s
	 * @return
	 */
	public CommonUser queryUser(C2S c2s) {
		return platformService.login(c2s);
	}
	
	public boolean getUserName(int id) {
		return platformService.getUserName(id);
	}
	
	public void afterLogin(int id) {
		platformService.afterLogin(id);
	}
}
