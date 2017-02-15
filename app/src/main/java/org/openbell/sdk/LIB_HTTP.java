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
 * Updated: 2/15/17 4:48 AM
 */

package org.openbell.sdk;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class LIB_HTTP {
	private static final String LOG_TAG = LIB_HTTP.class.getSimpleName ();

	private final static int HTTP_TIMEOUT = 10000;

	private URL httpURL = null;
	private HashMap <String, String> httpHeaders = null;
	private HashMap <String, String> httpPairs = null;
	private LIB_HTTP_Callback httpCallback = null;
	private int httpResponseCode = -1;
	private byte[] httpRequestData = null;
	private byte[] httpResponseData = null;

	public LIB_HTTP (String httpURL, HashMap <String, String> httpPairs, LIB_HTTP_Callback httpCallback) {
		this (httpURL, null, httpPairs, httpCallback);
	}

	public LIB_HTTP (String httpURL, HashMap <String, String> httpHeaders, HashMap <String, String> httpPairs, LIB_HTTP_Callback httpCallback) {
		if (BuildConfig.DEBUG)
			Log.d (LOG_TAG, "Creating new LIB_HTTP object, httpURL=" + httpURL);

		try {
			this.httpURL = new URL (httpURL);
			this.httpHeaders = httpHeaders;
			this.httpPairs = httpPairs;
			this.httpCallback = httpCallback;
		} catch (MalformedURLException e) {
			if (BuildConfig.DEBUG) {
				Log.d (LOG_TAG, "Error parsing URL");
				e.printStackTrace ();
			}
		}
	}

	public byte[] readStream (InputStream inputStream) {
		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream ();
			int i = inputStream.read ();
			while (i != -1) {
				bo.write (i);
				i = inputStream.read ();
			}

			return bo.toByteArray ();
		} catch (IOException e) {
			return "".getBytes ();
		}
	}

	public void exec () {
		if (httpURL == null)
			return;

		new Thread (new Runnable () {
			@Override
			public void run () {
				try {
					if (httpPairs != null) {
						StringBuilder inputText = new StringBuilder ();
						for (Map.Entry <String, String> httpPair : httpPairs.entrySet ()) {
							if (inputText.length () != 0)
								inputText.append ("&");

							if (BuildConfig.DEBUG)
								Log.d (LOG_TAG, "Adding keypair	" + httpPair.getKey () + "=" + httpPair.getValue ());

							inputText.append (URLEncoder.encode (httpPair.getKey (), "UTF-8") + "=" + URLEncoder.encode (String.valueOf (httpPair.getValue ()), "UTF-8"));
						}
						httpRequestData = inputText.toString ().getBytes ("UTF-8");
					}

					HttpURLConnection httpURLConnection = (HttpsURLConnection) httpURL.openConnection ();
					httpURLConnection.setReadTimeout (HTTP_TIMEOUT);
					httpURLConnection.setConnectTimeout (HTTP_TIMEOUT);
					httpURLConnection.setRequestMethod ("POST");

					httpURLConnection.setRequestProperty ("Content-Type", "text/plain; charset=UTF-8");
					httpURLConnection.setRequestProperty ("Content-Length", httpRequestData == null ? "0" : String.valueOf (httpRequestData.length));
					httpURLConnection.setRequestProperty ("Content-Type", "application/x-www-form-urlencoded");
					//httpURLConnection.setRequestProperty ("Accept-Charset", "UTF-8");

					if (httpHeaders != null) {
						for (Map.Entry <String, String> httpHeader : httpHeaders.entrySet ())
							httpURLConnection.setRequestProperty (httpHeader.getKey (), httpHeader.getValue ());
					}

					httpURLConnection.setDoInput (true);
					httpURLConnection.setDoOutput (true);
					httpURLConnection.setInstanceFollowRedirects (true);
					httpURLConnection.setUseCaches (false);

					if (httpRequestData != null)
						httpURLConnection.getOutputStream ().write (httpRequestData);

					httpURLConnection.connect ();

					httpResponseCode = httpURLConnection.getResponseCode ();

					if (BuildConfig.DEBUG)
						Log.d (LOG_TAG, "Due to httpResponseCode, reading from " + (httpResponseCode >= 200 && httpResponseCode < 400 ? "getInputStream()" : "getErrorStream()"));
					InputStream inputStream = new BufferedInputStream (httpResponseCode >= 200 && httpResponseCode < 400 ? httpURLConnection.getInputStream () : httpURLConnection.getErrorStream ());
					httpResponseData = readStream (inputStream);
				} catch (ProtocolException e) {
					if (BuildConfig.DEBUG) {
						Log.d (LOG_TAG, "Protocol exception");
						e.printStackTrace ();
					}
				} catch (IOException e) {
					if (BuildConfig.DEBUG) {
						Log.d (LOG_TAG, "I/O exception");
						e.printStackTrace ();
					}
				} finally {
					if (BuildConfig.DEBUG) {
						Log.d (LOG_TAG, "HTTP response: httpResponseCode=" + String.valueOf (httpResponseCode) + ", httpResponseData=" + (httpResponseData == null ? "NULL" : String.valueOf (httpResponseData)));
						Log.d (LOG_TAG, "httpCallback" + (httpCallback == null ? "=NULL" : " defined, calling"));
					}

					if (httpCallback != null)
						httpCallback.httpCallback (httpResponseCode, httpResponseData);
				}
			}
		}).start ();
	}
}
