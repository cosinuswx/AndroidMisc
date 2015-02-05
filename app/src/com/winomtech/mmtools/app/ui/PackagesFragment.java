package com.winomtech.mmtools.app.ui;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.winomtech.mmtools.app.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 2015-02-04
 * @author kevinhuang 
 */
public class PackagesFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_packages, container, false);
		ListView listView = (ListView) rootView.findViewById(R.id.lv_package_list);
		PackageAdapter adapter = new PackageAdapter(getActivity());
		adapter.setList(getAllInstalledAppInfo());
		listView.setAdapter(adapter);
		return rootView;
	}

	private List<AppInfo> getAllInstalledAppInfo() {
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
		return appInfoList;
	}

	private static class PackageAdapter extends BaseAdapter {
		private Context			mContext;
		private List<AppInfo>	mAppInfoList;

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
			if (null == convertView) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_package_item, parent, false);
			}
			ImageView ivIcon = (ImageView) convertView.findViewById(R.id.iv_app_icon);
			ivIcon.setImageDrawable(appInfo.icon);
			TextView tvName = (TextView) convertView.findViewById(R.id.tv_app_name);
			tvName.setText(appInfo.name);
			TextView tvPkgStr = (TextView) convertView.findViewById(R.id.tv_app_package);
			tvPkgStr.setText(appInfo.strPkg + " : "  + appInfo.verCode);
			return convertView;
		}
	}

	public static class AppInfo {
		public String	name;
		public Drawable icon;
		public String	strPkg;
		public int		verCode;
	}
}
