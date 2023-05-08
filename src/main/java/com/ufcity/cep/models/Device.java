package com.ufcity.cep.models;

import java.util.ArrayList;
import java.util.List;

public class Device {
    private String uuid_device;
    Location location;
    private List<Resource> resources = new ArrayList<>();

    public String getUuid_device() {
        return uuid_device;
    }

    public Location getLocation() {
        return location;
    }

    public void setUuid_device(String uuid_device) {
        this.uuid_device = uuid_device;
    }

    public void setLocation(Location locationObject) {
        this.location = locationObject;
    }

    public void addResource(Resource resource){
        if(getResourceByUUID(resource.getUuid_resource()) == null)
            this.resources.add(resource);
    }

    public void removeResource(Resource resource){
        this.resources.removeIf(r -> r.getUuid_resource().equals(resource.getUuid_resource()));
    }

    public void removeResourceByUUID(String uuid_resource){
        this.resources.removeIf(r -> r.getUuid_resource().equals(uuid_resource));
    }

    public Resource getResourceByUUID(String uuid){
        return this.resources.stream().filter(r -> r.getUuid_resource().equals(uuid)).findFirst().orElse(null);
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }
}
