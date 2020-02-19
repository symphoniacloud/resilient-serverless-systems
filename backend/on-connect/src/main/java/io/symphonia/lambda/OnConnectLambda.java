package io.symphonia.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyResponseEvent;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.Map;

public class OnConnectLambda {

    private static String CONNECTIONS_TABLE = System.getenv("CONNECTIONS_TABLE");

    private DynamoDbClient dynamoDbClient;

    public OnConnectLambda() {
        this(DynamoDbClient.builder().httpClientBuilder(UrlConnectionHttpClient.builder()).build());
    }

    public OnConnectLambda(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public APIGatewayV2ProxyResponseEvent handler(APIGatewayV2ProxyRequestEvent event) {
        var connectionId = event.getRequestContext().getConnectionId();
        var putItemRequest = PutItemRequest.builder()
                .tableName(CONNECTIONS_TABLE)
                .item(Map.of("id", AttributeValue.builder().s(connectionId).build(),
                        "type", AttributeValue.builder().s("connection").build()
                ))
                .build();

        dynamoDbClient.putItem(putItemRequest);

        var response = new APIGatewayV2ProxyResponseEvent();
        response.setStatusCode(200);
        response.setBody("Connect ok");
        return response;
    }

}
