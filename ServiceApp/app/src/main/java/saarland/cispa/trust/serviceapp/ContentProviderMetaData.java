package saarland.cispa.trust.serviceapp;

import android.net.Uri;
import android.provider.BaseColumns;

public class ContentProviderMetaData {
    public static final String AUTHORITY = "saarland.cispa.trust.serviceapp.contentprovider";
    public static final String DATABASE_NAME = "cp.db";
    public static final int DATABASE_VERSION = 1;
    private ContentProviderMetaData() {}


    // describe the only existing table
    public static final class TableMetaData implements BaseColumns {
        private TableMetaData() {}
        public static final String TABLE_NAME = "items";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.serviceapp.items";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.serviceapp.items";

        // Columns
        public static final String TITLE = "title";
        public static final String DESCRIPTION = "description";
        public static final String PRICE = "price";
        public static final String IMAGE_NAME = "image_path";
        public static final String LOC_LONGITUDE = "longitude";
        public static final String LOC_LATITUDE = "latitude";
    }
}