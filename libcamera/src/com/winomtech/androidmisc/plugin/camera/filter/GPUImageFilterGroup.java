package com.winomtech.androidmisc.plugin.camera.filter;

import java.util.ArrayList;
import java.util.List;

public class GPUImageFilterGroup extends GPUImageFilterGroupBase {
    protected List<GPUImageFilter> mFilters;
    protected List<GPUImageFilter> mMergedFilters;

    public GPUImageFilterGroup() {
        mFilters = new ArrayList<>();
        mMergedFilters = new ArrayList<>();
    }

    @Override
    public List<GPUImageFilter> getRenderFilters() {
        return mMergedFilters;
    }

    public void addFilter(GPUImageFilter filter) {
        if (filter == null) {
            return;
        }
        mFilters.add(filter);
        updateMergedFilters();
    }

    @Override
    public void onInit() {
        super.onInit();
        for (int i = 0; i < mMergedFilters.size(); ++i) {
            mMergedFilters.get(i).init();
        }
    }

    @Override
    public void onDestroy() {
        for (GPUImageFilter filter : mMergedFilters) {
            filter.destroy();
        }
        super.onDestroy();
    }

    public List<GPUImageFilter> getMergedFilters() {
        return mMergedFilters;
    }

    public void updateMergedFilters() {
        if (mFilters == null) {
            return;
        }

        mMergedFilters.clear();

        List<GPUImageFilter> filters;
        for (GPUImageFilter filter : mFilters) {
            if (filter instanceof GPUImageFilterGroup) {
                ((GPUImageFilterGroup) filter).updateMergedFilters();
                filters = ((GPUImageFilterGroup) filter).getMergedFilters();
                if (filters != null && !filters.isEmpty()) {
                    mMergedFilters.addAll(filters);
                }
            } else {
                mMergedFilters.add(filter);
            }
        }
    }
}
