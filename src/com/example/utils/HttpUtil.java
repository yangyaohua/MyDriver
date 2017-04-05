package com.example.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import android.content.Context;

import com.example.bean.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class HttpUtil {

	public static String URL = "http://172.23.243.219:8080/MyDataDriver/";
	private static final int TIME_OUT = 10 * 1000; // 超时时间
	private static final String CHARSET = "utf-8";
	
	// 根据传入的map，创建url,
	private static URL getUrl(String servletName, Map<String, String> map) {
		StringBuffer sb = new StringBuffer(URL + servletName);
		if (map != null && !map.isEmpty()) {
			sb.append("?");
			for (String s : map.keySet()) {
				sb.append(s + "=" + map.get(s) + "&");
			}
			sb.deleteCharAt(sb.length() - 1);
		}
		URL url = null;
		try {
			url = new URL(sb.toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return url;
	}

	public static boolean uploadData(Context context) {
		boolean flag = false;
		try {
			URL url = getUrl("UploadDataServlet", null);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setReadTimeout(TIME_OUT);
			connection.setConnectTimeout(TIME_OUT);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Charset", CHARSET);
			connection.setRequestProperty("connection", "keep-alive");
			
			String taskJson = DataManager.getInstance().taskList2Json(context);
			DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
			byte[] bytes = taskJson.getBytes();
			dos.write(bytes);
			dos.close();
			
			InputStream is = connection.getInputStream();
			String str = "";
			StringBuffer sb = new StringBuffer();
			if (connection.getResponseCode() == 200) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				while((str = reader.readLine()) != null){
					sb.append(str);
				}
				is.close();
			}
			if (!sb.toString().equals("") && Boolean.valueOf(sb.toString())) {
				flag = true;
			}
			if (connection != null) {
				connection.disconnect();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}

	public static String connection(Context context, String servletName,
			Map<String, String> map) {
		try {
			URL url = getUrl(servletName, map);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setRequestMethod("POST");
			conn.connect();
			InputStream is = conn.getInputStream();
			String str = "";
			StringBuffer sb = new StringBuffer();
			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is));
				while ((str = reader.readLine()) != null) {
					sb.append(str);
				}
			}
			conn.disconnect();
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Map<String, Task> downloadData(Context context){
		String result = connection(context, "QueryAndroidTaskServlet", null);
		if (result != null) {
			Gson gson = new Gson();
			Map<String, Task> taskMap = gson.fromJson(result,new TypeToken<Map<String, Task>>() {}.getType());	
			DataManager.getInstance().saveDownloadData(context, taskMap);
			return taskMap;
		}
		return null;
	}
}
