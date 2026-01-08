package com.telemetry.engine.auth.aws.props;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "aws")
@Configuration
public class AwsProperties {
  private String region;
  
  private String sesVerifiedSenderEmail;
}

