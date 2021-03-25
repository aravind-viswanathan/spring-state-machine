package com.samples.statemachine.services;

import com.samples.statemachine.enums.Events;
import com.samples.statemachine.enums.States;
import com.samples.statemachine.locks.RedissonCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.ObjectStateMachine;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {



    private final StateMachineFactory<States, Events> stateMachineFactory;

    private final RedissonCache cache;

    private final StateMachinePersister<States, Events, String> persister;

    private static final boolean useLock = true;

    public String handleEvent(Events event, UUID serverId, int sleep) {
        StateMachine<States, Events> stateMachine = stateMachineFactory.getStateMachine(serverId);
        try {
            log.info("Sending event {} for State machine", event);
            persister.restore(stateMachine, serverId.toString());
            sendEvent(stateMachine, event, sleep);
            persister.persist(stateMachine, serverId.toString());
        }catch(Exception ex){
           log.error("An error occurred while handling the event", ex);
        }
        return stateMachine.getState().getIds().stream().map(States::name).collect(Collectors.joining(","));
    }

    void sendEvent(StateMachine<States, Events> stateMachine, Events event, int sleep) {
        try {
            if (useLock) {
            while(!cache.getLock(stateMachine.getUuid().toString())){
                    Thread.sleep(2000);
                    //do nothing.. basically wait for the lock
                }
            }
            Thread.sleep(sleep*1000);
            stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(event).build())).subscribe();

        } catch (InterruptedException e) {
            log.error("Exception occurred.. ");
        } catch (Exception ex){
            log.error("Exception occurred...", ex);
        } finally {
            if (useLock) {
                cache.releaseLock(stateMachine.getUuid().toString());
            }
        }
    }

    public boolean createStateMachine(UUID uuid) throws Exception{
        var stateMachine = stateMachineFactory.getStateMachine(uuid);
        persister.persist(stateMachine, uuid.toString());
        return true;
    }


}