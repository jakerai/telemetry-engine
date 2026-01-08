package com.telemetry.engine.auth.core.role.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "roles-config")
public class RoleConfig {

  private List<Role> roles;
  
  @Getter
  @Setter
  public static class Role {
      private String name;
      private List<String> permissions;
  }
  
}
