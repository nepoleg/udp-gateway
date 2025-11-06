package com.github.nepoleg.udpgateway.server.udp.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UdpMulticastHandler extends SimpleChannelInboundHandler<DatagramPacket> {
  private static final Logger log = LogManager.getLogger();

  private final MessageListener messageListener;

  public UdpMulticastHandler(MessageListener messageListener) {
    this.messageListener = messageListener;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
    log.debug("Received message of size {}", msg.content().readableBytes());
    messageListener.messageReceived(ctx, msg);
  }
}
