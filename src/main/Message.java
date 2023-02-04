package main;

import java.io.Serializable;

public class Message implements Serializable {
    final MessageType type;
    final String data;

    public Message(MessageType type, String data) {
        this.type = type;
        this.data = data;
    }

    public Message(MessageType type) {
        this.type = type;
        data = null;
    }

    public MessageType getType() {
        return type;
    }

    public String getData() {
        return data;
    }
}
