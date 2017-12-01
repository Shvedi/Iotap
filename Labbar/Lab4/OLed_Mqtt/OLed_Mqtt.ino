// This example just provide basic function test;
// For more informations, please vist www.heltec.cn or mail to support@heltec.cn
#include <ESP8266WiFi.h>
#include <PubSubClient.h>
#include <Wire.h>
#include "OLED.h"

const char* ssid = "NetworkSSID";
const char* password =  "WifiPass";
const char* mqttServer = "ServerAdress";
const int mqttPort = 00000;
const char* mqttUser = "Username";
const char* mqttPassword = "Password";
//WIFI_Kit_8's OLED connection:
//SDA -- GPIO4 -- D2
//SCL -- GPIO5 -- D1
//RST -- GPIO16 -- D0

int red = D6;
int green = D8;
int blue = D7;

WiFiClient espClient;
PubSubClient client(espClient);
#define RST_OLED 16
OLED display(4, 5);
String str;

// If you bought WIFI Kit 8 before 2017-8-20, you may try this initial
//#define RST_OLED D2
//OLED display(SDA, SCL);

void setup() {
  Serial.begin(9600);
  setup_wifi();
  pinMode(RST_OLED, OUTPUT);

  //Rgb-Led
  pinMode(red,OUTPUT);
  pinMode(green,OUTPUT);
  pinMode(blue,OUTPUT);
  
  digitalWrite(RST_OLED, LOW);   // turn D2 low to reset OLED
  delay(50);
  digitalWrite(RST_OLED, HIGH);    // while OLED is running, must set D2 in high
  Serial.print("Testar.....");

  // Initialize display
  display.begin();

  // Test display ON
  display.on();
  delay(1*1000);

  //Mqtt settings
  client.setServer(mqttServer, mqttPort);
  client.setCallback(callback);

 //Mqtt connect to server
  while (!client.connected()) {
    Serial.println("Connecting to MQTT...");
 
    if (client.connect("ESP8266Client", mqttUser, mqttPassword )) {
 
      Serial.println("connected");  
 
    } else {
 
      Serial.print("failed with state ");
      Serial.print(client.state());
      delay(2000);
 
    }
  }
  client.setServer(mqttServer,mqttPort);
  client.setCallback(callback);
  client.publish("esp/test", "Hello from ESP8266");
  client.subscribe("esp/test");
  
}

void loop() {
    if (!client.connected()) {
      Serial.println("Lost connection");
     }
    client.loop();

}

void setup_wifi(){
  delay(50);
  // We start by connecting to a WiFi network
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);

  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}

//Method called when new message arrives
void callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Message arrived in topic: ");
  Serial.println(topic);
  display.clear();
  delay(200);
  str="";
  //Forloop for getting string from payload that was sent to mqtt
  //from the android phone
  for (int i = 0; i < length; i++) {
    str += (char)payload[i];
  }
  int r,g,b;
  int i = 0;
  int oldComma = 0;

  // For loop used for parsing RGB values Only!
  for(int j =0; j<str.length();j++){
    if(str.substring(j,j+1).equals(",")){
      if(i == 0){
        r = str.substring(oldComma,j).toInt();
        Serial.print("Red: ");
        delay(20);
        Serial.println(r);
        delay(20);
        oldComma = j+1;
        i++;
      }
      else if(i == 1){
        g = str.substring(oldComma,j).toInt();
        Serial.print("Green: ");
        delay(20);
        Serial.println(g);
        delay(20);
        oldComma = j+1;
        i++;

        delay(20);
        
        b = str.substring(oldComma,str.length()).toInt();
        delay(20);
        Serial.print("Blue: ");
        delay(20);
        Serial.println(b);
        i = 0;
        oldComma = 0;
        break;
      }
    }
    
  }
  analogWrite(red,r*2);
  delay(20);
  analogWrite(green,g*2);
  delay(20);
  analogWrite(blue,b*2);

  /*Uncomment if using normal led */
  /*if(str.equals("On")){
    digitalWrite(D7,HIGH);
  }
  else{
    digitalWrite(D7,LOW);
  }*/
  display.print(string2char(str));
}


char* string2char(String command){
    if(command.length()!=0){
        char *p = const_cast<char*>(command.c_str());
        return p;
    }
}

