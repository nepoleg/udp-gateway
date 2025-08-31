package com.github.nepoleg.udpgateway.server.websocket;

import com.github.nepoleg.netty.Constants;
import com.github.nepoleg.netty.handler.LoggingErrorHandler;
import com.github.nepoleg.udpgateway.server.websocket.handler.ServerHandler;
import com.github.nepoleg.udpgateway.server.websocket.session.WebSocketSession;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WebSocketServer {
  private final static Logger LOG = LogManager.getLogger();

  private final String host;
  private final int port;
  private final Supplier<WebSocketSession> sessionSupplier;
  private volatile Channel channel;

  public WebSocketServer(String host, int port, Supplier<WebSocketSession> sessionSupplier) {
    this.host = host;
    this.port = port;
    this.sessionSupplier = sessionSupplier;
  }

  public void start() {
    var thread = new Thread(() -> {
      try {
        listen();
      } catch (Exception e) {
        LOG.error("Error starting Websocket Server ", e);
      }
    });
    thread.setName("WebSocketServer");
    thread.start();
  }

  private void listen() throws Exception {
    var bossGroup = new NioEventLoopGroup(1);
    var workerGroup = new NioEventLoopGroup(1);
    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) {
              ch.pipeline().addLast(
                  new HttpServerCodec(),
                  new WebSocketServerProtocolHandler(Constants.WEBSOCKET_PATH),
                  new IdleStateHandler(30, 10, 0, TimeUnit.SECONDS),
                  new ServerHandler(sessionSupplier),
                  new LoggingErrorHandler()
              );
            }
          })
          .option(ChannelOption.SO_BACKLOG, 128)
          .childOption(ChannelOption.TCP_NODELAY, true);

      ChannelFuture f = b.bind(host, port).sync();
      channel = f.channel();
      channel.closeFuture().sync();
    } finally {
      workerGroup.shutdownGracefully();
      bossGroup.shutdownGracefully();
    }
  }

  public void shutdown() {
    channel.disconnect();
  }
}
