# udp-gateway

## Description

A proxy that forwards multicast UDP traffic from many streams on one machine to another machine over a WebSocket. The receiving machine replays the packets as UDP multicast.

## Modules

- **server** (Spring Boot, Netty): joins the configured multicast groups and streams every packet to all connected WebSocket clients.
- **client** (CLI): connects to the server and re-sends each packet to its original multicast group and port.
- **common**: packet format and shared helpers.
- **tools**: `UdpSender`, a test tool that sends multicast packets at a set rate.

## Features

- Many multicast channels in one server, configured in `application.yaml`.
- Network interface can be given by name or IP address (server and client).
- Channel watchdog: a channel that cannot join its group, or gets no data within `timeout`, is closed and joined again.
- Slow consumer detection: if a client's socket is not writable, packets for that client are dropped, so other clients are not affected.
- Ping/pong between client and server; the client closes the connection after 30 s without data and reconnects.
- Client can connect through an HTTP proxy (`--proxy host:port`).
- Builds with Java 17.

## Usage

Server: set channels, `local-interface` and listen `host`/`port` in `server/src/main/resources/application.yaml`, then run the Spring Boot app.

Client:

```
udp-gateway-client --connect ws://<server-host>:<port> [--iface <name|ip>] [--proxy <host:port>]
```
