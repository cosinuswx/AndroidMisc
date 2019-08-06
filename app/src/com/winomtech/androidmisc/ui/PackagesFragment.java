package com.winomtech.androidmisc.ui;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.winom.olog.OLog;
import com.winomtech.androidmisc.R;
import com.winomtech.androidmisc.common.asynccomponent.ITask;
import com.winomtech.androidmisc.common.asynccomponent.TaskExecutor;
import com.winomtech.androidmisc.view.TouchToShowLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @since 2015-02-04
 * @author kevinhuang 
 */
public class PackagesFragment extends Fragment implements
		TouchToShowLayout.ITouchListener,
		TouchToShowLayout.IItemInfoProvider {
	static final String TAG = PackagesFragment.class.getSimpleName();

	ListView		mListView;
	PackageAdapter	mPackageAdapter;
	LinearLayout	mWattingCtn;
	ImageView		mIvCover;

	final static int	STATE_GETDATA = 0;
	final static int	STATE_FINISH = 1;
	final static String	KEY_LIST = "key_list";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		TouchToShowLayout rootView = (TouchToShowLayout) inflater.inflate(R.layout.fragment_packages, container, false);
		mIvCover = (ImageView) rootView.findViewById(R.id.iv_cover_image);
		mListView = (ListView) rootView.findViewById(R.id.lv_package_list);
		mListView.setVisibility(View.INVISIBLE);

		mWattingCtn = (LinearLayout) rootView.findViewById(R.id.ll_please_waitting);
		mWattingCtn.setVisibility(View.VISIBLE);

		mPackageAdapter = new PackageAdapter(getActivity());
		mListView.setOnItemClickListener(mPackageAdapter);

		rootView.setTouchListener(this);
		rootView.setItemInfoProvider(this);

		try {
			TaskExecutor executor = new TaskExecutor();
			executor.addStateNode(STATE_GETDATA, mGetAppInfosTask, false);
			executor.addStateNode(STATE_FINISH, mAppInfosGetFinsih , true);
			executor.addTransRule(STATE_GETDATA, 0, STATE_FINISH);
			executor.execute(STATE_GETDATA, null, null);
		} catch (Exception e) {
			OLog.e(TAG, "executor failed: " + e.getMessage());
		}
		return rootView;
	}

	ITask mAppInfosGetFinsih = new ITask() {

			@Override
			public int run(Map<String, Object> data) {
			mPackageAdapter.setList((List<AppInfo>) data.get(KEY_LIST));
			mListView.setAdapter(mPackageAdapter);
			mListView.setVisibility(View.VISIBLE);
			mWattingCtn.setVisibility(View.GONE);
			return 0;
		}
	};

	ITask mGetAppInfosTask = new ITask() {

		@Override
		public int run(Map<String, Object> data) {
			if (null == getActivity()) {
				return 0;
			}

			List<AppInfo> appInfoList = new ArrayList<AppInfo>();
			PackageManager pm = getActivity().getPackageManager();
			List<PackageInfo> pkgInfos = pm.getInstalledPackages(0);
			for (PackageInfo info : pkgInfos) {
				AppInfo appInfo = new AppInfo();
				appInfo.strPkg = info.packageName;
				appInfo.name = info.applicationInfo.loadLabel(pm).toString();
				appInfo.icon = info.applicationInfo.loadIcon(pm);
				appInfo.verCode = info.versionCode;
				appInfoList.add(appInfo);
			}
			data.put(KEY_LIST, appInfoList);
			return 0;
		}
	};

	@Override
	public boolean onLongTouch(int pos) {
		int offset = pos - mListView.getFirstVisiblePosition();
		View v = mListView.getChildAt(offset);
		PackageAdapter.ViewHolder holder = (PackageAdapter.ViewHolder) v.getTag();
		if (holder.tvName.getText().toString().startsWith("com.")) {
			mIvCover.setVisibility(View.VISIBLE);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onTouchEnd() {
		mIvCover.setVisibility(View.GONE);
	}

	@Override
	public void onClkWhenLongTouch() {
		
	}

	@Override
	public int getItemOfPoint(float x, float y) {
		int pos = -1;
		int start = mListView.getFirstVisiblePosition();
		int end = mListView.getLastVisiblePosition();
		int count = end - start;
		for (int i = 0; i <= count; ++i) {
			OLog.i("test", "pos: " + i);
			View v = mListView.getChildAt(i);
			if (y <= v.getBottom()) {
				pos = i;
				PackageAdapter.ViewHolder holder = (PackageAdapter.ViewHolder) v.getTag();
				OLog.d("test", "on name: " + holder.tvName.getText());
				break;
			}
		}
		return start + pos;
	}

	static class PackageAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
		Context			mContext;
		List<AppInfo>	mAppInfoList;

		public PackageAdapter(Context context) {
			mContext = context;
		}

		public void setList(List<AppInfo> appInfos) {
			mAppInfoList = appInfos;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return null == mAppInfoList ? 0 : mAppInfoList.size();
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
			AppInfo appInfo = mAppInfoList.get(position);
			ViewHolder holder;
			if (null == convertView) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_package_item, parent, false);
				holder = new ViewHolder();
				holder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_app_icon);
				holder.tvName = (TextView) convertView.findViewById(R.id.tv_app_name);
				holder.tvPkgStr = (TextView) convertView.findViewById(R.id.tv_app_package);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.ivIcon.setImageDrawable(appInfo.icon);
			holder.tvName.setText(appInfo.name);
			holder.tvPkgStr.setText(appInfo.strPkg + " : "  + appInfo.verCode);
			return convertView;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			ViewHolder holder = (ViewHolder) view.getTag();
			TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -20);
			animation.setDuration(500);
			animation.setFillAfter(true);
			holder.tvName.startAnimation(animation);
		}

		static class ViewHolder {
			public ImageView ivIcon;
			public TextView tvName;
			public TextView tvPkgStr;
		}
	}

	public static class AppInfo {
		public String	name;
		public Drawable icon;
		public String	strPkg;
		public int		verCode;
	}
}
