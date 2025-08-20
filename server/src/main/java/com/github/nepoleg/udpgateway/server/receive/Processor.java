package com.github.nepoleg.udpgateway.server.receive;

import java.nio.ByteBuffer;

public interface Processor {
  void receive(ByteBuffer buffer);
}
