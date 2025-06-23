package com.daham.consul;

import io.vertx.ext.consul.ConsulClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.consul.ConsulClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class ConsulProvider {
  @ConfigProperty(name = "consul.agent.host", defaultValue = "localhost")
  String host;

  @ConfigProperty(name = "consul.agent.port", defaultValue = "8500")
  int port;

  @Produces
  public ConsulClient consulClient(Vertx vertx) {
    return ConsulClient.create(vertx, new ConsulClientOptions()
        .setHost(host)
        .setPort(port));
  }
}
