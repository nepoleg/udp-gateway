package com.github.nepoleg.udpgateway.server.receive;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import org.agrona.LangUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UdpChannel {
  private static final Logger log = LogManager.getLogger();

  private final Selector selector;
  private final InetSocketAddress address;
  private final long timeout;
  private DatagramChannel datagramChannel;
  private State state = State.NEW;

  private long timeoutTime;
  private InetSocketAddress port;
  private NetworkInterface nif;
  private InetAddress group;
  private MembershipKey groupKey;
  private SelectionKey selectionKey;

  public UdpChannel(Selector selector, InetSocketAddress address,
      long timeout) {
    this.selector = selector;
    this.address = address;
    this.timeout = timeout;
  }

  private void open() throws IOException {
    log.info("Opening UDP channel {}, local interface {}", address, nif);
    close();
    datagramChannel = DatagramChannel
        .open(StandardProtocolFamily.INET)
        .setOption(StandardSocketOptions.SO_REUSEADDR, true)
        .bind(port);
    datagramChannel.configureBlocking(false);
    groupKey = datagramChannel.join(group, nif);
    selectionKey = datagramChannel.register(selector, SelectionKey.OP_READ, this);
  }

  public void init(NetworkInterface nif){
    try {
      group = InetAddress.getByName(address.getHostName());
      port = new InetSocketAddress(address.getPort());
      this.nif = nif;
    } catch (Exception e) {
      log.error("Error creating UDP channel {}" , address, e);
      LangUtil.rethrowUnchecked(e);
    }
  }

  public int doWork(long time) {
    switch (state) {
      case NEW: {
        if (time < timeoutTime) {
          return 0;
        }
        try {
          open();
          state = State.OPEN;
        } catch (IOException e) {
          log.error("Error opening selector for channel {}. Retry in {} ms.", address, timeout, e);
        }
        timeoutTime = time + timeout;
      }
      break;
      case OPEN: {
        if (time > timeoutTime) {
          close();
        }
        return 0;
      }
    }
    return 0;
  }

  public State getState() {
    return state;
  }

  public SocketAddress read(long now, ByteBuffer buffer) {
    if (state == State.NEW) {
      log.warn("Should not happen: read from a closed channel");
    }

    try {
      var addr = datagramChannel.receive(buffer);
      timeoutTime = now + timeout;
      return addr;
    } catch (IOException e) {
      log.error("Error reading from channel {}", address, e);
    }
    return null;
  }

  public void close() {
    //todo: does it make sense to close the channel and selector? May be we can only reconnect to group on reconnect periodically?
    state = State.NEW;
    if (groupKey != null) {
      groupKey.drop();
    }
    if (selectionKey != null) {
      selectionKey.cancel();
    }
    try {
      if (datagramChannel != null) {
        datagramChannel.close();
      }
    } catch (IOException e) {
      LangUtil.rethrowUnchecked(e);
    }

  }

  enum State {
    NEW, OPEN, READ
  }
}
