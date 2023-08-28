package com.wfuertes.infra.aws;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wfuertes.infra.json.JsonParser;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SqsConsumer {

    private static final String LOCAL_ENDPOINT = "http://localhost:4566";

    public static final String ORDER_CREATED = "OrderCreated";
    public static final String ORDER_DISCOUNT = "OrderDiscount";
    public static final String ORDER_OFFER = "OrderOffer";
    public static final String ORDER_COMPLETED = "OrderCompleted";
    private static final List<String> TOPICS = List.of(ORDER_CREATED, ORDER_DISCOUNT, ORDER_OFFER, ORDER_COMPLETED);

    private final JsonParser jsonParser;
    private final SqsClient sqsClient;
    private final Map<String, String> queueUrl = new HashMap<>();

    public SqsConsumer(JsonParser jsonParser) {
        this.jsonParser = jsonParser;
        try (final SnsClient snsClient = SnsClient.builder()
                .credentialsProvider(AwsCredentialsProviderFactory.create())
                .endpointOverride(URI.create(LOCAL_ENDPOINT))
                .region(Region.US_EAST_1)
                .build()) {

            this.sqsClient = SqsClient
                    .builder()
                    .credentialsProvider(AwsCredentialsProviderFactory.create())
                    .endpointOverride(URI.create(LOCAL_ENDPOINT))
                    .region(Region.US_EAST_1)
                    .build();

            for (final var topicName : TOPICS) {
                // Create or Get Queue
                final var queueResponse = sqsClient.createQueue(
                        CreateQueueRequest
                                .builder()
                                .queueName(topicName)
                                .attributes(Map.of(QueueAttributeName.VISIBILITY_TIMEOUT, "30"))
                                .build()
                );
                final var queueAttributes = sqsClient.getQueueAttributes(
                        GetQueueAttributesRequest
                                .builder()
                                .queueUrl(queueResponse.queueUrl())
                                .attributeNames(QueueAttributeName.QUEUE_ARN)
                                .build());

                final var topicArn = "arn:aws:sns:us-east-1:000000000000:%s".formatted(topicName);
                final var queueArn = queueAttributes.attributes().get(QueueAttributeName.QUEUE_ARN);
                System.out.printf("Consumer:Topic[%s] -> %s", topicName, topicArn);

                final var subscription = SubscribeRequest
                        .builder()
                        .protocol("sqs")
                        .endpoint(queueArn)
                        .topicArn(topicArn)
                        .attributes(Map.of("RawMessageDelivery", "false"))
                        .build();

                // Create Subscription
                snsClient.subscribe(subscription);
                queueUrl.put(topicName, queueResponse.queueUrl());
            }
        }
    }

    public <T> void consume(String queueName, Consumer<T> consumer, Class<T> clazz) {
        while (true) {

            final var request = ReceiveMessageRequest
                    .builder()
                    .queueUrl(queueUrl.get(queueName))
                    .maxNumberOfMessages(1)
                    .waitTimeSeconds(20)
                    .build();
            final var response = sqsClient.receiveMessage(request);

            if (!response.hasMessages()) {
                return;
            }

            response
                    .messages()
                    .stream()
                    .findFirst()
                    .ifPresent(message -> {
                        final var body = jsonParser.fromJson(message.body(), MessageBody.class);
                        try {
                            if (!queueName.equals(body.subject)) {
                                throw new IllegalArgumentException("The event %s is not handled by queue %s".formatted(body.subject, queueName));
                            }

                            var content = URLDecoder.decode(body.message, StandardCharsets.UTF_8);
                            if (content.startsWith("\"") && content.endsWith("\"")) {
                                content = content.substring(1, content.length() - 1);
                                content = content.replace("\\", "");
                            }

                            final var event = jsonParser.fromJson(content, clazz);
                            consumer.accept(event);
                            // Doing the acknowledgement
                            sqsClient.deleteMessage(DeleteMessageRequest
                                    .builder()
                                    .queueUrl(queueUrl.get(queueName))
                                    .receiptHandle(message.receiptHandle())
                                    .build());
                        } catch (Exception err) {
                            System.out.println(err.getMessage());
                            throw new RuntimeException(err);
                        }
                    });
        }
    }

    private record MessageBody(@JsonProperty("Subject") String subject,
                               @JsonProperty("Message") String message) {
    }
}
