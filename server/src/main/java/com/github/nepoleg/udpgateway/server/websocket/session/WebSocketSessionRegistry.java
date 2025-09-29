package com.github.nepoleg.udpgateway.server.websocket.session;

import io.netty.buffer.ByteBuf;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class WebSocketSessionRegistry {
  public final List<WebSocketSession> activeSessions = new CopyOnWriteArrayList<>();

  public WebSocketSession create() {
    WebSocketSession session = new WebSocketSession(this);
    activeSessions.add(session);
    return session;
  }

  public void unsubscribe(WebSocketSession session) {
    activeSessions.remove(session);
  }

  public void send(SocketAddress sourceAddress, InetSocketAddress groupAddress, ByteBuf buffer) {
    for (WebSocketSession activeSession : activeSessions) {
      activeSession.send(sourceAddress, groupAddress, buffer);
      buffer.resetReaderIndex();
    }
  }
}
