package com.winomtech.androidmisc.sdk.asynccomponent;

import java.util.Map;

/**
 * @since 2015-02-05
 * @author kevinhuang
 * 定义task的相关接口
 */
public interface ITask {
	/**
	 * 一个task的动作执行的地方
	 * @param data 整个状态机的数据，这个数据一直跟随着状态机流转
	 * @return 本次动作执行的结果，用来选择下一步是什么状态
	 */
	public int run(Map<String, Object> data);
}
