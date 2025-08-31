package com.github.nepoleg.udpgateway.client;

import com.github.nepoleg.api.PacketSerializer;
import java.io.IOException;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WebSocketListener implements Listener {
  private static final Logger log = LogManager.getLogger();
  private final CountDownLatch latch;
  private final MulticastingPacketProcessor processor;
  private final ByteBuffer buffer = ByteBuffer.allocateDirect(1024*1024);

  public WebSocketListener(CountDownLatch latch, MulticastingPacketProcessor processor) {
    this.latch = latch;
    this.processor = processor;
  }

  @Override
  public void onOpen(WebSocket webSocket) {
    log.info("WebSocket opened");
    Listener.super.onOpen(webSocket);
  }

  @Override
  public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
    buffer.put(data);
    if (last) {
      try {
        buffer.flip();
        var packet = PacketSerializer.deserialize(buffer);
        log.info("WebSocket binary message received: {}", packet);
        processor.process(packet);
        buffer.clear();
      } catch (IOException e) {
        log.error("Error deserializing", e);
      }
    }
    return Listener.super.onBinary(webSocket, data, last);
  }

  @Override
  public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
    return Listener.super.onPing(webSocket, message);
  }

  @Override
  public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
    return Listener.super.onPong(webSocket, message);
  }

  @Override
  public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
    log.info("Websocket closed with statusCode={}, reason={}", statusCode, reason);
    latch.countDown();
    return Listener.super.onClose(webSocket, statusCode, reason);
  }

  @Override
  public void onError(WebSocket webSocket, Throwable error) {
    log.error("WebSocket Error", error);
    Listener.super.onError(webSocket, error);
    latch.countDown();
  }
}
