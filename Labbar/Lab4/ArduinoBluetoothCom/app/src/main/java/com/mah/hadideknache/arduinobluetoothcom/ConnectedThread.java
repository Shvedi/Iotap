package com.mah.hadideknache.arduinobluetoothcom;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Created by hadideknache on 2017-11-21.
 */

public class ConnectedThread extends Thread{
     private BluetoothSocket socket;
    private BufferedReader inputStream;
    private OutputStream outputStream;
    private Handler handler;
    private boolean recieve = true;
    private boolean flushing= false;
    private boolean okToRead =false;

    public ConnectedThread(BluetoothSocket socket,Handler handler) {
            this.socket = socket;
            this.handler = handler;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
            inputStream = new BufferedReader(new InputStreamReader(tmpIn));
            outputStream = tmpOut;
            write("f30".getBytes());
            write("w30".getBytes());


        }

        public void run() {
            byte[] buffer = new byte[1024];
            int begin = 0;
            int bytes = 0;
            int emptyCounter = 0;
            boolean reading = false;

            while (true) {
                if (recieve ){
                    String msg = "";
                    try {
                        msg = inputStream.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (msg.matches("h(,-?\\d+){6},?")){
                        Log.d("HANDLER: ", msg);
                        Message msgs = handler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putString("msg", msg);
                        msgs.setData(bundle);
                        handler.sendMessage(msgs);

                    }else{
                        Log.d("HANDLER: ", msg);
                    }
                }
            }
    }
        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) { }
        }
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) { }
        }

    public void stopRecieve() {
            this.recieve = false;
    }

    public void startRecieve() {
        this.recieve = true;
    }


}
