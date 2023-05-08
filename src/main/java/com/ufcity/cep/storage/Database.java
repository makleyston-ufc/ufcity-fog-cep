package com.ufcity.cep.storage;

import com.ufcity.cep.models.Resource;

import java.util.Map;

public abstract class Database {

    private String host;
    private String port;

    public Database(String host, String port) {
        this.host = host;
        this.port = port;
    }

    /* uuid_device & Resource */
    public abstract Map<String, Resource> find(String uuid_device, String uuid_resource, String uuid_service);
    public abstract String setOrGetFogUUIDFog(String uuid_fog);

}
