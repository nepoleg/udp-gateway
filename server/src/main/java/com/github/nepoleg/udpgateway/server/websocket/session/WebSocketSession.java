package com.github.nepoleg.udpgateway.server.websocket.session;

import com.github.nepoleg.ForwardedPacketSerializer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WebSocketSession { //todo: recycle
  private final static Logger LOG = LogManager.getLogger();

  private final WebSocketSessionRegistry registry;
  private ChannelHandlerContext context;

  public WebSocketSession(WebSocketSessionRegistry registry) {
    this.registry = registry;
  }

  public void connected(ChannelHandlerContext ctx) {
    this.context = ctx;
  }

  public void disconnected() {
    registry.unsubscribe(this);
  }

  public void send(SocketAddress sourceAddress, InetSocketAddress groupAddress, ByteBuffer data) {
    var channel = context.channel();
    if (!channel.isActive()) {
      LOG.warn("Error sending: channel is not active {}", context.channel().remoteAddress());
    }
    var buf = channel.alloc().buffer();
    ForwardedPacketSerializer.serialize(sourceAddress, groupAddress, data, buf);
    channel.writeAndFlush(new BinaryWebSocketFrame(buf));
    data.flip();
  }
}
