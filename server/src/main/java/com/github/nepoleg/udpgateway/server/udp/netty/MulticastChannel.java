package com.github.nepoleg.udpgateway.server.udp.netty;

import com.github.nepoleg.udpgateway.server.udp.receive.processor.Processor;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class MulticastChannel implements MessageListener {
  private static final Logger log = LogManager.getLogger();

  private final Bootstrap bootstrap;
  private final InetSocketAddress groupAddress;
  private final long timeout;
  private final NetworkInterface networkInterface;
  private final Processor processor;

  private DatagramChannel channel;
  private State state = State.NEW;
  private long lastUpdate;

  public MulticastChannel(Processor processor, EventLoopGroup workGroup, InetSocketAddress groupAddress, NetworkInterface networkInterface, long timeout, int bufferSize
       ) {
    this.processor = processor;
    this.bootstrap = new Bootstrap()
        .group(workGroup)
        .channel(NioDatagramChannel.class)
        .option(ChannelOption.SO_RCVBUF, bufferSize)
        .option(ChannelOption.SO_REUSEADDR, true)
        .handler(new ChannelInitializer<DatagramChannel>() {
          protected void initChannel(DatagramChannel datagramChannel) {
            datagramChannel.pipeline().addLast(new UdpMulticastHandler(MulticastChannel.this));
          }
        });
    this.groupAddress = groupAddress;
    this.timeout = timeout;
    this.networkInterface = networkInterface;
    workGroup.scheduleAtFixedRate(this::advanceState, 0, timeout, MILLISECONDS);
  }

  public void advanceState() {
    switch (state) {
      case NEW, ERROR -> {
        if (lastUpdate + timeout < System.currentTimeMillis()) {
          lastUpdate = System.currentTimeMillis();
          bind();
        }
      }
      case BOUND -> {
        if (lastUpdate + timeout < System.currentTimeMillis()) {
          log.error("Timeout joining group {}. Closing channel", groupAddress);
          channel.close();
          state = State.ERROR;
          lastUpdate = System.currentTimeMillis();
        } else {
          join();
        }
      }
      case JOINED -> {
        if (lastUpdate + timeout < System.currentTimeMillis()) {
          log.error("No messages received from {} for {} ms. Closing channel", groupAddress, timeout);
          channel.close();
          state = State.ERROR;
          lastUpdate = System.currentTimeMillis();
        }
      }
    }
  }

  public void join() {
    channel.joinGroup(groupAddress, networkInterface).addListener(
        (joinFuture) -> {
          lastUpdate = System.currentTimeMillis();
          if (joinFuture.isSuccess()) {
            state = State.JOINED;
          } else {
            state = State.ERROR;
            channel.close();
            log.error("UDP client failed to join group {}", groupAddress, joinFuture.cause());
          }
        }
    );
  }

  public void bind() {
    bootstrap.bind("0.0.0.0", groupAddress.getPort()).addListener(
        (future) -> {
          var f = (ChannelFuture) future;
          if (f.isSuccess()) {
            log.info("UDP client bound to {}", groupAddress);
            channel = (NioDatagramChannel) f.channel();
            lastUpdate = System.currentTimeMillis();
            state = State.BOUND;
          } else {
            state = State.ERROR;
            log.error("UDP client failed to bind {}", groupAddress, f.cause());
          }
        }
    );
  }

  public void shutdown() {
    if (state == State.BOUND || state == State.JOINED) {
      channel.close();
      lastUpdate = System.currentTimeMillis();
    }
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, DatagramPacket msg) {
    processor.process(msg.sender(), groupAddress, msg.content());
    lastUpdate = System.currentTimeMillis();
  }

  private enum State {
    NEW, BOUND, JOINED, ERROR
  }

}
