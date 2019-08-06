package com.winomtech.androidmisc.plugin.camera;

import com.winom.olog.OLog;
import com.winomtech.androidmisc.common.plugin.IPlugin;

/**
 * @author kevinhuang
 * @since 2016-12-15
 */
public class Plugin implements IPlugin {

    @Override
    public void init() {
        OLog.d("PluginManager", "camera plugin loaded");
        SubcoreCamera.getCore();
    }
}
