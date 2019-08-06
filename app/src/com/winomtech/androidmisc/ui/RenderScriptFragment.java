package com.winomtech.androidmisc.ui;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.winomtech.androidmisc.R;
import com.winomtech.androidmisc.rs.ScriptC_Gray;

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

	Allocation mAllocationIn;
	Allocation mAllocationOut;

	RenderScript mRenderScript;
	ScriptC_Gray mGrayScript;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootView = (RelativeLayout) inflater.inflate(R.layout.fragment_renderscript, container, false);
		mImageView = (ImageView) mRootView.findViewById(R.id.iv_renderscript);
		mRootView.setOnTouchListener(mRootTouchLsn);

		mInBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.renderscript_input);
		mOutBitmap = Bitmap.createBitmap(mInBitmap.getWidth(), mInBitmap.getHeight(), Bitmap.Config.ARGB_8888);
		mPosition = mInBitmap.getWidth();

		mRenderScript = RenderScript.create(getActivity());
		mGrayScript = new ScriptC_Gray(mRenderScript);

		mAllocationIn = Allocation.createFromBitmap(mRenderScript, mInBitmap);
		mAllocationOut = Allocation.createFromBitmap(mRenderScript, mOutBitmap);

		refreshBitmap();
		return mRootView;
	}

	void refreshBitmap() {
		mGrayScript.forEach_root(mAllocationIn, mAllocationOut);
		mAllocationOut.copyTo(mOutBitmap);
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
					mGrayScript.set_gPos(pos);
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
