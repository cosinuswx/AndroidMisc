package com.winomtech.androidmisc.common.utils;

import java.util.Stack;

/**
 * @since 2015年10月09日
 * @author kevinhuang 
 * 
 * 缓存对象,可重复利用回收的对象,减少GC的触发
 */
public abstract class ObjectCacher<T> {
	/**
	 * 外部提供的,当缓存的对象不够的时候,通过这个来创建新的实例
	 **/
	protected abstract T newInstance();

	int mCacheCnt;

	final Stack<T> mObjCacheStack = new Stack<>();

	/**
	 * 构建一个可缓存对象的实例
	 * 
	 * @param cacheCnt 缓存的最大数目
	 */
	public ObjectCacher(int cacheCnt) {
		mCacheCnt = cacheCnt;
	}

	public T obtain() {
		T obj = null;
		synchronized (mObjCacheStack) {
			if (0 != mObjCacheStack.size()) {
				obj = mObjCacheStack.pop();
			}
		}
		
		if (null == obj) {
			obj = newInstance();
		}
		
		return obj;
	}

	public void cache(T obj) {
		synchronized (mObjCacheStack) {
			mObjCacheStack.push(obj);
		}
	}
}
