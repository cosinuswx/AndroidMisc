package com.winomtech.androidmisc.ui;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.winomtech.androidmisc.R;
import com.winomtech.androidmisc.sdk.utils.MiscUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class CopyTracesFragment extends Fragment {
	private TextView	mProgressText;
	private CopyThread	mCopyThread;
	
	private Handler		mHandler = new Handler(new Handler.Callback() {
		
		@Override
		public boolean handleMessage(Message msg) {
			if (msg.arg1 == -1 && msg.arg2 == -1) {
				mProgressText.setText(R.string.str_finish);
			} else {
				mProgressText.setText(String.format(getString(R.string.str_progressing), msg.arg1, msg.arg2));
			}
			return false;
		}
	});
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_split_profile, container, false);
		mProgressText = (TextView) rootView.findViewById(R.id.tv_split_progress);
		mProgressText.setText(String.format(getString(R.string.str_progressing), 0, 0));
		
		mCopyThread = new CopyThread();
		mCopyThread.start();
		return rootView;
	}

	@Override
	public void onDestroy() {
		mCopyThread.stopRun();
		super.onDestroy();
	}

	public class CopyThread extends Thread {

		private boolean		mStoped = false;

		@Override
		public void run() {
			String fromPath = "/data/anr";
			String toPath = Environment.getExternalStorageDirectory() + "/anr/";
			MiscUtils.mkdirs(toPath);

			File file = new File(fromPath);
			String[] files = file.list();
			if (null == files || 0 == files.length) {
				sendProgress();
				return;
			}

			for (int j = 0; j < files.length && !mStoped; ++j) {
				sendProgress();
				if (!files[j].equals(".") && !files[j].equals("..")) {
					copyFile(fromPath + "/" + files[j], toPath + "/" + files[j]);
				}
			}
		}
		
		private void sendProgress() {
			Message msg = new Message();
			msg.arg1 = -1;
			msg.arg2 = -1;
			mHandler.sendMessage(msg);
		}

		public void stopRun() {
			mStoped = true;
		}

		public long copyFile(String src, String dest) {
			FileChannel s = null;
			FileChannel d = null;

			try {
				s = new FileInputStream(src).getChannel();
				d = new FileOutputStream(dest).getChannel();
				d.transferFrom(s, 0, s.size());
				return s.size();
			} catch (Exception e) {
				return -1;
			} finally {
				MiscUtils.safeClose(s);
				MiscUtils.safeClose(d);
			}
		}
	}
}
