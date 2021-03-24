package com.samples.statemachine.services;

import com.samples.statemachine.enums.Events;
import com.samples.statemachine.enums.States;
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

    //private final RedissonCache cache;

    private final StateMachinePersister<States, Events, String> persister;
    //private final StateMachineRuntimePersister<States, Events, String> persister;

    private static final boolean useLock = false;

    public void handleEvent(Events event, UUID serverId) {
         StateMachine<States, Events> stateMachine = stateMachineFactory.getStateMachine();
        try {
            log.info("Sending event {} for State machine", event);
            persister.restore(stateMachine, serverId.toString());
            sendEvent(stateMachine, event);
            persister.persist(stateMachine, serverId.toString());
        }catch(Exception ex){
           log.error("An error occurred while handling the event", ex);
        }
    }

    void sendEvent(StateMachine<States, Events> stateMachine, Events event, CountDownLatch latch, CountDownLatch resumeLatch) {
        try {
            if (latch != null) {
                latch.await();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            if (useLock) {
                //cache.getLock("id");
            }
            System.out.println("Sending " + event + " at " + LocalDateTime.now());
            stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(event).build())).subscribe();
        } finally {
            if (useLock) {
                //cache.releaseLock("id");
            }
        }
        if (resumeLatch != null) {
            resumeLatch.countDown();
        }
    }

    void sendEvent(StateMachine<States, Events> stateMachine, Events event, boolean toSleep, CountDownLatch latch, CountDownLatch resumeLatch) {
        if (toSleep) {
            Thread t = new Thread(() -> {
                sendEvent(stateMachine, event, latch, resumeLatch);
            });
            t.start();
        } else {
            sendEvent(stateMachine,event, latch, resumeLatch);
        }

    }


    void sendEvent(StateMachine<States, Events> stateMachine, Events events) {
        sendEvent(stateMachine, events, false, null, null);
    }

}