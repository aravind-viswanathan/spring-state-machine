package com.samples.statemachine;

import com.samples.statemachine.enums.Events;
import com.samples.statemachine.services.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.UUID;

@SpringBootApplication
@RequiredArgsConstructor
public class StateMachineApplication implements CommandLineRunner {

    private final EventService eventService;

    public static void main(String[] args) {
        var context = SpringApplication.run(StateMachineApplication.class, args);
        context.close();
    }

    @Override
    public void run(String... args) throws Exception {

        UUID serverID = UUID.randomUUID();
        eventService.handleEvent(Events.E1, serverID);
        eventService.handleEvent(Events.E2,serverID);
        eventService.handleEvent(Events.EF,serverID);
        eventService.handleEvent(Events.E31,serverID);
        eventService.handleEvent(Events.E41, serverID);
        eventService.handleEvent(Events.E42,serverID);
        eventService.handleEvent(Events.E32,serverID);
    }
}
