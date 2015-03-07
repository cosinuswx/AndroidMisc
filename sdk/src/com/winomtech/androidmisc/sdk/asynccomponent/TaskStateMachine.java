package com.winomtech.androidmisc.sdk.asynccomponent;

import android.annotation.SuppressLint;

import com.winomtech.androidmisc.sdk.utils.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * 状态机实现
 * @author kevinhuang 
 */

public class TaskStateMachine {
	final static String TAG = TaskStateMachine.class.getSimpleName();

	int		mState;

	Map<Integer, Map<Integer, Integer>>		mTransRules;

	public void setInitState(int initState) {
		mState = initState;
	}

	@SuppressLint("UseSparseArrays")
	public void addTransformRule(int oldState, int action, int newState) {
		if (null == mTransRules) {
			mTransRules = new HashMap<Integer, Map<Integer,Integer>>();
		}

		Map<Integer, Integer> secondMap;
		if (!mTransRules.containsKey(oldState)) {
			secondMap = new HashMap<Integer, Integer>();
			mTransRules.put(oldState, secondMap);
		} else {
			secondMap = mTransRules.get(oldState);
		}

		secondMap.put(action, newState);
	}

	public boolean haveRuleForAction(int oldState, int action) {
		if (null == mTransRules || !mTransRules.containsKey(oldState)) {
			return false;
		}

		Map<Integer, Integer> secondMap = mTransRules.get(oldState);
		if (!secondMap.containsKey(action)) {
			return false;
		}

		return true;
	}

	public void tranform(int action) {
		if (!haveRuleForAction(mState, action)) {
			Log.e(TAG, "state: %d don't contain rule for action: %d", mState, action);
			return;
		}

		int newState = mTransRules.get(mState).get(action);
		Log.d(TAG, "from oldState: %d to newState: %d, action: %d", mState, newState, action);
		mState = newState;
	}

	public int getState() {
		return mState;
	}
}
