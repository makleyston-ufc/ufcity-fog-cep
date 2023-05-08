package com.ufcity.cep.mqtt;

public interface MessageObserver {

    void receiveMessage(String topic, String message);

}
