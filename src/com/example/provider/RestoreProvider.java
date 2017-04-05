package com.example.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class RestoreProvider extends ContentProvider {

	public static final String AUTHORITY = "com.example.mydatadriver.RestoreProvider";
	public static final Uri URI = Uri.parse("content://" + AUTHORITY);
	public static final String COLUMN_PACKAGE = "package";
	public static final String COLUMN_ID = "task_id";
	public static final String COLUMN_KEY = "task_key";
	public static final String COLUMN_VALUE = "task_value";
	private static final String TABLE_NAME = "task";
	private TestDatabase mDb;
	
	@Override
	public boolean onCreate() {
		mDb = new TestDatabase(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase db = mDb.getReadableDatabase();
		qb.setTables(TABLE_NAME);
		Cursor query = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		query.setNotificationUri(getContext().getContentResolver(), uri);
		return query;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase sqlDb = mDb.getWritableDatabase();
		Cursor cursor = sqlDb.query(TABLE_NAME, null, COLUMN_PACKAGE + " = ? and " + COLUMN_ID + " = ? and " + COLUMN_KEY + " = ? ", new String[]{(String) values.get(COLUMN_PACKAGE), (String) values.get(COLUMN_ID), (String) values.get(COLUMN_KEY)}, null, null, null);
		if (cursor.moveToNext()) {
			update(URI, values, COLUMN_PACKAGE + " = ? and " + COLUMN_ID + " = ? and " + COLUMN_KEY + " = ? ", new String[]{(String) values.get(COLUMN_PACKAGE), (String) values.get(COLUMN_ID), (String) values.get(COLUMN_KEY)});
			return null;
		}
		long rowId = sqlDb.insert(TABLE_NAME, "", values);
		if (rowId > 0) {
			Uri rowUri = ContentUris.appendId(URI.buildUpon(), rowId).build();
			getContext().getContentResolver().notifyChange(rowUri, null);
			return rowUri;
		}
		return null;

	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mDb.getWritableDatabase();
		int count = db.delete(TABLE_NAME, selection, selectionArgs);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase database = mDb.getWritableDatabase();
		int count = database.update(TABLE_NAME, values, selection, selectionArgs);
		return count;
	}

	private static class TestDatabase extends SQLiteOpenHelper{

		private static int VERSION = 1;
		private static final String DB_NAME = "restore_provider.db";
		
		TestDatabase(Context context){
			super(context, DB_NAME, null, VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("create table " + TABLE_NAME + "(" + COLUMN_PACKAGE + " text not null," + COLUMN_ID + 
					" text not null, " + COLUMN_KEY + " text not null," + COLUMN_VALUE + 
					" text not null, primary key(" + COLUMN_PACKAGE + "," + COLUMN_KEY + "," + COLUMN_ID + "));");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("drop table if exists " + TABLE_NAME);
			onCreate(db);
		}
		
	}
}
