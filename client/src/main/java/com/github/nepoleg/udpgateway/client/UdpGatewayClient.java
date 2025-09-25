package com.github.nepoleg.udpgateway.client;

import com.github.nepoleg.netty.Constants;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UdpGatewayClient {
  private static final Logger log = LogManager.getLogger();

  private final String serverAddress;
  private final InetSocketAddress proxyAddress;
  private final NetworkInterface nif;
  private final CountDownLatch latch = new CountDownLatch(1);

  public UdpGatewayClient(String serverAddress, InetSocketAddress proxyAddress,
      NetworkInterface nif) {
    this.serverAddress = serverAddress;
    this.proxyAddress = proxyAddress;
    this.nif = nif;
  }

  public void connect() throws Throwable {
    try (HttpClient client = HttpClient.newBuilder()
          .proxy(proxyAddress != null ? ProxySelector.of(proxyAddress) : ProxySelector.getDefault())
          .build()
    ) {
      var multicastSender = new MulticastingPacketProcessor(nif);
      multicastSender.init();
      WebSocket ws = client.newWebSocketBuilder()
          .buildAsync(URI.create(serverAddress + Constants.WEBSOCKET_PATH), new WebSocketListener(latch, multicastSender))
          .join();
      ws.sendText("Hello", true);
      latch.await();
    } catch (CompletionException e) {
      if (e.getCause() instanceof ConnectException) {
        log.error("Cannot connect to server: {}", e.getCause().getMessage());
      } else {
        log.error("Error connecting to server", e.getCause());
      }
    }
  }

}
