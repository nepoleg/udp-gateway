package com.github.nepoleg.udpgateway.server.agent;

import org.agrona.concurrent.Agent;
import org.agrona.concurrent.AgentRunner;
import org.agrona.concurrent.SleepingIdleStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerAgentRunner extends AgentRunner {
  private static final Logger log = LogManager.getLogger();

  public ServerAgentRunner(Agent agent) {
    super(new SleepingIdleStrategy(), (e) -> log.error("Error running agent", e), null, agent);
  }

  public void start() {
    AgentRunner.startOnThread(this);
  }
}
