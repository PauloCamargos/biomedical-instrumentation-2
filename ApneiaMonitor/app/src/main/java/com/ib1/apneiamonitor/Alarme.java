package com.ib1.apneiamonitor;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class Alarme extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener {

    public static final String INICIAR_TRANSMISSAO = "s";
    public static final String PARAR_TRANSMISSAO = "e";

    static TextView statusConexaoBth;

    static LineGraphSeries<DataPoint> mSeries1;
    static int lastX = 0;
    static int points_amount = 100;
    DataPoint[] values = new DataPoint[points_amount];

    ConnectionThread connect;
    static String blth_address = "00:18:E4:40:00:06";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarme);

        statusConexaoBth = findViewById(R.id.status_dispositivo_conectado);


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
        connect.start();
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
    }

    static void addEntry(String dataString) {
        mSeries1.appendData(new DataPoint(lastX++,
                Double.parseDouble((String) dataString)), false, 50);
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
//                valorLidoBth.setText(dataString);
                addEntry(dataString);
            }

        }
    };

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String spinnerLabel = parent.getItemAtPosition(position).toString();
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
        if (connect.isConnected) {
            connect.write(INICIAR_TRANSMISSAO.getBytes());
            displayToast("Conecatado e transmitindo: " + INICIAR_TRANSMISSAO);
        } else {
            displayToast("DESCONECTADO");
        }
    }

    public void paraTransmissao(View view) {
        if (connect.isConnected) {
            connect.write(PARAR_TRANSMISSAO.getBytes());
            displayToast("Parando: " + PARAR_TRANSMISSAO);

        } else {
            displayToast("DESCONECTADO");
        }
    }
}
