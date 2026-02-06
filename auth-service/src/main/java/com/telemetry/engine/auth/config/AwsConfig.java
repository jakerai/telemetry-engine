package com.telemetry.engine.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.telemetry.engine.auth.aws.props.AwsProperties;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
public class AwsConfig {

  private final AwsProperties awsProperties;

  public AwsConfig(AwsProperties awsProperties) {
    this.awsProperties = awsProperties;
  }

  /**
   * AWS SDK DefaultCredentialsProvider automatically picks up credentials from: Environment
   * variables: AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY or ~/.aws/credentials file or IAM roles
   * 
   */
  private DefaultCredentialsProvider credentialsProvider() {
    return DefaultCredentialsProvider.builder().build();
  }

  @Bean
  public S3Client s3Client() {
    return S3Client.builder().credentialsProvider(credentialsProvider())
        .region(Region.of(awsProperties.getRegion())).build();
  }

  @Bean
  public SesClient sesClient() {
    return SesClient.builder().credentialsProvider(credentialsProvider())
        .region(Region.of(awsProperties.getRegion())).build();
  }

  @Bean
  public SecretsManagerClient secretsManagerClient() {
    return SecretsManagerClient.builder().credentialsProvider(credentialsProvider())
        .region(Region.of(awsProperties.getRegion())).build();
  }

}
