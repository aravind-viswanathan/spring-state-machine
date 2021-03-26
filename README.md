# spring-state-machine

The project has a pre-defined state machine with the state diagram as below. 

This project is as a PoC to have a distributed state machine using Redis as a backing store to store the StateMachineContext. 

The Distributed State Machine offerred by Spring uses Zookeeper and is currently not production ready and this project is a stop-gap for it until such timeZK solution is deemed production ready. 

Dependencies

* Redis running on Port 6379 in localhost. (This can be configured in redisson.yaml under resources folder)


Building from source

* Execute Gradle Build
* Execute the jar by using the command - `java -jar <jar name>` The jar is under build/libs folder.

The project uses Spring Shell and supports the following commands

* create-new _string_ - Creates a new state machine with the provided name. 
* get-status _string_ - Gets the current status/state of the state machine.
* send-event _statemachine_ _event_ - Sends the event to the state machine indicated by the name. 
* list-events - Lists all the events supported by the state machine. 
* list-states - Lists all the states supported by the state machine. 
* delete-sm _statemachine_ - Delete the state machine