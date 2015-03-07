package com.winomtech.androidmisc.audio;

import android.media.AudioFormat;


import com.winomtech.androidmisc.sdk.utils.Log;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author kevinhuang
 * @since 2015-01-20
 */
public class WavWriter {
	final static String TAG = WavWriter.class.getSimpleName();

	String mFilePath;
	DataOutputStream mDataOutputStream;
	int mFileSize;

	public WavWriter(String path, int channelCnt, int sampleRate, int audioEncoding) {
		mFilePath = path;
		File file = new File(path);
		try {
			file.createNewFile();
			mDataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

			ByteBuffer byteBuffer = ByteBuffer.allocate(44);
			byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
			byteBuffer.putInt(0x46464952);
			byteBuffer.putInt(0);
			byteBuffer.putInt(0x45564157);
			byteBuffer.putInt(0x20746d66);
			byteBuffer.putInt(16);
			byteBuffer.putShort((short)1);
			byteBuffer.putShort((short) channelCnt);
			byteBuffer.putInt(sampleRate);
			byteBuffer.putInt((sampleRate * channelCnt * (audioEncoding == AudioFormat.ENCODING_PCM_16BIT ? 2 : 1)));
			byteBuffer.putShort((short) (channelCnt * (audioEncoding == AudioFormat.ENCODING_PCM_16BIT ? 2 : 1)));
			byteBuffer.putShort((short) (audioEncoding == AudioFormat.ENCODING_PCM_16BIT ? 16 : 8));
			byteBuffer.putInt(0x61746164);
			byteBuffer.putInt(0);

			mFileSize = 44;
			mDataOutputStream.write(byteBuffer.array());
		} catch (IOException e) {
			Log.e(TAG, "create file failed: " + e.getMessage());
		}
	}

	public boolean writeToFile(byte[] buf, int len) {
		if (null == mDataOutputStream) {
			return false;
		}

		try {
			mDataOutputStream.write(buf, 0, len);
			mFileSize += len;
		} catch (Exception e) {
			Log.e(TAG, "write to file failed: " + e.getMessage());
			return false;
		}
		return true;
	}

	public void closeFile() {
		try {
			mDataOutputStream.close();
		} catch (IOException e) {
			Log.e(TAG, "close file failed: " + e.getMessage());
		}

		RandomAccessFile ras = null;
		try {
			ras = new RandomAccessFile(mFilePath, "rw");
			ras.seek(4);

			// 文件总长度
			ByteBuffer byteBuffer = ByteBuffer.allocate(4);
			byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
			byteBuffer.putInt(mFileSize - 8);
			ras.write(byteBuffer.array());

			// 数据总长度
			byteBuffer.rewind();
			byteBuffer.putInt(mFileSize - 42);
			ras.seek(40);
			ras.write(byteBuffer.array());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != ras) {
				try {
					ras.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
