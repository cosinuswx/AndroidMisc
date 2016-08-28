package com.winomtech.androidmisc.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.winomtech.androidmisc.R;
import com.winomtech.androidmisc.sdk.utils.SmTimer;

/**
 * @since 2015-03-27
 * @author kevinhuang
 * 选择时间的View
 */
public class PickTimeView extends View implements View.OnTouchListener {
	static final String TAG = PickTimeView.class.getSimpleName();

	final static double NATURE = 2.71828;

	// 圆的大小根据位置来看，呈现一个正态分布曲线的规则
	// 顶点坐标
	final static int 	ARCH_COORD	= 200;
	// 顶点值
	final static float	ARCH_VALUE	= 44;
	// 正态分布的系数
	final static float ARCH_RATIO = 1000f / 207;
	// 间距与圆心的比例系数
	final static float GAP_RADIUS_RATIO = 4.0f / 11;
	// 最大alpha值
	final static float MAX_ALPHA_VALUE = 127.0f;
	// 从当前点回到顶点的动画时间
	final static float BACK_ANIMATION_LEN = 300;

	// 最小的时间
	final static int MIN_NUMBER = 1;
	// 最大的时间
	final static int MAX_NUMBER = 9;
	// 默认的时间
	final static int DEFAULT_NUMBER = 3;
	// 数字与圆的比例
	final static float NUMBER_CIRCLE_RATIO = 204.0f / 264;

	final static int NORMAL_RES_ID[] = new int[] { -1,
			R.drawable.normal_one,
			R.drawable.normal_two,
			R.drawable.normal_three,
			R.drawable.normal_four,
			R.drawable.normal_five,
			R.drawable.normal_six,
			R.drawable.normal_seven,
			R.drawable.normal_eight,
			R.drawable.normal_nine };

	Bitmap[] normalBmp = new Bitmap[MAX_NUMBER + 1];

	float	mDensity;		// 屏幕密度
	float	mArchCoord;		// 正态分布曲顶点坐标
	float	mArchValue;		// 正态分布曲顶点值

	float	mFocusCoord;	// 当前用于定位的坐标
	int		mFocusIndex;	// 当前焦点所对应的值
	float	mLastY;			// 手上次的位置

	float	mAnimStartY;	// 松手的时候，手指的位置
	long	mAnimStartTick;	// 松手的时间

	Paint	mCirclePaint;	// 用来画圆圈的画笔
	Paint	mTransPaint;	// 图片的透明度
	SmTimer	mSmTimer;		// 定时器，用来做动画

	CircleInteger	mCircleIndex = new CircleInteger();

	public PickTimeView(Context context) {
		this(context, null);
	}

	public PickTimeView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PickTimeView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		mDensity = context.getResources().getDisplayMetrics().density;
		mSmTimer = new SmTimer(mTimerCallback);

		mArchCoord = mDensity * ARCH_COORD;
		mArchValue = mDensity * ARCH_VALUE;

		mFocusCoord = mArchCoord;
		mFocusIndex = DEFAULT_NUMBER;

		mCirclePaint = new Paint();
		mCirclePaint.setAntiAlias(true);
		mCirclePaint.setStyle(Paint.Style.FILL);

		mTransPaint = new Paint();
		mTransPaint.setAlpha(100);

		// 加载图片资源进来
		Resources res = context.getResources();
		for (int i = MIN_NUMBER; i <= MAX_NUMBER; ++i) {
			normalBmp[i] = BitmapFactory.decodeResource(res, NORMAL_RES_ID[i]);
		}

		setOnTouchListener(this);
	}

	void drawNumber(Canvas canvas, int index, float coord, float radius) {
		if (index < MIN_NUMBER || index > MAX_NUMBER) {
			return;
		}

		Bitmap bmp = normalBmp[index];
		Rect srcRect = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
		float rRatio = radius / mArchValue;
		float numR = mArchValue * NUMBER_CIRCLE_RATIO * rRatio;
		Rect dstRect = new Rect((int) (getWidth() / 2 - numR), (int) (coord - numR),
				(int) (getWidth() / 2 + numR), (int) (coord + numR));
		mTransPaint.setAlpha((int) (255 * (rRatio * rRatio)));
		canvas.drawBitmap(bmp, srcRect, dstRect, mTransPaint);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int width = getWidth();
		int height = getHeight();
		float curFocusCoord = mFocusCoord;
		int curIndex = mFocusIndex;
		mCircleIndex.setValue(curIndex);

		// 先画中心点
		float focusRadius = getRadius(curFocusCoord);
		int alpha = (int) ((focusRadius / mArchValue) * (focusRadius / mArchValue) * MAX_ALPHA_VALUE);
		mCirclePaint.setColor(0xffffff + (alpha << 24));
		canvas.drawCircle(width / 2.0f, curFocusCoord, focusRadius, mCirclePaint);
		drawNumber(canvas, mCircleIndex.getValue(), curFocusCoord, focusRadius);

		// 向上
		float loopCoord = curFocusCoord;
		float radius = getRadius(curFocusCoord);
		mCircleIndex.setValue(curIndex);
		for (int i = 0; i < 2; ++i) {
			float newEdge = loopCoord - radius - radius * GAP_RADIUS_RATIO;
			float coord = bsFrontCoord(newEdge);
			radius = newEdge - coord;
			mCircleIndex.decrease();

			if (radius <= 1.0) { break; }
			if (radius > focusRadius) {
				mFocusCoord = coord;
				mFocusIndex = mCircleIndex.getValue();
			}

			alpha = (int) ((radius / mArchValue) * (radius / mArchValue) * MAX_ALPHA_VALUE);
			mCirclePaint.setColor(0xffffff + (alpha << 24));
			canvas.drawCircle(width / 2.0f, coord, radius, mCirclePaint);
			drawNumber(canvas, mCircleIndex.getValue(), coord, radius);
			loopCoord = coord;
		}

		// 向下
		loopCoord = curFocusCoord;
		radius = getRadius(loopCoord);
		mCircleIndex.setValue(curIndex);
		for (int i = 0; i < 2; ++i) {
			float newEdge = loopCoord + radius + radius * GAP_RADIUS_RATIO;
			float coord = bsBackCoord(newEdge, height);
			radius = coord - newEdge;
			mCircleIndex.increase();

			if (radius <= 1.0) { break; }
			if (radius > focusRadius) {
				mFocusCoord = coord;
				mFocusIndex = mCircleIndex.getValue();
			}

			alpha = (int) ((radius / mArchValue) * (radius / mArchValue) * MAX_ALPHA_VALUE);
			mCirclePaint.setColor(0xffffff + (alpha << 24));
			canvas.drawCircle(width / 2.0f, coord, radius, mCirclePaint);
			drawNumber(canvas, mCircleIndex.getValue(), coord, radius);
			loopCoord = coord;
		}
	}

	// 二分搜索前面的坐标，返回坐标
	int bsFrontCoord(float edgeCoord) {
		int left = (int) -edgeCoord, right = (int) edgeCoord, middle;
		float radius, expR;
		while (right - left > 1) {
			middle = (right + left) / 2;
			expR = edgeCoord - middle;
			radius = getRadius(middle);
			if (radius > expR) {
				right = middle;
			} else {
				left = middle;
			}
		}

		if (Math.abs(edgeCoord - left - getRadius(left)) > Math.abs(edgeCoord - right - getRadius(right))) {
			return right;
		} else {
			return left;
		}
	}

	// 二分搜索后面的坐标，返回坐标
	int bsBackCoord(float edgeCoord, float height) {
		int left = (int) edgeCoord, right = (int) height, middle = right;
		float radius, expR;
		while (right - left > 1) {
			middle = (right + left) / 2;
			expR = middle - edgeCoord;
			radius = getRadius(middle);
			if (radius < expR) {
				right = middle;
			} else {
				left = middle;
			}
		}

		if (Math.abs(middle - edgeCoord - getRadius(left)) > Math.abs(middle - edgeCoord - getRadius(right))) {
			return right;
		} else {
			return left;
		}
	}

	// 标准正态分布曲线
	float getRadius(float coord) {
		return (float) (mArchValue * Math.pow(NATURE, -ARCH_RATIO * (coord - mArchCoord) * (coord - mArchCoord) / mArchCoord / mArchCoord / 2));
	}

	SmTimer.SmTimerCallback	mTimerCallback = new SmTimer.SmTimerCallback() {
		@Override
		public void onTimeout() {
			float delta = (mArchCoord - mAnimStartY) * (System.currentTimeMillis() - mAnimStartTick) / BACK_ANIMATION_LEN;
			mFocusCoord = mAnimStartY + delta;

			if ((mAnimStartY < mArchCoord && mFocusCoord > mArchCoord) ||
					(mAnimStartY > mArchCoord && mFocusCoord < mArchCoord)) {
				mSmTimer.stopTimer();
				mFocusCoord = mArchCoord;
			}
			invalidate();
		}
	};

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mLastY = event.getY();
				mSmTimer.stopTimer();
				break;
			case MotionEvent.ACTION_MOVE:
				if (Math.abs(event.getY() - mLastY) > 1) {
					mFocusCoord += (event.getY() - mLastY);
					mLastY = event.getY();
					invalidate();
				}
				break;
			case MotionEvent.ACTION_UP:
				// 触发定时器，让当前Focus的圆回到中间
				mAnimStartY = mFocusCoord;
				mAnimStartTick = System.currentTimeMillis();
				mSmTimer.startIntervalTimer(0, 10);
				break;
		}
		return true;
	}

	private static class CircleInteger {
		int value;

		public void setValue(int val) {
			value = val;
		}

		public int decrease() {
			value --;
			if (value < MIN_NUMBER) value = MAX_NUMBER;
			return value;
		}

		public int increase() {
			value ++;
			if (value > MAX_NUMBER) value = MIN_NUMBER;
			return value;
		}

		public int getValue() {
			return value;
		}
	}
}