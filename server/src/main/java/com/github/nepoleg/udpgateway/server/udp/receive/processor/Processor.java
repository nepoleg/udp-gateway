package com.github.nepoleg.udpgateway.server.udp.receive.processor;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

public interface Processor {
  void process(SocketAddress sourceAddress, InetSocketAddress groupAddress, ByteBuffer buffer);
}
