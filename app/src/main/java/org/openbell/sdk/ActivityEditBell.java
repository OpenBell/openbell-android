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
 * Updated: 2/15/17 4:53 AM
 */

package org.openbell.sdk;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class ActivityEditBell extends AppCompatActivity {
	private static final String LOG_TAG = ActivityEditBell.class.getSimpleName ();

	OpenBell openBell = null;
	EditText bellName;
	EditText bellUUID;
	Switch bellEnabled;
	String bellName_original;
	String bellUUID_original;
	int bellEnabled_original;
	int rowid;

	public void onCreate (Bundle savedInstanceState) {
		super.onCreate (savedInstanceState);

		setContentView (R.layout.activity_edit);

		openBell = OpenBell.getInstance (getApplicationContext ());

		bellName = (EditText) findViewById (R.id.edittext_bellname);
		bellEnabled = (Switch) findViewById (R.id.switch_bellenabled);
		bellUUID = (EditText) findViewById (R.id.edittext_belluuid);

		/**
		 * Check if this is a new bell or an existing one
		 * If it is an existing one, get the bell from DB
		 */
		rowid = getIntent ().getIntExtra ("rowid", 0);
		getBell (rowid);

		/**
		 * Initial name, bell code and enabled status to compare on onBackPressed()
		 */
		bellName_original = bellName.getText ().toString ();
		bellUUID_original = bellUUID.getText ().toString ();
		bellEnabled_original = (bellEnabled.isChecked () ? 1 : 0);
	}

	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		getMenuInflater ().inflate (R.menu.menu_editbell, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
		switch (item.getItemId ()) {
			/**
			 *  On save button pressed, make sure both name and bell code are completed, check for internet connection
			 *  and if this was a new bell, add it in the DB and call the api to register the bell. Else update existing bell.
			 * */
			case R.id.action_add:
				if (bellName.getText ().toString ().equals ("")) {
					Toast.makeText (this, R.string.no_name, Toast.LENGTH_SHORT).show ();
				} else if (bellUUID.getText ().toString ().equals ("") || bellUUID.getText ().toString ().equals (null)) {
					Toast.makeText (this, R.string.no_bell_code, Toast.LENGTH_SHORT).show ();
				} else if (rowid == 0) {
					if (!isNetworkAvailable ()) {
						Toast.makeText (this, R.string.conectivity_problem, Toast.LENGTH_LONG).show ();
					} else {
						callApiRegister (FirebaseInstanceId.getInstance ().getToken (), bellUUID.getText ().toString ());
					}
				} else {
					updateBell (rowid);
				}
				break;

			/**
			 * If this is a new bell, open the scanning activity, else show error message
			 */
			case R.id.action_scan:
				if (rowid == 0) {
					Intent mIntent = new Intent (this, com.google.zxing.client.android.CaptureActivity.class);
					mIntent.setAction ("com.google.zxing.client.android.SCAN");
					mIntent.putExtra ("SAVE_HISTORY", false);
					startActivityForResult (mIntent, 0);
				} else {
					Toast.makeText (this, R.string.existing_bell_change_error, Toast.LENGTH_SHORT).show ();
				}
				break;

			default:
		}

		return super.onOptionsItemSelected (item);
	}

	@Override
	protected void onActivityResult (int codeRequest, int codeResult, Intent data) {
		super.onActivityResult (codeRequest, codeResult, data);

		if (codeRequest == 0) {
			if (codeResult == RESULT_OK) {
				// Get the extras that are returned from the scanner activity
				String qrResult = data.getStringExtra ("SCAN_RESULT");
				String mQuery = "SELECT _ROWID_ FROM bells WHERE uuid = \"" + LIB_DB_SQLite.dbEscape (qrResult) + "\"";
				LIB_DB_SQLite.DBSQLResults mResults = openBell._appSQLiteDB.new DBSQLResults ();

				if (!openBell._appSQLiteDB.dbQuery (mQuery, mResults))
					finish ();

				if (mResults.numRows == 0) {
					bellUUID.setText (qrResult);
					bellUUID.setEnabled (false);
				} else {
					alreadyScannedDialog (mResults._int ("rowid"));
				}
			}
		}
	}

	@Override
	public void onBackPressed () {
		if (!(bellName_original.equals (bellName.getText ().toString ())) || !(bellUUID_original.equals (bellUUID.getText ().toString ())) || bellEnabled_original != (bellEnabled.isChecked () ? 1 : 0)) {
			AlertDialog.Builder builder = new AlertDialog.Builder (this);
			builder.setTitle (getString (R.string.back_confirmation_dialog));

			// Set up the buttons
			builder.setPositiveButton (getString (R.string.button_discard), new DialogInterface.OnClickListener () {
				@Override
				public void onClick (DialogInterface dialog, int which) {
					finish ();
				}
			});
			builder.setNegativeButton (getString (R.string.button_cancel), new DialogInterface.OnClickListener () {
				@Override
				public void onClick (DialogInterface dialog, int which) {
					dialog.cancel ();
				}
			});
			builder.show ();
		} else {
			finish ();
		}
	}

	public void getBell (int rowid) {
		if (rowid > 0) {
			String sqlQuery = "SELECT name, uuid, enabled FROM bells WHERE _ROWID_ = " + rowid;

			LIB_DB_SQLite.DBSQLResults sqlResults = openBell._appSQLiteDB.new DBSQLResults ();

			if (!openBell._appSQLiteDB.dbQuery (sqlQuery, sqlResults))
				finish ();

			this.rowid = rowid;
			bellName.setText (sqlResults._string ("name"));
			bellUUID.setText (sqlResults._string ("uuid"));
			bellUUID.setEnabled (false);
			bellEnabled.setChecked (sqlResults._int ("enabled") == 1);
		} else {
			bellName.setText ("");
			bellUUID.setText ("");
			bellUUID.setEnabled (true);
			bellEnabled.setChecked (true);
		}
	}

	public void callApiRegister (String token, String bellUUID) {
		if (BuildConfig.DEBUG)
			Log.d (LOG_TAG, "callApiRegister()");

		LIB_HTTP_Callback httpResponse = new LIB_HTTP_Callback () {
			@Override
			public void httpCallback (final int httpResponseCode, byte[] httpResponseData) {
				runOnUiThread (new Runnable () {
					@Override
					public void run () {
						if (httpResponseCode == 201) {
							updateBell (0);
						} else {
							Toast.makeText (getApplicationContext (), R.string.bell_register_problem, Toast.LENGTH_LONG).show ();
						}
					}
				});
			}
		};

		HashMap<String, String> paramas = new HashMap<String, String> ();
		paramas.put ("command", "REGISTER");
		paramas.put ("token", token);
		paramas.put ("belluuid", bellUUID);

		LIB_HTTP httpRequest = new LIB_HTTP (getString (R.string.api_url), paramas, httpResponse);
		httpRequest.exec ();
	}

	public void updateBell (int rowid) {
		if (BuildConfig.DEBUG)
			Log.d (LOG_TAG, "updateBell(rowid = " + rowid + ")");

		String mQuery = (rowid == 0 ? "INSERT INTO bells (name, uuid, enabled) VALUES (\"" + LIB_DB_SQLite.dbEscape (bellName.getText().toString()) + "\", \"" + LIB_DB_SQLite.dbEscape (bellUUID.getText().toString()) + "\", " + (bellEnabled.isChecked () ? "1" : "0") + ")" : "UPDATE bells SET name = \"" + LIB_DB_SQLite.dbEscape (bellName.getText ().toString ()) + "\", enabled = " + (bellEnabled.isChecked () ? "1" : "0") + " WHERE _ROWID_ =  " + rowid);
		if (!openBell._appSQLiteDB.dbQuery (mQuery)) {
			Toast.makeText (this, getString (R.string.bell_register_problem), Toast.LENGTH_LONG).show ();
		} else {
			Toast.makeText (getApplicationContext (), R.string.bell_updated, Toast.LENGTH_SHORT).show ();
		}

		finish ();
	}

	public void alreadyScannedDialog (final int rowid) {
		AlertDialog.Builder builder = new AlertDialog.Builder (this);
		builder.setTitle (getString (R.string.uuid_exist));

		// Set up the buttons
		builder.setPositiveButton (getString (R.string.button_ok), new DialogInterface.OnClickListener () {
			@Override
			public void onClick (DialogInterface dialog, int which) {
				getBell (rowid);
			}
		});
		builder.setNegativeButton (getString (R.string.button_cancel), new DialogInterface.OnClickListener () {
			@Override
			public void onClick (DialogInterface dialog, int which) {
				dialog.cancel ();
			}
		});
		builder.show ();
	}

	public boolean isNetworkAvailable () {
		ConnectivityManager cm = (ConnectivityManager) getSystemService (Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo ();
		return (ni != null && ni.isConnected ());
	}
}
