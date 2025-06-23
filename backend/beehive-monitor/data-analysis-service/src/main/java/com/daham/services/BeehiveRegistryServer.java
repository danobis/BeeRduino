package com.daham.services;

import com.daham.database.BeehiveRegistryDao;
import com.daham.domain.RegistryEntry;
import com.daham.domain.StatusType;
import com.daham.rabbitmq.RabbitmqConnectionFactory;
import com.daham.rabbitmq.RabbitmqRpcServer;
import com.daham.messaging.RpcServer;
import com.daham.rpc.model.Request;
import com.daham.rpc.model.Response;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
@ApplicationScoped
public class BeehiveRegistryServer implements AutoCloseable {
  private final RpcServer rpcServer;

  @Inject
  BeehiveRegistryDao beehiveRegistryDao;

  @Inject
  public BeehiveRegistryServer(RabbitmqConnectionFactory connectionFactory) {
    rpcServer = new RabbitmqRpcServer(connectionFactory);
  }

  void onStart(@Observes StartupEvent event) throws Exception {
    rpcServer.registerMethod("register_beehive", Request.class, request -> {
      var beehiveId = request.getBeehiveId();
      try {
        var result = beehiveRegistryDao.findById(beehiveId);
        var entry = result.orElse(new RegistryEntry());
        entry.setBeehiveId(beehiveId);
        entry.setTimestamp(request.getTimestamp());
        entry.setStatus(StatusType.ACTIVE);
        beehiveRegistryDao.persist(entry);
        return new Response(true, "Successfully registered Beehive<UUID=%s> ".formatted(beehiveId.toString()));
      } catch (Exception e) {
        log.error("Unable to register Beehive<UUID={}>, ERROR", beehiveId, e);
        return new Response(false, e.getMessage());
      }
    });
    rpcServer.registerMethod("unregister_beehive", Request.class, request -> {
      var beehiveId = request.getBeehiveId();
      try {
        var result = beehiveRegistryDao.findById(beehiveId);
        if (result.isEmpty()) {
          return new Response(false, "Beehive<UUID=%s> not found".formatted(beehiveId.toString()));
        }
        var entry = result.get();
        entry.setStatus(StatusType.ORPHANED);
        entry.setDeleted(true);
        beehiveRegistryDao.merge(entry);
        return new Response(true, "Successfully unregistered Beehive<UUID=%s> ".formatted(beehiveId.toString()));
      } catch (Exception e) {
        log.error("Unable to unregister Beehive<UUID={}>, ERROR", beehiveId, e);
        return new Response(false, e.getMessage());
      }
    });
    CompletableFuture.runAsync(() -> {
      try {
        rpcServer.start();
      } catch(Exception e) {
        log.error("Unable to start RPC server, ERROR", e);
      }
    });
  }

  @Override
  public void close() throws Exception {
    rpcServer.close();
  }
}
