package io.symphonia.shared;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.util.Map;

public class EnvelopeMessage {

    private String id;
    private Long ts;
    private String type = "message";
    private String message;
    private String source;
    private String region;

    public EnvelopeMessage() {
    }

    public EnvelopeMessage(Map<String, AttributeValue> item) {
        this.id = AttributeUtil.getOrThrowS(item, "id");
        this.ts = AttributeUtil.getOrThrowL(item, "ts");
        this.type = AttributeUtil.getOrThrowS(item, "type");
        this.message = AttributeUtil.getOrThrowS(item, "message");
        this.source = AttributeUtil.getOrThrowS(item, "source");
        this.region = AttributeUtil.getOrThrowS(item, "region");
    }

    public Map<String, AttributeValue> toItem() {
        return Map.of(
                "id", new AttributeValue(id),
                "ts", new AttributeValue().withN(Long.toString(ts)),
                "message", new AttributeValue(message),
                "region", new AttributeValue(region),
                "source", new AttributeValue(source),
                "type", new AttributeValue(type)
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

}
