package com.winomtech.androidmisc.plugin;

import com.winomtech.androidmisc.app.MMApplication;
import com.winomtech.androidmisc.sdk.plugin.IPlugin;
import com.winomtech.androidmisc.sdk.utils.Log;

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
			ClassLoader clsLoader = MMApplication.getContext().getClass().getClassLoader();
			Class<?> clsPlugin = clsLoader.loadClass(MMApplication.getSourcePkgName() + ".plugin." + pluginName + ".Plugin");
			plugin = (IPlugin) clsPlugin.newInstance();
		} catch (ClassNotFoundException e) {
			Log.e(TAG, "loadPlugin failed: " + e.getMessage());
		} catch (InstantiationException e) {
			Log.e(TAG, "loadPlugin failed: " + e.getMessage());
		} catch (IllegalAccessException e) {
			Log.e(TAG, "loadPlugin failed: " + e.getMessage());
		}
		
		if (null != plugin) {
			plugin.init();
			sPluginMap.put(pluginName, plugin);
		}

		return null != plugin;
	}
}
