package io.symphonia.shared;

import com.amazonaws.services.dynamodbv2.datamodeling.*;

public class EnvelopeMessage {
    private String id;
    private Long ts;
    private String type = "message";
    private String message = "No message";
    private String source;
    private String region;

    public EnvelopeMessage() {
    }

    @DynamoDBHashKey(attributeName = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @DynamoDBRangeKey
    @DynamoDBIndexRangeKey(attributeName = "ts", globalSecondaryIndexName = "messages_by_ts")
    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    @DynamoDBIndexHashKey(attributeName = "type", globalSecondaryIndexName = "messages_by_ts")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @DynamoDBAttribute(attributeName = "message")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @DynamoDBAttribute(attributeName = "source")
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @DynamoDBAttribute(attributeName = "region")
    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

}
