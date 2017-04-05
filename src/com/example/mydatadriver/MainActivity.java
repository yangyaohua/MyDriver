package com.example.mydatadriver;

import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import com.example.bean.Task;
import com.example.utils.HttpUtil;

public class MainActivity extends Activity {

	private Button btn_sync, btn_down;

	private static final int MESSAGE_SYNC_STATE = 0;
	private static final int MESSAGE_DOWN_DATA = 1;
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				boolean flag = (Boolean) msg.obj;
				if (flag) {
					Toast.makeText(getApplicationContext(), "数据同步成功", Toast.LENGTH_LONG).show();
				}else {
					Toast.makeText(getApplicationContext(), "同步失败", Toast.LENGTH_LONG).show();
				}
				break;
			case 1:
				Map<String, Task> taskMap = (Map<String, Task>) msg.obj;
				if (taskMap != null && !taskMap.isEmpty()) {
					Toast.makeText(getApplicationContext(), "恢复数据成功", Toast.LENGTH_LONG).show();
					Intent intent = new Intent(MainActivity.this,NerdLauncherActivity.class);
					startActivity(intent);
				}else {
					Toast.makeText(getApplicationContext(), "连接服务器失败", Toast.LENGTH_LONG).show();
					Intent intent = new Intent(MainActivity.this,NerdLauncherActivity.class);
					startActivity(intent);
				}
			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initView();
		
		initEvent();
	}

	private void initEvent() {
		btn_sync.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new Thread(){
					public void run() {
						boolean flag = HttpUtil.uploadData(MainActivity.this);
						//DataManager.getInstance().syncDatas(MainActivity.this);
						Message msg = Message.obtain();
						msg.what = MESSAGE_SYNC_STATE;
						msg.obj = flag;
						mHandler.sendMessage(msg);
					};
				}.start();
			}
		});
		
		btn_down.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new Thread(){
					public void run() {
						Map<String, Task> taskMap = HttpUtil.downloadData(MainActivity.this);
						Message message = Message.obtain();
						message.obj = taskMap;
						message.what = MESSAGE_DOWN_DATA;
						mHandler.sendMessage(message);
					};
				}.start();
			}
		});
	}

	private void initView() {
		btn_sync = (Button) findViewById(R.id.btn_sync);
		btn_down = (Button) findViewById(R.id.btn_down);
	}

}
