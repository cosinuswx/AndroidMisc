package com.winomtech.androidmisc.plugin;

import com.winom.olog.OLog;
import com.winomtech.androidmisc.app.MiscApplication;
import com.winomtech.androidmisc.common.plugin.IPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * @since 2015-03-07
 * @author kevinhuang 
 */
public class PluginManager {
	private static final String TAG = PluginManager.class.getSimpleName();

	static Map<String, IPlugin>		sPluginMap = new HashMap<String, IPlugin>();

	public static boolean loadPlugin(String pluginName) {
		IPlugin plugin = null;
		try {
			ClassLoader clsLoader = MiscApplication.getContext().getClass().getClassLoader();
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
			sPluginMap.put(pluginName, plugin);
		}

		return null != plugin;
	}
}
