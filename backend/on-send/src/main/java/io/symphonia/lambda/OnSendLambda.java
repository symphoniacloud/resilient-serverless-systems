package io.symphonia.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.symphonia.shared.EnvelopeMessage;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.io.IOException;
import java.util.Map;

public class OnSendLambda {

    private static String AWS_REGION = System.getenv("AWS_REGION");
    private static String MESSAGES_TABLE = System.getenv("MESSAGES_TABLE");
    private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private DynamoDbClient dynamoDbClient;

    public OnSendLambda() {
        this(DynamoDbClient.builder().httpClientBuilder(UrlConnectionHttpClient.builder()).build());
    }

    public OnSendLambda(DynamoDbClient dynamoDbClient) {
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

        var request = PutItemRequest.builder()
                .tableName(MESSAGES_TABLE)
                .item(message.toItem())
                .build();

        dynamoDbClient.putItem(request);


        var response = new APIGatewayV2ProxyResponseEvent();
        response.setStatusCode(200);
        response.setBody("Send ok");

        return response;
    }
}
