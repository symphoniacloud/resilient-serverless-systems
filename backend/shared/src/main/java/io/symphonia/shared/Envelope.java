package io.symphonia.shared;

import java.util.List;

public class Envelope {

    private List<EnvelopeMessage> messages;
    private String region;

    public Envelope(List<EnvelopeMessage> messages, String region) {
        this.messages = messages;
        this.region = region;
    }

    public List<EnvelopeMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<EnvelopeMessage> messages) {
        this.messages = messages;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
