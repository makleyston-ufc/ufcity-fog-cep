package com.ufcity.cep.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import static com.ufcity.cep.Main.URI_INNER_MQTT;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.LocalDateTime.now;

public class MQTTPublish {

    public static void publish(String uuid_device, String resourceJson) {
        String topic = TOPICS.COMMANDS_TO_EDGE + "/"+uuid_device;
        String clientId = "ceo_pub_"+now();
        try {
            System.out.print("PUB# Sending data to ServerURI: " + URI_INNER_MQTT);
            MqttClient client = new MqttClient(URI_INNER_MQTT, clientId);
            client.connect();
            client.publish(topic, resourceJson.getBytes(UTF_8),0, false);
            client.disconnect();
            System.out.println(" ... OK");
        } catch (MqttException e) {
            System.err.println(" ... Fail");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
