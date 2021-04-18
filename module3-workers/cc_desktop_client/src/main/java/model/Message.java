package model;

/**
 * Model class to handle incoming messages through mqtt.
 * Provides getters and setters for Message response object.
 */
public class Message {

    private String message;
    private byte[] data;

    public Message(String message, byte[] data ) {
        this.message = message;
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public byte[] getData() {
        return data;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
