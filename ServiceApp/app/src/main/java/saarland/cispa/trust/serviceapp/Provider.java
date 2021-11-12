package saarland.cispa.trust.serviceapp;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

public class Provider extends ContentProvider {

    private static final String TAG = "ServiceApp.Provider";
    private SQLiteOpenHelper mOpenHelper;
    // Mechanism to identify the incoming URI patterns
    private static final UriMatcher sUriMatcher;
    private static final int DATA = 1;
    private static final int DATA_ID = 2;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(ContentProviderMetaData.AUTHORITY,
                ContentProviderMetaData.TableMetaData.TABLE_NAME, DATA);
        sUriMatcher.addURI(ContentProviderMetaData.AUTHORITY,
                ContentProviderMetaData.TableMetaData.TABLE_NAME + "/#", DATA_ID);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, ContentProviderMetaData.DATABASE_NAME, null, ContentProviderMetaData.DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL("CREATE TABLE " + ContentProviderMetaData.TableMetaData.TABLE_NAME + "(" +
                    ContentProviderMetaData.TableMetaData._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    ContentProviderMetaData.TableMetaData.TITLE + " TEXT," +
                    ContentProviderMetaData.TableMetaData.DESCRIPTION + " TEXT," +
                    ContentProviderMetaData.TableMetaData.IMAGE_NAME + " TEXT," +
                    ContentProviderMetaData.TableMetaData.PRICE + " INTEGER," +
                    ContentProviderMetaData.TableMetaData.LOC_LATITUDE + " DOUBLE," +
                    ContentProviderMetaData.TableMetaData.LOC_LONGITUDE + " DOUBLE" +
                    ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            Log.d(TAG, "inner onupgrade called");
            Log.w(TAG, "upgrading database from " + oldVersion + " to " + newVersion);
            // Here: Just drop and re-create
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ContentProviderMetaData.TableMetaData.TABLE_NAME);
            onCreate(sqLiteDatabase);
        }
    }

    public Provider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case DATA:
                count = db.delete(ContentProviderMetaData.TableMetaData.TABLE_NAME, selection, selectionArgs);
                break;
            case DATA_ID:
                String rowid = uri.getLastPathSegment();
                count = db.delete(ContentProviderMetaData.TableMetaData.TABLE_NAME, ContentProviderMetaData.TableMetaData._ID + "=" + rowid
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case DATA:
                return ContentProviderMetaData.TableMetaData.CONTENT_TYPE;
            case DATA_ID:
                return ContentProviderMetaData.TableMetaData.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        if (sUriMatcher.match(uri) != DATA) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        ContentValues cv;
        if (values != null) {
            cv = new ContentValues(values);
        } else {
            cv = new ContentValues();
        }
        String template = "Failed to insert row because %s is needed";
        if (!cv.containsKey(ContentProviderMetaData.TableMetaData.TITLE)) {
            throw new SQLException(String.format(template, ContentProviderMetaData.TableMetaData.TITLE));
        }
        if (!cv.containsKey(ContentProviderMetaData.TableMetaData.DESCRIPTION)) {
            throw new SQLException(String.format(template, ContentProviderMetaData.TableMetaData.DESCRIPTION));
        }
        if (!cv.containsKey(ContentProviderMetaData.TableMetaData.PRICE)) {
            throw new SQLException(String.format(template, ContentProviderMetaData.TableMetaData.PRICE));
        }
        if (!cv.containsKey(ContentProviderMetaData.TableMetaData.IMAGE_NAME)) {
            throw new SQLException(String.format(template, ContentProviderMetaData.TableMetaData.IMAGE_NAME));
        }
        if (!cv.containsKey(ContentProviderMetaData.TableMetaData.LOC_LATITUDE)) {
            throw new SQLException(String.format(template, ContentProviderMetaData.TableMetaData.LOC_LATITUDE));
        }
        if (!cv.containsKey(ContentProviderMetaData.TableMetaData.LOC_LONGITUDE)) {
            throw new SQLException(String.format(template, ContentProviderMetaData.TableMetaData.LOC_LONGITUDE));
        }
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowid = db.insert(ContentProviderMetaData.TableMetaData.TABLE_NAME, null, cv);
        if (rowid > 0) {
            Uri insertedDataURI = ContentUris.withAppendedId(
                    ContentProviderMetaData.TableMetaData.CONTENT_URI, rowid);

            getContext().getContentResolver().notifyChange(insertedDataURI, null);
            return insertedDataURI;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (sUriMatcher.match(uri)) {
            case DATA:
                qb.setTables(ContentProviderMetaData.TableMetaData.TABLE_NAME);
                break;
            case DATA_ID:
                qb.setTables(ContentProviderMetaData.TableMetaData.TABLE_NAME);
                qb.appendWhere(ContentProviderMetaData.TableMetaData._ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = ContentProviderMetaData.TableMetaData._ID + " DESC";
        } else {
            orderBy = sortOrder;
        }
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
            case DATA:
                count = db.update(ContentProviderMetaData.TableMetaData.TABLE_NAME,
                        values, selection, selectionArgs);
                break;
            case DATA_ID:
                String rowId = uri.getPathSegments().get(1);
                count = db.update(ContentProviderMetaData.TableMetaData.TABLE_NAME,
                        values, ContentProviderMetaData.TableMetaData._ID + "=" + rowId
                                + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
