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
 * Updated: 2/15/17 6:07 AM
 */

package org.openbell.sdk;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.UUID;

public class OpenBell extends Application {
	private static final String LOG_TAG = OpenBell.class.getSimpleName ();

	private static OpenBell mInstance = null;
	public Context _appContext = null;
	public static LIB_DB_SQLite _appSQLiteDB = null;

	public OpenBell () {
	}

	private OpenBell (Context mContext) { // getInstance() constructor
		if (BuildConfig.DEBUG)
			Log.d (LOG_TAG, "OpenBell instantiated");

		_appContext = mContext;

		_appSQLiteDB = LIB_DB_SQLite.getInstance (_appContext);
		String mFileName = _appContext.getFilesDir ().getPath () + "/bells.db";
		if (BuildConfig.DEBUG)
			Log.d (LOG_TAG, "Opening database " + mFileName);

		if (!_appSQLiteDB.dbOpen (mFileName, _appSQLiteDB.OPEN_READWRITE | _appSQLiteDB.CREATE_IF_NECESSARY)) {
			if (BuildConfig.DEBUG)
				Log.d (LOG_TAG, "Unable to open database file");
			finish ();
		}

		String sqlQuery = "SELECT COUNT(*) FROM bells";
		if (!_appSQLiteDB.dbQuery (sqlQuery)) {
			if (BuildConfig.DEBUG)
				Log.d (LOG_TAG, "bells table not found, creating");

			sqlQuery = "CREATE TABLE IF NOT EXISTS bells (name VARCHAR (60), uuid CHAR (37) UNIQUE, enabled INTEGER)";
			if (!_appSQLiteDB.dbQuery (sqlQuery))
				finish ();

			if (BuildConfig.DEBUG)
				Log.d (LOG_TAG, "Table created with " + sqlQuery);

			sqlQuery = "INSERT INTO bells (name, uuid, enabled) VALUES (\"Default test record\", \"" + UUID.randomUUID ().toString () + "\", 0)";
			if (!_appSQLiteDB.dbQuery (sqlQuery))
					finish ();

			if (BuildConfig.DEBUG)
				Log.d (LOG_TAG, "Test record created with " + sqlQuery);
		}

		_appContext.startService (new Intent (_appContext, OpenBell.class));
	}

	@Override
	public void onCreate () {
		if (BuildConfig.DEBUG)
			Log.d (LOG_TAG, "onCreate()");

		super.onCreate ();
	}

	public static OpenBell getInstance (Context mContext) {
		if (BuildConfig.DEBUG)
			Log.d (LOG_TAG, "OpenBell getInstance()");

		/**
		 * Create DB if it doesn't exist or open the existing one
		 * For #Dev# purpose, a Default test record is added when the DB is first created
		 */
		if (mInstance == null) {
			synchronized (OpenBell.class) {
				if (mInstance == null) {
					if (BuildConfig.DEBUG)
						Log.d (LOG_TAG, "OpenBell Returning a new instance");

					mInstance = new OpenBell (mContext);
				}
			}
		}

		return mInstance;
	}

	void finish () {
		((Activity) _appContext).finish ();
	}
}
