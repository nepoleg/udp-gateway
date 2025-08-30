package com.github.nepoleg.utilities;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class NetworkHelper {
  private static final Logger log = LogManager.getLogger();

  private NetworkHelper() {}

  public static void printAvailableInterfaces() throws SocketException {
      log.info("Available interfaces: {} ", NetworkInterface.networkInterfaces()
          .map(nif -> nif.getName() + "\t" + nif.getDisplayName() + "\t" + nif.getInterfaceAddresses())
          .collect(Collectors.joining("\n\t", "\n\t", "")));
  }
}
