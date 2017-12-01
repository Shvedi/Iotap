package com.mah.hadideknache.arduinobluetoothcom;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Set;

/**
 * Created by hadideknache on 2017-11-21.
 */

public class MainActivity extends AppCompatActivity {
    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothConThread bluetoothConThread;
    private ConnectedThread conThread;
    private BluetoothDevice bluetoothDevice;
    private Handler handler;
    private TextView textView;
    private Button btn;
    private MqttHelper mqttHelper;
    private boolean isOn = false;

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
        btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mqttHelper.sendMessageMqtt();

            }
        });
        setUpBluetooth();
        handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                byte[] writeBuf = (byte[]) message.obj;
                int begin = (int)message.arg1;
                int end = (int)message.arg2;
                switch(message.what) {
                    case 1:
                        String msg = new String(writeBuf);
                        msg = msg.substring(begin, end);
                        break;
                }
            }
        };
        startMqtt();
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
            showMessage("This device doesnt support bluetooth");
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
                bluetoothDevice = device;
            }
            if (bluetoothDevice!=null){
                textView.setText("Device Available: "+bluetoothDevice);
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
        conThread = new ConnectedThread(socket,handler);
        conThread.start();
    }
}
