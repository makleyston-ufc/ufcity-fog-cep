package com.ufcity.cep.storage;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.ufcity.cep.models.Resource;
import org.bson.Document;
import org.bson.conversions.Bson;
import static com.mongodb.client.model.Filters.*;

import java.util.HashMap;
import java.util.Map;

public class MongoDB extends Database{

    MongoClient mongoClient;
    MongoDatabase database;
    MongoCollection<Document> collectionDevices;

    public MongoDB(String host, String port) {
        super(host, port);
        mongoClient = MongoClients.create("mongodb://root:example@"+host+":"+port);
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
