package com.github.nepoleg.api;

import java.net.InetAddress;

public record Packet(
    InetAddress sourceAddress,
    InetAddress groupAddress,
    int port,
    byte[] payload
) {
}
