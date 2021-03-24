package com.samples.statemachine.services;

import com.samples.statemachine.enums.Events;
import com.samples.statemachine.enums.States;
import com.samples.statemachine.locks.RedissonCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {



    private final StateMachineFactory<States, Events> stateMachineFactory;

    private final RedissonCache cache;

    private final StateMachinePersister<States, Events, String> persister;
    //private final StateMachineRuntimePersister<States, Events, String> persister;

    private static final boolean useLock = true;

    public void handleEvent(Events event, UUID serverId) {
        StateMachine<States, Events> stateMachine = stateMachineFactory.getStateMachine(serverId);
        try {
            log.info("Sending event {} for State machine", event);
            persister.restore(stateMachine, serverId.toString());
            sendEvent(stateMachine, event);
            persister.persist(stateMachine, serverId.toString());
        }catch(Exception ex){
           log.error("An error occurred while handling the event", ex);
        }
    }

    void sendEvent(StateMachine<States, Events> stateMachine, Events event) {
        try {
            if (useLock) {
            while(!cache.getLock(stateMachine.getUuid().toString())){
                    Thread.sleep(2000);
                    //do nothing.. basically wait for the lock
                }
            }
            stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(event).build())).subscribe();
        } catch (InterruptedException e) {
            log.error("Exception occurred.. ");
        } finally {
            if (useLock) {
                cache.releaseLock(stateMachine.getUuid().toString());
            }
        }
    }


}