package com.mah.hadideknache.arduinobluetoothcom;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;


/**
 * Created by Mikael on 2017-12-28.
 */

public class BtService extends Service {
    private BluetoothAdapter adapter= BluetoothAdapter.getDefaultAdapter();;
    private BluetoothDevice bluetoothDevice;
    private BluetoothConThread bluetoothConThread;
    private ConnectedThread conThread;
    private static int WINDOW_SIZE = 30;




    public class LocalService extends Binder {
        public BtService getService() {
            return BtService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (adapter==null){
            Toast.makeText(getApplicationContext(),"This device doesn´t support bluetooth", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getApplicationContext(),"Setting up bluetooth", Toast.LENGTH_SHORT).show();

            if (!adapter.isEnabled()){
                Intent btIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                btIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(btIntent);
            }
            initConnection();
        }
        return START_STICKY;
    }

    public void initConnection(){
        if (adapter.isEnabled()){
            Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName().equals("G11")){
                        bluetoothDevice = device;
                    }

                }
                if (bluetoothDevice!=null){
                    Log.d("BTSERVICE","BTDEVICE:"+ bluetoothDevice.getName()+" : "+bluetoothDevice.getAddress());
                    //textView.setText("Device Available: "+bluetoothDevice.getName());
                    bluetoothConThread = new BluetoothConThread(bluetoothDevice,adapter, this);
                    bluetoothConThread.start();
                }

            }
        }
    }

    public void startConnected(BluetoothSocket socket, BluetoothDevice device){
        if (socket.isConnected()){
            Log.d("MAIN: ","BT CONNECTIOn SUCCESSFUL");
            sendBtStatus("Connection Successfull: " + device.getName());
            conThread = new ConnectedThread(socket);
            conThread.setRunning(true);
            conThread.start();
        }
    }

    @Override
    public void onCreate() {
        Log.d("LocalService", "OnCreate " );
        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(receiver, filter);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        // Tell the user we stopped.
        unregisterReceiver(receiver);
        if (conThread.isAlive()){
            conThread.cancel();
        }
        Log.d("LocalService", "OnDestroy " );
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show();
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalService();
    }


    private class ConnectedThread extends Thread {
        private BluetoothSocket socket;
        private BufferedReader inputStream;
        private OutputStream outputStream;
        private boolean receive = true;
        private long timer = 0;
        private ArrayList<Integer> m_instance = new ArrayList<>();

        private boolean running = true;

        public ConnectedThread(BluetoothSocket socket) {
            this.socket = socket;
            initStreams();
            write("w30".getBytes());
            write("f30".getBytes());



        }

        private void initStreams(){

            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }
            inputStream = new BufferedReader(new InputStreamReader(tmpIn));
            outputStream = tmpOut;
        }

        public void run() {

            while (running) {
                if (receive) {
                    String msg = "";
                    try {
                        msg = inputStream.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (msg.matches("window size = 30")) {
                        write("f30".getBytes());
                    }
                    if (msg.matches("h(,-?\\d+){6},?")) {
                        Log.d("HANDLER: ", msg);

                        if (System.currentTimeMillis() - timer > 600) {
                            m_instance.clear();
                            Log.d("BTSERVICE","CLEARING LIST!!!!");
                        }
                        timer = System.currentTimeMillis();

                        String[] ints = msg.split(",");

                        for (int i = 1; i < 7; ++i) {
                            m_instance.add(Integer.valueOf(ints[i]));
                        }
                        if (m_instance.size() == WINDOW_SIZE * 6) {
                            stopRecieve();
                            sendMessage(new ArrayList<Integer>(m_instance));
                            m_instance.clear();
                        }
                    } else {
                        Log.d("HANDLER NO MATCH: ", msg);
                    }
                }

            }cancel();
        }


        public void setRunning(boolean running) {
            this.running = running;
        }


        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
            }
        }

        public void cancel() {
            setRunning(false);
            try {
                socket.close();
                sendBtStatus("Device Disconnected");
            } catch (IOException e) {
            }
        }

        public void stopRecieve() {
            this.receive = false;
        }

        public void startRecieve() {
            m_instance.clear();
           initStreams();
            this.receive = true;
        }


    }

    public void disconnectBt() {
        if (conThread!=null){
            conThread.cancel();
        }
    }

    private void sendBtStatus(String msg){
        Intent intent = new Intent("btStatus");
        // add data
        intent.putExtra("theBtStatus", msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendMessage(ArrayList<Integer> dataList) {
        Intent intent = new Intent("newList");
        // add data
        intent.putIntegerArrayListExtra("theList", dataList);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void startReceive(){
        conThread.startRecieve();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(TextUtils.equals(action,BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                BluetoothDevice device = intent.getExtras()
                        .getParcelable(BluetoothDevice.EXTRA_DEVICE);
                if (bluetoothDevice!=null && conThread!=null && bluetoothDevice.getName().equals(device.getName())) {

                   conThread.setRunning(false);
                    Log.d("BtService","G11 DEVICE DISCONNECTED");
                    // to push your notification
                }else{
                     Log.d("BtService","SOME DEVICE DISCONNECTED");
                }
            }

        }
    };


}
