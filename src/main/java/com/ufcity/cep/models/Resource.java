package com.ufcity.cep.models;

import com.google.gson.Gson;

import java.util.ArrayList;

public class Resource {

    private Location location;
    private String uuid_resource;
    ArrayList < Service > services = new ArrayList< Service >();

    public ArrayList< Service > getServices(){
        return this.services;
    }

    public Location getLocation() {
        return location;
    }

    public String getUuid_resource() {
        return uuid_resource;
    }

    public void setLocation(Location locationObject) {
        this.location = locationObject;
    }

    public void setUuid_resource(String uuid_resource) {
        this.uuid_resource = uuid_resource;
    }

    public String toJson(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
