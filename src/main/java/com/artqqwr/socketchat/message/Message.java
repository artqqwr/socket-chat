package com.artqqwr.socketchat.message;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    visible = false 
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ConnectMessage.class, name = "Connect"),
    @JsonSubTypes.Type(value = TextMessage.class, name = "Text"),
    @JsonSubTypes.Type(value = CommandMessage.class, name = "Command"),
    @JsonSubTypes.Type(value = ErrorMessage.class, name = "Error"),
    @JsonSubTypes.Type(value = NotificationMessage.class, name = "Notification"),

})
public abstract class Message {
    private String sender;
    private LocalDateTime timestamp;

    public Message(String sender) {
        this.sender = sender;
        this.timestamp = LocalDateTime.now();
    }

    public Message() {
        this.timestamp = LocalDateTime.now();
    }

    public String getSender() {
        return sender;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public abstract String getType();

    @Override
    public String toString() {
        return "[" + timestamp + "] (" + getType() + ") " + sender;
    }
}
