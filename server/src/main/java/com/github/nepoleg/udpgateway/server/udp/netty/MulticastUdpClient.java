package com.github.nepoleg.udpgateway.server.udp.netty;

import com.github.nepoleg.udpgateway.server.config.UdpGatewayServerProperties.ReceiverProperties;
import com.github.nepoleg.udpgateway.server.udp.receive.processor.Processor;
import com.github.nepoleg.utilities.NetworkHelper;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class MulticastUdpClient {

  private final ReceiverProperties properties;
  private final Processor processor;
  private EventLoopGroup workGroup;
  private final List<MulticastChannel> channels = new ArrayList<>();

  public MulticastUdpClient(ReceiverProperties properties, Processor processor) {
    this.properties = properties;
    this.processor = processor;
  }

  public void start() {
    var remoteAddresses = properties.channels().stream()
        .map(ch -> new InetSocketAddress(ch.host(), ch.port())).toList();

    NetworkInterface nif;
    try {
      nif = NetworkHelper.getNetworkInterface(properties.localInterface());
      if (nif == null) {
        throw new IllegalStateException("Interface " + properties.localInterface() + " not found");
      }
    } catch (SocketException|UnknownHostException e) {
      throw new RuntimeException(e);
    }

    workGroup = new NioEventLoopGroup(properties.threadsCount());

    long timeout = properties.timeout().toMillis();

    for (InetSocketAddress groupAddress : remoteAddresses) {
      channels.add(new MulticastChannel(processor, workGroup, groupAddress, nif, timeout, properties.bufferSize()));
    }
  }

  /**
   * Shutdown a client
   */
  public void shutdown() {
    for (MulticastChannel channel : channels) {
      channel.shutdown();
    }
    if (workGroup != null) {
      workGroup.shutdownGracefully();
    }
  }
}
