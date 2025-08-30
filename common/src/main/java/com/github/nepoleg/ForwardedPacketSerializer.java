package com.github.nepoleg;

import io.netty.buffer.ByteBuf;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

public class ForwardedPacketSerializer {

  public static void serialize(SocketAddress sourceAddress, InetSocketAddress groupAddress,
      ByteBuffer data, ByteBuf buf) {
    if (sourceAddress instanceof InetSocketAddress sourceIp) {
      writeAddress(buf, sourceIp);
    } else {
      writeEmptyAddress(buf);
    }
    writeAddress(buf, groupAddress);
    buf.writeInt(groupAddress.getPort());
    buf.writeBytes(data);
  }

  private static void writeAddress(ByteBuf buf, InetSocketAddress address) {
    if (address.getAddress() instanceof Inet4Address ip4Address) {
      buf.writeBytes(ip4Address.getAddress());
    } else {
      writeEmptyAddress(buf);
    }
  }

  private static void writeEmptyAddress(ByteBuf buf) {
    buf.writeByte(0);
    buf.writeByte(0);
    buf.writeByte(0);
    buf.writeByte(0);
  }
}
