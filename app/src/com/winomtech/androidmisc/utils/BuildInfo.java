package com.winomtech.androidmisc.utils;

/**
 * @since 2015-03-31
 * @author kevinhuang 
 */
public class BuildInfo {
	public static final String GIT_BRANCH = "@GIT_BRANCH@";
	public static final String GIT_REVERSION = "@GIT_REVERSION@";
	public static final String BUILD_TIME = "@BUILD_TIME@";


	public static String info() {
		StringBuilder sb = new StringBuilder();
		sb.append("[branch] ").append(GIT_BRANCH).append("\n");
		sb.append("[rev   ] ").append(GIT_REVERSION).append("\n");
		sb.append("[time  ] ").append(BUILD_TIME).append("\n");
		return sb.toString();
	}
}
