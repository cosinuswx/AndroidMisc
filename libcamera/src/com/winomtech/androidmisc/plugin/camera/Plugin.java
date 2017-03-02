package com.winomtech.androidmisc.plugin.camera;

import com.winom.olog.Log;
import com.winomtech.androidmisc.common.plugin.IPlugin;

/**
 * @author kevinhuang
 * @since 2016-12-15
 */
public class Plugin implements IPlugin {

    @Override
    public void init() {
        Log.d("PluginManager", "camera plugin loaded");
        SubcoreCamera.getCore();
    }
}
