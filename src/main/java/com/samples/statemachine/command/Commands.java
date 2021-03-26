package com.samples.statemachine.command;

import com.samples.statemachine.enums.Events;
import com.samples.statemachine.enums.States;
import com.samples.statemachine.services.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.io.Console;
import java.io.IOException;
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

    private static final Console console = System.console();

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

    @ShellMethod(value = "Send an event to the state machine", key = "delete-sm", prefix = "-")
    public String deleteStateMachine(@ShellOption({"-n","--name"})String name){
        try{
            UUID uuid = getUUIDFromName(name);
            return eventService.deleteMachine(uuid)?"State machine deleted":"State machine not present";
        }catch(Exception ex){
            log.error("An exception has occurred");
            throw ex;
        }
    }

    @ShellMethod(value = "Show the defined state machine", key = "show-sm")
    public void showStateMachine() throws IOException {
        print("The defined state machine");
        StringBuilder theString = new StringBuilder();

        Scanner scanner = new Scanner(configFile.getInputStream());

        theString.append(scanner.nextLine());
        while (scanner.hasNextLine()) {
            theString.append("\n").append(scanner.nextLine());
        }
        print(theString.toString());
    }

    @ShellMethod(value = "Send an event to the state machine", key = "send-event", prefix = "-")
    public String sendEvent(@ShellOption({"-sm", "--state-machine"}) @NotNull String stateMachine, @ShellOption({"-e", "--event"}) @NotNull Events event, @ShellOption(value = {"-t 10", "--sleep-time"}, defaultValue = "0") int sleepTime) {
        print("Processing event " + event + " for state machine " + stateMachine);
        String currentState = eventService.handleEvent(event, getUUIDFromName(stateMachine), sleepTime);
        return "Current state machine status-->"+currentState;
    }

    @ShellMethod(value="Get the current status of the state machine", key = "get-status", prefix="-")
    public String getStatus(@ShellOption({"-sm", "--state-machine"})@NotNull String stateMachine){
        try{
            return eventService.getCurrentState(getUUIDFromName(stateMachine));
        }catch(Exception ex){

        }
        return "An error occurred while fetching the state";
    }

    @ShellMethod(value = "List all events in the state machine", key = "list-events")
    public void listEvents() {
        String eventsList = Stream.of(Events.values()).map(Events::name).collect(Collectors.joining(","));
        print(eventsList);
    }

    @ShellMethod(value = "List all states in the state machine", key = "list-states")
    public void listStates() {
        String statesList = Stream.of(States.values()).map(States::name).collect(Collectors.joining(","));
        print(statesList);
    }

    public static void print(String message){
       // if(console==null){
            System.out.println(message);
       // }else{
            console.writer().print(message);
       // }
    }
}
