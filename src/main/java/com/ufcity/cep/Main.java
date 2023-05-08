package com.ufcity.cep;
import com.espertech.esper.client.*;

import com.google.gson.Gson;
import com.ufcity.cep.cep.Listener;
import com.ufcity.cep.models.Resource;
import com.ufcity.cep.mqtt.MQTTPublish;
import com.ufcity.cep.mqtt.MessageObserver;
import com.ufcity.cep.mqtt.MQTTSubscribe;
import com.ufcity.cep.mqtt.TOPICS;
import com.ufcity.cep.queue.ResultQueue;
import com.ufcity.cep.storage.Database;
import com.ufcity.cep.storage.MongoDB;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Main {

    public static String URI_INNER_MQTT = "tcp://mqtt:1883";
    static String URI_CLOUD_MQTT = "tcp://cloud:1883";
    static String MONGO_DB_HOST = "mongo";
    static String MONGO_DB_PORT = "27017";
    static String uuidItself = UUID.randomUUID().toString();

    /* GSON */
    static Gson gson = new Gson();

    private static final List<String> epls = new ArrayList<>();
    static Database database;
    public static void main(String[] args) throws IOException {

        if (args.length == 0)
            if (Menu(ReaderConfig()) != 0) return;

        /* Verificando o uuid_fog no MongoDB */
        uuidItself = database.setOrGetFogUUIDFog(uuidItself);

        /* EsperCEP */
        Configuration cepConfig = new Configuration();
        cepConfig.addEventType("Resource", Resource.class);

        // Create an instance of the EPServiceProvider
        EPServiceProvider cep = EPServiceProviderManager.getDefaultProvider(cepConfig);

        // Use the EPServiceProvider to create an instance of the EPAdministrator
        EPAdministrator cepAdm = cep.getEPAdministrator();

        /* MQTT Broker Inner */
        MQTTSubscribe mqttInner = new MQTTSubscribe(URI_INNER_MQTT, TOPICS.getSubscribeTopics());
        MQTTSubscribe mqttCloud = new MQTTSubscribe(URI_CLOUD_MQTT, TOPICS.getSubscribeTopics(uuidItself));

        mqttCloud.subscribe((new MessageObserver() {
            @Override
            public void receiveMessage(String topic, String message) {
                String[] t = topic.split("/");
                switch (t[0]) {
                    case TOPICS.ADD_EPL -> addEPL(message);
                    case TOPICS.REMOVE_EPL -> removeEPL(message);
                }
            }
            private void addEPL(String epl) {
                /* Adding EPL in CEP */
                // Use the EPAdministrator to create an EPL statement
                EPStatement statement = cepAdm.createEPL(epl);
                epls.add(epl);
                // Add a listener to the statement
                statement.addListener(new Listener());
                System.out.println(">> EPL added successfully!");
            }

            private void removeEPL(String message) {
                epls.removeIf(s -> s.equals(message));
                cepAdm.destroyAllStatements();
                epls.forEach(s -> cepAdm.createEPL(s));
                System.out.println(">> EPL removed successfully!");
            }
        }));
        mqttInner.subscribe(new MessageObserver() {
            @Override
            public void receiveMessage(String topic, String message) {
                String[] t = topic.split("/");
                switch (t[0]) {
                    case TOPICS.RESOURCE_DATA -> sendCEP(message);
                }
            }

            private void sendCEP(String resourceJson) {
                try{
                    /*  Convert message to Object */
                    Resource resource = gson.fromJson(resourceJson, Resource.class);
                    /* Send events to the EPServiceProvider */
                    cep.getEPRuntime().sendEvent(resource);
                }catch (Exception e){
                    e.printStackTrace();
                }
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
                        MQTTPublish.publish(uuid_device, resource.toJson());
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

//        /* MQTT Broker Cloud */
//        MQTTSubscribe mqttCloud = new MQTTSubscribe(URICloud, "");
//        mqttCloud.subscribe(new MessageObserver() {
//            @Override
//            public void receiveMessage(String topic, String message) {
//                switch (topic){
//                    case
//                }
//
//            }
//        });

    }

    public static int Menu(String[] params){

        String inner_a = null, inner_p= null, cloud_a= null, cloud_p= null;

        int qtArgs = params.length;
        if(qtArgs == 0) {
            System.out.println("Invalid parameters. Type -h (or --help) for help.");
            return 1;
        }
        if(qtArgs == 1){
            if(params[0].equals("-h") || params[0].equals("--help")){
                System.out.println("-fa \t--fog-address         \tAddress to fog computing.");
                System.out.println("-ca \t--cloud-address       \tAddress to cloud computing.");
                System.out.println("-fp \t--fog-port            \tPort to edge computing.");
                System.out.println("-cp \t--cloud-port          \tPort to cloud computing.");
                System.out.println("-da \t--database-address    \tAddress to database.");
                System.out.println("-dp \t--database-port       \tPort to database");
                System.out.println("-v  \t--version             \tVersion of this system.");
            } else if (params[0].equals("-v") || params[0].equals("--version")) {
                System.out.println("Version: 0.1.0 March 2023.");
            } else {
                System.out.println("Invalid parameters. Type -h (or --help) for help.");
            }
            return 1;
        }
        if(qtArgs % 2 != 0){
            System.out.println("Invalid parameters. Type -h (or --help) for help.");
            return 1;
        }else{
            int i = 0;
            while (i < qtArgs){
                switch (params[i]) {
                    case "-fa", "--fog-address" -> inner_a = params[i + 1];
                    case "-ca", "--cloud-address" -> cloud_a = params[i + 1];
                    case "-fp", "--fog-port" -> inner_p = params[i + 1];
                    case "-cp", "--cloud-port" -> cloud_p = params[i + 1];
                    case "-da", "--database-address" -> MONGO_DB_HOST = params[i + 1];
                    case "-dp", "--database-port" -> MONGO_DB_PORT = params[i + 1];
                }
                i = i + 2;
            }
            if(MONGO_DB_HOST != null) {
                System.out.println(">> Connecting database! Database address: "+MONGO_DB_HOST+":"+MONGO_DB_PORT);
                database = new MongoDB(MONGO_DB_HOST, MONGO_DB_PORT);
            }
            if((inner_a != null) && (inner_p != null)){
                URI_INNER_MQTT = "tcp://"+inner_a+":"+inner_p;
            }
            if((cloud_a != null) && (cloud_p != null)){
                URI_CLOUD_MQTT = "tcp://"+cloud_a+":"+cloud_p;
            }
            return 0;
        }
    }

    public static String[] ReaderConfig() throws IOException {
        String path = new File("ufcity-cep.config").getAbsolutePath();
//        System.out.println(path);
        BufferedReader buffRead = new BufferedReader(new FileReader(path));
        List<String> args = new ArrayList<>();
        String line = "";
        while (true) {
            line = buffRead.readLine();
            if (line != null) {
                String[] l = line.split(":");
                args.add(l[0].trim());
                args.add(l[1].trim());
//                System.out.println(l[0] + " ## " + l[1]);
            } else {
                buffRead.close();
                System.out.println(Arrays.toString(args.toArray(new String[0])));
                return args.toArray(new String[0]);
            }
        }
    }

}