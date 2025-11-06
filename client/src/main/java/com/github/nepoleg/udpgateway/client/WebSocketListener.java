package com.github.nepoleg.udpgateway.client;

import com.github.nepoleg.api.PacketSerializer;
import java.io.IOException;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WebSocketListener implements Listener {
  private static final Logger log = LogManager.getLogger();

  public static final int READ_TIMEOUT = 30000;

  private final CountDownLatch latch;
  private final MulticastingPacketProcessor processor;
  private final ByteBuffer buffer = ByteBuffer.allocateDirect(1024*1024);
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  private ScheduledFuture<?> statusFuture;
  private final ByteBuffer pingByteBuffer = ByteBuffer.wrap("Hello".getBytes());
  private long lastMessageTime;

  public WebSocketListener(CountDownLatch latch, MulticastingPacketProcessor processor) {
    this.latch = latch;
    this.processor = processor;
  }

  @Override
  public void onOpen(WebSocket webSocket) {
    log.info("WebSocket opened");
    Listener.super.onOpen(webSocket);
    schedulePing(webSocket);
    lastMessageTime = System.currentTimeMillis();
  }

  private void schedulePing(WebSocket webSocket) {
    statusFuture = executor.scheduleAtFixedRate(() -> {
      if (lastMessageTime + READ_TIMEOUT < System.currentTimeMillis()) {
        webSocket.sendClose(1001, "Timeout")
            .thenRun(webSocket::abort);
        return;
      }
      pingByteBuffer.rewind();
      webSocket.sendPing(pingByteBuffer);
    }, 10, 10, TimeUnit.SECONDS);
  }

  @Override
  public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
    lastMessageTime = System.currentTimeMillis();
    buffer.put(data);
    if (last) {
      try {
        buffer.flip();
        var packet = PacketSerializer.deserialize(buffer);
        log.debug("WebSocket binary message received for {}: {}", packet.groupAddress(), new String(packet.payload()));
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
    lastMessageTime = System.currentTimeMillis();
    return Listener.super.onPing(webSocket, message);
  }

  @Override
  public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
    lastMessageTime = System.currentTimeMillis();
    return Listener.super.onPong(webSocket, message);
  }

  @Override
  public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
    log.info("Websocket closed with statusCode={}, reason={}", statusCode, reason);
    statusFuture.cancel(true);
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
