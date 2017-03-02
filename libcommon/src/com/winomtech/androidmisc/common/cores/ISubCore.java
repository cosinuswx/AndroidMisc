package com.winomtech.androidmisc.common.cores;

/**
 * @since 2015-04-11
 * @author kevinhuang 
 */
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
