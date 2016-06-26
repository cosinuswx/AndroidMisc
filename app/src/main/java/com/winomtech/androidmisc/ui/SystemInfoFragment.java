package com.winomtech.androidmisc.ui;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.winomtech.androidmisc.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kevinhuang 
 * @since 2015-02-10
 */
public class SystemInfoFragment extends Fragment {
	ListView	mListView;
	InfoAdapter	mInfoAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_system_info, container, false);
		mListView = (ListView) rootView.findViewById(R.id.lv_info_list);
		mInfoAdapter = new InfoAdapter(getActivity());
		mListView.setAdapter(mInfoAdapter);
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		List<String> lstInfo = new ArrayList<String>();
		lstInfo.add(getAudioModeInfo());
		mInfoAdapter.setInfos(lstInfo);
	}
	
	String getAudioModeInfo() {
		AudioManager audioMgr = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
		return String.format(getString(R.string.str_audio_mode_info), audioMgr.getMode());
	}
	
	static class InfoAdapter extends BaseAdapter {
		List<String> 	mLstInfo;
		Context			mContext;

		public InfoAdapter(Context context) {
			mContext = context;
		}

		public void setInfos(List<String> lstInfo) {
			mLstInfo = lstInfo;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return null == mLstInfo ? 0 : mLstInfo.size();
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
				convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_info_item, parent, false);
			}
			TextView tvInfo = (TextView) convertView.findViewById(R.id.tv_info_item);
			tvInfo.setText(mLstInfo.get(position));
			return convertView;
		}
	}
}
