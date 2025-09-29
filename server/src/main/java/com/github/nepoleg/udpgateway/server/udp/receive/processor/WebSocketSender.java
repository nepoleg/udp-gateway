package com.github.nepoleg.udpgateway.server.udp.receive.processor;

import com.github.nepoleg.udpgateway.server.websocket.session.WebSocketSessionRegistry;
import io.netty.buffer.ByteBuf;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

public class WebSocketSender implements Processor {
  private final WebSocketSessionRegistry sessionRegistry;

  public WebSocketSender(WebSocketSessionRegistry sessionRegistry) {
    this.sessionRegistry = sessionRegistry;
  }

  @Override
  public void process(SocketAddress sourceAddress, InetSocketAddress groupAddress, ByteBuf buffer) {
    buffer.markReaderIndex();
    sessionRegistry.send(sourceAddress, groupAddress, buffer);
  }
}
