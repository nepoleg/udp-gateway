package com.github.nepoleg.udpgateway.server.config;

import com.github.nepoleg.udpgateway.server.agent.ServerAgentRunner;
import com.github.nepoleg.udpgateway.server.config.ServerProperties.Receiver;
import com.github.nepoleg.udpgateway.server.receive.PrintingProcessor;
import com.github.nepoleg.udpgateway.server.receive.Processor;
import com.github.nepoleg.udpgateway.server.receive.UdpAgent;
import java.net.InetSocketAddress;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServerConfiguration {

  private final ServerProperties serverProperties;

  public ServerConfiguration(ServerProperties serverProperties) {
    this.serverProperties = serverProperties;
  }

  @Bean
  public Processor processor() {
    return new PrintingProcessor();
  }

  @Bean
  public UdpAgent udpAgent(Processor processor) {
    Receiver receiver = serverProperties.receiver();
    var remoteAddresses = receiver.channels().stream()
        .map(ch -> new InetSocketAddress(ch.host(), ch.port())).toList();
    return new UdpAgent(remoteAddresses, receiver.localInterface(), receiver.bufferSize(),
        receiver.timeout().toMillis(), processor);
  }

  @Bean(initMethod = "start")
  public ServerAgentRunner udpChannelsAgentRunner(UdpAgent udpAgent) {
    return new ServerAgentRunner(udpAgent);
  }

}
