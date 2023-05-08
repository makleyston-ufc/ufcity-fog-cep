package com.ufcity.cep.cep;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.ufcity.cep.queue.ResultQueue;

import java.util.HashMap;
import java.util.Map;

public class Listener implements UpdateListener {

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents) {
        if (newEvents != null) {
            for (EventBean event : newEvents) {
                String uuid_device = (String) event.get("uuid_device");
                String uuid_resource = (String) event.get("uuid_resource");
                String uuid_service = (String) event.get("uuid_service");
                String action = (String) event.get("action");
                Map<String, String> resultQuery = new HashMap<>();
                resultQuery.put("uuid_device", uuid_device);
                resultQuery.put("uuid_resource", uuid_resource);
                resultQuery.put("uuid_service", uuid_service);
                resultQuery.put("action", action);
                ResultQueue.add(resultQuery);
            }
        }
    }
}
