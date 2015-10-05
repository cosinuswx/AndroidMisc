package com.winomtech.androidmisc.ui;

import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptGroup;
import android.support.v8.renderscript.ScriptIntrinsicYuvToRGB;
import android.support.v8.renderscript.Type;

import com.winomtech.androidmisc.rs.ScriptC_Gray;

public class GrayInteract {
	int mHeight;
	int mWidth;
	RenderScript mRS;
	Allocation mAllocationOut;
	Allocation mAllocationIn;

	ScriptC_Gray mGrayScript;

	GrayInteract(RenderScript rs) {
		mRS = rs;
		mGrayScript = new ScriptC_Gray(rs);
	}

	void reset(int width, int height) {
		if (mAllocationOut != null) {
			mAllocationOut.destroy();
		}

		mHeight = height;
		mWidth = width;
		mGrayScript.invoke_setSize(width, height);

		Type.Builder tb = new Type.Builder(mRS, Element.RGBA_8888(mRS));
		tb.setX(mWidth);
		tb.setY(mHeight);
		Type t = tb.create();
		mAllocationOut = Allocation.createTyped(mRS, t, Allocation.USAGE_SCRIPT);

		tb = new Type.Builder(mRS, Element.RGBA_8888(mRS));
		tb.setX(mWidth);
		tb.setY(mHeight);
		mAllocationIn = Allocation.createTyped(mRS, tb.create(), Allocation.USAGE_SCRIPT);
	}

	public void setPos(int pos) {
		mGrayScript.invoke_setPos(pos);
	}

	public int getWidth() {
		return mWidth;
	}

	public int getHeight() {
		return mHeight;
	}

	void execute(byte[] yuv, byte[] out) {
		mAllocationIn.copyFrom(yuv);
		mGrayScript.forEach_root(mAllocationIn, mAllocationOut);
		mAllocationOut.copyTo(out);
	}
}
