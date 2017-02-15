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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
	private static final String LOG_TAG = MyFirebaseMessagingService.class.getSimpleName ();

	private OpenBell openBell = null;

	private String bellUUID;
	private int messageID;
	private String messageBody;

	private MediaPlayer mediaPlayer;

	public void onMessageReceived (RemoteMessage remoteMessage) {
		if (BuildConfig.DEBUG)
			Log.d (LOG_TAG, "onMessageReceived()");

		openBell = OpenBell.getInstance (getApplicationContext ());

		if (BuildConfig.DEBUG) {
			Log.d (LOG_TAG, "getData() = " + remoteMessage.getData ().toString ());
			Log.d (LOG_TAG, "getBody() = " + remoteMessage.getNotification ().getBody ());
			Log.d (LOG_TAG, "getMessageId() = " + remoteMessage.getMessageId ());
		}

		bellUUID = remoteMessage.getData ().get ("belluuid");
		messageID = 0; // FIXME Firebase weird bugs :( (remoteMessage.getMessageId () == null ? 0 : Integer.parseInt (remoteMessage.getMessageId ()));
		messageBody = remoteMessage.getNotification ().getBody ();

		if (bellUUID == null) {
			sendNotification (getString (R.string.no_code_title), getString (R.string.no_code_error), 0);
			return;
		}

		String sqlQuery = "SELECT name, enabled FROM bells WHERE uuid = \"" + LIB_DB_SQLite.dbEscape (bellUUID) + "\"";
		LIB_DB_SQLite.DBSQLResults sqlResults = openBell._appSQLiteDB.new DBSQLResults ();
		if (!openBell._appSQLiteDB.dbQuery (sqlQuery, sqlResults)) {
			onDeletedMessages (); // FIXME What is this?
			return;
		}

		String bellName = "";
		int bellEnabled = 0;
		if (sqlResults.numRows > 0) {
			bellName = sqlResults._string ("name");
			bellEnabled = sqlResults._int ("enabled");
		}

		if (BuildConfig.DEBUG)
			Log.d (LOG_TAG, "Looked up bell code for " + bellName + ", enabled = " + (bellEnabled == 1 ? "yes" : "no"));

		if (bellEnabled == 1) {
			if (messageID == 0)
				messageID = (int) ((System.currentTimeMillis () % (1000 * 60 * 60 * 24)) * 0.1);
			sendNotification (bellName, messageBody, messageID);
		}
	}

	private void sendNotification (String notificationTitle, String notificationContent, int notificationMessageID) {
		if (BuildConfig.DEBUG)
			Log.d (LOG_TAG, "sendNotification()");

		Intent mIntent = new Intent (this, ActivityMain.class);
		mIntent.addFlags (Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity (this, 0 /* Request code */, mIntent, PendingIntent.FLAG_ONE_SHOT);

		int mediaResource = PreferenceManager.getDefaultSharedPreferences (getApplicationContext ()).getString ("preference_notification", "bell_default").equals ("bell_default") ? R.raw.bell_default : R.raw.bell_trump;
		mediaPlayer = MediaPlayer.create (this, mediaResource);
		mediaPlayer.start ();

		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder (this);
		notificationBuilder.setSmallIcon (R.mipmap.ic_launcher);
		notificationBuilder.setContentTitle (notificationTitle);
		notificationBuilder.setContentText (notificationContent);
		notificationBuilder.setSubText (getString (R.string.app_name));
		notificationBuilder.setStyle (new NotificationCompat.BigTextStyle ().bigText (notificationContent));
		notificationBuilder.setVibrate (new long[] {0, 1000, 250, 1000, 250, 1000, 250, 150, 50, 150, 50, 150, 50, 150, 50, 150, 50, 150, 50, 150});
		notificationBuilder.setAutoCancel (false);
		notificationBuilder.setContentIntent (pendingIntent);

		NotificationManager notificationManager = (NotificationManager) getSystemService (Context.NOTIFICATION_SERVICE);
		notificationManager.notify (notificationMessageID, notificationBuilder.build ());
	}

	@Override
	public void onDeletedMessages () {
		if (BuildConfig.DEBUG)
			Log.d (LOG_TAG, "onDeletedMessages()");

		super.onDeletedMessages ();
	}

	@Override
	public void onMessageSent (String s) {
		if (BuildConfig.DEBUG)
			Log.d (LOG_TAG, "onMessageSent(s=" + s + ")");
	}
}
