package com.github.nepoleg.udpgateway.server.udp.receive.processor;

import io.netty.buffer.ByteBuf;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public interface Processor {
  void process(SocketAddress sourceAddress, InetSocketAddress groupAddress, ByteBuf buffer);
}
