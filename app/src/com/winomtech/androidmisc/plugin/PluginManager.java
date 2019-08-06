package com.winomtech.androidmisc.plugin;

import com.winom.olog.OLog;
import com.winomtech.androidmisc.app.MiscApplication;
import com.winomtech.androidmisc.common.plugin.IPlugin;
import com.winomtech.androidmisc.common.utils.AppProperties;

import java.util.HashMap;
import java.util.Map;

public class PluginManager {
    private static final String TAG = PluginManager.class.getSimpleName();
    private static final Map<String, IPlugin> PLUGIN_MAP = new HashMap<>();

	public static boolean loadPlugin(String pluginName) {
		IPlugin plugin = null;
		try {
			ClassLoader clsLoader = AppProperties.getInstance().getAppContext().getClass().getClassLoader();
			Class<?> clsPlugin = clsLoader.loadClass(MiscApplication.getSourcePkgName() + ".plugin." + pluginName + ".Plugin");
			plugin = (IPlugin) clsPlugin.newInstance();
		} catch (ClassNotFoundException e) {
			OLog.e(TAG, "loadPlugin failed: " + e.getMessage());
		} catch (InstantiationException e) {
			OLog.e(TAG, "loadPlugin failed: " + e.getMessage());
		} catch (IllegalAccessException e) {
			OLog.e(TAG, "loadPlugin failed: " + e.getMessage());
		}
		
		if (null != plugin) {
			plugin.init();
			PLUGIN_MAP.put(pluginName, plugin);
		}

		return null != plugin;
	}
}
