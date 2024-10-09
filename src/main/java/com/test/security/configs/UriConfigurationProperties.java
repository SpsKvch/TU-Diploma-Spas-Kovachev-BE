package com.test.security.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("endpoints")
public class UriConfigurationProperties {

  private String[] publicEndpoints;
  private String[] protectedGetEndpoints;

}
