package com.github.nepoleg.udpgateway.server.udp.receive.processor;

import io.netty.buffer.ByteBuf;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PrintingProcessor implements Processor {
  private static final Logger log = LogManager.getLogger();
  public static final int BUFFER_SIZE = 1024;

  private final byte[] bytes = new byte[BUFFER_SIZE];

  @Override
  public void process(SocketAddress sourceAddress, InetSocketAddress groupAddress, ByteBuf buffer) {
    String message = buffer.toString(buffer.readerIndex(), buffer.readableBytes(), Charset.defaultCharset());
    log.info("Received message: {}", message);
  }

}
