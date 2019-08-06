package com.winomtech.androidmisc.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.winomtech.androidmisc.R;
import com.winomtech.androidmisc.rs.ScriptC_Gray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class RenderScriptFragment extends Fragment {
    private RelativeLayout mRootView;
    private ImageView mImageView;
    private int mPosition;

    private Bitmap mInBitmap;
    private Bitmap mOutBitmap;

    private Allocation mAllocationIn;
    private Allocation mAllocationOut;

    private RenderScript mRenderScript;
    private ScriptC_Gray mGrayScript;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = (RelativeLayout) inflater.inflate(R.layout.fragment_renderscript, container, false);
        mImageView = mRootView.findViewById(R.id.iv_renderscript);
        mRootView.setOnTouchListener(mRootTouchLsn);

        mInBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.renderscript_input);
        mOutBitmap = Bitmap.createBitmap(mInBitmap.getWidth(), mInBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        mPosition = mInBitmap.getWidth();

        mRenderScript = RenderScript.create(getActivity());
        mGrayScript = new ScriptC_Gray(mRenderScript);

        mAllocationIn = Allocation.createFromBitmap(mRenderScript, mInBitmap);
        mAllocationOut = Allocation.createFromBitmap(mRenderScript, mOutBitmap);

        refreshBitmap();
        return mRootView;
    }

    void refreshBitmap() {
        mGrayScript.forEach_root(mAllocationIn, mAllocationOut);
        mAllocationOut.copyTo(mOutBitmap);
        mImageView.setImageBitmap(mOutBitmap);
    }

    private View.OnTouchListener mRootTouchLsn = new View.OnTouchListener() {
        private float mDownPos;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownPos = event.getRawX();
                return true;

            case MotionEvent.ACTION_MOVE:
                int pos = (int) (mPosition + (event.getRawX() - mDownPos));
                mGrayScript.set_gPos(pos);
                refreshBitmap();
                return true;

            case MotionEvent.ACTION_UP:
                mPosition = (int) (mPosition + (event.getRawX() - mDownPos));
                return true;
            }
            return false;
        }
    };
}
