package com.wfuertes.infra.aws;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

public class AwsCredentialsProviderFactory {

    public static StaticCredentialsProvider create() {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create("di8t7d", "iodr0f"));
    }
}
