package com.github.nepoleg.udpgateway.client;

import com.github.nepoleg.netty.Constants;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CountDownLatch;

public class UdpGatewayClient {

  private final String serverAddress;
  private final InetSocketAddress proxyAddress;
  private final CountDownLatch latch = new CountDownLatch(1);

  public UdpGatewayClient(String serverAddress, InetSocketAddress proxyAddress) {
    this.serverAddress = serverAddress;
    this.proxyAddress = proxyAddress;
  }

  public void connect() throws InterruptedException {
    try (HttpClient client = HttpClient.newBuilder()
          .proxy(proxyAddress != null ? ProxySelector.of(proxyAddress) : ProxySelector.getDefault())
          .build()
    ) {
      WebSocket ws = client.newWebSocketBuilder()
          .buildAsync(URI.create(serverAddress + Constants.WEBSOCKET_PATH), new WebSocketListener(latch))
          .join();
      ws.sendText("Hello", true);
      latch.await();
    }

  }



}
