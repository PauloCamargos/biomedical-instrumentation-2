package com.ib1.apneiamonitor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Historico extends Activity implements AdapterView.OnItemSelectedListener {
    public static final String TAG = "HistoricoActivity";
    DatabaseHelper mDatabaseHelper;
    public ArrayList<String> fileSignalData;
    private Spinner mSpinner;
    public TextView valueAverage;
    public TextView valueMin;
    public TextView valueMax;
    public TextView valueDiparos;
    public TextView tempoColeta;
    // GRAFICO
    public GraphView graph = null;
    public LineGraphSeries<DataPoint> mSeries1 = null;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);
        mSpinner = findViewById(R.id.spinnerHistory);
        mDatabaseHelper = new DatabaseHelper(this);

        valueAverage = findViewById(R.id.valueAverage);
        valueMax = findViewById(R.id.valueMax);
        valueMin = findViewById(R.id.valueMin);
        valueDiparos = findViewById(R.id.valueDisparos);
        tempoColeta = findViewById(R.id.tempoColeta);

        Cursor data = mDatabaseHelper.getData();
        ArrayList<String> listData = new ArrayList<String>();

        long line;
        long id;
        while (data.moveToNext()) {
            id = data.getLong(0);
            line = data.getLong(1);

//            Log.d(TAG, "line id: " + id);

            SimpleDateFormat sf = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
            Date date = new Date(line);
            listData.add("(" + id + ") " + sf.format(date));
        }
        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, listData);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(this
        );

        // GRAFICO
        graph = (GraphView) findViewById(R.id.graphHistory);


    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        fileSignalData = new ArrayList<String>();
        String date = (String) parent.getItemAtPosition(position);
        String[] entryList = date.split("\\s+");
        String entryId = entryList[0].replaceAll("[^\\d.]", "");
//        Log.d(TAG, "entryId: " + entryId);

        String filename = mDatabaseHelper.getFilenameFromId(entryId);

        File path = this.getFilesDir();
        File file = new File(path, filename);
        File[] files = path.listFiles();
//        for (File f : files) {
//            Log.d(TAG, "onItemSelected: arquivo: " + f);
//        }

//        Log.d(TAG, "Abrindo arquivo: " + file.getAbsolutePath());

        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
//                Log.d(TAG, "line: " + line);
                fileSignalData.add(line);

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // recuperando tempoLimite e expansaoLimite

        Cursor data = mDatabaseHelper.getTransmissionParamFromId(entryId);
        long tempoLimite = 0;
        long expansaoLimite = 0;
        while (data.moveToNext()) {
            tempoLimite = data.getLong(3);
            expansaoLimite = data.getLong(4);
        }

//        Log.d(TAG, "onItemSelected: " + fileSignalData);
//        Log.d(TAG, "tempoLimite: " + tempoLimite);
//        Log.d(TAG, "expansaoLimite: " + expansaoLimite);

        showGraph(fileSignalData);
        calculateAverage(fileSignalData);
//        calcuteTriggerAmmount(tempoLimite, expansaoLimite, fileSignalData);
        calculateMax(fileSignalData);
        calculateMin(fileSignalData);
        addFireAmount(entryId);
        calculateTempoColeta(fileSignalData);
        fileSignalData = null;

    }

    private void calculateTempoColeta(ArrayList<String> fileSignalData) {
        int tempo = (int) (fileSignalData.size() * 0.1);

        tempoColeta.setText(Integer.toString(tempo) + " s");
    }

    private void addFireAmount(String entryId) {
        Cursor data = mDatabaseHelper.getTransmissionParamFromId(entryId);
        long disparos = 0;
        while (data.moveToNext()) {
            disparos = data.getLong(5);
            Log.d(TAG, "addFireAmount: Disparos: " + disparos);

        }
        valueDiparos.setText(Long.toString(disparos));

    }

    private void calcuteTriggerAmmount(long tempoLimite, long expansaoLimite, ArrayList<String> fileSignalData) {
        ArrayList<Integer> time = new ArrayList<>();
        for (int i = 0; i < fileSignalData.size(); i++) {
            time.add(i * 100);
        }
        int stdDev;
        ArrayList<Integer> buffer = new ArrayList<>(); // vetor com 10 elementos

        for (int i = 0; i < time.size(); i++) {
            buffer.add(Integer.parseInt(fileSignalData.get(i)));
        }


    }

    private void calculateMax(ArrayList<String> fileSignalData) {
        if (!fileSignalData.isEmpty()) {
            Integer max = Integer.parseInt(fileSignalData.get(0));
            for (String point : fileSignalData) {
                Integer current = Integer.parseInt(point);
                if (current > max) {
                    max = current;
                }
            }
            this.valueMax.setText(Integer.toString(max));
        }

    }

    private void calculateMin(ArrayList<String> fileSignalData) {
        if (!fileSignalData.isEmpty()) {
            Integer min = Integer.parseInt(fileSignalData.get(0));
            for (String point : fileSignalData) {
                Integer current = Integer.parseInt(point);
                if (current < min) {
                    min = current;
                }
            }

            this.valueMin.setText(Integer.toString(min));
            Log.d(TAG, "mín: " + Integer.toString(min));
        }


    }

    private void calculateAverage(ArrayList<String> fileSignalData) {
        Integer sum = 0;
        if (!fileSignalData.isEmpty()) {
            for (String point : fileSignalData) {
                sum += Integer.parseInt(point);
            }
        }
        this.valueAverage.setText(Integer.toString(sum / fileSignalData.size()));
    }

    private void showGraph(ArrayList<String> fileSignalData) {
        graph.removeAllSeries();
        mSeries1 = null;
        DataPoint[] points = null;
        try {
            int lastX = 0;
            points = new DataPoint[fileSignalData.size()];
            for (String dataPoint : fileSignalData) {
                points[lastX] = new DataPoint(lastX, Double.parseDouble(dataPoint));
                lastX++;
            }
            mSeries1 = new LineGraphSeries<>(points);
            Log.d(TAG, "points: " + points.length);

            // set manual X bounds
            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setMinY(0);
            graph.getViewport().setMaxY(260);

            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMinX(0);
            graph.getViewport().setMaxX(lastX);

            // enable scaling and scrolling
            graph.getViewport().setScalable(true);
            graph.getViewport().setScalableY(true);

            graph.getViewport().setScrollable(true); // enables horizontal scrolling
            graph.getViewport().setScrollableY(true); // enables vertical scrolling
            graph.getViewport().setScalable(true); // enables horizontal zooming and scrolling
            graph.getViewport().setScalableY(true); // enables vertical zooming and scrolling

            GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
            gridLabel.setHorizontalAxisTitle("T [s]");

            graph.addSeries(mSeries1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
