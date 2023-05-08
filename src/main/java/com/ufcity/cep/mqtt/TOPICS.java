package com.ufcity.cep.mqtt;

import java.util.ArrayList;
import java.util.List;

public class TOPICS {
    public static final String ADD_EPL = "add_epl";
    public static final String REMOVE_EPL = "remove_epl";
    public static final String RESOURCE_DATA = "resource_data";
    public static final String COMMANDS_TO_EDGE = "commands_fog_to_edge";

    public static String[] getSubscribeTopics(){
        List<String> topics = new ArrayList<>();
        topics.add(RESOURCE_DATA + "/+" + "/+"); //resource_data/[uuid_device]/[uuid_resource]
        return topics.toArray(new String[0]);
    }

    public static String[] getSubscribeTopics(String uuidFog){
        List<String> topics = new ArrayList<>();
        topics.add(ADD_EPL + "/" + uuidFog);
        topics.add(REMOVE_EPL + "/" + uuidFog);
        return topics.toArray(new String[0]);
    }

}
