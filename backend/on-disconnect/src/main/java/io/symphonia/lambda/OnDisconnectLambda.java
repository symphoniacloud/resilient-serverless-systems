package io.symphonia.lambda;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyResponseEvent;

import java.util.Collections;

public class OnDisconnectLambda {

    private AmazonDynamoDB dynamoDB;
    private String tableName;

    public OnDisconnectLambda() {
        this(AmazonDynamoDBClientBuilder.defaultClient());
    }

    public OnDisconnectLambda(AmazonDynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
        this.tableName = System.getenv("CONNECTIONS_TABLE");
    }

    public APIGatewayV2ProxyResponseEvent handler(APIGatewayV2ProxyRequestEvent event) {
        var connectionId = event.getRequestContext().getConnectionId();

        dynamoDB.deleteItem(tableName, Collections.singletonMap("id", new AttributeValue(connectionId)));

        var response = new APIGatewayV2ProxyResponseEvent();
        response.setStatusCode(200);
        response.setBody("Disconnect ok");
        return response;
    }

}
