package io.symphonia.lambda;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.symphonia.shared.Envelope;
import io.symphonia.shared.EnvelopeMessage;

import java.util.Map;
import java.util.stream.Collectors;

public class HistoryReaderLambda {

    private static String AWS_REGION = System.getenv("AWS_REGION");
    private static String MESSAGES_TABLE = System.getenv("MESSAGES_TABLE");
    private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private AmazonDynamoDB dynamoDbClient;

    public HistoryReaderLambda() {
        this(AmazonDynamoDBClientBuilder.defaultClient());
    }

    public HistoryReaderLambda(AmazonDynamoDB dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public APIGatewayProxyResponseEvent handler(APIGatewayProxyRequestEvent event) throws JsonProcessingException {

        var queryRequest = new QueryRequest()
                .withConsistentRead(false)
                .withScanIndexForward(false)
                .withLimit(100)
                .withKeyConditionExpression("#type = :type")
                .withExpressionAttributeNames(Map.of("#type", "type"))
                .withExpressionAttributeValues(Map.of(":type", new AttributeValue("message")))
                .withTableName(MESSAGES_TABLE)
                .withIndexName("messages_by_ts");

        var messages = dynamoDbClient
                .query(queryRequest)
                .getItems().stream()
                .map(EnvelopeMessage::new)
                .collect(Collectors.toList());

        var envelope = new Envelope(messages, AWS_REGION);

        var response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(200);
        response.setBody(OBJECT_MAPPER.writeValueAsString(envelope));
        response.setHeaders(Map.of("Access-Control-Allow-Origin", "*"));

        return response;
    }
}
