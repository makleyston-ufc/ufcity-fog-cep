package com.ufcity.cep.queue;


import java.util.LinkedList;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class ResultQueue {

    private static final Semaphore semaphore = new Semaphore(1);
    private static final Queue<Map<String, String>> queue = new LinkedList<>();

    public static void add(Map<String, String> resultQuery){
        try{
            semaphore.acquire();
            queue.add(resultQuery);
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

    public static Map<String, String> getAndRemove(){
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
