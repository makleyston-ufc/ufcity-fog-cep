package com.ufcity.cep;
import com.espertech.esper.client.*;

import com.google.gson.Gson;
import com.ufcity.cep.cep.Listener;
import com.ufcity.cep.queue.ResultQueue;
import com.ufcity.cep.storage.Database;
import com.ufcity.cep.storage.MongoDB;
import org.eclipse.paho.client.mqttv3.MqttException;
import ufcitycore.models.Resource;
import ufcitycore.mqtt.ConnectionConfig;
import ufcitycore.mqtt.Publish;
import ufcitycore.mqtt.Subscribe;
import java.io.IOException;
import java.util.*;

import static com.ufcity.cep.Menu.ReaderConfig;
import static ufcitycore.mqtt.ConnectionData.*;

public class Main {
    static String MONGO_DB_HOST;
    static String MONGO_DB_PORT;
    static String uuidItself = UUID.randomUUID().toString();
    /* GSON */
    static Gson gson = new Gson();
    private static final List<String> epls = new ArrayList<>();
    static Database database;
    static final String version = "0.1";
    static EPServiceProvider cep;
    static EPAdministrator cepAdm;

    public static void main(String[] args) throws IOException, MqttException {

        if (args.length == 0)
            if (Menu.check(ReaderConfig()) != 0) return;

        /* Check the uuid_fog on MongoDB */
        uuidItself = database.setOrGetFogUUIDFog(uuidItself);

        /* EsperCEP */
        Configuration cepConfig = new Configuration();
        cepConfig.addEventType("Resource", Resource.class);

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
            System.out.println("## Received message from Edge Computing: ");
            System.out.println("## Topic: "+topic+", Message: "+message);
            String[] topicSep = topic.split("/");
            String firstLevelTopic = topicSep[0];
            switch (firstLevelTopic) {
                /* device/[uuid_device] -> json */
                case EDGE_RESOURCES_DATA_SUBSCRIBE -> sendCEP(message);
            }
        });

        /*  Initializing the MQTT Broker for edge communication. */
        System.out.println("### Cloud MQTT Broker ###");
        ConnectionConfig connectionConfigSubCloud = new ConnectionConfig(HOST_CLOUD, PORT_CLOUD);
        connectionConfigSubCloud.setTopics(getCEPSubscribeTopics(uuidItself));
        Subscribe subscribeCloud = new Subscribe(connectionConfigSubCloud);
        subscribeCloud.subscribe((topic, message) -> {
            System.out.println("## Received message from Cloud Computing: ");
            System.out.println("## Topic: "+topic+", Message: "+message);
            String[] topicSep = topic.split("/");
            String firstLevelTopic = topicSep[0];
            switch (firstLevelTopic) {
                case ADD_EPL -> addEPL(message);
                case REMOVE_EPL -> removeEPL(message);
            }
        });

        Thread publishThread = new Thread(new Runnable() {
            final Database mongoDB = new MongoDB(MONGO_DB_HOST, MONGO_DB_PORT);
            @Override
            public void run() {
                for (int i = 0; i < ResultQueue.size(); i++){
                    Map<String, String> r = ResultQueue.getAndRemove();
                    if(r != null){
                        Map<String, Resource> result = findResourceInStorage(r.get("uuid_device"), r.get("uuid_resource"), r.get("uuid_service"), r.get("action"));
                        String uuid_device = (String) result.keySet().toArray()[0];
                        Resource resource = result.get(uuid_device);

                        ConnectionConfig connectionConfigPubEdge = new ConnectionConfig(INNER_HOST, INNER_PORT);
                        connectionConfigPubEdge.setTopics(getCommandsToEdge(uuid_device));
                        Publish publish = new Publish(connectionConfigPubEdge);

                        publish.publish(resource.toJson());
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            private Map<String, Resource> findResourceInStorage(String uuidDevice, String uuidResource, String uuidService, String action) {
                return mongoDB.find(uuidDevice, uuidResource, uuidService);
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

    private static void sendCEP(String resourceJson) {
        try{
            /*  Convert message to Object */
            Resource resource = gson.fromJson(resourceJson, Resource.class);
            /* Send events to the EPServiceProvider */
            cep.getEPRuntime().sendEvent(resource);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
