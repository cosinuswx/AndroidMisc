package com.winomtech.androidmisc.encpic;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.bumptech.glide.load.resource.bitmap.Downsampler;

/**
 * @author kevinhuang
 * @since 2016-12-04
 */
public class EncPicStreamBitmapDecoder implements ResourceDecoder<EncPicStream, Bitmap> {
    private static final String ID = "StreamBitmapDecoder.com.winomtech.androidmisc.load.resource.bitmap";
    private final Downsampler downsampler;
    private BitmapPool bitmapPool;
    private DecodeFormat decodeFormat;
    private String id;

    public EncPicStreamBitmapDecoder(Context context) {
        this(Glide.get(context).getBitmapPool());
    }

    public EncPicStreamBitmapDecoder(BitmapPool bitmapPool) {
        this(bitmapPool, DecodeFormat.DEFAULT);
    }

    public EncPicStreamBitmapDecoder(Context context, DecodeFormat decodeFormat) {
        this(Glide.get(context).getBitmapPool(), decodeFormat);
    }

    public EncPicStreamBitmapDecoder(BitmapPool bitmapPool, DecodeFormat decodeFormat) {
        this(Downsampler.AT_LEAST, bitmapPool, decodeFormat);
    }

    public EncPicStreamBitmapDecoder(Downsampler downsampler, BitmapPool bitmapPool, DecodeFormat decodeFormat) {
        this.downsampler = downsampler;
        this.bitmapPool = bitmapPool;
        this.decodeFormat = decodeFormat;
    }

    @Override
    public Resource<Bitmap> decode(EncPicStream source, int width, int height) {
        Bitmap bitmap = downsampler.decode(source.getInputStream(), bitmapPool, width, height, decodeFormat);
        return BitmapResource.obtain(bitmap, bitmapPool);
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    @Override
    public String getId() {
        if (id == null) {
            id = new StringBuilder()
                    .append(ID)
                    .append(downsampler.getId())
                    .append(decodeFormat.name())
                    .toString();
        }
        return id;
    }
}
