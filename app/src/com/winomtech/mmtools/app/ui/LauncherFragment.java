package com.winomtech.mmtools.app.ui;

import com.winomtech.mmtools.app.R;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class LauncherFragment extends Fragment {
	private static final String TAG = LauncherFragment.class.getSimpleName();

	private static class EntryInfo {
		public String	name;
		public String	className;
		
		public EntryInfo(String name, String className) {
			this.name = name;
			this.className = className;
		}
	}

	private static List<EntryInfo> sEntryInfoList = null;

	private static void initList(Context context) {
		sEntryInfoList = new ArrayList<EntryInfo>();
		sEntryInfoList.add(new EntryInfo(context.getString(R.string.str_copy_traces_to_sdcard),
				CopyTracesFragment.class.getCanonicalName()));
		sEntryInfoList.add(new EntryInfo(context.getString(R.string.str_app_list),
				PackagesFragment.class.getCanonicalName()));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (null == sEntryInfoList) {
			initList(getActivity());
		}

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
				Class<?> clazz = Class.forName(sEntryInfoList.get(position).className);
				Constructor<?> construct = clazz.getConstructor();
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
			tvName.setText(sEntryInfoList.get(position).name);
			return convertView;
		}
	}
}
