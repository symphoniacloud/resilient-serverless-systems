package io.symphonia.lambda;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.symphonia.shared.EnvelopeMessage;

import java.io.IOException;
import java.util.Map;

public class OnSendLambda {

    private static String MESSAGES_TABLE = System.getenv("MESSAGES_TABLE");
    private static DynamoDBMapperConfig DYNAMODB_MAPPER_CONFIG =
            DynamoDBMapperConfig.builder()
                    .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(MESSAGES_TABLE))
                    .build();

    private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private DynamoDBMapper dynamoDBMapper;
    private String region;

    public OnSendLambda() {
        this(new DynamoDBMapper(AmazonDynamoDBClientBuilder.defaultClient(), DYNAMODB_MAPPER_CONFIG));
    }

    public OnSendLambda(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
        this.region = System.getenv("AWS_REGION");
    }

    public APIGatewayV2ProxyResponseEvent handler(APIGatewayV2ProxyRequestEvent event) throws IOException {
        var connectionId = event.getRequestContext().getConnectionId();
        var incoming = OBJECT_MAPPER.readValue(event.getBody(), Map.class);

        var message = new EnvelopeMessage();
        message.setId(event.getRequestContext().getRequestId());
        message.setTs(System.currentTimeMillis());
        message.setMessage((String) incoming.getOrDefault("message", "No message"));
        message.setRegion(region);
        message.setSource(connectionId);
        message.setType("message");
        dynamoDBMapper.save(message);

        var response = new APIGatewayV2ProxyResponseEvent();
        response.setStatusCode(200);
        response.setBody("Send ok");

        return response;
    }
}
