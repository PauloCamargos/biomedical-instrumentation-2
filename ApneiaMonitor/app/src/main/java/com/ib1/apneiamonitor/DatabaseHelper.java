package com.ib1.apneiamonitor;

import android.content.ContentValues;
import android.content.Context;
import android.content.QuickViewConstants;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.net.IDN;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String TAG = "DatabaseHelper";
    public static final String TABLE_NAME = "records";
    public static final String COL1 = "id";
    public static final String COL2 = "date";
    public static final String COL3 = "filename";

    public DatabaseHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + "(" +
                COL1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL2 + " INTEGER ," +
                COL3 + " VARCHAR(60));";
        db.execSQL(createTable);
        Log.d(TAG, "onCreate: TABELA CRIADA");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addData(long recordDate, String filename) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, recordDate);
        contentValues.put(COL3, filename);

        Log.d(TAG, "addData: Adding " + recordDate + ":  " + filename + " at " + TABLE_NAME);

        long result = db.insert(TABLE_NAME, null, contentValues);

        return result != -1;
    }

    public Cursor getData() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    public String getFilenameFromId(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE id=" + id;
        Log.d(TAG, "query: " + query);

        Cursor data = db.rawQuery(query, null);
        String filename = "";
        while (data.moveToNext()) {
            filename = data.getString(2);
            Log.d(TAG, "retornando: " + filename);

        }
        return filename;
    }

    public String getLastFilename() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME +
                " ORDER BY  id DESC LIMIT 1";
        Cursor data = db.rawQuery(query, null);
        String filename = "";
        while (data.moveToNext()) {
            filename = data.getString(2);
        }
        return filename;
    }

}
