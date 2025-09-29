package com.github.nepoleg.api;

import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

public class PacketSerializer {

  public static Packet deserialize(ByteBuffer buf) throws IOException {
    byte[] ip4 = new byte[4];
    buf.get(ip4);
    var srcAddr = Inet4Address.getByAddress(ip4);
    buf.get(ip4);
    var grpAddr = Inet4Address.getByAddress(ip4);
    var port = buf.getInt();
    var data = new byte[buf.remaining()];
    buf.get(data);
    return new Packet(srcAddr, grpAddr, port, data);
  }

  public static void serialize(SocketAddress sourceAddress, InetSocketAddress groupAddress,
      ByteBuf data, ByteBuf buf) {
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
