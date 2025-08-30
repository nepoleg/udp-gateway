package com.github.nepoleg.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoggingErrorHandler extends ChannelInboundHandlerAdapter {
  private final static Logger LOG = LogManager.getLogger(LoggingErrorHandler.class);

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    LOG.error("Uncaught exception", cause);
    ctx.close();
  }
}
