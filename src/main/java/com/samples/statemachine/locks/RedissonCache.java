package com.samples.statemachine.locks;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

//@Component
@RequiredArgsConstructor
public class RedissonCache<K, V> {

    private final RedissonClient client;

    public boolean getLock(String id){
        RLock lock = client.getFairLock(id);
        return lock.tryLock();
    }

    public void releaseLock(String id){
        RLock lock = client.getFairLock(id);
        lock.unlock();
    }

    public boolean putInCache(String name, K id, V value){
        RMap<K, V> map = client.getMap(name);
        var result = map.put(id, value);
        return true;
    }

    public V readFromCache(String name, K id){
        RMap<K,V> map = client.getMap(name);
        return map.get(id);
    }

}
