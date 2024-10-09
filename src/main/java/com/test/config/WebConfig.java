package com.test.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebConfig {

  @Value(value = "${server.port}")
  private String appPort;

  private final String appUrl = "localhost:" + appPort;

  @Bean
  public RestTemplate defaultRestTemplate() {
    return new RestTemplate();
  }

  @Bean
  public WebClient defaultWebClient() {
    return WebClient.builder()
        .baseUrl(appUrl)
        .clientConnector(new ReactorClientHttpConnector(HttpClient.create().followRedirect(true)))
        .build();
  }

  //@Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper().registerModule(new JavaTimeModule());
  }

}
