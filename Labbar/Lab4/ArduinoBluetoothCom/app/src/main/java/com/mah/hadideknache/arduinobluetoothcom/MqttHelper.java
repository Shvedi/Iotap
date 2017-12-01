package com.mah.hadideknache.arduinobluetoothcom;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.util.Log;

import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Created by hadideknache on 2017-12-01.
 */

public class MqttHelper {
    public MqttAndroidClient mqttAndroidClient;

    private String serverUri = "tcp://server:port";
    private String mqttUser = "username";
    private String mqttPassword = "password";
    private String clientId = "AndroidClient";
    private String subs = "esp/test";
    private final ColorPicker colorPick;
    private String message;
    public MqttHelper(MainActivity main, Context context){
        mqttAndroidClient = new MqttAndroidClient(context,serverUri,clientId);
        colorPick = new ColorPicker(main, 255, 125, 125, 125);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.v("ConnectCompleted","The connection was successful");
            }

            @Override
            public void connectionLost(Throwable throwable) {
                Log.v("ConnectionLost","The connection was lost");
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                Log.v("Message Arrived", mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                Log.v("Delivered", "Message was sent");
            }
        });
        connectToMqtt();
    }

    private void connectToMqtt() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(mqttUser);
        mqttConnectOptions.setPassword(mqttPassword.toCharArray());

        try{
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    Log.v("OnSuccess","Connected!");
                    DisconnectedBufferOptions disconnectBuffer = new DisconnectedBufferOptions();
                    disconnectBuffer.setBufferEnabled(true);
                    disconnectBuffer.setBufferSize(100);
                    disconnectBuffer.setPersistBuffer(false);
                    mqttAndroidClient.setBufferOpts(disconnectBuffer);
                    subscribe();
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    Log.v("onFailure","The connection failed");
                }
            });
        }catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    private void subscribe() {

        try {
            mqttAndroidClient.subscribe(subs, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {

                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void setCallback(MqttCallbackExtended callback){
        mqttAndroidClient.setCallback(callback);
    }

    public void sendMessageMqtt(){
        colorPick.show();

    /* Listener when clicking on the button */
        colorPick.setCallback(new ColorPickerCallback() {
            @Override
            public void onColorChosen(@ColorInt int color) {
                // Do whatever you want
                // Examples
                message = Integer.toString(Color.red(color))+","+Integer.toString(Color.green(color))+","+Integer.toString(Color.blue(color));

                MqttMessage mqttMessage = new MqttMessage(message.getBytes());
                try {
                    mqttAndroidClient.publish(subs,mqttMessage);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                colorPick.cancel();
            }
        });

    }


}
