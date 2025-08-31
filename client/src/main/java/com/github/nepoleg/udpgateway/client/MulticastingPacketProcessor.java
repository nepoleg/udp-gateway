package com.github.nepoleg.udpgateway.client;

import com.github.nepoleg.api.Packet;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

public class MulticastingPacketProcessor {

  private final NetworkInterface nif;
  private MulticastSocket socket;

  public MulticastingPacketProcessor(NetworkInterface nif) {
    this.nif = nif;
  }

  public void init() throws IOException {
    socket = new MulticastSocket();
    if (nif != null)
      socket.setNetworkInterface(nif);
  }

  public void process(Packet packet) throws IOException {
    var datagram = new DatagramPacket(packet.payload(), packet.payload().length, packet.groupAddress(), packet.port());
    socket.send(datagram);
  }
}
