package com.samples.statemachine.repository;

import com.samples.statemachine.enums.Events;
import com.samples.statemachine.enums.States;
import com.samples.statemachine.locks.RedissonCache;
import lombok.RequiredArgsConstructor;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;


import java.util.HashMap;
import java.util.UUID;

@RequiredArgsConstructor
public class HashMapPersister implements StateMachinePersist<States, Events, UUID> {

    private final RedissonCache<UUID, StateMachineContext<States, Events>> cache;

    private final HashMap<UUID, StateMachineContext<States, Events>> storage = new HashMap<>();


    @Override
    public void write(StateMachineContext<States, Events> context, UUID contextObj) throws Exception {
        cache.putInCache("statemachine", contextObj, context);
    }

    @Override
    public StateMachineContext<States, Events> read(UUID contextObj) throws Exception {
        return cache.readFromCache("statemachine", contextObj );
    }
}
