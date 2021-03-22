package com.samples.statemachine;

import com.samples.statemachine.enums.Events;
import com.samples.statemachine.enums.States;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import reactor.core.publisher.Mono;

@SpringBootApplication
@RequiredArgsConstructor
public class StateMachineApplication implements CommandLineRunner {

    private final StateMachine<States, Events> stateMachine;

    public static void main(String[] args) {
        SpringApplication.run(StateMachineApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        System.out.println("Sending E1");
        stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(Events.E1).build())).subscribe();
        System.out.println("Sending E2");
        stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(Events.E2).build())).subscribe();
        System.out.println("Sending EF");
        stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(Events.EF).build())).subscribe();
        System.out.println("Sending E31");
        stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(Events.E31).build())).subscribe();
        System.out.println("Sending E41");
        stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(Events.E41).build())).subscribe();
        System.out.println("Sending E42");
        stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(Events.E42).build())).subscribe();
        System.out.println("Sending E32");
        stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(Events.E32).build())).subscribe();

    }
}
