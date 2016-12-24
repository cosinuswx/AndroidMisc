package com.winomtech.androidmisc.encpic;

import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;

import java.io.File;
import java.io.IOException;

/**
 * @author kevinhuang
 * @since 2016-12-04
 */
public class EncPicFileDecoder<T> implements ResourceDecoder<File, T> {
    private ResourceDecoder<EncPicStream, T> streamDecoder;

    public EncPicFileDecoder(ResourceDecoder<EncPicStream, T> streamDecoder) {
        this.streamDecoder = streamDecoder;
    }

    @Override
    public Resource<T> decode(File source, int width, int height) throws IOException {
        Resource<T> result = null;
        EncPicStream eps = null;
        try {
            eps = new EncPicStream(source);
            result = streamDecoder.decode(eps, width, height);
        } finally {
            if (null != eps) {
                eps.close();
            }
        }
        return result;
    }

    @Override
    public String getId() {
        return "";
    }
}
