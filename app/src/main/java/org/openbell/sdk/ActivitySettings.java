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
 * Updated: 2/15/17 5:15 AM
 */

package org.openbell.sdk;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class ActivitySettings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
	private static final String LOG_TAG = ActivitySettings.class.getSimpleName ();

	MediaPlayer mediaPlayer;
	SharedPreferences sp;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate (savedInstanceState);

		addPreferencesFromResource (R.xml.preferences_notification);
		setContentView (R.layout.activity_settings);

		sp = PreferenceManager.getDefaultSharedPreferences (this);
		onSharedPreferenceChanged (sp, "preference_notification");
		sp.registerOnSharedPreferenceChangeListener (this);
	}

	@Override
	protected void onResume () {
		if (BuildConfig.DEBUG)
			Log.d (LOG_TAG, "onResume()");

		super.onResume ();
	}

	@Override
	public void onSharedPreferenceChanged (SharedPreferences sharedPreferences, String key) {
		if (BuildConfig.DEBUG)
			Log.d (LOG_TAG, "onSharedPreferenceChanged(key=" + key + ")");

		if (key.equals ("preference_notification")) {
			int mediaResource = sp.getString (key, "bell_default").equals ("bell_default") ? R.raw.bell_default : R.raw.bell_trump;
			mediaPlayer = MediaPlayer.create (this, mediaResource);
			mediaPlayer.start ();
		}
	}
}
