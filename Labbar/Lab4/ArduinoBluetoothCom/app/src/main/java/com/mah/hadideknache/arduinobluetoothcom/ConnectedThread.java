package com.mah.hadideknache.arduinobluetoothcom;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by hadideknache on 2017-11-21.
 */

public class ConnectedThread extends Thread{
     private BluetoothSocket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Handler handler;
        public ConnectedThread(BluetoothSocket socket,Handler handler) {
            this.socket = socket;
            this.handler = handler;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int begin = 0;
            int bytes = 0;
            while (true) {
                try {
                    bytes += inputStream.read(buffer, bytes, buffer.length - bytes);
                    for(int i = begin; i < bytes; i++) {

                        if(buffer[i] == "#".getBytes()[0]) {

                            handler.obtainMessage(1, begin, i, buffer).sendToTarget();
                            begin = i + 1;

                            if(i == bytes - 1) {
                                bytes = 0;
                                begin = 0;
                            }
                        } }
                } catch (IOException e) {
                    break;
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
}
