package io.symphonia.lambda;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.symphonia.shared.Envelope;
import io.symphonia.shared.EnvelopeMessage;

import java.util.Map;

public class HistoryReaderLambda {

    private static String MESSAGES_TABLE = System.getenv("MESSAGES_TABLE");
    private static DynamoDBMapperConfig DYNAMODB_MAPPER_CONFIG =
            DynamoDBMapperConfig.builder()
                    .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(MESSAGES_TABLE))
                    .build();

    private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private DynamoDBMapper dynamoDBMapper;
    private String region;

    public HistoryReaderLambda() {
        this(new DynamoDBMapper(AmazonDynamoDBClientBuilder.defaultClient(), DYNAMODB_MAPPER_CONFIG));
    }

    public HistoryReaderLambda(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
        this.region = System.getenv("AWS_REGION");
    }

    public APIGatewayProxyResponseEvent handler(APIGatewayProxyRequestEvent event) throws JsonProcessingException {

        var query = new DynamoDBQueryExpression<EnvelopeMessage>()
                .withConsistentRead(false)
                .withScanIndexForward(false)
                .withLimit(100)
                .withKeyConditionExpression("#type = :type")
                .withExpressionAttributeNames(Map.of("#type", "type"))
                .withExpressionAttributeValues(Map.of(":type", new AttributeValue("message")))
                .withIndexName("messages_by_ts");

        var messages = dynamoDBMapper.query(EnvelopeMessage.class, query);
        var envelope = new Envelope(messages, region);

        var response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(200);
        response.setBody(OBJECT_MAPPER.writeValueAsString(envelope));
        response.setHeaders(Map.of("Access-Control-Allow-Origin", "*"));

        return response;
    }
}
