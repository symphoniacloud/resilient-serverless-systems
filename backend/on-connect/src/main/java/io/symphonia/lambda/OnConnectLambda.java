package io.symphonia.lambda;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyResponseEvent;

import java.util.Map;

public class OnConnectLambda {

    private AmazonDynamoDB dynamoDB;
    private String tableName;

    public OnConnectLambda() {
        this(AmazonDynamoDBClientBuilder.defaultClient());
    }

    public OnConnectLambda(AmazonDynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
        this.tableName = System.getenv("CONNECTIONS_TABLE");
    }

    public APIGatewayV2ProxyResponseEvent handler(APIGatewayV2ProxyRequestEvent event) {
        var request = new PutItemRequest()
                .withTableName(tableName)
                .withItem(Map.of("id", new AttributeValue(event.getRequestContext().getConnectionId())));

        dynamoDB.putItem(request);

        var response = new APIGatewayV2ProxyResponseEvent();
        response.setStatusCode(200);
        response.setBody("Connect ok");
        return response;
    }

}
