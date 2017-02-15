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
 * Updated: 2/15/17 4:24 AM
 */

package org.openbell.sdk;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

public class ActivityAbout extends AppCompatActivity {
	int licensePages = 0;

	@Override
	protected void onCreate (Bundle savedInstanceState) {

		super.onCreate (savedInstanceState);

		setContentView (R.layout.activity_about);

		ActionBar ab = getSupportActionBar ();
		if (ab != null) {
			ab.setIcon (R.mipmap.ic_launcher);
			ab.setDisplayShowHomeEnabled (true);
		}

		// Update the license textView from raw resource
		TextView textLicense = (TextView) findViewById (R.id.textViewLicense);
		String license = "Please see LICENSE file in project root";
		try {
			InputStream mInputStream = getResources ().openRawResource (R.raw.license);
			byte[] b = new byte[mInputStream.available ()];
			mInputStream.read (b);
			mInputStream.close ();
			license = new String (b);
		} catch (IOException e) {
			if (BuildConfig.DEBUG)
				e.printStackTrace ();
		}
		textLicense.setText (new String (license));

		// Update the version with the current version
		String version = "unknown";
		try {
			PackageInfo pInfo = getPackageManager ().getPackageInfo (getPackageName (), 0);
			version = pInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			if (BuildConfig.DEBUG)
				e.printStackTrace ();
		}
		TextView textVersion = (TextView) findViewById (R.id.textViewVersion);
		textVersion.setText (getString (R.string.app_version) + " " + version);
		textVersion.setOnClickListener (new View.OnClickListener () {
			@Override
			public void onClick (View v) {
				if (++licensePages == 5) {
					byte[] licensePage = {0x4e, 0x6f, 0x20, 0x45, 0x61, 0x73, 0x74, 0x65, 0x72, 0x20, 0x45, 0x67, 0x67, 0x73, 0x20, 0x68, 0x65, 0x72, 0x65, 0x20, 0x3b, 0x29};
					Toast.makeText (getApplicationContext (), new String (licensePage), Toast.LENGTH_LONG).show ();
				}
			}
		});
	}
}
