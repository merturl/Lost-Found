package com.example.jongho.newproject_1;

/**
 * Created by merturl on 2017-12-10.
 */
import android.provider.BaseColumns;

public class FeedReaderContract {
    public FeedReaderContract() {};

    public static final String SQL_CREATE_ENTRY = "CREATE TABLE " + FeedEntry.TABLE_NAME + " ( " + FeedEntry._ID + " INTEGER PRIMARY KEY," + FeedEntry.COLUMN_NAME_ADDRESS + " text, " + FeedEntry.COLUMN_NAME_LAT + " text, " + FeedEntry.COLUMN_NAME_LON + " text " + " ) ";
    public static final String SQL_DELETE_ENTRY = "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;

    public static abstract class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "my_Geo";
        public static final String COLUMN_NAME_ADDRESS = "addr";
        public static final String COLUMN_NAME_LAT = "lat";
        public static final String COLUMN_NAME_LON = "lon";
    }
}

