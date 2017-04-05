package com.example.mydatadriver;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

public class NerdLauncherActivity extends SingleFramentActivity {

	@Override
	protected Fragment createFragment() {
		return new NerdLauncherFragment();
	}

	
}
