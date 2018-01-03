package com.mah.hadideknache.arduinobluetoothcom;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by hadideknache on 2017-11-21.
 */

public class BluetoothConThread extends Thread {
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice device;
    private BluetoothAdapter adapter;
    private BtService btService;
    private UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    public BluetoothConThread(BluetoothDevice device, BluetoothAdapter adapter,BtService btService) {

        this.adapter = adapter;
        this.btService = btService;
        BluetoothSocket tmp = null;
        this.device = device;
        try {
            tmp = this.device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) { }
        bluetoothSocket = tmp;
    }
    public void run() {
        adapter.cancelDiscovery();
        try {
            bluetoothSocket.connect();
            btService.startConnected(bluetoothSocket, device);
        } catch (IOException connectException) {
            Log.d("BTCONTHREAD: ","ConnectionFailed");
            try {
                bluetoothSocket.close();

            } catch (IOException closeException) { }
            return;
        }
    }
    public void cancel() {
        try {
            bluetoothSocket.close();
        }
        catch (IOException e) { }
    } }
