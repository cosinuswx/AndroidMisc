package com.winomtech.androidmisc.view;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.winomtech.androidmisc.common.utils.SmTimer;

/**
 * @since 2015年11月05日
 * @author kevinhuang 
 */
public class TouchToShowLayout extends RelativeLayout {

	/**
	 * item信息的提供接口,使用者需要提供下当前点击的是谁
	 */
	public interface IItemInfoProvider {
		int getItemOfPoint(float x, float y);
	}

	/**
	 * 回调接口
	 */
	public interface ITouchListener {
		/**
		 * 触发了长按
		 * @param pos 当前长按的item的下标
		 * @return 如果显示了全屏内容,则返回true,否则返回false
		 */
		boolean onLongTouch(int pos);

		/**
		 * 长按结束
		 */
		void onTouchEnd();

		/**
		 * 长按的时候,点击了下
		 */
		void onClkWhenLongTouch();
	}

	SmTimer mSmTimer = null;
	boolean mInLongTouch = false;
	boolean mNeedCallLongTouch = false;

	float mLastX = 0;
	float mLastY = 0;
	float mDownX = 0;
	float mDownY = 0;

	IItemInfoProvider mItemInfoProvider = null;
	ITouchListener mTouchListener = null;

	SmTimer.SmTimerCallback mSmTimerCallback = new SmTimer.SmTimerCallback() {
		@Override
		public void onTimeout() {
			// Log.d("test", "onTimeout");
			mInLongTouch = true;
			mNeedCallLongTouch = true;
			mSmTimer.stopTimer();
			
			// 触发一个移动操作,触发系统的onInterceptTouchEvent
			long now = SystemClock.uptimeMillis();
			MotionEvent event = MotionEvent.obtain(now, now, MotionEvent.ACTION_MOVE, mLastX, mLastY, 0);
			dispatchTouchEvent(event);
			event.recycle();
		}
	};

	void startTimer() {
		// Log.d("test", "startTimer");
		if (null == mSmTimer) {
			mSmTimer = new SmTimer(mSmTimerCallback);
		}
		mSmTimer.startIntervalTimer(500, 2000);
	}

	void stopTimer() {
		// Log.d("test", "stopTimer");
		if (null != mSmTimer) {
			mSmTimer.stopTimer();
			mSmTimer = null;
		}
	}

	public void setItemInfoProvider(IItemInfoProvider provider) {
		mItemInfoProvider = provider;
	}

	public void setTouchListener(ITouchListener listener) {
		mTouchListener = listener;
	}

	public TouchToShowLayout(Context context) {
		super(context);
	}

	public TouchToShowLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TouchToShowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// Log.d("test", "dispatchTouchEvent event: %d, inLongTouch: %b", ev.getAction(), mInLongTouch);
		mLastX = ev.getX();
		mLastY = ev.getY();
		boolean ret = super.dispatchTouchEvent(ev);
		if (ev.getAction() == MotionEvent.ACTION_UP) {
			// Log.d("test", "up");
			stopTimer();
			mInLongTouch = false;
		} else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
			// Log.d("test", "move pos x: %.2f %.2f", mDownX, mDownY);
			if (!mInLongTouch && (Math.abs(ev.getX() - mDownX) > 20 || Math.abs(ev.getY() - mDownY) > 20)) {
				// Log.d("test", "too long, downX: %.2f moveX: %.2f, downY: %.2f moveY: %.2f",
				//		mDownX, ev.getX(), mDownY, ev.getY());
				stopTimer();
				mNeedCallLongTouch = false;
			}
		}
		return ret;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// Log.d("test", "onInterceptTouchEvent event: %d, inLongTouch: %b", ev.getAction(), mInLongTouch);
		if (!mInLongTouch) {
			if (ev.getAction() == MotionEvent.ACTION_DOWN) {
				startTimer();
				mDownX = ev.getX();
				mDownY = ev.getY();
				// Log.d("test", "down pos x: %.2f %.2f", mDownX, mDownY);
			} else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
				// 检查有没有出当前item,如果出了的话,此次就不应该再可能触发长按
			} else if (ev.getAction() == MotionEvent.ACTION_UP) {
				stopTimer();
			}
			// Log.d("test", "don't intercept it");
			return super.onInterceptTouchEvent(ev);
		} else {
			boolean needIntercept = false;
			if (mNeedCallLongTouch) {
				int pos = mItemInfoProvider.getItemOfPoint(ev.getX(), ev.getY());
				if (mTouchListener.onLongTouch(pos)) {
					mNeedCallLongTouch = false;
					needIntercept = true;
					// Log.d("test", "intercept it");
				}
			}

			return needIntercept ? true : super.onInterceptTouchEvent(ev);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Log.d("test", "onTouchEvent event: %d, inLongTouch: %b", event.getAction(), mInLongTouch);
		if (mInLongTouch) {
			// 现在已经在长按了,说明已经显示了内容了,全部给拦截掉
			if (event.getAction() == MotionEvent.ACTION_UP) {
				mInLongTouch = false;
				mNeedCallLongTouch = false;
				if (null != mTouchListener) {
					mTouchListener.onTouchEnd();
				}
				stopTimer();
			} else if (event.getAction() == MotionEvent.ACTION_POINTER_DOWN) {
				if (null != mTouchListener) {
					mTouchListener.onClkWhenLongTouch();
				}
			}
			return true;
		}
		return super.onTouchEvent(event);
	}
}
