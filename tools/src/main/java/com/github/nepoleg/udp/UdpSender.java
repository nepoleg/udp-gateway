package com.github.nepoleg.udp;

import com.github.nepoleg.utilities.NetworkHelper;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static java.lang.System.exit;

@Command(name = "udp-send", description = "Sends UDP packets to the given channels")
public class UdpSender implements Callable<Integer> {

  private static final Logger log = LogManager.getLogger();

  @Option(names = {"--address", "-a"}, arity="1..*")
  List<String> address;

  @Option(names = {"--interface", "-i"}, arity = "1")
  String interfaceName;


  @Override
  public Integer call() {
    if (address == null || address.isEmpty()) {
      System.err.println("No addresses specified");
      return 1;
    }

    List<SenderChannel> senderChannels = null;
    try {
      NetworkInterface iface = NetworkInterface.getByName(interfaceName);
      if (iface == null) {
        log.info("Could not find interface " + interfaceName);
        NetworkHelper.printAvailableInterfaces();
        return 1;
      }
      senderChannels = address.stream().map(addr -> new SenderChannel(addr, iface)).toList();
      senderChannels.forEach(SenderChannel::init);

      while (true) {
        senderChannels.forEach(SenderChannel::send);
        Thread.sleep(1000);
      }
    } catch (Exception e) {
      log.error("Unexpected exception", e);
      if (senderChannels != null) {
        senderChannels.forEach(SenderChannel::close);
      }
    }
    return 0;
  }

  public static void main(String[] args) {
    int exitCode = new CommandLine(new UdpSender()).execute(args);
    exit(exitCode);
  }
}
