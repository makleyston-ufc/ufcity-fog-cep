package com.ufcity.cep.models;

import java.util.ArrayList;
import java.util.Map;

public class Service {
    public String uuid_service;
    public ArrayList<Data> data = new ArrayList<Data>();

    public String getUuid_service() {
        return uuid_service;
    }

    public ArrayList<Data> getData() {
        return data;
    }

    public void setUuid_service(String uuid_service) {
        this.uuid_service = uuid_service;
    }




}
