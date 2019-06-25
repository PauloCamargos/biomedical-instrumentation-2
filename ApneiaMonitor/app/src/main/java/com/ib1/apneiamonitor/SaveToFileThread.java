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
    public boolean hasStarted=false;

    public SaveToFileThread(Context context) {
        this.context = context;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public boolean isHasStarted() {
        return hasStarted;
    }

    public void setHasStarted(boolean hasStarted) {
        this.hasStarted = hasStarted;
    }

    @Override
    public void run() {
        Log.d(TAG, "run-isRunning?" + running);

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
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean status) {
        this.running = status;
    }
}
