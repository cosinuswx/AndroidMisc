package com.winomtech.androidmisc.common.thread;

/**
 * @since 2015-03-10
 * @author kevinhuang 
 */
public class ThreadTask implements Runnable, Comparable<ThreadTask> {
	final static String TAG = "ThreadTask";

	// 只定义三个优先级
	public static int THREAD_PRORITY_LOWEST = -10;
	public static int THREAD_PRORITY_NORMAL = 0;
	public static int THREAD_PRORITY_HIGH = 10;

	// 10s优先级升高一个
	public static int THREAD_PRORITY_COEFFICIENT = 10 * 1000;

	/**
	 * 任务的优先级，会随着时间而变动
	 */
	int orgPrority;

	/**
	 * 真正执行的任务实体
	 */
	public Runnable task;
	
	/**
	 * 任务名
	 */
	public String taskName;

	/**
	 * 添加的时刻
	 */
	public long addTime;

	public ThreadTask(Runnable task, String taskName, int prority) {
		this.task = task;
		this.orgPrority = prority;
		this.taskName = taskName;
		this.addTime = System.currentTimeMillis();
	}

	@Override
	public void run() {
		task.run();
	}

	@Override
	public int compareTo(ThreadTask otherTask) {
		return otherTask.getPrority() - getPrority();
	}
	
	public int getPrority() {
		int prority = orgPrority;
		prority = (int) (prority + (System.currentTimeMillis() - addTime) / THREAD_PRORITY_COEFFICIENT);
		if (prority < THREAD_PRORITY_LOWEST) {
			prority = THREAD_PRORITY_LOWEST;
		} else if (prority > THREAD_PRORITY_HIGH) {
			prority = THREAD_PRORITY_HIGH;
		}
		return prority;
	}
}
