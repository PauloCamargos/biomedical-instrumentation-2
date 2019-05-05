package com.ib1.apneiamonitor;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener  {
    // Tag for the intent extra.
    public static final String EXTRA_MESSAGE =
            "com.example.android.droidcafe.extra.MESSAGE";
    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_inicio:
                    mTextMessage.setText(R.string.title_inicio);
                    return true;
                case R.id.navigation_alarme:
//                    mTextMessage.setText(R.string.title_alarme);
//                    return true;
                    Intent intentAlarme = new Intent(MainActivity.this, Alarme.class);
                    intentAlarme.putExtra(EXTRA_MESSAGE, R.string.title_alarme);
                    startActivity(intentAlarme);
                    return true;
                case R.id.navigation_calibracao:
                    Intent intentCalibracao = new Intent(MainActivity.this, Calibracao.class);
                    intentCalibracao.putExtra(EXTRA_MESSAGE, R.string.title_calibracao);
                    startActivity(intentCalibracao);
                    return true;
                case R.id.navigation_historico:
                    Intent intentHistorico = new Intent(MainActivity.this, Historico.class);
                    intentHistorico.putExtra(EXTRA_MESSAGE, R.string.title_alarme);
                    startActivity(intentHistorico);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        mTextMessage = findViewById(R.id.message);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Spinner spinner = findViewById(R.id.spinner_conexao);
        if (spinner != null) {
            spinner.setOnItemSelectedListener(this);
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.dispositivos_disponiveis, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears.
        adapter.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner.
        if (spinner != null) {
            spinner.setAdapter(adapter);
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
}
