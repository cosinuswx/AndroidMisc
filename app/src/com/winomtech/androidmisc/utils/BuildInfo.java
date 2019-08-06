package com.winomtech.androidmisc.utils;

public class BuildInfo {
	private static final String GIT_BRANCH = "@GIT_BRANCH@";
	private static final String GIT_REVERSION = "@GIT_REVERSION@";
	private static final String BUILD_TIME = "@BUILD_TIME@";

	public static String info() {
        return "[branch] " + GIT_BRANCH + "\n" +
               "[rev   ] " + GIT_REVERSION + "\n" +
               "[time  ] " + BUILD_TIME + "\n";
	}
}
