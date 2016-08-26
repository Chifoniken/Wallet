package com.vovasoft.wallet.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by arsen on 02.03.2016.
 */
public class SpentDatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "Spent.db";

    private static final String SQL_CREATE_SPENT =
            "CREATE TABLE IF NOT EXISTS SPENT (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "Date DATE, " +
                    "Sum TEXT, " +
                    "Category INTEGER, " +
                    "Desc TEXT, " +
                    "Cash INTEGER, " +
                    "Card INTEGER" +
                    ");";

    private static final String SQL_DELETE_SPENT = "DROP TABLE IF EXISTS SPENT";


    public SpentDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_SPENT);
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DELETE_SPENT);
        onCreate(sqLiteDatabase);
    }
}
