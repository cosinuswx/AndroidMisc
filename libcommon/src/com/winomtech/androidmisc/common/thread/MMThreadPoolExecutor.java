package com.winomtech.androidmisc.common.thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @since 2015-04-08
 * @author kevinhuang 
 */
public class MMThreadPoolExecutor extends ThreadPoolExecutor {
	final static String TAG = "FuThreadPoolExecutor";

	public interface IExecutorCallback {
		void beforeExecute(Thread t, Runnable r);
		void afterExecute(Runnable r, Throwable t);
	}

	IExecutorCallback mExecutorCb;

	public MMThreadPoolExecutor(int corePoolSize,
                                int maximumPoolSize,
                                long keepAliveTime,
                                TimeUnit unit,
                                BlockingQueue<Runnable> workQueue,
                                IExecutorCallback callback) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		mExecutorCb = callback;
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		if (null != mExecutorCb) {
			mExecutorCb.beforeExecute(t, r);
		}
		super.beforeExecute(t, r);
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		if (null != mExecutorCb) {
			mExecutorCb.afterExecute(r, t);
		}
		super.afterExecute(r, t);
	}
}
