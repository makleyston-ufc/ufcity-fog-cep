package com.ufcity.cep.storage;

import ufcitycore.models.Resource;
import java.util.Map;

public abstract class Database {

    private String host;
    private String port;
    private String username;
    private String password;

    public Database(String host, String port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    /* uuid_device & Resource */
    public abstract Map<String, Resource> find(String uuid_device, String uuid_resource, String uuid_service);
    public abstract String setOrGetFogUUIDFog(String uuid_fog);

}
