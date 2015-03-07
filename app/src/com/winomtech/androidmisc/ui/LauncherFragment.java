package com.winomtech.androidmisc.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.winomtech.androidmisc.R;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class LauncherFragment extends Fragment {
	private static final String TAG = LauncherFragment.class.getSimpleName();

	private static class EntryInfo {
		public int 		resId;
		public Class<?>	clazz;
		
		public EntryInfo(int resId, Class<?> clazz) {
			this.resId = resId;
			this.clazz = clazz;
		}
	}

	private static List<EntryInfo> sEntryInfoList = null;

	static {
		sEntryInfoList = new ArrayList<EntryInfo>();
		sEntryInfoList.add(new EntryInfo(R.string.entry_copy_traces_to_sdcard, CopyTracesFragment.class));
		sEntryInfoList.add(new EntryInfo(R.string.entry_app_list, PackagesFragment.class));
		sEntryInfoList.add(new EntryInfo(R.string.entry_mediarecorder_audio, MediaRecorderAudioFragment.class));
		sEntryInfoList.add(new EntryInfo(R.string.entry_record_play, AudioTrackRecordFragment.class));
		sEntryInfoList.add(new EntryInfo(R.string.entry_take_picture, TaskPictureFragment.class));
		sEntryInfoList.add(new EntryInfo(R.string.entry_system_info, SystemInfoFragment.class));
		sEntryInfoList.add(new EntryInfo(R.string.entry_test_jni_hook, JniHookFragment.class));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_launcher, container, false);
		ListView lvEntries = (ListView) rootView.findViewById(R.id.lv_entry_list);
		lvEntries.setAdapter(new EntryAdapter());
		lvEntries.setOnItemClickListener(mItemClickLsn);
		return rootView;
	}

	private AdapterView.OnItemClickListener mItemClickLsn = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
			TextView tvName = (TextView) convertView.findViewById(R.id.tv_entry_name);
			tvName.setText(sEntryInfoList.get(position).resId);
			return convertView;
		}
	}
}
