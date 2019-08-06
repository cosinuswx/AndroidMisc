package com.winomtech.androidmisc.ui;

import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;
import com.winomtech.androidmisc.R;

import androidx.fragment.app.Fragment;

public class SVGDrawableFragment extends Fragment {
    private final static int DEFAULT_VALUE = 400;

    private ImageView mIvPicContainer;
    private SeekBar mPbScaleValue;
    private SVG mSvg;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_svg_drawable, container, false);
        mIvPicContainer = rootView.findViewById(R.id.iv_pic_container);
        
        mPbScaleValue = rootView.findViewById(R.id.pb_scale_value);
        mPbScaleValue.setMax(100);
        mPbScaleValue.setOnSeekBarChangeListener(mSeekBarChgLsn);

        mSvg = SVGParser.getSVGFromResource(getResources(), R.raw.pic_s9_11);
        mIvPicContainer.setImageDrawable(mSvg.createPictureDrawable());
        return rootView;
    }

    private SeekBar.OnSeekBarChangeListener mSeekBarChgLsn = new SeekBar.OnSeekBarChangeListener() {
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
