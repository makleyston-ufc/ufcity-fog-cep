package com.ufcity.cep.storage;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;
import ufcitycore.models.Resource;

import static com.mongodb.client.model.Filters.*;

import java.util.HashMap;
import java.util.Map;

public class MongoDB extends Database{

    MongoClient mongoClient;
    MongoDatabase database;
    MongoCollection<Document> collectionDevices;

    public MongoDB(String host, String port, String username, String password) {
        super(host, port, username, password);
        mongoClient = MongoClients.create("mongodb://"+username+":"+password+"@"+host+":"+port);
        database = mongoClient.getDatabase("ufcity");
        collectionDevices = database.getCollection("devices");
    }

    @Override
    public Map<String, Resource> find(String uuid_device, String uuid_resource, String uuid_service) {
        Bson filter = and(eq("uuid_device", uuid_device), eq("resources.uuid_resource", uuid_resource),
                eq("resources.services.uuid_service", uuid_service));
        Document docDev = collectionDevices.find(filter).first();
        if(docDev != null){
            Resource resource = docDev.get("resource", Resource.class);
            Map<String, Resource> result = new HashMap<>();
            result.put(uuid_device, resource);
            return result;
        }
        return null;
    }

    @Override
    public String setOrGetFogUUIDFog(String uuid_fog) {
        System.out.println(">> Verifying if this node is registered on Storage DB.");
        Document docFog = database.getCollection("fog_computing").find().first();
        if(docFog != null){
            return docFog.getString("uuid_fog");
        }else {
            Document document = new Document();
            document.append("uuid_fog", uuid_fog);
            database.getCollection("fog_computing").insertOne(document);
            return uuid_fog;
        }
    }


}
