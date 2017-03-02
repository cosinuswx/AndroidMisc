package com.winomtech.androidmisc.common.cores;

import android.content.Context;

import junit.framework.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kevinhuang
 * @since 2017-03-01
 */
public class AmCore {
    private final static String TAG = "AmCore";

    private static AmCore theCore = null;

    public static void initialize(Context context) {
        theCore = new AmCore();
        theCore.init(context);
    }

    public static AmCore getCore() {
        Assert.assertNotNull("AmCore not initialize!", theCore);
        return theCore;
    }

    private Map<String, ISubCore> mSubcoreMap = new HashMap<String, ISubCore>();
    private Context mContext;

    public void addSubCore(String name, ISubCore subCore) {
        Assert.assertFalse(mSubcoreMap.containsKey(name));
        mSubcoreMap.put(name, subCore);
    }

    public void init(Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }
}
