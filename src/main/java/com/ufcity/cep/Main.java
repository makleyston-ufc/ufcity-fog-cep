package com.ufcity.cep;

import com.espertech.esper.client.*;
import com.google.gson.Gson;
import com.ufcity.cep.cep.Listener;
import com.ufcity.cep.model.Resource;
import com.ufcity.cep.queue.ResultQueue;
import com.ufcity.cep.storage.Database;
import org.eclipse.paho.client.mqttv3.MqttException;
import ufcitycore.mqtt.ConnectionConfig;
import ufcitycore.mqtt.Publish;
import ufcitycore.mqtt.Subscribe;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static ufcitycore.config.Config.ReaderYAMLConfig;
import static ufcitycore.mqtt.ConnectionData.*;

public class Main {
    static String uuidItself = UUID.randomUUID().toString();
    /* GSON */
    static Gson gson = new Gson();
    private static final List<String> epls = new ArrayList<>();
    static Database database;
    static final String version = "0.1";
    static EPServiceProvider cep;
    static EPAdministrator cepAdm;

    public static void main(String[] args) throws IOException, MqttException {

        try {
            ReaderYAMLConfig(new Config());
        } catch (FileNotFoundException e) {
            System.err.println("Configuration file not found or not properly written!");
            throw new RuntimeException(e);
        }

        /* Check the uuid_fog on MongoDB */
        uuidItself = database.setOrGetFogUUIDFog(uuidItself);

        /* EsperCEP */
        Configuration cepConfig = new Configuration();
        cepConfig.addEventType("Resource", com.ufcity.cep.model.Resource.class);

        // Create an instance of the EPServiceProvider
        cep = EPServiceProviderManager.getDefaultProvider(cepConfig);

        // Use the EPServiceProvider to create an instance of the EPAdministrator
        cepAdm = cep.getEPAdministrator();

        /*  MQTT Broker - Edge communication. */
        System.out.println("### Inner MQTT Broker ###");
        ConnectionConfig connectionConfigSubCEP = new ConnectionConfig(INNER_HOST, INNER_PORT);
        connectionConfigSubCEP.setTopics(getResourceDataSubscribeTopic());
        Subscribe subscribeCEP = new Subscribe(connectionConfigSubCEP);
        subscribeCEP.subscribe((topic, message) -> {
            System.out.println("## Topic: "+topic+", Message: "+message);
            String[] topicSep = topic.split("/");
            String firstLevelTopic = topicSep[0];
            String uuidDevice = topicSep[1];
            /* resource_data/[uuid_device]/[uuid_resource]			-> resource_json */
            if (firstLevelTopic.equals(EDGE_RESOURCES_DATA_SUBSCRIBE)) {
                sendCEP(uuidDevice, message);
            }
        });

        /*  Initializing the MQTT Broker for edge communication. */
        System.out.println("### Cloud MQTT Broker ###");
        ConnectionConfig connectionConfigSubCloud = new ConnectionConfig(CLOUD_HOST, CLOUD_PORT);
        connectionConfigSubCloud.setTopics(getCEPSubscribeTopics(uuidItself));
        Subscribe subscribeCloud = new Subscribe(connectionConfigSubCloud);
        subscribeCloud.subscribe((topic, message) -> {
//            System.out.println("## Received message from Cloud Computing: ");
            System.out.println("## Topic: "+topic+", Message: "+message);
            String[] topicSep = topic.split("/");
            String firstLevelTopic = topicSep[0];
            switch (firstLevelTopic) {
                case ADD_EPL -> addEPL(message);
                case REMOVE_EPL -> removeEPL(message);
            }
        });

        Thread publishThread = new Thread(new Runnable() {
            @Override
            public void run() {
                ConnectionConfig connectionConfigPubEdge = new ConnectionConfig(INNER_HOST, INNER_PORT);
                for (int i = 0; i < ResultQueue.size(); i++){
                    Resource resource = ResultQueue.getAndRemove();
                    if(resource != null){
                        connectionConfigPubEdge.setTopics(getCommandsToEdge(resource.getUuid_device()));
                        Publish publish = new Publish(connectionConfigPubEdge);
                        ufcitycore.models.Resource r = resource;
                        publish.publish(r.toJson());
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        });
    }

    private static void addEPL(String epl) {
        /* Adding EPL in CEP */
        // Use the EPAdministrator to create an EPL statement
        EPStatement statement = cepAdm.createEPL(epl);
        epls.add(epl);
        // Add a listener to the statement
        statement.addListener(new Listener());
        System.out.println(">> EPL added successfully!");
    }

    private static void removeEPL(String message) {
        epls.removeIf(s -> s.equals(message));
        cepAdm.destroyAllStatements();
        epls.forEach(s -> cepAdm.createEPL(s));
        System.out.println(">> EPL removed successfully!");
    }

    private static void sendCEP(String uuidDevice, String resourceJson) {
        try{
            /*  Convert message to Object */
            com.ufcity.cep.model.Resource resource = gson.fromJson(resourceJson, com.ufcity.cep.model.Resource.class);
            resource.setUuid_device(uuidDevice);
            /* Send events to the EPServiceProvider */
            cep.getEPRuntime().sendEvent(resource);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
