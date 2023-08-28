package com.wfuertes.infra.aws;

import com.wfuertes.infra.json.JsonParser;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SnsPublisher {
    private static final String LOCAL_ENDPOINT = "http://localhost:4566";

    public static final String ORDER_CREATED = "OrderCreated";
    public static final String ORDER_DISCOUNT = "OrderDiscount";
    public static final String ORDER_OFFER = "OrderOffer";
    public static final String ORDER_COMPLETED = "OrderCompleted";
    private static final List<String> TOPICS = List.of(ORDER_CREATED, ORDER_DISCOUNT, ORDER_OFFER, ORDER_COMPLETED);

    private final SnsClient snsClient;
    private final Map<String, String> topicArn = new HashMap<>();
    private final JsonParser jsonParser;

    public SnsPublisher(JsonParser jsonParser) {
        this.jsonParser = jsonParser;
        this.snsClient = SnsClient
                .builder()
                .credentialsProvider(AwsCredentialsProviderFactory.create())
                .endpointOverride(URI.create(LOCAL_ENDPOINT))
                .region(Region.US_EAST_1)
                .build();

        for (final var topicName : TOPICS) {
            final var response = snsClient.createTopic(CreateTopicRequest.builder().name(topicName).build());
            topicArn.put(topicName, response.topicArn());
            System.out.printf("Publisher:Topic[%s] -> %s", topicName, response.topicArn());
        }
    }

    public void publish(String topicName, Object value) {
        final var json = jsonParser.toJson(value);
        snsClient.publish(PublishRequest
                .builder()
                .subject(topicName)
                .message(json)
                .topicArn(topicArn.get(topicName))
                .build());
    }
}
