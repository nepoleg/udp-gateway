package com.github.nepoleg.udpgateway.server.websocket.handler;

import com.github.nepoleg.udpgateway.server.websocket.session.WebSocketSession;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.ReferenceCountUtil;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerHandler extends ChannelInboundHandlerAdapter {
  private final static Logger LOG = LogManager.getLogger(ServerHandler.class);
  private final Supplier<WebSocketSession> sessionSupplier;
  private WebSocketSession session;

  public ServerHandler(Supplier<WebSocketSession> sessionSupplier) {
    this.sessionSupplier = sessionSupplier;
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
    if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
      LOG.info("Handshake complete for {}", ctx.channel().remoteAddress());
      session = sessionSupplier.get();
      session.connected(ctx);
    }
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) {
    if (session != null) {
      LOG.info("Disconnected {}", ctx.channel().remoteAddress());
      session.disconnected();
    }
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    ReferenceCountUtil.release(msg); //todo: handle channel subscription
  }
}
