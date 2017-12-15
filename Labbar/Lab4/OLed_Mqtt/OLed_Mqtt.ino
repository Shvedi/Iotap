// This example just provide basic function test;
// For more informations, please vist www.heltec.cn or mail to support@heltec.cn
#include <ESP8266WiFi.h>
#include <PubSubClient.h>
#include <Wire.h>

int counter = 0;
const int inRange = 580;
const int trig = D1;
const int echo = D0;
const int led = D6;
const char* ssid = "Sona";
const char* password =  "cff7350a";
const char* mqttServer = "m23.cloudmqtt.com";
const int mqttPort = 15311;
const char* mqttUser = "xuorfeiq";
const char* mqttPassword = "e_Zn_HTQYEiz";
//WIFI_Kit_8's OLED connection:
//SDA -- GPIO4 -- D2
//SCL -- GPIO5 -- D1
//RST -- GPIO16 -- D0

//int red = D6;
//int green = D8;
//int blue = D7;

WiFiClient espClient;
PubSubClient client(espClient);
//#define RST_OLED 16
//OLED display(4, 5);
String str;

// If you bought WIFI Kit 8 before 2017-8-20, you may try this initial
//#define RST_OLED D2
//OLED display(SDA, SCL);

void setup() {
  pinMode(trig,OUTPUT);
  pinMode(led, OUTPUT);
  digitalWrite(trig,LOW);
  digitalWrite(led,LOW);
  
  Serial.begin(9600);
  setup_wifi();
  /*pinMode(RST_OLED, OUTPUT);

  //Rgb-Led
  pinMode(red,OUTPUT);
  pinMode(green,OUTPUT);
  pinMode(blue,OUTPUT);
  
  digitalWrite(RST_OLED, LOW);   // turn D2 low to reset OLED
  delay(50);
  digitalWrite(RST_OLED, HIGH);    // while OLED is running, must set D2 in high
  Serial.print("Testar.....");*/

  // Initialize display
//  display.begin();

  // Test display ON
 // display.on();
 // delay(1*1000);

  //Mqtt settings
  client.setServer(mqttServer, mqttPort);
  client.setCallback(callback);
/*while(inRange){
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
}*/
  //client.setServer(mqttServer,mqttPort);
  //client.setCallback(callback);
  //client.publish("esp/test", "Hello from ESP8266");
  //client.subscribe("esp/test");
  
}

void loop() {

unsigned long t1;
unsigned long t2;
unsigned pulse_width;


digitalWrite(trig,HIGH);
delayMicroseconds(10);
digitalWrite(trig,LOW);

while(digitalRead(echo)==LOW);
 
t1 = micros();
while(digitalRead(echo)==HIGH);
t2=micros();

pulse_width = t2-t1;

if(inRange>pulse_width){
  counter = 0;
 //Mqtt connect to server
  while (!client.connected()) {
    Serial.println("Connecting to MQTT...");
 
    if (client.connect("ESP8266Client", mqttUser, mqttPassword )) {
      digitalWrite(led,HIGH);
      Serial.println("Connected");  
 
    } else {
 
      Serial.print("failed with state ");
      Serial.print(client.state());
      delay(2000);
 
    }
  }
  
  //kod för känna igen rörelse och LED tändning
}
if(inRange<pulse_width){
  counter++;
  if(counter>=10){
    digitalWrite(led,LOW);
   client.disconnect();
   counter = 0;
  }
Serial.println("Disconnected"); 
}
client.loop();
delay(60);
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
void callback(char* topic, byte* payload, unsigned int length) {
 
  Serial.print("Message arrived in topic: ");
  Serial.println(topic);
 
  Serial.print("Message:");
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
  }
 
  Serial.println();
  Serial.println("-----------------------");
 
}
//Method called when new message arrives
/*void callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Message arrived in topic: ");
  Serial.println(topic);
  //display.clear();
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

  Uncomment if using normal led 
  if(str.equals("On")){
    digitalWrite(D7,HIGH);
  }
  else{
    digitalWrite(D7,LOW);
  }
  display.print(string2char(str));
}*/


char* string2char(String command){
    if(command.length()!=0){
        char *p = const_cast<char*>(command.c_str());
        return p;
    }
}

