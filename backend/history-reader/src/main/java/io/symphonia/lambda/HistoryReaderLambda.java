package io.symphonia.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.symphonia.shared.Envelope;
import io.symphonia.shared.EnvelopeMessage;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

import java.util.Map;
import java.util.stream.Collectors;

public class HistoryReaderLambda {

    private static String AWS_REGION = System.getenv("AWS_REGION");
    private static String MESSAGES_TABLE = System.getenv("MESSAGES_TABLE");
    private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private DynamoDbClient dynamoDbClient;

    public HistoryReaderLambda() {
        this(DynamoDbClient.builder().httpClientBuilder(UrlConnectionHttpClient.builder()).build());
    }

    public HistoryReaderLambda(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public APIGatewayProxyResponseEvent handler(APIGatewayProxyRequestEvent event) throws JsonProcessingException {

        var queryRequest = QueryRequest.builder()
                .consistentRead(false)
                .scanIndexForward(false)
                .limit(100)
                .keyConditionExpression("#type = :type")
                .expressionAttributeNames(Map.of("#type", "type"))
                .expressionAttributeValues(Map.of(":type", AttributeValue.builder().s("message").build()))
                .tableName(MESSAGES_TABLE)
                .indexName("messages_by_ts")
                .build();

        var messages = dynamoDbClient
                .query(queryRequest)
                .items().stream()
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
