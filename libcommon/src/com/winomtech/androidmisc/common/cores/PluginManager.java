package com.winomtech.androidmisc.common.cores;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.winom.olog.Log;
import com.winomtech.androidmisc.common.plugin.IPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kevinhuang
 * @since 2015-03-07
 */
public class PluginManager {
    private static final String TAG = "PluginManager";

    static Map<String, IPlugin> sPluginMap = new HashMap<String, IPlugin>();
    static Context sContext = null;
    static String sPackageName;

    public static void init(Context context, String packageName) {
        sContext = context;
        sPackageName = packageName;
    }

    public static boolean loadPlugin(String pluginName) {
        Log.d(TAG, "loadPlugin: " + pluginName);
        IPlugin plugin = null;
        try {
            ClassLoader clsLoader = sContext.getClass().getClassLoader();
            Class<?> clsPlugin = clsLoader.loadClass(sPackageName + ".plugin." + pluginName + ".Plugin");
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

    public static void startActivity(Context context, String pluginName, String activityPath, Intent intent) {
        IPlugin plugin = sPluginMap.get(pluginName);
        if (null == plugin) {
            throw new RuntimeException("can't find plugin: " + pluginName);
        }

        String activityFullPath = sPackageName + ".plugin." + pluginName + ".Plugin" + activityPath;
        try {
            Class<?> clsActivity = plugin.getClass().getClassLoader().loadClass(activityFullPath);
            intent.setClass(context, clsActivity);
            context.startActivity(intent);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("can't find activity: " + activityFullPath);
        }
    }

    public static void startActivityForResult(Activity activity, String pluginName, String activityPath,
                                              Intent intent, int requestCode) {
        IPlugin plugin = sPluginMap.get(pluginName);
        if (null == plugin) {
            throw new RuntimeException("can't find plugin: " + pluginName);
        }

        String activityFullPath = sPackageName + ".plugin." + pluginName + ".Plugin" + activityPath;
        try {
            Class<?> clsActivity = plugin.getClass().getClassLoader().loadClass(activityFullPath);
            intent.setClass(activity.getApplicationContext(), clsActivity);
            activity.startActivityForResult(intent, requestCode);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("can't find activity: " + activityFullPath);
        }
    }
}
