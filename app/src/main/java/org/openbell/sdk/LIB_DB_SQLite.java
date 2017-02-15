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

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements a database object based on SQLite
 *
 */
public class LIB_DB_SQLite {
	private static final String LOG_TAG = LIB_DB_SQLite.class.getSimpleName ();

	public static final int OPEN_READONLY = SQLiteDatabase.OPEN_READONLY;
	public static final int OPEN_READWRITE = SQLiteDatabase.OPEN_READWRITE;
	public static final int CREATE_IF_NECESSARY = SQLiteDatabase.CREATE_IF_NECESSARY;

	private static LIB_DB_SQLite dbInstance = null;
	@SuppressWarnings ("unused")
	private static Context dbContext = null;
	private SQLiteDatabase dbDatabase = null;

	/***
	 * Constructor
	 * @param context Database context for .getInstance ()
	 */
	private LIB_DB_SQLite (Context context) {
		if (BuildConfig.DEBUG)
			Log.d (LOG_TAG, "Constructor called");

		// Store the context for getInstance ()
		dbContext = context;
	}

	/**
	 * Implements a class of database SQL result cursor helper
	 */
	public class DBSQLResults {
		private Cursor resultCursor;
		private Map <String, Integer> resultFields = new HashMap <String, Integer> ();
		private boolean resultReady = false;
		public int numRows = 0;

		/**
		 * Progresses the cursor to the next row in the result set
		 *
		 * @return boolean flag whether the cursor could be progressed
		 */
		public boolean next () {
			if (resultCursor == null)
				return false;

			resultReady = true;
			return resultCursor.moveToNext ();
		}

		/**
		 * Closes the cursor and resets members to a null or empty state,
		 * freeing up memory
		 */
		public void close () {
			if (resultCursor != null && !resultCursor.isClosed ())
				resultCursor.close ();
			resultCursor = null;
			resultFields = null;
			resultReady = false;
			numRows = 0;
		}

		/**
		 * Resets the cursor position to before the first row
		 *
		 * @return boolean flag whether the cursor could be reset
		 */
		public boolean reset () {
			resultReady = false;

			if (resultCursor == null)
				return false;

			return resultCursor.moveToPosition (-1);
		}

		/**
		 * Returns the column position ID in the result set
		 *
		 * @param resultFieldName
		 *            The name of the field in the result set to lookup
		 * @param resultReady
		 *            Flag whether to check that a record is retrievable
		 * @return int of column position or -1 for no match
		 */
		public int index (String resultFieldName, boolean resultReady) {
			Integer resultFieldID = resultFields.get (resultFieldName);
			if (resultFieldID == null || (resultReady && !this.resultReady && !next ()))
				return -1;

			return resultFieldID;
		}

		/**
		 * Returns the field type for the named column
		 *
		 * @param resultFieldName
		 *            The name of the field in the result set to lookup
		 * @return integer constant corresponding to Cursor.FIELD_TYPE_*
		 */
		public int type (String resultFieldName) {
			int resultFieldID = index (resultFieldName, false);
			if (resultFieldID < 0)
				return 0;

			return resultCursor.getType (resultFieldID);
		}

		/**
		 * Returns whether the field for the named column is null
		 *
		 * @param resultFieldName
		 *            The name of the field in the result set to lookup
		 * @return boolean true if null or error, false if not null
		 */
		public boolean isNull (String resultFieldName) {
			int resultFieldID = index (resultFieldName, false);
			if (resultFieldID < 0)
				return true;

			return resultCursor.isNull (resultFieldID);
		}

		/**
		 * Returns the field as a String for the named column
		 *
		 * @param resultFieldName
		 *            The name of the field in the result set to lookup
		 * @return String field data or empty string for no match
		 */
		public String _string (String resultFieldName) {
			int resultFieldID = index (resultFieldName, true);
			if (resultFieldID < 0)
				return "";

			return resultCursor.getString (resultFieldID);
		}

		/**
		 * Returns the field as an int for the named column
		 *
		 * @param resultFieldName
		 *            The name of the field in the result set to lookup
		 * @return int field data or -1 for no match
		 */
		public int _int (String resultFieldName) {
			int resultFieldID = index (resultFieldName, true);
			if (resultFieldID < 0)
				return -1;

			return resultCursor.getInt (resultFieldID);
		}

		/**
		 * Returns the field as a byte array for the named column
		 *
		 * @param resultFieldName
		 *            The name of the field in the result set to lookup
		 * @return Byte array field data or null byte for no match
		 */
		public byte[] _blob (String resultFieldName) {
			int resultFieldID = index (resultFieldName, true);
			if (resultFieldID < 0)
				return new byte[0];

			return resultCursor.getBlob (resultFieldID);
		}
	}

	public static synchronized LIB_DB_SQLite getInstance (Context mContext) {
		if (BuildConfig.DEBUG)
			Log.d (LOG_TAG, "getInstance()");

		if (dbInstance == null)
			dbInstance = new LIB_DB_SQLite (mContext);

		return dbInstance;
	}

	public synchronized boolean dbOpen (String dbFilename, int dbOpenFlags) {
		if (dbDatabase != null)
			return true;

		try {
			dbDatabase = SQLiteDatabase.openDatabase (dbFilename, null, dbOpenFlags);
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	public synchronized boolean dbClose () {
		if (dbDatabase == null)
			return false;

		dbDatabase.close ();
		dbDatabase = null;

		return true;
	}

	public boolean dbQuery (String dbSQLQuery) {
		LIB_DB_SQLite.DBSQLResults dbSQLResults = new DBSQLResults ();
		return dbQuery (dbSQLQuery, dbSQLResults);
	}

	public /* FIXME: synchronized? */ boolean dbQuery (String dbSQLQuery, DBSQLResults dbSQLResults) {
		if (BuildConfig.DEBUG)
			Log.d (LOG_TAG + " SQL", dbSQLQuery);

		if (dbSQLQuery.substring (0, 6).equals ("SELECT")) {
			if (BuildConfig.DEBUG)
				Log.d (LOG_TAG, "dbQuery() called for a -R-- statement");

			try {
				dbSQLResults.resultCursor = dbDatabase.rawQuery (dbSQLQuery, null);
				dbSQLResults.numRows = dbSQLResults.resultCursor.getCount ();

				for (int i = 0, j = dbSQLResults.resultCursor.getColumnCount (); i < j; ++i) {
					if (BuildConfig.DEBUG)
						Log.d (LOG_TAG, "Returned field " + String.valueOf (i) + "=" + dbSQLResults.resultCursor.getColumnName (i));

					dbSQLResults.resultFields.put (dbSQLResults.resultCursor.getColumnName (i), i);
				}

				if (BuildConfig.DEBUG)
					Log.d (LOG_TAG, "dbSQLResults has " + dbSQLResults.resultCursor.getColumnCount () + " fields and " + dbSQLResults.numRows + " rows");
			} catch (Exception e) {
				if (BuildConfig.DEBUG)
					Log.d (LOG_TAG, "dbQuery() error: " + e.getMessage ());
				dbSQLResults.resultCursor = null;
				return false;
			}
		} else {
			if (BuildConfig.DEBUG)
				Log.d (LOG_TAG, "dbQuery() called for C-UD statement");

			try {
				dbDatabase.execSQL (dbSQLQuery);
			} catch (SQLException e) {
				if (BuildConfig.DEBUG)
					Log.d (LOG_TAG, "dbQuery() error: " + e.getMessage ());
				dbSQLResults.resultCursor = null;
				return false;
			}
		}

		return true;
	}

	public static String dbEscape (String dbString) {
		return dbString.replaceAll ("\"", "\"\"");
	}
}
