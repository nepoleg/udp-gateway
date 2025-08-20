package com.github.nepoleg.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

public class SenderChannel {
  private final String address;
  private final NetworkInterface iface;
  private InetAddress group;
  private int port;
  private MulticastSocket socket;

  private int data;

  public SenderChannel(String address, NetworkInterface iface) {
    this.address = address;
    this.iface = iface;
  }

  public void init() {
    String[] split = address.split(":");
    if (split.length != 2) {
      throw new IllegalArgumentException("Invalid address: " + address);
    }
    try {
      group = InetAddress.getByName(split[0]);
      port = Integer.parseInt(split[1]);
      socket = new MulticastSocket();
      socket.setNetworkInterface(iface);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void send() {
    if (data == 0) {
      System.out.println("Sending data to " + address);
    }
    var buf = (address + ": " + data++).getBytes();
    var packet = new DatagramPacket(buf, buf.length, group, port);
    try {
      socket.send(packet);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void close() {
    socket.close();
  }
}
