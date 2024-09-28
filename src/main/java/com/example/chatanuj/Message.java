package com.example.chatanuj;

public class Message {
    private String messageText;
    private String sentBy;
    private long timestamp;
    private boolean seen;

    public Message() {}

    public Message(String messageText, String sentBy, long timestamp) {
        this.messageText = messageText;
        this.sentBy = sentBy;
        this.timestamp = timestamp;
        this.seen = false;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getSentBy() {
        return sentBy;
    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
}
