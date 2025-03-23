package com.artqqwr.socketchat.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("Text")
@JsonIgnoreProperties({"type"})
public class TextMessage extends Message {
    private String text;

    public TextMessage(String sender, String text) {
        super(sender);
        this.text = text;
    }

    public TextMessage() {
        super();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String getType() {
        return "Text";
    }

    @Override
    public String toString() {
        return super.toString() + ": " + text;
    }
}
