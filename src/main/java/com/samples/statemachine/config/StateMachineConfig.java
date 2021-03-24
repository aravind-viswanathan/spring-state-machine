package com.samples.statemachine.config;

import com.samples.statemachine.enums.Events;
import com.samples.statemachine.enums.States;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;
import org.springframework.statemachine.state.State;

import java.util.Set;

@Configuration
@EnableStateMachineFactory
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<States, Events> {

    @Autowired
    private StateMachineRuntimePersister<States, Events, String> stateMachineRuntimePersister;


    @Override
    public void configure(StateMachineConfigurationConfigurer<States, Events> config) throws Exception {
        config
                .withPersistence()
                    .runtimePersister(stateMachineRuntimePersister);
        config.withConfiguration()
                    .autoStartup(true)
                    .listener(listener());
    }

    @Override
    public void configure(StateMachineStateConfigurer<States, Events> states)
            throws Exception {

        states.withStates()
                .initial(States.INITIAL)
                .fork(States.FORK)
                .states(Set.of(States.S1, States.S2))
                .end(States.FINAL)
                .join(States.JOIN)
                .and()
                .withStates()
                .parent(States.FORK)
                .initial(States.S31)
                .state(States.S32)
                .end(States.S33)
                .and()
                .withStates()
                .parent(States.FORK)
                .initial(States.S41)
                .state(States.S42)
                .end(States.S43);

    }

    @Override
    public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
            throws Exception {
        transitions
                .withExternal().source(States.INITIAL).target(States.S1).event(Events.E1)
                .and()
                .withExternal().source(States.S1).target(States.S2).event(Events.E2)
                .and()
                .withExternal().source(States.S2).target(States.FORK).event(Events.EF)
                .and()
                .withFork().source(States.FORK).target(States.S31).target(States.S41)
                .and()
                .withJoin().source(States.S33).source(States.S43).target(States.JOIN)
                .and()
                .withExternal().source(States.S31).target(States.S32).event(Events.E31)
                .and()
                .withExternal().source(States.S32).target(States.S33).event(Events.E32)
                .and()
                .withExternal().source(States.S41).target(States.S42).event(Events.E41)
                .and()
                .withExternal().source(States.S42).target(States.S43).event(Events.E42)
                .and()
                .withExternal().source(States.JOIN).target(States.FINAL);
    }

    @Bean
    public StateMachineListener<States, Events> listener() {
        return new StateMachineListenerAdapter<States, Events>() {
            @Override
            public void stateChanged(State<States, Events> from, State<States, Events> to) {
                System.out.println("State change to " + to.getId());
            }
        };
    }




}
