package com.winomtech.androidmisc.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.winomtech.androidmisc.R;
import com.winomtech.androidmisc.plugin.camera.ui.CameraFragmentV1;
import com.winomtech.androidmisc.plugin.camera.ui.CameraFragmentV2;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class LauncherFragment extends Fragment {
    private static final String TAG = LauncherFragment.class.getSimpleName();

    private static class EntryInfo {
        public int resId;
        public Class<?> clazz;

        public EntryInfo(int resId, Class<?> clazz) {
            this.resId = resId;
            this.clazz = clazz;
        }
    }

    private static final List<EntryInfo> sEntryInfoList;

    static {
        sEntryInfoList = new ArrayList<>();
        sEntryInfoList.add(new EntryInfo(R.string.entry_mediarecorder_audio, MediaRecorderAudioFragment.class));
        sEntryInfoList.add(new EntryInfo(R.string.entry_record_play, AudioTrackRecordFragment.class));
        sEntryInfoList.add(new EntryInfo(R.string.entry_take_picture, TaskPictureFragment.class));
        sEntryInfoList.add(new EntryInfo(R.string.entry_svg_drawable, SVGDrawableFragment.class));
        sEntryInfoList.add(new EntryInfo(R.string.entry_render_script_exp, RenderScriptFragment.class));
        sEntryInfoList.add(new EntryInfo(R.string.entry_gpuimage_use_old_camera, CameraFragmentV1.class));
        sEntryInfoList.add(new EntryInfo(R.string.entry_gpuimage_use_camera2, CameraFragmentV2.class));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_launcher, container, false);
        ListView lvEntries = rootView.findViewById(R.id.lv_entry_list);
        lvEntries.setAdapter(new EntryAdapter());
        lvEntries.setOnItemClickListener(mItemClickLsn);
        return rootView;
    }

    private AdapterView.OnItemClickListener mItemClickLsn = (parent, view, position, id) -> {
        try {
            Constructor<?> construct = sEntryInfoList.get(position).clazz.getConstructor();
            Fragment object = (Fragment) construct.newInstance();

            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.ll_container, object);
            transaction.addToBackStack(null);
            transaction.commit();
        } catch (Exception e) {
            Log.e(TAG, "Class Forname failed : " + e.getMessage());
        }
    };

    private class EntryAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return null == sEntryInfoList ? 0 : sEntryInfoList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (null == convertView) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_entry_item, parent, false);
            }
            TextView tvName = convertView.findViewById(R.id.tv_entry_name);
            tvName.setText(sEntryInfoList.get(position).resId);
            return convertView;
        }
    }
}
