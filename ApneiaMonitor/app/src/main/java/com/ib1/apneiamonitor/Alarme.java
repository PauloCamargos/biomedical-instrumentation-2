package com.ib1.apneiamonitor;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class Alarme extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener {
    public static final String TAG = "Alarme";

    public static final String CHAR_INICIO = "S";
    public static final String CHAR_FIM = "E";
    public static final String CHAR_CONTROLE = "$";
    public static Queue<String> buffer = new LinkedList<>();

    static TextView statusConexaoBth;

    static LineGraphSeries<DataPoint> mSeries1;
    static int lastX = 0;
    static int points_amount = 100;
    DataPoint[] values = new DataPoint[points_amount];

    ConnectionThread connect;
    static String blth_address = "00:18:E4:40:00:06";

    public Byte tempo_limite;
    public Byte expansao_limite;
    static DatabaseHelper mDatabaseHelper;
    public SaveToFileThread saveToFileThread = new SaveToFileThread(this);
    public Button startBtn;
    public Button stopBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarme);

        statusConexaoBth = findViewById(R.id.status_dispositivo_conectado);
        startBtn = (Button) findViewById(R.id.iniciarTransmissao);
        stopBtn = (Button) findViewById(R.id.paraTransmissao);

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


        // BLUETOOTH CODE
//        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
//        btAdapter.enable();

//        if (btAdapter.isEnabled()) {
        // iniciar thread de conexao
        connect = new ConnectionThread(blth_address);
        if (!connect.isConnected) {
            connect.start();
            stopBtn.setEnabled(false);
        }
//        }
//        try {
//            Thread.sleep(50);
//        } catch (Exception E) {
//            E.printStackTrace();
//        }

        // GRAPH CODE

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

        // DATABASE CONFIGS
        mDatabaseHelper = new DatabaseHelper(this);
    }


    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            /* Esse método é invocado
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
//                valorLidoBth.setText(dataString);
                addEntry(dataString);
                Alarme.buffer.add(dataString);

            }

        }
    };


    public static void addEntry(String dataString) {
        mSeries1.appendData(new DataPoint(lastX++,
                Double.parseDouble(dataString)), false, 50);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String spinnerLabel = parent.getItemAtPosition(position).toString();
        if (parent.getId() == R.id.spinner_limite_expansao) {
            tempo_limite = Byte.valueOf(spinnerLabel);
        } else {
            expansao_limite = Byte.valueOf(spinnerLabel);
        }
//
//        displayToast(spinnerLabel);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void displayToast(String message) {
        Toast.makeText(getApplicationContext(), message,
                Toast.LENGTH_SHORT).show();
    }

    public void iniciarTransmissao(View view) {
        String pacote =
                CHAR_CONTROLE +
                        CHAR_INICIO + expansao_limite + CHAR_CONTROLE + tempo_limite +
                        CHAR_FIM + CHAR_CONTROLE;

        pacote = CHAR_INICIO + tempo_limite + CHAR_CONTROLE + expansao_limite + CHAR_FIM;

        try {
            if (connect.isConnected) {
                connect.write(pacote.getBytes());
                Log.d(TAG, "Conectado e transmitindo: " + pacote);

                stopBtn.setEnabled(true);
                startBtn.setEnabled(false);
                createDatabaseRecord();

            } else {
                displayToast("DESCONECTADO");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    Chama a thread para recolher dados do buffer salvar no arquivo
     */
    private void createDatabaseRecord() {
        String filename = UUID.randomUUID().toString().replace("-", "") + ".txt";
        mDatabaseHelper.addData(System.currentTimeMillis(), filename);
        startFileFill(filename);
    }

    private void startFileFill(String filename) {
//        String filename = mDatabaseHelper.getLastFilename();
        Log.d(TAG, "startFileFill: arquivo: " + filename);

        saveToFileThread.setFilename(filename);
        saveToFileThread.setRunning(true);

        if (!saveToFileThread.hasStarted) {
            saveToFileThread.setHasStarted(true);
            saveToFileThread.start();
        }


        Log.d(TAG, "startFileFill:Buffer: " + Alarme.buffer);
    }


    public void paraTransmissao(View view) {
        try {
            String pacote = CHAR_FIM;
            if (connect.isConnected) {
                connect.write(pacote.getBytes());
                displayToast("Parando: " + CHAR_FIM);

                saveToFileThread.setRunning(false);

                stopBtn.setEnabled(false);
                startBtn.setEnabled(true);
            } else {
                displayToast("DESCONECTADO");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
