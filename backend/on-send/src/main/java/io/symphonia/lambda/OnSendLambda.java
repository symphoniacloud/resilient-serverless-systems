package io.symphonia.lambda;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.symphonia.shared.EnvelopeMessage;

import java.io.IOException;
import java.util.Map;

public class OnSendLambda {

    private static String AWS_REGION = System.getenv("AWS_REGION");
    private static String MESSAGES_TABLE = System.getenv("MESSAGES_TABLE");
    private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private AmazonDynamoDB dynamoDbClient;

    public OnSendLambda() {
        this(AmazonDynamoDBClientBuilder.defaultClient());
    }

    public OnSendLambda(AmazonDynamoDB dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public APIGatewayV2ProxyResponseEvent handler(APIGatewayV2ProxyRequestEvent event) throws IOException {
        var connectionId = event.getRequestContext().getConnectionId();
        var incoming = OBJECT_MAPPER.readValue(event.getBody(), Map.class);

        var message = new EnvelopeMessage();
        message.setId(event.getRequestContext().getRequestId());
        message.setTs(System.currentTimeMillis());
        message.setMessage((String) incoming.getOrDefault("message", "No message"));
        message.setRegion(AWS_REGION);
        message.setSource(connectionId);

        var request = new PutItemRequest()
                .withTableName(MESSAGES_TABLE)
                .withItem(message.toItem());

        dynamoDbClient.putItem(request);


        var response = new APIGatewayV2ProxyResponseEvent();
        response.setStatusCode(200);
        response.setBody("Send ok");

        return response;
    }
}
