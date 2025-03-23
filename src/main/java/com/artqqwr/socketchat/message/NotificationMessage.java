package com.artqqwr.socketchat.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("Notification")
@JsonIgnoreProperties({"type"})
public class NotificationMessage extends Message {
    private String notification;

    public NotificationMessage(String sender, String notification) {
        super(sender);
        this.notification = notification;
    }

    public NotificationMessage() {
        super();
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    @Override
    public String getType() {
        return "Notification";
    }

    @Override
    public String toString() {
        return super.toString() + " [Notification: " + notification + "]";
    }
}
