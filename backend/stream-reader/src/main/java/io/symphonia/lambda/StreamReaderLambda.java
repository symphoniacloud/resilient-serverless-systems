package io.symphonia.lambda;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApiClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.model.PostToConnectionRequest;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.symphonia.shared.AttributeUtil;
import io.symphonia.shared.Envelope;
import io.symphonia.shared.EnvelopeMessage;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.stream.Collectors;

public class StreamReaderLambda {

    private static String AWS_REGION = System.getenv("AWS_REGION");
    private static String CONNECTIONS_TABLE = System.getenv("CONNECTIONS_TABLE");
    private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private AmazonDynamoDB dynamoDbClient;
    private AmazonApiGatewayManagementApi managementApiClient;

    public StreamReaderLambda() {
        var endpoint = System.getenv("WEB_SOCKETS_ENDPOINT");
        var config = new AwsClientBuilder.EndpointConfiguration(endpoint, AWS_REGION);

        this.dynamoDbClient = AmazonDynamoDBClientBuilder.defaultClient();

        this.managementApiClient = AmazonApiGatewayManagementApiClientBuilder.standard()
                .withEndpointConfiguration(config)
                .build();
    }

    public StreamReaderLambda(AmazonDynamoDB dynamoDbClient, AmazonApiGatewayManagementApi managementApiClient) {
        this.dynamoDbClient = dynamoDbClient;
        this.managementApiClient = managementApiClient;
    }

    public void handler(DynamodbEvent event) throws JsonProcessingException {

        var messages = event.getRecords().stream()
                .map(record -> record.getDynamodb().getNewImage())
                .filter(Objects::nonNull)
                .map(EnvelopeMessage::new)
                .distinct()
                .collect(Collectors.toList());

        var envelope = new Envelope(messages, AWS_REGION);
        var jsonBytes = ByteBuffer.wrap(OBJECT_MAPPER.writeValueAsBytes(envelope));

        var scanRequest = new ScanRequest()
                .withTableName(CONNECTIONS_TABLE)
                .withAttributesToGet("id");

        dynamoDbClient.scan(scanRequest).getItems()
                .forEach(item -> {
                    var connectionId = AttributeUtil.getOrThrowS(item, "id");
                    var postRequest = new PostToConnectionRequest()
                            .withConnectionId(connectionId)
                            .withData(jsonBytes);
                    try {
                        managementApiClient.postToConnection(postRequest);
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                });

    }
}
