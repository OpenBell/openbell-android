/*
 * Copyright (c) 2016-17 Open Bell Project, and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 * WLD-Sebastian R
 * WLD-Paul J
 * WLD-Muj H
 *
 * Updated: 2/15/17 6:08 AM
 */

package org.openbell.sdk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

public class ActivitySplash extends Activity {
	private Handler mHandler = null;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate (savedInstanceState);

		setContentView (R.layout.activity_splash);
	}

	@Override
	protected void onResume () {
		super.onResume ();

		mHandler = new Handler (Looper.getMainLooper ());
		mHandler.postDelayed (taskShowApp, (BuildConfig.DEBUG ? 1000 : 3000));
	}

	@Override
	protected void onDestroy () {
		super.onDestroy ();

		mHandler.removeCallbacksAndMessages (null);
	}

	private final Runnable taskShowApp = new Runnable () {
		@Override
		public void run () {
			Intent mIntent = new Intent (getApplicationContext (), ActivityMain.class);
			startActivity (mIntent);
			overridePendingTransition (R.anim.abc_slide_in_bottom, R.anim.abc_slide_out_top);
			finish ();
		}
	};
}
