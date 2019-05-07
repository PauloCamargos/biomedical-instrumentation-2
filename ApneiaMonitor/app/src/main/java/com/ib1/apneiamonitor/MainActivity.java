package com.ib1.apneiamonitor;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Random;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener {
    // Tag for the intent extra.
    public static final String EXTRA_MESSAGE =
            "com.ib1.apneiamonitor.extra.MESSAGE";
    static TextView statusConexaoBth;
    static TextView valorLidoBth;
    static String blth_address = "00:18:E4:40:00:06";
    ConnectionThread connect;

    //    private final Handler mHandler = new Handler();
//    private Runnable mTimer1;
    static LineGraphSeries<DataPoint> mSeries1;
    static int lastX = 0;

//    static int points_amount = 100;
//    DataPoint[] values = new DataPoint[points_amount];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        statusConexaoBth = findViewById(R.id.status_dispositivo_conectado);
        valorLidoBth = findViewById(R.id.valor_lido_bth);

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

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            statusConexaoBth.setText("Que pena! Hardware Bluetooth não está funcionando :(");
        } else {
            statusConexaoBth.setText("Ótimo! Hardware Bluetooth está funcionando :)");
        }

        /* A chamada do seguinte método liga o Bluetooth no dispositivo Android
            sem pedido de autorização do usuário. É altamente não recomendado no
            Android Developers, mas, para simplificar este app, que é um demo,
            faremos isso. Na prática, em um app que vai ser usado por outras
            pessoas, não faça isso.
         */
        btAdapter.enable();

        /* Definição da thread de conexão como cliente.
            Aqui, você deve incluir o endereço MAC do seu módulo Bluetooth.
            O app iniciará e vai automaticamente buscar por esse endereço.
            Caso não encontre, dirá que houve um erro de conexão.
         */

        connect = new ConnectionThread(blth_address);
        connect.start();

        /* Um descanso rápido, para evitar bugs esquisitos.
         */
        try {
            Thread.sleep(100);
        } catch (Exception E) {
            E.printStackTrace();
        }
//        for (int i = 0; i < points_amount; i++) {
//            values[i] = new DataPoint(i, 1);
//        }
        GraphView graph = (GraphView) findViewById(R.id.graph);
        mSeries1 = new LineGraphSeries<DataPoint>();
        graph.addSeries(mSeries1);

        Viewport viewport = graph.getViewport();
        viewport.setYAxisBoundsManual(true);
//        viewport.setXAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(255);
        viewport.setScrollable(true);


    }

    static void addEntry(String dataString) {
        mSeries1.appendData(new DataPoint(lastX++, Double.parseDouble((String) dataString)), false, 50);
    }


    //    @Override
//    public void onResume() {
//        super.onResume();
//        mTimer1 = new Runnable() {
//            @Override
//            public void run() {
//                mSeries1.resetData(generateData());
//                mHandler.postDelayed(this, 200);
//            }
//        };
//        mHandler.postDelayed(mTimer1, 200);
//    }
//
//    @Override
//    public void onPause() {
//        mHandler.removeCallbacks(mTimer1);
//        super.onPause();
//    }
//
//    private DataPoint[] generateData() {
//        double x = values.length+1;
////        double f = mRand.nextDouble() * 0.15 + 0.3;
////        double y = Math.sin(i * f + 2) + mRand.nextDouble() * 0.3;
//        double y = Double.parseDouble((String) valorLidoBth.getText());
//
//        for (int i = 0; i < 99; i++) {
//            values[i] = values[i + 1];
//
//        }
//        DataPoint v = new DataPoint(x, y);
//        values[99] = v;
//        return values;
//    }
//
//    double mLastRandom = 2;
//    Random mRand = new Random();
//
//    private double getRandom() {
//        return mLastRandom += mRand.nextDouble() * 0.5 - 0.25;
//    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_inicio:
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            /* Esse método é invocado na Activity principal
                sempre que a thread de conexão Bluetooth recebe
                uma mensagem.
             */
            Bundle bundle = msg.getData();
            byte[] data = bundle.getByteArray("data");
            String dataString = new String(data);

            /* Aqui ocorre a decisão de ação, baseada na string
                recebida. Caso a string corresponda à uma das
                mensagens de status de conexão (iniciadas com --),
                atualizamos o status da conexão conforme o código.
             */
            if (dataString.equals("---N"))
                statusConexaoBth.setText("Ocorreu um erro durante a conexão D:");
            else if (dataString.equals("---S"))
                statusConexaoBth.setText(blth_address);
            else {

                /* Se a mensagem não for um código de status,
                    então ela deve ser tratada pelo aplicativo
                    como uma mensagem vinda diretamente do outro
                    lado da conexão. Nesse caso, simplesmente
                    atualizamos o valor contido no TextView do
                    contador.
                 */
                valorLidoBth.setText(dataString);
                addEntry(dataString);
            }

        }
    };

    /* Esse método é invocado sempre que o usuário clicar na TextView
        que contem o contador. O app Android transmite a string "restart",
        seguido de uma quebra de linha, que é o indicador de fim de mensagem.
     */
    public void restartCounter(View view) {
        connect.write("restart\n".getBytes());
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
