package com.ufcity.cep.mqtt;

import org.eclipse.paho.client.mqttv3.*;

public class MQTTSubscribe {

    private String URI;
    private String[] topics;

    public MQTTSubscribe(String UIR, String[] topics) {
        this.URI = UIR;
        this.topics = topics;
    }

    public void subscribe(MessageObserver messageObserver) {
        System.out.println("### Inner Computing ###");
        try {
            System.out.println("SUB# ServerURI: " + this.URI);
            MqttClient client = new MqttClient(this.URI, "cep-sub");
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            client.connect(connOpts);
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {}
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    System.out.println("topic: "+topic+", "+(new String(message.getPayload())));
                    messageObserver.receiveMessage(topic, new String(message.getPayload()));
                }
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {}
            });
            System.out.println(topics);
            client.subscribe(topics);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}
