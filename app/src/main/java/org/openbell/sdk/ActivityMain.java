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
 * Updated: 2/15/17 7:09 AM
 */

package org.openbell.sdk;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ActivityMain extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {
	private static final String LOG_TAG = ActivityMain.class.getSimpleName ();

	private OpenBell openBell = null;

	private List<ItemBell> bellList = new ArrayList<> ();
	private RecyclerView mRecyclerView = null;
	private BellRecyclerAdapter mBellRecyclerAdapter = new BellRecyclerAdapter ();

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		if (BuildConfig.DEBUG)
			Log.d (LOG_TAG, "onCreate()");

		super.onCreate (savedInstanceState);

		openBell = OpenBell.getInstance (getApplicationContext ());

		setContentView (R.layout.activity_main);

		ActionBar ab = getSupportActionBar ();
		if (ab != null) {
			ab.setIcon (R.mipmap.ic_launcher);
			ab.setDisplayShowHomeEnabled (true);
		}

		mRecyclerView = (RecyclerView) findViewById (R.id.recycler_view);
		mRecyclerView.setHasFixedSize (true);
		mRecyclerView.setAdapter (mBellRecyclerAdapter);
		mRecyclerView.setLayoutManager (new LinearLayoutManager (this));
		mRecyclerView.setItemAnimator (new DefaultItemAnimator ());
	}

	@Override
	protected void onResume () {
		if (BuildConfig.DEBUG)
			Log.d (LOG_TAG, "onResume()");

		super.onResume ();

		bellList = new ArrayList<> ();
		mBellRecyclerAdapter.notifyDataSetChanged ();
		String sqlQuery = "SELECT _ROWID_, name, uuid, enabled FROM bells ORDER BY name ASC";
		LIB_DB_SQLite.DBSQLResults sqlResults = openBell._appSQLiteDB.new DBSQLResults ();
		if (!openBell._appSQLiteDB.dbQuery (sqlQuery, sqlResults))
			finish ();

		for (int i = 0, j = sqlResults.numRows; i < j; i++) {
			sqlResults.next ();
			mBellRecyclerAdapter.add (new ItemBell (sqlResults._int ("rowid"), sqlResults._string ("uuid"), sqlResults._string ("name"), sqlResults._int ("enabled")));
		}

		// Get the device token in Log for #Dev#
		if (FirebaseInstanceId.getInstance ().getToken () != null) {
			if (BuildConfig.DEBUG)
				Log.d (LOG_TAG, "Firebase Token " + FirebaseInstanceId.getInstance ().getToken ());
		}
	}

	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		getMenuInflater ().inflate (R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
		switch (item.getItemId ()) {
			case R.id.action_add:
				startActivity (new Intent (this, ActivityEditBell.class).putExtra ("rowid", 0));
				break;

			case R.id.action_about:
				startActivity (new Intent (this, ActivityAbout.class));
				break;

			case R.id.action_settings:
				startActivity (new Intent (this, ActivitySettings.class));

			default:
		}

		return super.onOptionsItemSelected (item);
	}

	@Override
	public void onClick (View v) {
		if (BuildConfig.DEBUG)
			Log.d (LOG_TAG, "onClick(v.getTag=" + v.getTag () + ")");

		Integer tagID = (Integer) v.getTag ();
		Intent mIntent = new Intent (this, ActivityEditBell.class);
		mIntent.putExtra ("rowid", tagID);
		startActivity (mIntent);
	}

	@Override
	public boolean onLongClick (View v) {
		if (BuildConfig.DEBUG)
			Log.d (LOG_TAG, "onLongClick(v.getID=" + String.valueOf (v.getTag ()) + ")");

		int tagID = Integer.valueOf (v.getTag ().toString ());
		for (int i = 0, j = bellList.size (); i < j; ++i) {
			if (bellList.get (i).getID () == tagID) {
				bellDelete (bellList.get (i));
				break;
			}
		}

		return true;
	}

	/**
	 * Method called every time a bell is deleted
	 * It delete the bell from the DB and call the API to unregister the bell
	 */
	public void bellDelete (final ItemBell item) {
		// Create the Dialog
		AlertDialog.Builder builder = new AlertDialog.Builder (this);
		builder.setTitle (getString (R.string.delete_confirm_text));

		// Set up the buttons
		builder.setPositiveButton (getString (R.string.button_ok), new DialogInterface.OnClickListener () {
			@Override
			public void onClick (DialogInterface dialog, int which) {
				LIB_HTTP_Callback httpResponse = new LIB_HTTP_Callback () {
					@Override
					public void httpCallback (int httpResponseCode, byte[] httpResponseData) {
						if (httpResponseCode == 200 || httpResponseCode == 404) {
							// The API call was successful OR the bell is unregistered, now delete from DB
							String sqlQuery = "DELETE FROM bells WHERE _ROWID_ = " + item.getID ();
							openBell._appSQLiteDB.dbQuery (sqlQuery);

							mBellRecyclerAdapter.remove (item);
							runOnUiThread (new Runnable () {
								@Override
								public void run () {
									Toast.makeText (openBell._appContext, R.string.delete_ok, Toast.LENGTH_LONG).show ();
								}
							});
						} else {
							runOnUiThread (new Runnable () {
								@Override
								public void run () {
									Toast.makeText (openBell._appContext, getString (R.string.delete_fail), Toast.LENGTH_LONG).show ();
								}
							});
						}
					}
				};

				if (isNetworkAvailable ()) {
					// HasMap with parameters for the API call
					HashMap<String, String> paramas = new HashMap<String, String> ();
					paramas.put ("command", "UNREGISTER");
					paramas.put ("token", FirebaseInstanceId.getInstance ().getToken ());
					paramas.put ("belluuid", item.getUUID ());

					LIB_HTTP httpRequest = new LIB_HTTP (getString (R.string.api_url), paramas, httpResponse);
					httpRequest.exec ();
				} else {
					Toast.makeText (getApplicationContext (), R.string.conectivity_problem, Toast.LENGTH_SHORT).show ();
				}
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

	/**
	 * Method to check if the device is connected
	 * @return true for connected
	 */
	public boolean isNetworkAvailable () {
		ConnectivityManager cm = (ConnectivityManager) getSystemService (Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo ();
		return (ni != null && ni.isConnected ());
	}

	public class BellRecyclerAdapter extends RecyclerView.Adapter<BellRecyclerAdapter.ViewHolder> {
		public BellRecyclerAdapter () {
		}

		@Override
		public ViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {
			View v = LayoutInflater.from (parent.getContext ()).inflate (R.layout.item_bell, parent, false);

			v.setOnClickListener (ActivityMain.this);
			v.setOnLongClickListener (ActivityMain.this);
			return new ViewHolder (v);
		}

		@Override
		public void onBindViewHolder (ViewHolder holder, int position) {
			ItemBell item = bellList.get (position);
			holder.bellRowID.setText (String.valueOf (item.getID ()));
			holder.bellName.setText (item.getName ());
			holder.bellUUID.setText (item.getUUID ());
			holder.bellEnabled.setChecked (item.getEnabled ());
			holder.itemView.setTag (item.getID ());
		}

		@Override
		public int getItemCount () {
			return bellList.size ();
		}

		public void add (ItemBell item, int position) {
			bellList.add (position, item);
			notifyItemInserted (position);
		}

		public void add (ItemBell item) {
			bellList.add (item);
			notifyItemInserted (bellList.size () - 1);
		}

		public void remove (ItemBell item) {
			int position = bellList.indexOf (item);
			bellList.remove (position);
			notifyItemRemoved (position);
		}

		public class ViewHolder extends RecyclerView.ViewHolder {
			public TextView bellRowID;
			public TextView bellName;
			public TextView bellUUID;
			public Switch bellEnabled;

			public ViewHolder (final View itemView) {
				super (itemView);
				bellRowID = (TextView) itemView.findViewById (R.id.bell_rowid);
				bellName = (TextView) itemView.findViewById (R.id.bell_name);
				bellUUID = (TextView) itemView.findViewById (R.id.bell_uuid);
				bellEnabled = (Switch) itemView.findViewById (R.id.bell_enabled);

				// Update the DB every time a bell is enabled or disabled
				bellEnabled.setOnClickListener (new View.OnClickListener () {
					@Override
					public void onClick (View v) {
						String mQuery = "UPDATE bells SET enabled = " + (bellEnabled.isChecked () ? "1" : "0") + " WHERE _ROWID_ = " + bellRowID.getText ().toString ();
						if (!openBell._appSQLiteDB.dbQuery (mQuery))
							finish ();

						Toast.makeText (getApplicationContext (), (bellEnabled.isChecked () ? R.string.bell_enabled : R.string.bell_disabled),Toast.LENGTH_LONG).show ();
					}
				});
			}
		}
	}
}
