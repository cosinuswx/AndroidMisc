package com.winomtech.androidmisc.encpic;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.bumptech.glide.module.GlideModule;
import com.winomtech.androidmisc.common.constants.Constants;

/**
 * @author kevinhuang
 * @since 2016-12-24
 */
public class AmGlideModule implements GlideModule {

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        builder.setDiskCache(new DiskLruCacheFactory(Constants.GLIDE_CACHE, 50 * 1024 * 1024));
    }

    @Override
    public void registerComponents(Context context, Glide glide) {
    }
}

