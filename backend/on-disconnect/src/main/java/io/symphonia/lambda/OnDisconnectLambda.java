package io.symphonia.lambda;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyResponseEvent;

import java.util.Collections;

public class OnDisconnectLambda {

    private static String CONNECTIONS_TABLE = System.getenv("CONNECTIONS_TABLE");

    private AmazonDynamoDB dynamoDbClient;

    public OnDisconnectLambda() {
        this(AmazonDynamoDBClientBuilder.defaultClient());
    }

    public OnDisconnectLambda(AmazonDynamoDB dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public APIGatewayV2ProxyResponseEvent handler(APIGatewayV2ProxyRequestEvent event) {
        var connectionId = event.getRequestContext().getConnectionId();

        var request = new DeleteItemRequest()
                .withTableName(CONNECTIONS_TABLE)
                .withKey(Collections.singletonMap("id", new AttributeValue(connectionId)));

        dynamoDbClient.deleteItem(request);

        var response = new APIGatewayV2ProxyResponseEvent();
        response.setStatusCode(200);
        response.setBody("Disconnect ok");
        return response;
    }

}
