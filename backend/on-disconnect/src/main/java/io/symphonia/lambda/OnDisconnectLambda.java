package io.symphonia.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyResponseEvent;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;

import java.util.Collections;

public class OnDisconnectLambda {

    private static String CONNECTIONS_TABLE = System.getenv("CONNECTIONS_TABLE");

    private DynamoDbClient dynamoDbClient;

    public OnDisconnectLambda() {
        this(DynamoDbClient.builder().httpClientBuilder(UrlConnectionHttpClient.builder()).build());
    }

    public OnDisconnectLambda(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public APIGatewayV2ProxyResponseEvent handler(APIGatewayV2ProxyRequestEvent event) {
        var connectionId = event.getRequestContext().getConnectionId();

        var request = DeleteItemRequest.builder()
                .tableName(CONNECTIONS_TABLE)
                .key(Collections.singletonMap("id", AttributeValue.builder().s(connectionId).build()))
                .build();

        dynamoDbClient.deleteItem(request);

        var response = new APIGatewayV2ProxyResponseEvent();
        response.setStatusCode(200);
        response.setBody("Disconnect ok");
        return response;
    }

}
