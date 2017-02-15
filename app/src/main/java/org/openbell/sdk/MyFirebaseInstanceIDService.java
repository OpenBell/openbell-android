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
 * Updated: 2/15/17 7:10 AM
 */

package org.openbell.sdk;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.HashMap;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
	private static final String LOG_TAG = MyFirebaseInstanceIDService.class.getSimpleName ();

	String token = FirebaseInstanceId.getInstance ().getToken ();

	@Override
	public void onTokenRefresh () {
		if (BuildConfig.DEBUG)
			Log.d (LOG_TAG, "onTokenRefresh()");

		// Get updated InstanceID token.
		String tokenNew = FirebaseInstanceId.getInstance ().getToken ();
		if (BuildConfig.DEBUG)
			Log.d (LOG_TAG, "Refreshed token: " + tokenNew);

		updateAPIRegistrations (tokenNew);
		token = tokenNew;
	}

	public void updateAPIRegistrations (String newToken) {
		if (BuildConfig.DEBUG)
			Log.d (LOG_TAG, "updateAPIRegistrations(newToken=" + newToken + ")");

		LIB_HTTP_Callback httpResponse = new LIB_HTTP_Callback () {
			@Override
			public void httpCallback (final int httpResponseCode, byte[] httpResponseData) {
				// FIXME How do we deal with failures?
			}
		};

		HashMap<String, String> paramas = new HashMap<String, String> ();
		paramas.put ("command", "REREGISTER");
		paramas.put ("token", token);
		paramas.put ("token_new", newToken);

		LIB_HTTP httpRequest = new LIB_HTTP (getString (R.string.api_url), paramas, httpResponse);
		httpRequest.exec ();
	}
}
