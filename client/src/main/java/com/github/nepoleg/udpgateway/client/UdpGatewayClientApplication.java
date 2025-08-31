package com.github.nepoleg.udpgateway.client;

import com.github.nepoleg.utilities.NetworkHelper;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.tools.picocli.CommandLine.Command;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import static java.lang.System.exit;

@Command(name = "udp-gateway-client", description = "UDP Gateway Client")
public class UdpGatewayClientApplication implements Callable<Integer> {
  private static final Logger log = LogManager.getLogger();

  @Option(names = {"--connect", "-c"})
  String serverUrl;

  @Option(names = {"--proxy", "-p"}, arity = "0..1")
  String proxyAddress;

  @Option(names = {"--iface", "-i"}, arity = "0..1")
  String localInterfaceAddress;


  @Override
  public Integer call() {
    try {
      checkUrl(serverUrl);
      var proxyAddr = parseAddress(proxyAddress);
      NetworkInterface nif = null;
      if (localInterfaceAddress != null) {
        nif = NetworkInterface.getByInetAddress(InetAddress.getByName(localInterfaceAddress));
        if (nif == null) {
          log.info("Could not find local interface for {}", localInterfaceAddress);
          NetworkHelper.printAvailableInterfaces();
        }
      }
      var client = new UdpGatewayClient(serverUrl, proxyAddr, nif);
      client.connect();
      return 0;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return 1;
    }
  }

  private void checkUrl(String serverAddress) throws URISyntaxException {
    if (serverAddress == null || serverAddress.isEmpty()) {
      throw new IllegalArgumentException("Server address must not be empty");
    }
    try {
      new URI(serverAddress);
    } catch (URISyntaxException e) {
      log.error("Error in address {}. Should be ws[s]://<host>:<port>", serverAddress, e);
      throw e;
    }
  }

  public static void main(String[] args) {
    int exitCode = new CommandLine(new UdpGatewayClientApplication()).execute(args);
    exit(exitCode);
  }

  private InetSocketAddress parseAddress(String address) {
    if (address == null || address.isEmpty()) {
      return null;
    }
    String[] parts = address.split(":");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Invalid address: " + address);
    }
    return new InetSocketAddress(parts[0], Integer.parseInt(parts[1]));
  }

}
