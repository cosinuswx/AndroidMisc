package com.winomtech.androidmisc.sdk.asynccomponent;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;

import java.util.HashMap;
import java.util.Map;

/**
 * @since 2015-02-06
 * @author kevinhuang 
 * 任务的执行器
 */
public class TaskExecutor {
	TaskStateMachine		mStateMachine = new TaskStateMachine();
	SparseArray<NodeInfo>	mNodeArray = new SparseArray<NodeInfo>();
	Map<String, Object> 	mExtraData = new HashMap<String, Object>();
	Handler					mUiHandler = new Handler(Looper.getMainLooper());
	ExecutorCacllback		mCallback;

	/**
	 * 任务执行结束的回调
	 */
	public interface ExecutorCacllback {
		/**
		 * @param result 最后执行的结果
		 * @param data 跟随这个task的数据
		 */
		public void onTaskFinish(int result, Map<String, Object> data);
	}

	/**
	 * 增加状态节点
	 * @param nodeId 节点的值
	 * @param task 这个节点所需要执行的任务
	 * @param inMainThread 这个任务是在主线程处理，还是在任意线程   
	 * @throws MalTaskException 当节点已经存在时，抛出该异常
	 */
	public void addStateNode(int nodeId, ITask task, boolean inMainThread) throws MalTaskException {
		if (mNodeArray.indexOfKey(nodeId) >= 0) {
			throw new MalTaskException("nodeId " + nodeId + " already added to state machine");
		}
		mNodeArray.put(nodeId, new NodeInfo(nodeId, task, inMainThread));
	}

	/**
	 * 添加转换规则
	 * @param curId 当前节点的id
	 * @param action 动作，或者说是task执行的返回值
	 * @param newId 新的节点的id
	 * @throws MalTaskException 当规则已经存在时，抛出该异常
	 */
	public void addTransRule(int curId, int action, int newId) throws MalTaskException {
		if (mStateMachine.haveRuleForAction(curId, action)) {
			throw new MalTaskException("curId " + curId + " for action " + action + " already have rule");
		}
		mStateMachine.addTransformRule(curId, action, newId);
	}

	/**
	 * 开始执行
	 * @param startId 指定开始的节点
	 * @param extraData 指定初始化数据，可为null
	 * @param callback 整个任务结束后的回掉，可为null   
	 */
	public void execute(int startId, Map<String, Object> extraData, ExecutorCacllback callback) {
		mCallback = callback;
		if (null != extraData) {
			mExtraData.putAll(extraData);
		}
		mStateMachine.setInitState(startId);
		executeInternal();
	}

	void executeInternal() {
		final NodeInfo info = mNodeArray.get(mStateMachine.getState());
		if (info.inMainThread) {
			mUiHandler.post(new Runnable() {
				@Override
				public void run() {
					moveToNext(info.task.run(mExtraData));
				}
			});
		} else {
			new AsyncTask<Void, Void, Void>() {

				@Override
				protected Void doInBackground(Void... params) {
					moveToNext(info.task.run(mExtraData));
					return null;
				}
			}.execute();
		}
	}

	void moveToNext(int result) {
		if (!mStateMachine.haveRuleForAction(mStateMachine.getState(), result)) {
			if (null != mCallback) {
				mCallback.onTaskFinish(result, mExtraData);
			}
			return;
		}
		
		mStateMachine.tranform(result);
		executeInternal();
	}

	static class NodeInfo {
		public int		id;
		public ITask	task;
		public boolean	inMainThread;
		
		public NodeInfo(int id, ITask task, boolean inMainThread) {
			this.id = id;
			this.task = task;
			this.inMainThread = inMainThread;
		}
	}

}
