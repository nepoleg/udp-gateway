package com.github.nepoleg.udpgateway.server.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServerConfiguration {

  private final ServerProperties serverProperties;

  public ServerConfiguration(ServerProperties serverProperties) {
    this.serverProperties = serverProperties;
  }

  @PostConstruct
  void printConfig() {
    System.err.println(serverProperties);
  }
}
