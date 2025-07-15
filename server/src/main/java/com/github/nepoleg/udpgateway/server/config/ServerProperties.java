package com.github.nepoleg.udpgateway.server.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "udp-gateway")
public record ServerProperties(
  List<ChannelProperties> channels
) {

  public record ChannelProperties (
    String name,
    String address
  ) {}

}