package com.winomtech.androidmisc.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v8.renderscript.RenderScript;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.winomtech.androidmisc.R;
import com.winomtech.androidmisc.sdk.utils.Log;

import java.nio.ByteBuffer;

/**
 * @since 2015年10月05日
 * @author kevinhuang 
 */
public class RenderScriptFragment extends Fragment {
	RelativeLayout mRootView;
	ImageView mImageView;
	int mPosition;

	Bitmap mInBitmap;
	Bitmap mOutBitmap;

	ByteBuffer mInBuffer;
	ByteBuffer mOutBuffer;

	RenderScript mRenderScript;
	GrayInteract mGrayInteract;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = (RelativeLayout) inflater.inflate(R.layout.fragment_renderscript, container, false);
		mImageView = (ImageView) mRootView.findViewById(R.id.iv_renderscript);
		mRootView.setOnTouchListener(mRootTouchLsn);

		mInBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.renderscript_input);
		mOutBitmap = Bitmap.createBitmap(mInBitmap.getWidth(), mInBitmap.getHeight(), Bitmap.Config.ARGB_8888);
		mPosition = mInBitmap.getWidth();

		mInBuffer = ByteBuffer.allocate(mInBitmap.getWidth() * mInBitmap.getHeight() * 4);
		mOutBuffer = ByteBuffer.allocate(mInBitmap.getWidth() * mInBitmap.getHeight() * 4);
		mInBitmap.copyPixelsToBuffer(mInBuffer);

		mRenderScript = RenderScript.create(getActivity());
		mGrayInteract = new GrayInteract(mRenderScript);
		mGrayInteract.reset(mInBitmap.getWidth(), mInBitmap.getHeight());

		refreshBitmap();
		return mRootView;
	}

	void refreshBitmap() {
		mInBuffer.position(0);
		mGrayInteract.execute(mInBuffer.array(), mOutBuffer.array());
		mOutBuffer.position(0);
		mOutBitmap.copyPixelsFromBuffer(mOutBuffer);

		mImageView.setImageBitmap(mOutBitmap);
	}

	View.OnTouchListener mRootTouchLsn = new View.OnTouchListener() {
		float mDownPos;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mDownPos = event.getRawX();
					return true;

				case MotionEvent.ACTION_MOVE:
					int pos = (int) (mPosition + (event.getRawX() - mDownPos));
					mGrayInteract.setPos(pos);
					refreshBitmap();
					return true;

				case MotionEvent.ACTION_UP:
					mPosition = (int) (mPosition + (event.getRawX() - mDownPos));
					return true;
			}
			return false;
		}
	};
}
