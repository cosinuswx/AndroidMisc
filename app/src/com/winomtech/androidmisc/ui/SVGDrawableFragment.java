package com.winomtech.androidmisc.ui;

import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;
import com.winomtech.androidmisc.R;

/**
 * @since 2015年09月02日
 * @author kevinhuang 
 */
public class SVGDrawableFragment extends Fragment {
	static final String TAG = "SVGDrawableFragment";

	ImageView mIvPicContainer;
	SeekBar mPbScaleValue;
	SVG mSvg;

	final static int DEFAULT_VALUE = 400;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_svg_drawable, container, false);
		mIvPicContainer = (ImageView) rootView.findViewById(R.id.iv_pic_container);
		
		mPbScaleValue = (SeekBar) rootView.findViewById(R.id.pb_scale_value);
		mPbScaleValue.setMax(100);
		mPbScaleValue.setOnSeekBarChangeListener(mSeekBarChgLsn);

		mSvg = SVGParser.getSVGFromResource(getResources(), R.raw.pic_s9_11);
		mIvPicContainer.setImageDrawable(mSvg.createPictureDrawable());
		return rootView;
	}

	SeekBar.OnSeekBarChangeListener mSeekBarChgLsn = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			int value = (int) (progress / 10f * DEFAULT_VALUE); 
		
			Picture resizePicture = new Picture();
			Canvas canvas = resizePicture.beginRecording(value, value);

			canvas.drawPicture(mSvg.getPicture(), new Rect(0, 0, value, value));
			resizePicture.endRecording();

			// get a drawable from resizePicture
			Drawable vectorDrawing = new PictureDrawable(resizePicture);
			mIvPicContainer.setImageDrawable(vectorDrawing);

			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mIvPicContainer.getLayoutParams();
			params.width = params.height = value;
			mIvPicContainer.setLayoutParams(params);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
	};
}
