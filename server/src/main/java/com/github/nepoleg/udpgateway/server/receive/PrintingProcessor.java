package com.github.nepoleg.udpgateway.server.receive;

import java.nio.ByteBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PrintingProcessor implements Processor {

  private static final Logger log = LogManager.getLogger();
  public static final int BUFFER_SIZE = 1024;

  private final byte[] bytes = new byte[BUFFER_SIZE];

  @Override
  public void receive(ByteBuffer buffer) {
    int remaining = buffer.remaining();
    int len = Math.min(remaining, bytes.length);
    buffer.get(bytes, 0, len);
    log.info("Received {} bytes: {}", remaining, new String(bytes, 0, len));
  }

}
