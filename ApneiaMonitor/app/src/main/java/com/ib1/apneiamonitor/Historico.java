package com.ib1.apneiamonitor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.Spinner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Logger;

public class Historico extends Activity implements AdapterView.OnItemSelectedListener {
    public static final String TAG = "HistoricoActivity";
    DatabaseHelper mDatabaseHelper;
    public ArrayList<String> fileSignalData = new ArrayList<String>();

    private Spinner mSpinner;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);
        mSpinner = findViewById(R.id.spinnerHistory);
        mDatabaseHelper = new DatabaseHelper(this);

        Cursor data = mDatabaseHelper.getData();
        ArrayList<String> listData = new ArrayList<String>();

        long line;
        long id;
        while (data.moveToNext()) {
            id = data.getLong(0);
            line = data.getLong(1);
            Log.d(TAG, "line id: " + id);

            SimpleDateFormat sf = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
            Date date = new Date(line);
            listData.add("(" + id + ") " + sf.format(date));
        }
        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, listData);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(this
        );

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String date = (String) parent.getItemAtPosition(position);
        String[] entryList = date.split("\\s+");
        String entryId = entryList[0].replaceAll("[^\\d.]", "");
        Log.d(TAG, "entryId: " + entryId);

        String filename = mDatabaseHelper.getFilenameFromId(entryId);

        File path = this.getFilesDir();
        File file = new File(path, filename);
        File[] files = path.listFiles();
        for (File f : files) {
            Log.d(TAG, "onItemSelected: arquivo: " + f);
        }

        Log.d(TAG, "Abrindo arquivo: " + file.getAbsolutePath());

        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                Log.d(TAG, "line: " + line);
                fileSignalData.add(line);

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "onItemSelected: " + fileSignalData);


    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
