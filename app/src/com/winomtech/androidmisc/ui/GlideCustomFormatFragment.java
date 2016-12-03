package com.winomtech.androidmisc.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.winomtech.androidmisc.R;

/**
 * @author kevinhuang
 * @since 2016-12-03
 */
public class GlideCustomFormatFragment extends Fragment {
    ImageView mIvContent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_custom_glide_format, container, false);

        mIvContent = (ImageView) rootView.findViewById(R.id.iv_image);
        Glide.with(this)
             .load("http://img1.gtimg.com/news/pics/hv1/208/50/2163/140662033.jpg")
             .asBitmap()
             .into(mIvContent);

        return rootView;
    }
}
