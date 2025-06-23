package com.daham.services;

import com.daham.domain.Beehive;
import com.daham.rabbitmq.RabbitmqConnectionFactory;
import com.daham.rabbitmq.RabbitmqRpcClient;
import com.daham.messaging.RpcClient;
import com.daham.rpc.model.Request;
import com.daham.rpc.model.Response;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
@ApplicationScoped
public class BeehiveRegistryClient implements BeehiveRegistryService {
  private final RpcClient rpcClient;

  @Inject
  public BeehiveRegistryClient(RabbitmqConnectionFactory connectionFactory) {
    rpcClient = new RabbitmqRpcClient(connectionFactory);
  }

  @Override
  public void registerBeehive(Beehive beehive) {
    CompletableFuture.runAsync(() -> {
      var beehiveId = beehive.getId();
      try {
        var request = new Request(beehiveId, beehive.getTimestamp());
        var response = rpcClient.execute("register_beehive", request, Response.class);
        handleResponse(response);
      } catch (Exception e) {
        log.error("Unable to register Beehive<UUID={}> via RPC method, ERROR", beehiveId, e);
        throw new RuntimeException(e);
      }
    });
  }

  @Override
  public void unregisterBeehive(Beehive beehive) {
    CompletableFuture.runAsync(() -> {
      var beehiveId = beehive.getId();
      try {
        var request = new Request(beehive.getId(), null);
        var response = rpcClient.execute("unregister_beehive", request, Response.class);
        handleResponse(response);
      } catch (Exception e) {
        log.error("Unable to register Beehive<UUID={}> via RPC method, ERROR", beehiveId, e);
        throw new RuntimeException(e);
      }
    });
  }

  @Override
  public void close() throws Exception {
    rpcClient.close();
  }

  private void handleResponse(Response response) {
    if (response.isSuccess()) {
      log.info("RPC response is: {}", response.getMessage());
    } else {
      log.warn("RPC response is: {}", response.getMessage());
    }
  }
}
