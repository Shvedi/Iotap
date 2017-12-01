package com.mah.hadideknache.esp8266mqtt;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {
    private Button btn;
    private TextView color;
    private MqttHelper mqttHelper;
    private Switch aSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        color = (TextView) findViewById(R.id.tvColor);
        aSwitch = (Switch) findViewById(R.id.light);
        aSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (aSwitch.isChecked()){
                    mqttHelper.sendMessageMqtt("On");
                    setOn(true);
                }
                else{
                    mqttHelper.sendMessageMqtt("Off");
                    setOn(false);
                }
            }
        });
        btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mqttHelper.sendMessageMqtt();

            }
        });
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
    public void setColor(String hexColor){
        color.setBackgroundColor(Color.parseColor(hexColor));
    }
    private void setOn(boolean isOn){
        if (isOn){
            color.setText("ON");
            color.setBackgroundColor(Color.parseColor("#ffff00"));
        }
        else {
            color.setText("OFF");
            color.setBackgroundColor(Color.parseColor("#000000"));
        }
    }
}

