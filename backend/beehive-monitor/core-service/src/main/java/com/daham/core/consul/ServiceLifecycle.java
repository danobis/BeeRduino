package com.daham.core.consul;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.vertx.ext.consul.CheckOptions;
import io.vertx.ext.consul.ServiceOptions;
import io.vertx.mutiny.ext.consul.ConsulClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@ApplicationScoped
@SuppressWarnings("all")
public class ServiceLifecycle {
  private final ScheduledExecutorService executor;

  @ConfigProperty(name = "quarkus.application.name")
  String name;

  @ConfigProperty(name = "quarkus.http.port")
  Integer port;

  @ConfigProperty(name = "consul.service.health-check.host", defaultValue = "localhost")
  String host;

  @ConfigProperty(name = "consul.service.health-check.interval", defaultValue = "10s")
  String interval;

  @ConfigProperty(name = "consul.service.health-check.deregister-after", defaultValue = "1m")
  String deregisterAfter;

  @ConfigProperty(name = "consul.service.tags")
  List<String> tags;

  @Inject
  Instance<ConsulClient> consulClient;

  public ServiceLifecycle() {
    executor = Executors.newSingleThreadScheduledExecutor();
  }

  void onStart(@Observes StartupEvent event) {
    if (consulClient.isResolvable()) {
      executor.schedule(() -> {
        if (port == null || port == 0) {
          port = ConfigProvider.getConfig()
              .getValue("quarkus.http.port", Integer.class);
        }
        var id = "%s-%d".formatted(name, port);
        var checkOptions = new CheckOptions()
            .setHttp("http://%s:%d/q/health".formatted(host, port))
            .setInterval(interval)
            .setDeregisterAfter(deregisterAfter);
        var serviceOptions = new ServiceOptions()
            .setCheckOptions(checkOptions)
            .setTags(tags)
            .setPort(port)
            .setAddress("localhost")
            .setName(name)
            .setId(id);
        consulClient.get().registerServiceAndAwait(serviceOptions);
        log.info("Service<ID='{}'> successfully registered", id);
      }, 3000, TimeUnit.MILLISECONDS);
    }
  }

  void onStop(@Observes ShutdownEvent event) {
    if (consulClient.isResolvable()) {
      var id = "%s-%d".formatted(name, port);
      consulClient.get()
          .deregisterServiceAndAwait(id);
      log.info("Service<ID='{}'> successfully deregistered", id);
    }
  }
}
