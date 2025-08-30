package com.github.nepoleg.udpgateway.server.config;

import com.github.nepoleg.udpgateway.server.config.UdpGatewayServerProperties.ReceiverProperties;
import com.github.nepoleg.udpgateway.server.udp.agent.ServerAgentRunner;
import com.github.nepoleg.udpgateway.server.udp.receive.UdpAgent;
import com.github.nepoleg.udpgateway.server.udp.receive.processor.Processor;
import com.github.nepoleg.udpgateway.server.udp.receive.processor.WebSocketSender;
import com.github.nepoleg.udpgateway.server.websocket.WebSocketServer;
import com.github.nepoleg.udpgateway.server.websocket.session.WebSocketSessionRegistry;
import java.net.InetSocketAddress;
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
  public UdpAgent udpAgent(Processor processor) {
    ReceiverProperties receiverProperties = serverProperties.receiver();
    var remoteAddresses = receiverProperties.channels().stream()
        .map(ch -> new InetSocketAddress(ch.host(), ch.port())).toList();
    return new UdpAgent(remoteAddresses, receiverProperties.localInterface(), receiverProperties.bufferSize(),
        receiverProperties.timeout().toMillis(), processor);
  }

  @Bean(initMethod = "start")
  public ServerAgentRunner udpChannelsAgentRunner(UdpAgent udpAgent) {
    return new ServerAgentRunner(udpAgent);
  }

  @Bean
  public WebSocketSessionRegistry sessionRegistry() {
    return new WebSocketSessionRegistry();
  }

  @Bean(initMethod = "start")
  public WebSocketServer webSocketServer() {
    return new WebSocketServer(serverProperties.listen().host(), serverProperties.listen().port(),
        sessionRegistry()::create);
  }

}
