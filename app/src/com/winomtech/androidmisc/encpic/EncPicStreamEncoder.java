package com.winomtech.androidmisc.encpic;

import android.util.Log;

import com.bumptech.glide.load.Encoder;
import com.bumptech.glide.util.ByteArrayPool;
import com.winomtech.androidmisc.sdk.utils.MiscUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author kevinhuang
 * @since 2016-12-04
 */
public class EncPicStreamEncoder implements Encoder<EncPicStream> {
    final static String TAG = "EncPicStreamEncoder";

    @Override
    public boolean encode(EncPicStream data, OutputStream os) {
        byte[] buffer = ByteArrayPool.get().getBytes();
        InputStream inputStream = null;
        try {
            int read;
            inputStream = data.getSourceStream();
            while ((read = inputStream.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
            return true;
        } catch (IOException e) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Failed to encode data onto the OutputStream", e);
            }
            return false;
        } finally {
            MiscUtils.safeClose(inputStream);
            ByteArrayPool.get().releaseBytes(buffer);
        }
    }

    @Override
    public String getId() {
        return "";
    }
}
