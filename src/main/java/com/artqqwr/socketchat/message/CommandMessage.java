package com.artqqwr.socketchat.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("Command")
@JsonIgnoreProperties({"type"})
public class CommandMessage extends Message {
    private String command;
    private String password;   
    private String argument; 

    public CommandMessage(String sender, String command, String password, String argument) {
        super(sender);
        this.command = command;
        this.password = password;
        this.argument = argument;
    }

    public CommandMessage() {
        super();
    }

    public String getCommand() {
        return command;
    }

    public String getPassword() {
        return password;
    }

    public String getArgument() {
        return argument;
    }

    @Override
    public String getType() {
        return "Command";
    }

    @Override
    public String toString() {
        return super.toString() + " [Command: " + command + ", Argument: " + argument + "]";
    }
}
