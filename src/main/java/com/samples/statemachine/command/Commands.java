package com.samples.statemachine.command;

import com.samples.statemachine.enums.Events;
import com.samples.statemachine.enums.States;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import com.samples.statemachine.services.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Scanner;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ShellComponent
@RequiredArgsConstructor
@Slf4j
public class Commands {

    private final EventService eventService;

    @Value("classpath:/sm.txt")
    private Resource configFile;

    private UUID getUUIDFromName(String name) {
        return StringUtils.hasText(name) ? UUID.nameUUIDFromBytes(name.getBytes()) : UUID.randomUUID();
    }

    @ShellMethod(value = "Create a state machine", key = "create-new")
    public String createStateMachine(@ShellOption({"-n", "--name"}) String name) throws Exception {
        try {
            UUID uuid = getUUIDFromName(name);
            eventService.createStateMachine(uuid);
            return "A new state machine with the id " + uuid + " has been created";
        } catch (Exception ex) {
            log.error("An exception has occurred");
            throw ex;
        }
    }

    @ShellMethod(value = "Send an event to the state machine", key = "send-event", prefix = "-")
    public String sendEvent(@ShellOption({"-sm", "--state-machine"}) @NotNull String stateMachine, @ShellOption({"-e", "--event"}) @NotNull Events event, @ShellOption(value = {"-t 10", "--sleep-time"}, defaultValue = "0") int sleepTime) {
        System.out.println("Processing event " + event + " for state machine" + stateMachine);
        String currentState = eventService.handleEvent(event, getUUIDFromName(stateMachine), sleepTime);
        return "Current state machine status--"+currentState;
    }

    @ShellMethod(value = "Show the defined state machine", key = "show-sm")
    public void showStateMachine() throws IOException {
        System.out.println("The defined state machine");
        StringBuilder theString = new StringBuilder();

        Scanner scanner = new Scanner(configFile.getInputStream());

        theString.append(scanner.nextLine());
        while (scanner.hasNextLine()) {
            theString.append("\n").append(scanner.nextLine());
        }
        System.out.println(theString);
    }

    @ShellMethod(value = "List all events in the state machine", key = "list-events")
    public void listEvents() {
        String eventsList = Stream.of(Events.values()).map(Events::name).collect(Collectors.joining(","));
        System.out.println(eventsList);
    }

    @ShellMethod(value = "List all states in the state machine", key = "list-states")
    public void listStates() {
        String statesList = Stream.of(States.values()).map(States::name).collect(Collectors.joining(","));
        System.out.println(statesList);
    }

}
