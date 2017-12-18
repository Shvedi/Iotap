package com.mah.hadideknache.arduinobluetoothcom;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Set;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;


/**
 * Created by hadideknache on 2017-11-21.
 */

public class MainActivity extends AppCompatActivity {
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothConThread bluetoothConThread;
    private ConnectedThread conThread;
    private BluetoothDevice bluetoothDevice;
    Spinner spinner;
    private TextView textView, tv_bluetooth, tv_server;
    private Button btn, btn_bt, btn_server;
    private MqttHelper mqttHelper;
    private boolean isOn = false;
    private String labelSelected = "left";
    ArrayList<Integer> m_instance = new ArrayList<>();
    Classifier classifier;
    Instances data;
    MyHandler btMsgHandler;
    @Override
    public boolean isFinishing() {
        conThread.cancel();
        bluetoothConThread.cancel();
        return super.isFinishing();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.blueDevice);
        initComponents();
        setUpBluetooth();
        startMqtt();

        initWeka();

        btMsgHandler = new MyHandler(this, classifier, data);
    }

    private void initComponents() {


        btn_bt = findViewById(R.id.btn_bt);
        btn_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setUpBluetooth();
            }
        });

        btn_server = findViewById(R.id.btn_server);
        btn_server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMqtt();
            }
        });

        tv_bluetooth = findViewById(R.id.tv_bluetooth);
        tv_server = findViewById(R.id.tv_server);
    }


    private void initWeka() {
        AssetManager assetManager = getAssets();
        try {
            classifier = (Classifier)weka.core.SerializationHelper.read(assetManager.open("Simple_Logistics_big.model"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try{
            InputStream inputStream = getResources().openRawResource(R.raw.train_final);
            File tempFile = File.createTempFile("train_final",".arff");
            copyFile(inputStream, new FileOutputStream(tempFile));
            ConverterUtils.DataSource source = null;
            source = new ConverterUtils.DataSource(tempFile.getAbsolutePath());
            data = source.getDataSet();
            data.setClassIndex(data.numAttributes() - 1);
        } catch (IOException e) {
            throw new RuntimeException("Can't create temp file ", e);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    private void startMqtt() {
        mqttHelper = new MqttHelper(this,getApplicationContext());
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {

            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });

    }

    private void setUpBluetooth() {
        if (adapter==null){
            showMessage("This device doesn´t support bluetooth");
        }
        else{
            showMessage("Setting up bluetooth");
            if (!adapter.isEnabled()){
                Intent enableIntent = new Intent(adapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent,1);
            }
        }

        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals("G11")){
                    bluetoothDevice = device;
                }

            }
            if (bluetoothDevice!=null){
                textView.setText("Device Available: "+bluetoothDevice.getName());
                bluetoothConThread = new BluetoothConThread(bluetoothDevice,adapter,this);
                bluetoothConThread.start();
            }

        }
        else{
            textView.setText("No device connected!");
        }
        if(adapter.isEnabled()){
            tv_bluetooth.setText("Bluetooth connected");
        }

    }

    private void showMessage(String message) {
        Toast.makeText(this,message,Toast.LENGTH_SHORT);
    }
    public void startConnected(BluetoothSocket socket){
        if (socket.isConnected()){
            Log.d("MAIN: ","BT CONNECTIOn SUCCESSFUL");
        }
        conThread = new ConnectedThread(socket,btMsgHandler);
        conThread.start();
    }

    public void postResult(String s) {
        Toast.makeText(this ,s, Toast.LENGTH_SHORT).show();

    }

    public String getLabelSelected() {
        return labelSelected;
    }

    public void setServerText(String text){
        tv_server.setText(text);
    }


    //static inner class doesn't hold an implicit reference to the outer class
    public static class MyHandler extends Handler {
        //Using a weak reference means you won't prevent garbage collection
        private final WeakReference<MainActivity> myClassWeakReference;
        private final Classifier classifier;
        private final Instances data;
        private final MainActivity myClass;
        private ClassifierThread classifierThread;
        ArrayList<Integer> m_instance = new ArrayList<>();


        public MyHandler(MainActivity myClassInstance, Classifier classifier, Instances data) {
            myClassWeakReference = new WeakReference<MainActivity>(myClassInstance);
            myClass = myClassWeakReference.get();
            this.classifier = classifier;
            this.data = data;

        }

        @Override
        public void handleMessage(Message message) {


            if (myClass != null) {
                byte[] writeBuf = (byte[]) message.obj;
                int begin = (int) message.arg1;
                int end = (int) message.arg2;
                switch (message.what) {
                    case 1:
                        String msg = new String(writeBuf);
                        msg = msg.substring(begin, end);
                       // Log.d("RECIEVEDMSG: ",msg);
                        int commas = 0;
                        if(msg.length()>1){
                            if (msg.charAt(0) == 'h') {
                                for (int i = 0; i < msg.length(); i++) {
                                    if (msg.charAt(i) == ',') {
                                        commas++;
                                    }
                                }
                            }
                            if (commas == 7) {
                                String[] ints = msg.split(",");

                                for (int i = 1; i < 7; ++i) {
                                    m_instance.add(Integer.valueOf(ints[i]));
                                }

                                if (m_instance.size() == 40 * 6) {
                                    // Create the instance
                                    double[] values = new double[m_instance.size() + 1];
                                    for (int i = 0; i < m_instance.size(); ++i) {
                                        values[i] = m_instance.get(i);
                                    }
                                    values[m_instance.size()] = -1;
                                    m_instance.clear();

                                    // Add to test data
                                    this.classifierThread = new ClassifierThread(data, classifier,myClass);
                                    classifierThread.execute(values);
                                    /*Normalize normalize = new Normalize();

                                    DenseInstance denseInstance = new DenseInstance(1.0, values);
                                    denseInstance.setDataset(data);

                                    try {
                                        // Label it
                                        int label = (int) classifier.classifyInstance(denseInstance);
                                        Log.d("CLASSYFIED:", String.valueOf(label));
                                        Log.d("CLASSIFIED","LABEL: "+data.classAttribute().value(label) );
                                    } catch (Exception err) {
                                        Log.e("MainActivity", "Unable to classify", err);
                                    }*/


                                }
                                break;
                            }}
                }
            }
        }
    }

    private class LabelListener implements AdapterView.OnItemSelectedListener {


        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            Log.v("DATA ENTRY",String.valueOf(position));
            labelSelected = String.valueOf(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }


}
