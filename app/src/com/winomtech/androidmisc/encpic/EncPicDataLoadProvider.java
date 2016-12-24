package com.winomtech.androidmisc.encpic;

import android.graphics.Bitmap;

import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.Encoder;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.ResourceEncoder;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapEncoder;
import com.bumptech.glide.provider.DataLoadProvider;

import java.io.File;

/**
 * @author kevinhuang
 * @since 2016-12-04
 */
public class EncPicDataLoadProvider implements DataLoadProvider<EncPicStream, Bitmap> {
    private final EncPicStreamBitmapDecoder decoder;
    private final BitmapEncoder encoder;
    private final EncPicStreamEncoder sourceEncoder;
    private final EncPicFileDecoder<Bitmap> cacheDecoder;

    public EncPicDataLoadProvider(BitmapPool bitmapPool, DecodeFormat decodeFormat) {
        sourceEncoder = new EncPicStreamEncoder();
        decoder = new EncPicStreamBitmapDecoder(bitmapPool, decodeFormat);
        encoder = new BitmapEncoder();
        cacheDecoder = new EncPicFileDecoder<>(decoder);
    }

    @Override
    public ResourceDecoder<File, Bitmap> getCacheDecoder() {
        return cacheDecoder;
    }

    @Override
    public ResourceDecoder<EncPicStream, Bitmap> getSourceDecoder() {
        return decoder;
    }

    @Override
    public Encoder<EncPicStream> getSourceEncoder() {
        return sourceEncoder;
    }

    @Override
    public ResourceEncoder<Bitmap> getEncoder() {
        return encoder;
    }
}
