package com.github.nepoleg.udpgateway.server.udp.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

public interface MessageListener {
  void messageReceived(ChannelHandlerContext ctx, DatagramPacket msg);
}
