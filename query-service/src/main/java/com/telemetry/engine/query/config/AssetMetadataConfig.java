package com.telemetry.engine.query.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "asset-metadata")
public class AssetMetadataConfig {

  private List<Category> categories;
  private List<Type> types;

  @Getter
  @Setter
  public static class Category {

    private String code;
    private String description;

  }


  @Getter
  @Setter
  public static class Type {

    private String code;
    private String category; // references Category.code
    private String description;

  }

}


