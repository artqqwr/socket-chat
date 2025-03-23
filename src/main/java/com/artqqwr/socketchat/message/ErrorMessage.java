package com.artqqwr.socketchat.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("Error")
@JsonIgnoreProperties({"type"})
public class ErrorMessage extends Message {
    private String error;

    public ErrorMessage(String sender, String error) {
        super(sender);
        this.error = error;
    }

    public ErrorMessage() {
        super();
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String getType() {
        return "Error";
    }
    
    @Override
    public String toString() {
        return super.toString() + " [Error: " + error + "]";
    }
}
