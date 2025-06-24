package com.daham;

import com.daham.simulator.BeehiveSimulator;
import com.daham.services.DataCollectorService;
import com.daham.simulator.Simulator;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@QuarkusMain
public class ApplicationMain implements QuarkusApplication {

  @Inject
  DataCollectorService collectorService;

  public static void main(String[] args) {
    Quarkus.run(ApplicationMain.class, args);
  }

  @Override
  public int run(String... args) {
    try (var simulator1 = startSimulator(UUID.fromString("6c5a5066-07a8-4a21-8dc2-766cbc63f0eb"));
         var simulator2 = startSimulator(UUID.fromString("8c1c230c-4b2c-4d88-bef8-db4d45e5e1b3"));
         var simulator3 = startSimulator(UUID.fromString("d1280171-e8bc-4e8f-8001-0b6f74a09847"))) {
      Quarkus.waitForExit();
    } catch (Exception e) {
      return 1;
    }
    return 0;
  }

  private Simulator startSimulator(UUID beehiveId) {
    var simulator = new BeehiveSimulator(collectorService, beehiveId);
    simulator.start();
    log.info("Started producing sensor data for Beehive<UUID={}>", beehiveId);
    return simulator;
  }
}
