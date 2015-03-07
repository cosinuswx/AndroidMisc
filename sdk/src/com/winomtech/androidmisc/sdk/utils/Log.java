package com.winomtech.androidmisc.sdk.utils;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Log {
	public static LogImpl		gLogImpl;

	private final static int	LEVEL_VERBOSE	= 0;
	private final static int	LEVEL_DEBUG		= 1;
	private final static int	LEVEL_INFO		= 2;
	private final static int	LEVEL_WARNING	= 3;
	private final static int	LEVEL_ERROR		= 4;

	private final static String[] lvlStr = {"V", "D", "I", "W", "E"};

	synchronized public static void initImpl(String logDir, String prefix) {
		gLogImpl = new LogImpl(logDir, prefix);
	}

	public static void v(String tag, String text) {
		gLogImpl.logWriter(LEVEL_VERBOSE, tag, text);
		if (SdkConstants.LOG_TO_LOGCAT) {
			android.util.Log.v(tag, text);
		}
	}
	
	public static void d(String tag, String text) {
		gLogImpl.logWriter(LEVEL_DEBUG, tag, text);
		if (SdkConstants.LOG_TO_LOGCAT) {
			android.util.Log.d(tag, text);
		}
	}
	
	public static void i(String tag, String text) {
		gLogImpl.logWriter(LEVEL_INFO, tag, text);
		if (SdkConstants.LOG_TO_LOGCAT) {
			android.util.Log.i(tag, text);
		}
	}
	
	public static void w(String tag, String text) {
		gLogImpl.logWriter(LEVEL_WARNING, tag, text);
		if (SdkConstants.LOG_TO_LOGCAT) {
			android.util.Log.w(tag, text);
		}
	}
	
	public static void e(String tag, String text) {
		gLogImpl.logWriter(LEVEL_ERROR, tag, text);
		if (SdkConstants.LOG_TO_LOGCAT) {
			android.util.Log.e(tag, text);
		}
	}
	
	public static void v(String tag, String format, Object... args) {
		v(tag, String.format(format, args));
	}

	public static void d(String tag, String format, Object... args) {
		d(tag, String.format(format, args));
	}

	public static void i(String tag, String format, Object... args) {
		i(tag, String.format(format, args));
	}
	
	public static void w(String tag, String format, Object... args) {
		w(tag, String.format(format, args));
	}
	
	public static void e(String tag, String format, Object... args) {
		e(tag, String.format(format, args));
	}

	public static class LogImpl {
		private PrintStream mPrintStream;
		private SimpleDateFormat mSimpleDataFormat;

		public LogImpl(String logDir, String perfix) {
			Calendar calendar = Calendar.getInstance();
			mSimpleDataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ", Locale.US); 
			if (!(new File(logDir).mkdirs())) {
				return;
			}

			String path = String.format(Locale.US, "%s/%s_%04d%02d%02d.log", logDir, perfix,
					calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
			try {
				mPrintStream = new PrintStream(new FileOutputStream(new File(path), true));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		synchronized void logWriter(int lvl, String tag, String format, Object... args) {
			StringBuilder sBuilder = new StringBuilder();
			sBuilder.append("[").append(lvlStr[lvl]).append("]");
			sBuilder.append("[").append(mSimpleDataFormat.format(new Date())).append("]");
			sBuilder.append("[").append(tag).append("]");
			sBuilder.append("[").append(Thread.currentThread().getId()).append("]");
			sBuilder.append("[").append(String.format(format, args));
			if (null != mPrintStream) {
				mPrintStream.println(sBuilder.toString());
			}
		}
	}

}