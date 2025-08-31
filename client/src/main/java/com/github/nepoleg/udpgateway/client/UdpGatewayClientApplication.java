package com.github.nepoleg.udpgateway.client;

import java.net.InetSocketAddress;
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
  String serverAddress;

  @Option(names = {"--proxy", "-p"}, arity = "0..1")
  String proxyAddress;

  @Override
  public Integer call() {
    try {
      checkAddress(serverAddress);
      var proxyAddr = parseAddress(proxyAddress);
      var client = new UdpGatewayClient(serverAddress, proxyAddr);
      client.connect();
      return 0;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return 1;
    }
  }

  private void checkAddress(String serverAddress) throws URISyntaxException {
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
