package com.artqqwr.socketchat.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("Connect")
@JsonIgnoreProperties({"type"})
public class ConnectMessage extends Message {
    private String clientInfo;

    public ConnectMessage(String sender, String clientInfo) {
        super(sender);
        this.clientInfo = clientInfo;
    }

    public ConnectMessage() {
        super();
    }

    public String getClientInfo() {
        return clientInfo;
    }

    public void setClientInfo(String clientInfo) {
        this.clientInfo = clientInfo;
    }

    @Override
    public String getType() {
        return "Connect";
    }

    @Override
    public String toString() {
        return super.toString() + " [ClientInfo: " + clientInfo + "]";
    }
}
