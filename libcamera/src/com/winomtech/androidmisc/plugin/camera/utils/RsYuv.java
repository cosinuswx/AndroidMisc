package com.winomtech.androidmisc.plugin.camera.utils;

import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;

public class RsYuv {
	int mHeight = 0;
	int mWidth = 0;

	RenderScript mRS;
	Allocation mAllocationOut;
	Allocation mAllocationIn;
	ScriptIntrinsicYuvToRGB mYuv;

	public RsYuv(RenderScript rs) {
		mRS = rs;
		mYuv = ScriptIntrinsicYuvToRGB.create(rs, Element.RGBA_8888(mRS));
	}

	public void reset(int width, int height) {
		if (mAllocationOut != null) {
			mAllocationOut.destroy();
		}

		mHeight = height;
		mWidth = width;

		Type.Builder tb = new Type.Builder(mRS, Element.RGBA_8888(mRS));
		tb.setX(mWidth);
		tb.setY(mHeight);
		Type t = tb.create();
		mAllocationOut = Allocation.createTyped(mRS, t, Allocation.USAGE_SHARED);

		tb = new Type.Builder(mRS, Element.createPixel(mRS, Element.DataType.UNSIGNED_8, Element.DataKind.PIXEL_YUV));
		tb.setX(mWidth);
		tb.setY(mHeight);
		tb.setYuvFormat(android.graphics.ImageFormat.NV21);
		mAllocationIn = Allocation.createTyped(mRS, tb.create(), Allocation.USAGE_SHARED);
		mYuv.setInput(mAllocationIn);
	}

	public int getWidth() {
		return mWidth;
	}

	public int getHeight() {
		return mHeight;
	}

	public void execute(byte[] yuv, byte[] out) {
		mAllocationIn.copyFrom(yuv);
		mYuv.setInput(mAllocationIn);
		mYuv.forEach(mAllocationOut);

		mAllocationOut.copyTo(out);
	}
}
