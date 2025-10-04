package com.github.nepoleg.udpgateway.server.config;

import com.github.nepoleg.udpgateway.server.udp.netty.MulticastUdpClient;
import com.github.nepoleg.udpgateway.server.udp.receive.processor.Processor;
import com.github.nepoleg.udpgateway.server.udp.receive.processor.WebSocketSender;
import com.github.nepoleg.udpgateway.server.websocket.WebSocketServer;
import com.github.nepoleg.udpgateway.server.websocket.session.WebSocketSessionRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServerConfiguration {

  private final UdpGatewayServerProperties serverProperties;

  public ServerConfiguration(UdpGatewayServerProperties serverProperties) {
    this.serverProperties = serverProperties;
  }

  @Bean
  public Processor processor() {
    return new WebSocketSender(sessionRegistry());
  }

  @Bean
  public WebSocketSessionRegistry sessionRegistry() {
    return new WebSocketSessionRegistry();
  }

  @Bean(initMethod = "start")
  public WebSocketServer webSocketServer() {
    return new WebSocketServer(serverProperties.server().host(), serverProperties.server().port(), serverProperties.server().bufferSize(),
        sessionRegistry()::create);
  }

  @Bean(initMethod = "start", destroyMethod = "shutdown")
  public MulticastUdpClient udpChannelsListener() {
    return new MulticastUdpClient(serverProperties.receiver(), processor());
  }
}
