package io.symphonia.lambda;

import com.amazonaws.SdkClientException;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApi;
import com.amazonaws.services.apigatewaymanagementapi.AmazonApiGatewayManagementApiClientBuilder;
import com.amazonaws.services.apigatewaymanagementapi.model.PostToConnectionRequest;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.symphonia.shared.AttributeUtil;
import io.symphonia.shared.Envelope;
import io.symphonia.shared.EnvelopeMessage;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

public class StreamReaderLambda {

    private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private AmazonDynamoDB dynamoDB;
    private AmazonApiGatewayManagementApi apiClient;
    private String region;
    private String connectionsTableName;

    public StreamReaderLambda() {
        var endpoint = System.getenv("WEB_SOCKETS_ENDPOINT");
        var region = System.getenv("AWS_REGION");
        var config = new AwsClientBuilder.EndpointConfiguration(endpoint, region);

        this.dynamoDB = AmazonDynamoDBClientBuilder.defaultClient();
        this.apiClient = AmazonApiGatewayManagementApiClientBuilder.standard()
                .withEndpointConfiguration(config)
                .build();
        this.region = region;
        this.connectionsTableName = System.getenv("CONNECTIONS_TABLE");
    }

    public void handler(DynamodbEvent event) throws JsonProcessingException {

        var messages = event.getRecords().stream()
                .map(record -> record.getDynamodb().getNewImage())
                .filter(Objects::nonNull)
                .map(image -> {
                    try {
                        System.out.println(OBJECT_MAPPER.writeValueAsString(image));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                    var message = new EnvelopeMessage();
                    message.setId(AttributeUtil.getOrThrowS(image, "id"));
                    message.setTs(AttributeUtil.getOrThrowL(image, "ts"));
                    message.setMessage(AttributeUtil.getOrThrowS(image, "message"));
                    message.setSource(AttributeUtil.getOrDefaultS(image, "source", "Unknown"));
                    message.setRegion(AttributeUtil.getOrThrowS(image, "region"));
                    message.setType(AttributeUtil.getOrDefaultS(image, "type", "message"));
                    return message;
                })
                .distinct()
                .collect(Collectors.toList());

        var envelope = new Envelope(messages, region);
        var jsonBytes = ByteBuffer.wrap(OBJECT_MAPPER.writeValueAsBytes(envelope));

        dynamoDB.scan(connectionsTableName, Collections.singletonList("id"))
                .getItems()
                .forEach(item -> {
                    var connectionId = AttributeUtil.getOrThrowS(item, "id");
                    var request = new PostToConnectionRequest()
                            .withConnectionId(connectionId)
                            .withData(jsonBytes);
                    try {
                        apiClient.postToConnection(request);
                    } catch (SdkClientException e) {
                        System.err.println(e.getMessage());
                    }
                });

    }
}
