// This example just provide basic function test;
// For more informations, please vist www.heltec.cn or mail to support@heltec.cn
#include <ESP8266WiFi.h>
#include <PubSubClient.h>
#include <Wire.h>

String command = "";
int counterDisconnect = 0;
int counterConnect = 0;
const int inRange = 4500;

//Digital pins
const int trig = D1;
const int echo = D0;
const int ledSensor = D2;
const int ledUp = D5;
const int ledDown = D6;
const int ledLeft = D7;
const int ledRight = D8; 

//User credentials
const char* ssid = "Sona";
const char* password =  "cff7350a";
const char* mqttServer = "m23.cloudmqtt.com";
const int mqttPort = 15311;
const char* mqttUser = "xuorfeiq";
const char* mqttPassword = "e_Zn_HTQYEiz";

WiFiClient espClient;
PubSubClient client(espClient);
String str;

void setup() {
  pinMode(trig,OUTPUT);
  pinMode(ledSensor, OUTPUT);
  pinMode(ledUp, OUTPUT);
  pinMode(ledDown, OUTPUT);
  pinMode(ledLeft, OUTPUT);
  pinMode(ledRight, OUTPUT);
  digitalWrite(trig,LOW);
  digitalWrite(ledSensor, LOW);
  digitalWrite(ledUp, LOW);
  digitalWrite(ledDown, LOW);
  digitalWrite(ledLeft, LOW);
  digitalWrite(ledRight, LOW);
  
  Serial.begin(115200);
  setup_wifi();
  

  //Mqtt settings
  client.setServer(mqttServer, mqttPort);
  client.setCallback(callback);

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
  counterConnect++;
  counterDisconnect = 0;
 //Mqtt connect to server
 if(counterConnect>=5){
  counterConnect = 0;
  while (!client.connected()) {
    Serial.println("Connecting to MQTT...");
 
    if (client.connect("ESP8266Client", mqttUser, mqttPassword )) {
      digitalWrite(ledSensor,HIGH);
      Serial.println("Connected");
      client.publish("esp/test", "Hello from ESP8266");
      client.subscribe("esp/test");  
      client.subscribe("esp/test2");
 
    } else {
 
      Serial.print("failed with state ");
      Serial.print(client.state());
      delay(2000);
 
    }
  }
 }
}
if(inRange<pulse_width){
  counterDisconnect++;
  counterConnect = 0;
  if(counterDisconnect>=10&&client.connected()){
   digitalWrite(ledSensor,LOW);
   client.publish("esp/test2","Goodbye from ESP8266");
   client.disconnect();
   counterDisconnect = 0;   
   Serial.println("Disconnected"); 
  }
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
  command = "";
  Serial.print("Message arrived in topic: ");
  Serial.println(topic);
 
  Serial.print("Message:");
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
    command+=(char)payload[i];
  }
  translateCommand();
  Serial.println();
  Serial.println("-----------------------");
 
}

void translateCommand(){
  Serial.println("\n" + command);
  ledsOff();
  if(command == "up"){
    Serial.println("Led 1 on");
    digitalWrite(ledUp, HIGH);
  }
  if(command == "down"){
    Serial.println("Led 2 on");
    digitalWrite(ledDown, HIGH);
  }
  if(command == "left"){
    Serial.println("Led 3 on");
    digitalWrite(ledLeft, HIGH);
  }
  if(command == "right"){
    Serial.println("Led 4 on");
    digitalWrite(ledRight, HIGH);
  }
 
}
void ledsOff(){
  digitalWrite(ledUp, LOW);
  digitalWrite(ledDown, LOW);
  digitalWrite(ledLeft, LOW);
  digitalWrite(ledRight, LOW);
}
char* string2char(String command){
    if(command.length()!=0){
        char *p = const_cast<char*>(command.c_str());
        return p;
    }
}

