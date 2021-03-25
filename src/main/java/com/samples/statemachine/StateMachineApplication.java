package com.samples.statemachine;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class StateMachineApplication  {

    public static void main(String[] args) {
        var context = SpringApplication.run(StateMachineApplication.class, args);
        context.close();
    }

//        UUID serverID = UUID.randomUUID();
//        eventService.handleEvent(Events.E1, serverID);
//        eventService.handleEvent(Events.E2,serverID);
//        eventService.handleEvent(Events.EF,serverID);
//        eventService.handleEvent(Events.E31,serverID);
//        eventService.handleEvent(Events.E41, serverID);
//        eventService.handleEvent(Events.E42,serverID);
//        eventService.handleEvent(Events.E32,serverID);


//    }
}
