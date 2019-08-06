package com.winomtech.androidmisc.common.cores;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kevinhuang
 * @since 2017-03-01
 */
public class AmCore {
    private static AmCore theCore = null;

    public static void initialize(Context context) {
        theCore = new AmCore();
        theCore.init(context);
    }

    public static AmCore getCore() {
        return theCore;
    }

    private static Map<String, ISubCore> mSubcoreMap = new HashMap<>();
    private Context mContext;

    public void addSubCore(String name, ISubCore subCore) {
        mSubcoreMap.put(name, subCore);
    }

    public void init(Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }
}
