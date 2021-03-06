package com.samples.statemachine.services;

import com.samples.statemachine.command.Commands;
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

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final StateMachineFactory<States, Events> stateMachineFactory;

    private final RedissonCache<String, Object> cache;

    private final StateMachinePersister<States, Events, String> persister;

    private static final boolean useLock = true;

    public String getCurrentState(UUID serverId) throws Exception{
        StateMachine<States, Events> stateMachine = stateMachineFactory.getStateMachine(serverId);
        try{
            persister.restore(stateMachine, serverId.toString());
            return stateMachine.getState().getIds().stream().map(States::name).collect(Collectors.joining(","));
        }catch(Exception ex){
            log.error("An error has occurred");
            throw ex;
        }

    }

    public String handleEvent(Events event, UUID serverId, int sleep) {
        StateMachine<States, Events> stateMachine = stateMachineFactory.getStateMachine(serverId);
        try {
            log.info("Sending event {} for State machine", event);
            if (useLock) {
                while(!cache.getLock(stateMachine.getUuid().toString())){
                    //wait for lock
                }
            }
            persister.restore(stateMachine, serverId.toString());
            sendEvent(stateMachine, event, sleep);
            persister.persist(stateMachine, serverId.toString());
        }catch(Exception ex){
           log.error("An error occurred while handling the event", ex);
        }finally {
            if (useLock) {
                cache.releaseLock(stateMachine.getUuid().toString());
            }
        }
        return stateMachine.getState().getIds().stream().map(States::name).collect(Collectors.joining(","));
    }

    public boolean deleteMachine(UUID serverId){
        return cache.deleteFromCache(serverId.toString());
    }

    void sendEvent(StateMachine<States, Events> stateMachine, Events event, int sleep) {
        try {
            Thread.sleep(sleep*1000);
            stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(event).build())).doOnNext(s->{
                Commands.print("The transition has been "+s.getResultType().toString());
            }).subscribe();

        } catch (InterruptedException e) {
            log.error("Exception occurred.. ");
        } catch (Exception ex){
            log.error("Exception occurred...", ex);
        }
    }

    public boolean createStateMachine(UUID uuid) throws Exception{
        var stateMachine = stateMachineFactory.getStateMachine(uuid);
        persister.persist(stateMachine, uuid.toString());
        return true;
    }

}