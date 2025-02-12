package com.example.yogiapp.models;

public class ChatMessage {
    public enum Type { REQUEST, RESPONSE }
    private Type type;
    private String message;

    public ChatMessage(Type type, String message) {
        this.type = type;
        this.message = message;
    }

    public Type getType() {
        return type;
    }
    public String getMessage() {
        return message;
    }
}
