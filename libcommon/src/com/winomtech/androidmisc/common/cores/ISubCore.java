package com.winomtech.androidmisc.common.cores;

public interface ISubCore {
	/**
	 * 帐号初始化好了
	 */
	void onAccountPostSet();

	/**
	 * 帐号需要注销了
	 */
	void onAccountPreRelease();
}
