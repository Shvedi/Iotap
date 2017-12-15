#include <ESP8266WiFi.h>
#include <PubSubClient.h>

const char* ssid = "Wifi";
const char* password = "hadi969696";

const char* mqttServer = "m23.cloudmqtt.com";
const int mqttPort = 15311;
const char* mqttUser = "xuorfeiq";
const char* mqttPassword = "e_Zn_HTQYEiz";
WiFiClient espClient;
PubSubClient client(espClient);
void setup() {
  
  Serial.begin(115200);
  WiFi.begin(ssid,password);

  while(WiFi.status() != WL_CONNECTED){
    delay(500);
    Serial.println("Connecting to WiFi...");
  }
  Serial.println("Connected to the Wifi network");

  client.setServer(mqttServer,mqttPort);

  client.setCallback(callback);
  
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
    client.publish("esp/test", "Hello from ESP8266");
    client.subscribe("esp/test");
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

void loop() {
 client.loop();

}

