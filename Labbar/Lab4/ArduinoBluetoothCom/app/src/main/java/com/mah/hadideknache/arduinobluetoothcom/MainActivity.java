package com.mah.hadideknache.arduinobluetoothcom;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
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
    private TextView textView, tv_bluetooth, tv_server;
    private Button btn, btn_bt, btn_server;
    private MqttHelper mqttHelper;
    private Classifier classifier;
    private static final int WINDOW_SIZE = 30;
    private ArrayList<Attribute> attrList = new ArrayList<>();
    private ArrayList<String> classVal = new ArrayList<>();
    BtServiceHandler btServiehandler;
    private BroadcastReceiver mMessageReceiver = new broadCastReceiver();
    private BroadcastReceiver btStatusReciever = new StatusBroadCastReceiver();
    private Spinner clsSpinner;
    final String[] classifiersArr = new String[]{"bayesnet.model", "SimpleLogistics.model","Logistic.model","J48.model","MultiLayerPerceptron.model",  "naiveBayes.model", "bayesNet20.model"};
    private boolean appstart = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        appstart = i.getBooleanExtra("appStart",true);
        i.putExtra("appStart",false);

        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.blueDevice);
        initComponents();
        btServiehandler = new BtServiceHandler();
        startMqtt();
        initWeka("bayesnet.model");
        initiateAttributeList();

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


        clsSpinner = (Spinner) findViewById(R.id.classifierSpinner);
        final ArrayAdapter<String> classifiers = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, classifiersArr);
        clsSpinner.setAdapter(classifiers);
        clsSpinner.setSelection(0);
        clsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                initWeka(classifiersArr[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    private void initWeka(String type) {
        AssetManager assetManager = getAssets();
        try {
            classifier = (Classifier)weka.core.SerializationHelper.read(assetManager.open(type));
            Log.d("INITWEKA: ",classifier.toString());
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
        classVal.add("tilt left");
        classVal.add("tilt right");
        classVal.add("clockwise");
        classVal.add("notClockwise");
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

    @Override
    protected void onResume() {
        super.onResume();
        btServiehandler.doBindService(this, appstart);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter ("newList"));
        LocalBroadcastManager.getInstance(this).registerReceiver(btStatusReciever, new IntentFilter ("btStatus"));
    }

    @Override
    protected void onPause() {
        btServiehandler.doUnbindService(this);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(btStatusReciever);
        super.onPause();
    }


    private void setUpBluetooth() {
        btServiehandler.connectDevice();
    }


    public void postResult(String s) {
        Toast.makeText(this ,s, Toast.LENGTH_SHORT).show();
        mqttHelper.sendMessageMqtt(s);
    }

    public void setServerText(String text){
        tv_server.setText(text);
    }



    private class broadCastReceiver extends BroadcastReceiver{
        private ArrayList<Integer> dataList;
        private double[] values;
        private Preprocessor preprocessor;
        Instances data = new Instances("Janne", attrList, 0);

        @Override
        public void onReceive(Context context, Intent intent) {
            this.dataList = intent.getIntegerArrayListExtra("theList");
            Log.d("MAINACTIVITY", "LIST SIZE: "+dataList.size());
            values = new double[dataList.size() + 1];
            for (int i = 0; i < dataList.size(); ++i) {
                values[i] = dataList.get(i);
            }
            values[dataList.size()] = -1;
            dataList.clear();
            preprocessor = new Preprocessor(values);
            values = preprocessor.run();

            DenseInstance denseInstance = new DenseInstance(1.0, values);
            denseInstance.setDataset(data);
            data.add(denseInstance);
            // Tell the dataset what attribute is the class
            data.setClassIndex(0);
            try {
                // Label it
                int label = (int) classifier.classifyInstance(denseInstance);
                postResult(data.classAttribute().value(label));
                Log.d("CLASSYFIED:", String.valueOf(label));
                Log.d("CLASSIFIED","LABEL: "+data.classAttribute().value(label) );

            } catch (Exception err) {
                Log.e("MainActivity", "Unable to classify", err);
            }
            btServiehandler.startReceive();
        }
    }


    @Override
    public boolean isFinishing() {
        btServiehandler.disconnectBT();
        btServiehandler.stopService(this);
        return super.isFinishing();
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
          //  maxMin();
            //normalize();


            return average;
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


    private class StatusBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra("theBtStatus");
            tv_bluetooth.setText(msg);
        }
    }
}
