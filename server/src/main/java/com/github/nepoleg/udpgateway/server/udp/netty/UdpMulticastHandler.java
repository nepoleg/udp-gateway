package com.github.nepoleg.udpgateway.server.udp.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import java.nio.charset.Charset;

public class UdpMulticastHandler extends SimpleChannelInboundHandler<DatagramPacket> {
  private final MessageListener messageListener;

  public UdpMulticastHandler(MessageListener messageListener) {
    this.messageListener = messageListener;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
    ByteBuf content = msg.content();
    String message = content.toString(content.readerIndex(), content.readableBytes(), Charset.defaultCharset());
    System.out.println("Received Message : " + message);
    messageListener.messageReceived(ctx, msg);
  }
}
