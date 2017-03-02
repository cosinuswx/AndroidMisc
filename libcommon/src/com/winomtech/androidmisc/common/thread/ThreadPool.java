package com.winomtech.androidmisc.common.thread;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.winom.olog.Log;

import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @since 2015-04-08
 * @author kevinhuang 
 */
public class ThreadPool implements MMThreadPoolExecutor.IExecutorCallback {
	final static String TAG = "ThreadPool";

	static ThreadPool thiz;
	
	public static ThreadPool getInstance() {
		if (null == thiz) {
			synchronized (ThreadPool.class) {
				if (null == thiz) {
					thiz = new ThreadPool();
				}
			}
		}
		return thiz;
	}
	
	public static void post(Runnable runnable, String name) {
		getInstance().addTask(runnable, name, ThreadTask.THREAD_PRORITY_NORMAL);
	}
	
	public static void post(Runnable runnable, String name, int prority) {
		Log.i(TAG, "add task, runnable: %s, name: %s, prority: %d", runnable.toString(), name, prority);
		getInstance().addTask(runnable, name, prority);
	}

	public static void remove(Runnable runnable) {
		Log.i(TAG, "remote task, runnable: %s", runnable);
		getInstance().removeTask(runnable);
	}

	final static int	MSG_EXECUTE_TASK = 0;
	final static int	MAX_CORE_SIZE = 4;
	final static int	MAX_THREAD_SIZE = 16;				// 线程数
	final static int 	KEEP_ALIVE_NORMAL_THREAD = 2 * 60; 	// CORE线程在2分钟后清理

	/**
	 * 真正的执行器
	 */
	MMThreadPoolExecutor mPoolExecutor;

	/**
	 * 给FuThreadPoolExecutor用的队列，最大大小不能超过MAX_THREAD_SIZE
	 */
	PriorityBlockingQueue<Runnable> mExecutingQueue = new PriorityBlockingQueue<Runnable>(MAX_THREAD_SIZE + 1);

	/**
	 * 保存未塞给Executor的线程
	 */
	PriorityBlockingQueue<ThreadTask> mWaittingQueue = new PriorityBlockingQueue<ThreadTask>();

	/**
	 * 用来异步执行任务
	 */
	PoolHandler mHandler;

	/**
	 * 操作的锁
	 */
	final Object mLockObj = new Object();
	
	public ThreadPool() {
		int cpuCnt = getNumCores();
		if (cpuCnt > MAX_CORE_SIZE) {
			cpuCnt = MAX_CORE_SIZE;
		}
		mPoolExecutor = new MMThreadPoolExecutor(cpuCnt,
				MAX_THREAD_SIZE,
				KEEP_ALIVE_NORMAL_THREAD,
				TimeUnit.SECONDS,
				mExecutingQueue,
				this);
		Log.i(TAG, "normal thread timeout: " + mPoolExecutor.getKeepAliveTime(TimeUnit.SECONDS));

		HandlerThread handlerThread = new HandlerThread("pool_handler");
		handlerThread.start();
		mHandler = new PoolHandler(handlerThread.getLooper());
	}

	public void addTask(Runnable runnable, String name, int prority) {
		synchronized (mLockObj) {
			ThreadTask threadTask = new ThreadTask(runnable, name, prority);
			mWaittingQueue.add(threadTask);
			
			mHandler.sendEmptyMessage(MSG_EXECUTE_TASK);
		}
	}

	boolean removeTask(Runnable runnable) {
		if (null == runnable) {
			return false;
		}

		synchronized (mLockObj) {
			ThreadTask threadTask = null;
			for (Iterator<ThreadTask> it = mWaittingQueue.iterator(); it.hasNext(); ) {
				ThreadTask tmp = it.next();
				if (null != tmp && tmp.task.equals(runnable)) {
					threadTask = tmp;
					it.remove();
					break;
				} 
			}
			
			if (null != threadTask) {
				mPoolExecutor.remove(threadTask);
				return true;
			}
		}

		return false;
	}

	void executeTask() {
		synchronized (mLockObj) {
			Log.d(TAG, "executeTask, waitting queue size: " + mWaittingQueue.size());

			// 先检查下当前列表里面的任务是不是过多了
			if (mExecutingQueue.size() > MAX_THREAD_SIZE) {
				Log.i(TAG, "too many task in exectour queue");
				return;
			}
			
			if (mWaittingQueue.size() <= 0) {
				Log.d(TAG, "no task need to executor");
				return;
			}
			
			// 取栈顶的执行
			Iterator<ThreadTask> it = mWaittingQueue.iterator();
			if (it.hasNext()) {
				ThreadTask task = it.next();
				it.remove();
				
				mPoolExecutor.execute(task);
			}
			
			mHandler.sendEmptyMessage(MSG_EXECUTE_TASK);
		}
	}

	@Override
	public void beforeExecute(Thread t, Runnable r) {
		String name = ((ThreadTask) r).taskName;
		Log.d(TAG, "beforeExecute, name: %s, r: %s", name, r.toString());
		synchronized (mLockObj) {
			t.setName("ThreadPool_" + name);
		}
	}

	@Override
	public void afterExecute(Runnable r, Throwable t) {
		Log.d(TAG, "afterExecute, name: %s, r: %s", ((ThreadTask) r).taskName, r.toString());
		mHandler.sendEmptyMessage(MSG_EXECUTE_TASK);
	}
	
	class PoolHandler extends Handler {
		public PoolHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			if (MSG_EXECUTE_TASK == msg.what) {
				while (hasMessages(msg.what)) {
					removeMessages(msg.what);
				}

				executeTask();
			}
			super.handleMessage(msg);
		}	
	}

    /**
     * Gets the number of cores available in this device, across all processors.
     * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
     * @return The number of cores, or 1 if failed to get result
     */
    public static int getNumCores() {
        //Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                //Check if filename is "cpu", followed by a single digit number
                if(Pattern.matches("cpu[0-9]+", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }

        try {
            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            //Return the number of cores (virtual CPU devices)
            return files.length;
        } catch(Exception e) {
            //Default to return 1 core
            return 1;
        }
    }
}
