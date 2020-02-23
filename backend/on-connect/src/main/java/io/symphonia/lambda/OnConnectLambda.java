package io.symphonia.lambda;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyResponseEvent;

import java.util.Map;

public class OnConnectLambda {

    private static String CONNECTIONS_TABLE = System.getenv("CONNECTIONS_TABLE");

    private AmazonDynamoDB dynamoDbClient;

    public OnConnectLambda() {
        this(AmazonDynamoDBClientBuilder.defaultClient());
    }

    public OnConnectLambda(AmazonDynamoDB dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public APIGatewayV2ProxyResponseEvent handler(APIGatewayV2ProxyRequestEvent event) {
        var connectionId = event.getRequestContext().getConnectionId();
        var putItemRequest = new PutItemRequest()
                .withTableName(CONNECTIONS_TABLE)
                .withItem(Map.of("id", new AttributeValue(connectionId),
                        "type", new AttributeValue("connection")));

        dynamoDbClient.putItem(putItemRequest);

        var response = new APIGatewayV2ProxyResponseEvent();
        response.setStatusCode(200);
        response.setBody("Connect ok");
        return response;
    }

}
