package com.example.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;

import com.example.bean.Task;
import com.example.provider.MyProvider;
import com.example.provider.RestoreProvider;

public class DataManager{
	public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/MyDataDriver/";
	public static final String TEMP_DATA_PATH = "data.txt";
	
	private static DataManager dataUtil= new DataManager();
	
	private DataManager(){
	}
	
	public static DataManager getInstance(){
		return dataUtil;
	}
	
	/**
	 * 从本地数据库获取数据
	 * @param context
	 * @return
	 */
	private List<Task> queryDatas(Context context) {
		List<Task> list = new ArrayList<Task>();
		ContentResolver contentResolver = context.getContentResolver();
		Cursor cursor = contentResolver.query(MyProvider.URI, null, null, null, null);
		
		if (cursor != null) {
			while(cursor.moveToNext()){
				String packageName= cursor.getString(cursor.getColumnIndex(MyProvider.COLUMN_PACKAGE));
				String index = cursor.getString(cursor.getColumnIndex(MyProvider.COLUMN_ID));
				String key = cursor.getString(cursor.getColumnIndex(MyProvider.COLUMN_KEY));
				String value = cursor.getString(cursor.getColumnIndex(MyProvider.COLUMN_VALUE));
				Task task = new Task(packageName,index, key, value);
				list.add(task);
			}
		}
		return list;
	}

	/**
	 * 将task list 转换为json数据串，方便上传。，
	 * @param context
	 * @return
	 */
	public String taskList2Json(Context context) {
		List<Task> list = queryDatas(context);
		String json = Convert2Json.listTask2Json(list);
		return json;
	}
	
	/**
	 * 将下载的数据保存到contentProvider;
	 */
	public void saveDownloadData(Context context, Map<String, Task> map){
		ContentResolver contentResolver = context.getContentResolver();
		contentResolver.delete(RestoreProvider.URI, null, null);
		ContentValues values = new ContentValues();
		for(String key : map.keySet()){
			Task task = map.get(key);
			values.put(RestoreProvider.COLUMN_PACKAGE, task.getPackageName());
			values.put(RestoreProvider.COLUMN_ID, task.getIndex());
			values.put(RestoreProvider.COLUMN_KEY, task.getKey());
			values.put(RestoreProvider.COLUMN_VALUE, task.getValue());
			contentResolver.insert(RestoreProvider.URI, values);
		}
	}
}
