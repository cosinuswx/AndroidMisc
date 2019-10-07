package com.winomtech.androidmisc.plugin.camera.camera;

/**
 * @author kevinhuang
 * 用来保存当前机器摄像头的相关信息
 */
public class CameraInfo {
	/**
	 * 前面这一段内容在{@link CameraCompat#initCameraInfo()}的时候一定被会初始化
	 **/
    private int mCameraNum = 1;
    private boolean mIsHasFrontCamera = false;
    private boolean mIsHasBackCamera = false;
    private int mFrontId = 0;
    private int mBackId = 0;
    private int mFrontPreRotate = 0;
    private int mBackPreRotate = 0;

	public int getCameraNum() {
		return mCameraNum;
	}

	public void setCameraNum(int cameraNum) {
		mCameraNum = cameraNum;
	}

	public boolean getIsHasFrontCamera() {
		return mIsHasFrontCamera;
	}

	public void setIsHasFrontCamera(boolean isHasFrontCamera) {
		mIsHasFrontCamera = isHasFrontCamera;
	}

	public boolean getIsHasBackCamera() {
		return mIsHasBackCamera;
	}

	public void setIsHasBackCamera(boolean isHasBackCamera) {
		mIsHasBackCamera = isHasBackCamera;
	}

	public int getFrontOrien() {
		return mFrontPreRotate;
	}

	public void setFrontOrien(int frontOrien) {
		mFrontPreRotate = frontOrien;
	}

	public int getBackOrien() {
		return mBackPreRotate;
	}

	public void setBackOrien(int backOrien) {
		mBackPreRotate = backOrien;
	}
	
	public void setFrontId(int frontId) {
		mFrontId = frontId;
	}
	
	public int getFrontId() {
		return mFrontId;
	}
	
	public void setBackId(int backId) {
		mBackId = backId;
	}
	
	public int getBackId() {
		return mBackId;
	}
	
	public String dump() {
		return "\nmCameraNum: " + mCameraNum
			 + "\nmIsHasFrontCamera: " + mIsHasFrontCamera
			 + "\nmIsHasBackCamera: " + mIsHasBackCamera
			 + "\nmFrontId: " + mFrontId
			 + "\nmBackId: " + mBackId
			 + "\nmFrontPreRotate: " + mFrontPreRotate
			 + "\nmBackPreRotate: " + mBackPreRotate;
	}
}
