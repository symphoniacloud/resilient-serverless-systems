package io.symphonia.lambda;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.symphonia.shared.AttributeUtil;
import io.symphonia.shared.Envelope;
import io.symphonia.shared.EnvelopeMessage;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.PostToConnectionRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

import java.net.URI;
import java.util.Objects;
import java.util.stream.Collectors;

public class StreamReaderLambda {

    private static String AWS_REGION = System.getenv("AWS_REGION");
    private static String CONNECTIONS_TABLE = System.getenv("CONNECTIONS_TABLE");
    private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private DynamoDbClient dynamoDbClient;
    private ApiGatewayManagementApiClient managementApiClient;

    public StreamReaderLambda() {
        var endpoint = System.getenv("WEB_SOCKETS_ENDPOINT");
        var httpClient = UrlConnectionHttpClient.builder().build();

        this.dynamoDbClient = DynamoDbClient.builder()
                .httpClient(httpClient)
                .build();

        this.managementApiClient = ApiGatewayManagementApiClient.builder()
                .httpClient(httpClient)
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(AWS_REGION))
                .build();
    }

    public StreamReaderLambda(DynamoDbClient dynamoDbClient, ApiGatewayManagementApiClient managementApiClient) {
        this.dynamoDbClient = dynamoDbClient;
        this.managementApiClient = managementApiClient;
    }

    public void handler(DynamodbEvent event) throws JsonProcessingException {

        var messages = event.getRecords().stream()
                .map(record -> record.getDynamodb().getNewImage())
                .filter(Objects::nonNull)
                .map(AttributeValueConverter::oldToNew)
                .map(EnvelopeMessage::new)
                .distinct()
                .collect(Collectors.toList());

        var envelope = new Envelope(messages, AWS_REGION);
        var jsonBytes = OBJECT_MAPPER.writeValueAsBytes(envelope);

        var scanRequest = ScanRequest.builder()
                .tableName(CONNECTIONS_TABLE)
                .attributesToGet("id")
                .build();

        dynamoDbClient.scan(scanRequest).items()
                .forEach(item -> {
                    var connectionId = AttributeUtil.getOrThrowS(item, "id");
                    var postRequest = PostToConnectionRequest.builder()
                            .connectionId(connectionId)
                            .data(SdkBytes.fromByteArray(jsonBytes))
                            .build();
                    try {
                        managementApiClient.postToConnection(postRequest);
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                });

    }
}
