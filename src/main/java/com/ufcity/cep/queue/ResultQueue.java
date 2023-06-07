package com.ufcity.cep.queue;


import com.ufcity.cep.model.Resource;

import java.util.LinkedList;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class ResultQueue {

    private static final Semaphore semaphore = new Semaphore(1);
    private static final Queue<Resource> queue = new LinkedList<>();

    public static void add(Resource resource){
        try{
            semaphore.acquire();
            queue.add(resource);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            semaphore.release();
        }
    }

    public static void remove(){
        try{
            semaphore.acquire();
            queue.remove();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            semaphore.release();
        }
    }

    public static Resource getAndRemove(){
        try{
            semaphore.acquire();
            return queue.poll();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            semaphore.release();
        }
    }

    public static int size(){
        return queue.size();
    }


}
