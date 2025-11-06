package com.github.nepoleg.utilities;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class NetworkHelper {
  private static final Logger log = LogManager.getLogger();

  private NetworkHelper() {}

  //very simplified version with limitations, eg 127.1 is considered an invalid IP.
  //it's ok, we only use it to distinguish interface name from IP address
  private static boolean isIp4Address(String ipOrName) {
    var parts = ipOrName.split("\\.");
    if (parts.length != 4) {
      return false;
    }
    try {
      for (var part : parts) {
        int value = Integer.parseInt(part);
        if (value < 0 || value > 255) {
          return false;
        }
      }
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  public static NetworkInterface getNetworkInterface(String nameOrIp)
      throws SocketException, UnknownHostException {
    NetworkInterface nif = isIp4Address(nameOrIp) ?
        NetworkInterface.getByInetAddress(Inet4Address.getByName(nameOrIp)) :
        NetworkInterface.getByName(nameOrIp);
    if (nif == null) {
      log.info("Could not find local interface for {}", nameOrIp);
      NetworkHelper.printAvailableInterfaces();
    }
    return nif;
  }

  private static void printAvailableInterfaces() throws SocketException {
      log.info("Available interfaces: {} ", NetworkInterface.networkInterfaces()
          .map(nif -> nif.getName() + "\t" + nif.getDisplayName() + "\t" + nif.getInterfaceAddresses())
          .collect(Collectors.joining("\n\t", "\n\t", "")));
  }
}
