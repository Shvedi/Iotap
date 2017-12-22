package com.mah.hadideknache.arduinobluetoothcom;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
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
    private TextView textView;
    private Button btn;
    private MqttHelper mqttHelper;
    private boolean isOn = false;
    private String labelSelected = "left";
    ArrayList<Integer> m_instance = new ArrayList<>();
    Classifier classifier;
    Instances data;
    MyHandler btMsgHandler;
    private static final int WINDOW_SIZE = 30;
    private ArrayList<Attribute> attrList = new ArrayList<>();
    private ArrayList<String> classVal = new ArrayList<>();
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
        setUpBluetooth();
        startMqtt();
        initComponents();
        initWeka();
        initiateAttributeList();
        initBtHandler();

    }

    private void initBtHandler() {
        btMsgHandler = new MyHandler(this, classifier, attrList);
    }

    private void initComponents() {
        String[] labels = {"left", "right", "up", "down","tilt_left","tilt_right"};
        spinner = (Spinner)findViewById(R.id.spinner_1);
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.support_simple_spinner_dropdown_item,labels);
        spinner.setAdapter(typeAdapter);
        spinner.setOnItemSelectedListener(new LabelListener());


        btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mqttHelper.sendMessageMqtt();

            }
        });
    }


    private void initWeka() {
        AssetManager assetManager = getAssets();
        try {
            classifier = (Classifier)weka.core.SerializationHelper.read(assetManager.open("SimpleLogistic_12_18.model"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initiateAttributeList() {
        /*
        The attribute list must resemble the training dataset structure.
        The first attribute is a list of the classes, the rest is
        the sensor values.
         */
        classVal.add("left");
        classVal.add("right");
        classVal.add("up");
        classVal.add("down");
        classVal.add("tilt_left");
        classVal.add("tilt_right");
        attrList.add(new Attribute("class", classVal));
        for (int i = 0; i < WINDOW_SIZE; i++) {
            attrList.add(new Attribute("AccX" + (i + 1)));
            attrList.add(new Attribute("AccY" + (i + 1)));
            attrList.add(new Attribute("AccZ" + (i + 1)));
            attrList.add(new Attribute("GyrX" + (i + 1)));
            attrList.add(new Attribute("GyrY" + (i + 1)));
            attrList.add(new Attribute("GyrZ" + (i + 1)));
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
        btMsgHandler.setConThread(conThread);

    }

    public void postResult(String s) {
        Toast.makeText(this ,s, Toast.LENGTH_SHORT).show();
        conThread.startRecieve();
    }

    public String getLabelSelected() {
        return labelSelected;
    }


    //static inner class doesn't hold an implicit reference to the outer class
    public static class MyHandler extends Handler {
        //Using a weak reference means you won't prevent garbage collection
        private final WeakReference<MainActivity> myClassWeakReference;
        private final Classifier classifier;
        private final Instances data;
        private final MainActivity myClass;
        private ConnectedThread conThread;
        private ClassifierThread classifierThread;
        private long timer = 0;
        ArrayList<Integer> m_instance = new ArrayList<>();
        private ArrayList<Attribute> attrList = new ArrayList<>();

        public MyHandler(MainActivity myClassInstance, Classifier classifier, ArrayList<Attribute> attrList) {
            myClassWeakReference = new WeakReference<MainActivity>(myClassInstance);
            myClass = myClassWeakReference.get();
            this.classifier = classifier;

            this.attrList = attrList;
            this.data = new Instances("Janne", this.attrList, 0);

        }

        @Override
        public void handleMessage(Message message) {


            if (myClass != null) {
                Bundle bundle = message.getData();
                String msg = bundle.getString("msg");
                if (System.currentTimeMillis()-timer>500){
                    m_instance.clear();
                }
                timer = System.currentTimeMillis();

                String[] ints = msg.split(",");

                for (int i = 1; i < 7; ++i) {
                    m_instance.add(Integer.valueOf(ints[i]));
                }
               // Log.d("MAIN M_INSTANCE", String.valueOf(m_instance.size()));
                if (m_instance.size() == WINDOW_SIZE * 6) {
                    conThread.stopRecieve();
                    // Create the instance
                    double[] values = new double[m_instance.size() + 1];
                    for (int i = 0; i < m_instance.size(); ++i) {
                        values[i] = m_instance.get(i);
                    }
                    values[m_instance.size()] = -1;
                    m_instance.clear();
                    Preprocessor preprocessor = new Preprocessor(values);
                    values = preprocessor.run();


                    DenseInstance denseInstance = new DenseInstance(1.0, values);
                    denseInstance.setDataset(data);
                    data.add(denseInstance);
                    // Tell the dataset what attribute is the class
                    data.setClassIndex(0);
                    try {
                        // Label it
                        int label = (int) classifier.classifyInstance(denseInstance);
                        myClass.postResult(data.classAttribute().value(label));
                        Log.d("CLASSYFIED:", String.valueOf(label));
                        Log.d("CLASSIFIED","LABEL: "+data.classAttribute().value(label) );

                    } catch (Exception err) {
                        Log.e("MainActivity", "Unable to classify", err);
                    }
                }
            }
        }
        public void setConThread(ConnectedThread conThread) {
            this.conThread = conThread;
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


    private static class Preprocessor {
        private final double[] dataset;
        private double minAcc = 1, maxAcc = 1, minGyro = 1, maxGyro = 1;
        private double[] average = new double[(WINDOW_SIZE * 6) + 1];

        public Preprocessor(double[] dataset) {
            this.dataset = dataset;
        }

        public double[] run() {
            movingAverage();
            maxMin();
            normalize();

            return dataset;
        }

        public void movingAverage() {

            for (int i = 0; i < WINDOW_SIZE; i++) {

                if (i == 0) {
                    average[i] = dataset[0];
                    average[i + 1] = dataset[1];
                    average[i + 2] = dataset[2];
                    average[i + 3] = dataset[3];
                    average[i + 4] = dataset[4];
                    average[i + 5] = dataset[5];
                } else if (i == 1) {
                    average[(i * 6)] = ((dataset[(i * 6)] + dataset[0]) / 2);
                    average[(i * 6) + 1] = ((dataset[(i * 6) + 1] + dataset[1]) / 2);
                    average[(i * 6) + 2] = ((dataset[(i * 6) + 2] + dataset[2]) / 2);
                    average[(i * 6) + 3] = ((dataset[(i * 6) + 3] + dataset[3]) / 2);
                    average[(i * 6) + 4] = ((dataset[(i * 6) + 4] + dataset[4]) / 2);
                    average[(i * 6) + 5] = ((dataset[(i * 6) + 5] + dataset[5]) / 2);
                } else if (i == 2) {
                    average[(i * 6)] = ((dataset[(i * 6)] + dataset[6] + dataset[0]) / 3);
                    average[(i * 6) + 1] = ((dataset[(i * 6) + 1] + dataset[7] + dataset[1]) / 3);
                    average[(i * 6) + 2] = ((dataset[(i * 6) + 2] + dataset[8] + dataset[2]) / 3);
                    average[(i * 6) + 3] = ((dataset[(i * 6) + 3] + dataset[9] + dataset[3]) / 3);
                    average[(i * 6) + 4] = ((dataset[(i * 6) + 4] + dataset[10] + dataset[4]) / 3);
                    average[(i * 6) + 5] = ((dataset[(i * 6) + 5] + dataset[11] + dataset[5]) / 3);
                } else if (i == 3) {
                    average[(i * 6)] = ((dataset[(i * 6)] + dataset[(12)] + dataset[6] + dataset[0]) / 4);
                    average[(i * 6) + 1] = ((dataset[(i * 6) + 1] + dataset[13] + dataset[7] + dataset[1]) / 4);
                    average[(i * 6) + 2] = ((dataset[(i * 6) + 2] + dataset[14] + dataset[8] + dataset[2]) / 4);
                    average[(i * 6) + 3] = ((dataset[(i * 6) + 3] + dataset[15] + dataset[9] + dataset[3]) / 4);
                    average[(i * 6) + 4] = ((dataset[(i * 6) + 4] + dataset[16] + dataset[10] + dataset[4]) / 4);
                    average[(i * 6) + 5] = ((dataset[(i * 6) + 5] + dataset[17] + dataset[11] + dataset[5]) / 4);
                }
    /*else if(i==4){
        average[(i*6)]   = ((dataset[(i*6)]+dataset[18]+dataset[12]+dataset[6]+dataset[0])/5);
        average[(i*6)+1] = ((dataset[(i*6)]+dataset[19]+dataset[13]+dataset[7]+dataset[1])/5);
        average[(i*6)+2] = ((dataset[(i*6)]+dataset[20]+dataset[14]+dataset[8]+dataset[2])/5);
        average[(i*6)+3] = ((dataset[(i*6)]+dataset[21]+dataset[15]+dataset[9]+dataset[3])/5);
        average[(i*6)+4] = ((dataset[(i*6)]+dataset[22]+dataset[16]+dataset[10]+dataset[4])/5);
        average[(i*6)+5] = ((dataset[(i*6)]+dataset[23]+dataset[17]+dataset[11]+dataset[5])/5);
    }*/
                else {
                    average[(i * 6)] = ((dataset[(i * 6)] + dataset[((i - 1) * 6)] + dataset[((i - 2) * 6)] + dataset[((i - 3) * 6)] + dataset[((i - 4) * 6)]) / 5);
                    average[(i * 6) + 1] = ((dataset[(i * 6) + 1] + dataset[((i - 1) * 6) + 1] + dataset[((i - 2) * 6) + 1] + dataset[((i - 3) * 6) + 1] + dataset[((i - 4) * 6) + 1]) / 5);
                    average[(i * 6) + 2] = ((dataset[(i * 6) + 2] + dataset[((i - 1) * 6) + 2] + dataset[((i - 2) * 6) + 2] + dataset[((i - 3) * 6) + 2] + dataset[((i - 4) * 6) + 2]) / 5);
                    average[(i * 6) + 3] = ((dataset[(i * 6) + 3] + dataset[((i - 1) * 6) + 3] + dataset[((i - 2) * 6) + 3] + dataset[((i - 3) * 6) + 3] + dataset[((i - 4) * 6) + 3]) / 5);
                    average[(i * 6) + 4] = ((dataset[(i * 6) + 4] + dataset[((i - 1) * 6) + 4] + dataset[((i - 2) * 6) + 4] + dataset[((i - 3) * 6) + 4] + dataset[((i - 4) * 6) + 4]) / 5);
                    average[(i * 6) + 5] = ((dataset[(i * 6) + 5] + dataset[((i - 1) * 6) + 5] + dataset[((i - 2) * 6) + 5] + dataset[((i - 4) * 6) + 5] + dataset[((i - 4) * 6) + 5]) / 5);
                }
            }
        }

        public void maxMin() {

            for (int i = 0; i < WINDOW_SIZE; i++) {
                for (int j = 0; j < 6; j++) {
                    if (j < 3) {
                        if (minAcc > average[i * j]) {
                            minAcc = average[i * j];
                        }
                        if (maxAcc < average[i * j]) {
                            maxAcc = average[i * j];
                        }
                    } else {
                        if (minGyro > average[i * j]) {
                            minGyro = average[i * j];
                        }
                        if (maxGyro < average[i * j]) {
                            maxGyro = average[i * j];
                        }
                    }
                }
            }
          /*  System.out.println("MinAcc: "+minAcc);
            System.out.println("MaxAcc: "+maxAcc);
            System.out.println("MinGyro: "+minGyro);
            System.out.println("MaxGyro: "+maxGyro);*/
        }

        public void normalize() {
            // System.out.println("Normalized Data: ");
            for (int i = 0; i < WINDOW_SIZE; i++) {
                for (int j = 0; j < 6; j++) {
                    if (j < 3) {
                        dataset[(i * 6) + j] = ((((average[(i * 6) + j] - minAcc) / (maxAcc - minAcc)) * (100 - 0)) + 0);
                    } else {
                        dataset[(i * 6) + j] = ((((average[(i * 6) + j] - minGyro) / (maxGyro - minGyro)) * (100 - 0)) + 0);
                    }
                    // System.out.println(dataset[i+j]);

                }

            }
        }
    }


}
