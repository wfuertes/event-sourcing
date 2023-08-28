package com.wfuertes.infra;


import com.wfuertes.infra.aws.AwsCredentialsProviderFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

public class DynamoDBConfig {

    public static DynamoDbClient createClient() {
        return DynamoDbClient.builder()
                .region(Region.US_EAST_1) // Change to your preferred region
                .credentialsProvider(AwsCredentialsProviderFactory.create())
                // Uncomment the below line if connecting to a local DynamoDB instance
                .endpointOverride(URI.create("http://localhost:8000"))
                .build();
    }
}
