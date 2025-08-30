package com.github.nepoleg.udpgateway.server.config;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.convert.DurationUnit;

@ConfigurationProperties(prefix = "udp-gateway")
public record UdpGatewayServerProperties(
    ReceiverProperties receiver,
    ListenProperties listen
) {
  public record ReceiverProperties(List<Address> channels,
                                   String localInterface,
                                   @DefaultValue("65546") int bufferSize,
                                   @DefaultValue("1m") @DurationUnit(ChronoUnit.MILLIS) Duration timeout
                         ) {}
  public record Address (
    String name,
    String host,
    int port
  ) {}

  public record ListenProperties (
      String host,
      int port
  ) {}
}