package com.ufcity.cep.cep;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.ufcity.cep.model.Resource;
import com.ufcity.cep.queue.ResultQueue;

import java.util.HashMap;
import java.util.Map;

public class Listener implements UpdateListener {

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents) {
        if (newEvents != null) {
            for (EventBean event : newEvents) {
                ResultQueue.add((Resource) event);
            }
        }
    }
}
