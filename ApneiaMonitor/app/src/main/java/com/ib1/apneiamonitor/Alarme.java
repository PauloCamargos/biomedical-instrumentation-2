package com.ib1.apneiamonitor;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class Alarme extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener {

    public LineGraphSeries<DataPoint> series;
    int points_amount = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarme);

        Spinner spinner_tempo_limite = findViewById(R.id.spinner_tempo_limite);
        Spinner spinner_limite_expansao = findViewById(R.id.spinner_limite_expansao);

        if (spinner_tempo_limite != null && spinner_limite_expansao != null) {
            spinner_tempo_limite.setOnItemSelectedListener(this);
            spinner_limite_expansao.setOnItemSelectedListener(this);
        }

        ArrayAdapter<CharSequence> adapter_tempo_limite = ArrayAdapter.createFromResource(this,
                R.array.tempos_alarme, android.R.layout.simple_spinner_item);


        ArrayAdapter<CharSequence> adapter_limite_expansao = ArrayAdapter.createFromResource(this,
                R.array.limites_expansao, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears.
        adapter_tempo_limite.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);
        adapter_limite_expansao.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner.
        if (spinner_tempo_limite != null && spinner_limite_expansao != null) {
            spinner_tempo_limite.setAdapter(adapter_tempo_limite);
            spinner_limite_expansao.setAdapter(adapter_limite_expansao);
        }
        // ... End of onCreate code ...
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String spinnerLabel = parent.getItemAtPosition(position).toString();
        displayToast(spinnerLabel);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void displayToast(String message) {
        Toast.makeText(getApplicationContext(), message,
                Toast.LENGTH_SHORT).show();
    }

    public void plotData(View view) {
        double x, y;
        x = 0;
        points_amount+=50;
        GraphView graph = (GraphView) findViewById(R.id.graph);
        series = new LineGraphSeries<DataPoint>();
        for (int i = 0; i < points_amount; i++) {
            x += 0.1;
            y = Math.sin(x);
            series.appendData(new DataPoint(x, y), true, points_amount);
        }
        graph.addSeries(series);
    }
}
