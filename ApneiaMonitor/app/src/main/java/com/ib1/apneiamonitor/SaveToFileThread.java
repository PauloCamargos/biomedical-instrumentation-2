package com.ib1.apneiamonitor;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class SaveToFileThread extends Thread {
    public static final String TAG = "SaveToFileThread";
    public Context context;
    public String filename;
    public boolean running = false;

    public SaveToFileThread(Context context, String filename) {
        this.context = context;
        this.filename = filename;
    }

    public SaveToFileThread(Context context) {
        this.context = context;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void run() {
        this.running = true;

        File path = context.getFilesDir();
        File file = new File(path, filename);
        try {
//            FileWriter writer = new FileWriter(file);
            PrintWriter writer = new PrintWriter(file);
            while (running) {
                if (!Alarme.buffer.isEmpty()) {
//                FileOutputStream stream = new FileOutputStream(file);

                    while (Alarme.buffer.size() >= 1) {
                        String signalData = Alarme.buffer.remove();
                        Log.d(TAG, "signalData: " + signalData);
                        writer.println(signalData);
                    }
                }
            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        try {
            running = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        running = false;
    }
}
