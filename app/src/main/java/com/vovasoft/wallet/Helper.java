package com.vovasoft.wallet;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.DisplayMetrics;
import android.util.Pair;

import com.vovasoft.wallet.Database.CategoryDatabaseHelper;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by arsen on 28.03.2016.
 */
public class Helper {
    private static Helper ourInstance = new Helper();

    public static Helper getInstance() {
        return ourInstance;
    }

    private Helper() {
    }

    private String locale;

    public void setLocale(String str) {
        locale = str;
    }

    public String getLocale() {
        return locale;
    }


    private boolean languageChanged;

    public void setLanguageChanged(boolean b) {
        languageChanged = b;
    }

    public boolean isLanguageChanged() {
        return languageChanged;
    }

    public void setLanguage(Context context) {
        Locale locale = new Locale(getLocale());
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = locale;
        res.updateConfiguration(conf, dm);
    }


    public String getSpacedString(String originalString) {

        int interval = 3;
        char separator = ' ';

        StringBuilder builder = new StringBuilder(originalString);

        for(int i = 0; i < (originalString.length() - 1) / interval; i++) {
            builder.insert(originalString.length() - interval * (i + 1), separator);
        }

        return builder.toString();
    }


    public void fillCategoriesFirstTime(Context context) {

        CategoryDatabaseHelper databaseHelper = new CategoryDatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        int[] categories = new int[] {
                R.string.other,
                R.string.food,
                R.string.products,
                R.string.transport,
                R.string.communication,
                R.string.house,
                R.string.entertainment
        };

        for (int category : categories) {
            ContentValues values = new ContentValues();
            values.put("_id", category);
            database.insertWithOnConflict("Category", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }

        databaseHelper.close();
    }


    public ArrayList<Pair<Integer, String>> loadCategoryList(Context context) {

        ArrayList<Pair<Integer, String>> categories = new ArrayList<>();

        CategoryDatabaseHelper databaseHelper = new CategoryDatabaseHelper(context);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        Cursor cursor = database.rawQuery("SELECT * FROM CATEGORY", null);

        if (cursor.moveToFirst()) {
            do {
                if (cursor.getColumnIndex("_id") >= 0) {

                    int id = cursor.getInt(cursor.getColumnIndex("_id"));
                    String name = context.getText(id).toString();

                    Pair<Integer, String> pair = new Pair<>(id, name);
                    categories.add(pair);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        database.close();

        return categories;
    }

}
