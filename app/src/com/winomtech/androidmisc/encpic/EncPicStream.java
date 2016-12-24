package com.winomtech.androidmisc.encpic;

import com.winomtech.androidmisc.sdk.utils.MiscUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author kevinhuang
 * @since 2016-12-04
 */
public class EncPicStream {
    byte[] mEncPwd = MiscUtils.hexStrToByte("1778e992532ef2a93805f7ab29acde84d98f11c8f05e9f8f85971eb71fed9f5d"
            + "ce344a1abe1297c73089c69a7f787162bd0a5a149c860a5d2b59ce39d8d1941b");
    ByteArrayInputStream mDecodedBais;

    File mFile;
    byte[] mData;
    int mOffset;
    int mLength;

    public EncPicStream(File file) {
        mFile = file;
    }

    public EncPicStream(byte[] data, int offset, int length) {
        mData = data;
        mOffset = offset;
        mLength = length;
    }

    public InputStream getInputStream() {
        if (null != mDecodedBais) {
            return mDecodedBais;
        }

        byte[] data = null;
        int offset = 0, length = 0;
        if (null != mFile) {
            data = readFromFile(mFile);
            offset = 0;
            length = data.length;
        } else {
            offset = 0;
            length = mLength;
            data = new byte[length];
            System.arraycopy(mData, offset, data, 0, length);
        }

        MiscUtils.xorByteArray(data, offset, length, mEncPwd);
        mDecodedBais = new ByteArrayInputStream(data, offset, length);
        return mDecodedBais;
    }

    public InputStream getSourceStream() throws FileNotFoundException {
        if (null != mFile) {
            return new FileInputStream(mFile);
        } else {
            return new ByteArrayInputStream(mData, mOffset, mLength);
        }
    }

    public void close() {
    }

    private byte[] readFromFile(File file) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            byte[] readBuf = new byte[1024];
            int readCnt;
            while ((readCnt = fis.read(readBuf, 0, readBuf.length)) != -1) {
                baos.write(readBuf, 0, readCnt);
            }
        } catch (Exception e) {
            throw new RuntimeException("can't read from file: " + file.getAbsolutePath());
        } finally {
            MiscUtils.safeClose(fis);
        }

        return baos.toByteArray();
    }
}
