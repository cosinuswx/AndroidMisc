package com.winomtech.androidmisc.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.BitmapEncoder;
import com.winomtech.androidmisc.R;
import com.winomtech.androidmisc.encpic.EncPicFileDecoder;
import com.winomtech.androidmisc.encpic.EncPicStream;
import com.winomtech.androidmisc.encpic.EncPicStreamBitmapDecoder;
import com.winomtech.androidmisc.encpic.EncPicStreamEncoder;
import com.winomtech.androidmisc.encpic.EncPicUrl;
import com.winomtech.androidmisc.encpic.EncPicUrlFetcher;

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
             .using(new EncPicUrlFetcher.EncPicUrlLoader(), EncPicStream.class)
             .load(new EncPicUrl("http://7tebun.com1.z0.glb.clouddn.com/androidmisc/baff2ac823c832f6a25eef8b0cc3cad3.jpg"))
             .as(Bitmap.class)
             .decoder(new EncPicStreamBitmapDecoder(getActivity()))
             .cacheDecoder(new EncPicFileDecoder<>(new EncPicStreamBitmapDecoder(getActivity())))
             .encoder(new BitmapEncoder())
             .sourceEncoder(new EncPicStreamEncoder())
             .diskCacheStrategy(DiskCacheStrategy.SOURCE)
             .into(mIvContent);

        return rootView;
    }
}
