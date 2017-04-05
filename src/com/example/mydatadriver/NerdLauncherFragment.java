package com.example.mydatadriver;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.provider.MyProvider;
import com.example.provider.RestoreProvider;


public class NerdLauncherFragment extends ListFragment {
	
	private static final String TAG = "NerdLauncherFragment";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent startupIntent = new Intent(Intent.ACTION_MAIN);
		startupIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		PackageManager pm = getActivity().getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(startupIntent, 0);
		
		Log.i(TAG,"I've found "+activities.size()+" activities.");
		
		Collections.sort(activities, new Comparator<ResolveInfo>(){
			@Override
			public int compare(ResolveInfo lhs, ResolveInfo rhs) {
				PackageManager pm = getActivity().getPackageManager();
				return String.CASE_INSENSITIVE_ORDER.compare(lhs.loadLabel(pm).toString(), rhs.loadLabel(pm).toString());
			}
		});
		
		ArrayAdapter<ResolveInfo> adapter = new ArrayAdapter<ResolveInfo>(getActivity(), android.R.layout.simple_list_item_1, activities){
			public View getView(int pos, View convertView,ViewGroup parent){
				PackageManager pm = getActivity().getPackageManager();
				View v = super.getView(pos, convertView, parent);
				TextView tv = (TextView)v;
				ResolveInfo ri = getItem(pos);
				tv.setText(ri.loadLabel(pm));
				return v;
			}
		};
		
		setListAdapter(adapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		ResolveInfo resolveInfo = (ResolveInfo)l.getAdapter().getItem(position);
		ActivityInfo activityInfo = resolveInfo.activityInfo;
		
		if(activityInfo == null) return;
		Intent i = new Intent(Intent.ACTION_MAIN);
		String packageName = activityInfo.applicationInfo.packageName;
		
		String activityName = queryActivity(packageName);
		i.setClassName(packageName, activityName == null ? activityInfo.name : activityName);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
		getActivity().finish();
	}

	private String queryActivity(String packageName) {
		ContentResolver contentResolver = getActivity().getContentResolver();
		Cursor query = contentResolver.query(RestoreProvider.URI, null, RestoreProvider.COLUMN_PACKAGE + " = ? ", new String[]{packageName}, null);
		while(query.moveToNext()){
			String activityName = query.getString(query.getColumnIndex(RestoreProvider.COLUMN_ID));
			return activityName;
		}
		return null;
	}
}
