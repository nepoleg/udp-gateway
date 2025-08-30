package com.github.nepoleg.udpgateway.server.udp.receive;

import com.github.nepoleg.udpgateway.server.udp.receive.UdpChannel.State;
import com.github.nepoleg.udpgateway.server.udp.receive.processor.Processor;
import com.github.nepoleg.utilities.NetworkHelper;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.List;
import org.agrona.LangUtil;
import org.agrona.concurrent.Agent;
import org.agrona.nio.TransportPoller;

public class UdpAgent extends TransportPoller implements Agent {

  private final String localInterface;
  private final ByteBuffer buffer;
  private final Processor processor;
  private final List<UdpChannel> channels;

  public UdpAgent(List<InetSocketAddress> remoteAddresses, String localInterface, int bufferSize,
      long timeout, Processor processor) {
    this.localInterface = localInterface;
    this.buffer = ByteBuffer.allocate(bufferSize);
    this.processor = processor;
    this.channels = remoteAddresses.stream().map(addr -> new UdpChannel(selector, addr, timeout))
        .toList();
  }

  @Override
  public void onStart() {
    NetworkInterface nif = null;
    try {
      nif = NetworkInterface.getByName(localInterface);
      if (nif == null) {
        NetworkHelper.printAvailableInterfaces();
        throw new IllegalStateException("Interface " + localInterface + " not found");
      }
    } catch (SocketException e) {
      LangUtil.rethrowUnchecked(e);
    }
    for (UdpChannel channel : channels) {
      channel.init(nif);
    }
  }

  @Override
  public int doWork() {
    long now = System.currentTimeMillis();
    int result = 0;
    for (int i = 0; i < channels.size(); i++) {
      UdpChannel channel = channels.get(i);
      channel.doWork(now);
    }

    if (channels.size() <= ITERATION_THRESHOLD) {
      for (int i = 0; i < channels.size(); i++) {
        UdpChannel channel = channels.get(i);
        if (channel.getState() == State.OPEN) {
          result += read(channel, now);
        }
      }
    } else {
      try {
        selector.selectNow();
        final SelectionKey[] keys = selectedKeySet.keys();
        for (int i = 0, length = selectedKeySet.size(); i < length; i++) {
          result += read((UdpChannel) keys[i].attachment(), now);
        }
        selectedKeySet.reset();
      } catch (IOException e) {
        LangUtil.rethrowUnchecked(e);
      }
    }
    return result;
  }

  private int read(UdpChannel channel, long now) {
    buffer.clear();
    var sourceAddress = channel.read(now, buffer);
    if (sourceAddress != null) {
      buffer.flip();
      processor.process(sourceAddress, channel.getGroupAddress(), buffer);
      return 1;
    }
    return 0;
  }

  @Override
  public void onClose() {
    this.close();
    for (UdpChannel channel : channels) {
      channel.close();
    }
  }

  @Override
  public String roleName() {
    return "UdpAgent";
  }
}
